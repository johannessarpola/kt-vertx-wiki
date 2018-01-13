package fi.johannes.data

import com.github.salomonbrys.kodein.*
import fi.johannes.data.dao.PageDao
import fi.johannes.data.dao.PageDaoImpl
import fi.johannes.data.enums.ErrorCodes
import fi.johannes.data.enums.SqlQuery
import fi.johannes.data.ext.WikiDatabaseServiceExt
import fi.johannes.data.ext.WikiDatabaseServiceExtFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.serviceproxy.ProxyHelper
import io.vertx.serviceproxy.ServiceBinder
import java.util.*
import io.vertx.core.eventbus.MessageConsumer



/**
 * Johannes on 8.1.2018.
 */
class WikiDatabaseVerticle : AbstractVerticle() {

  companion object {
    val CONFIG_WIKIDB_JDBC_URL = "wikidb.jdbc.url";
    val CONFIG_WIKIDB_JDBC_DRIVER_CLASS = "wikidb.jdbc.driver_class";
    val CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE = "wikidb.jdbc.max_pool_size";
    val CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE = "wikidb.sqlqueries.resource.file";
    val CONFIG_WIKIDB_QUEUE = "wikidb.queue";
  }

  private val logger: Logger  by lazy {
    io.vertx.core.logging.LoggerFactory.getLogger("WikiDatabaseVerticle")
  }

  private val dbClient: JDBCClient by lazy {
    JDBCClient.createShared(vertx, JsonObject()
      .put("url", config().getString(CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:file:db/wiki"))
      .put("driver_class", config().getString(CONFIG_WIKIDB_JDBC_DRIVER_CLASS, "org.hsqldb.jdbcDriver"))
      .put("max_pool_size", config().getInteger(CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, 30)))
  }

  private val sqlQueries: Map<SqlQuery, String> by lazy {
    loadQueries()
  }

  private val actions: Map<String, (Message<JsonObject>) -> Unit> by lazy {
    mapActions()
  }


  private val modules by lazy {
    Kodein {
      bind<SQLClient>() with singleton { dbClient }
      constant("sqlQueries") with sqlQueries
      bind<PageDao>() with singleton {
        PageDaoImpl(dbClient, sqlQueries)
      }
    }
  }

  override fun start(startFuture: Future<Void>) {
    dbClient.getConnection({ ar ->
      if (ar.failed()) {
        logger.error("Could not open a database connection", ar.cause());
        startFuture.fail(ar.cause());
      } else {
        val connection = ar.result();
        connection.execute(sqlQueries[SqlQuery.CREATE_PAGES_TABLE], { create ->
          connection.close();
          if (create.failed()) {
            logger.error("Database preparation error", create.cause());
            startFuture.fail(create.cause());
          } else {
            vertx.eventBus().consumer(config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue"), this::onMessage);
            startFuture.complete();
          }
        });
      }
    });

  }

  private fun loadQueries(): Map<SqlQuery, String> {
    val queriesPath = config()?.getString(CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE) ?: "/db-queries.properties"
    val stream = this.javaClass.getResourceAsStream(queriesPath);
    val queriesProps = Properties()
    queriesProps.load(stream);
    stream.close();

    return mapQueries(queriesProps)
  }

  private fun mapActions(): Map<String, (Message<JsonObject>) -> Unit> {
    val service: WikiDatabaseService = modules.instance()
    return mapOf(
      "all-pages" to { message -> service.fetchAllPages(message) },
      "get-page" to { message -> service.fetchPage(message) },
      "create-page" to { message -> service.createPage(message) },
      "save-page" to { message -> service.savePage(message) },
      "delete-page" to { message -> service.deletePage(message) }
    )
  }

  private fun mapQueries(props: Properties): Map<SqlQuery, String> {
    return mapOf(
      SqlQuery.CREATE_PAGES_TABLE to props.getProperty("create-pages-table"),
      SqlQuery.ALL_PAGES to props.getProperty("all-pages"),
      SqlQuery.GET_PAGE to props.getProperty("get-page"),
      SqlQuery.CREATE_PAGE to props.getProperty("create-page"),
      SqlQuery.SAVE_PAGE to props.getProperty("save-page"),
      SqlQuery.DELETE_PAGE to props.getProperty("delete-page")
    )
  }

  fun onMessage(message: Message<JsonObject>) {
    ifActionPresent(message, { m -> doAction(m) })
  }

  private fun doAction(message: Message<JsonObject>): Unit {
    val action = message.headers().get("action")
    if (actions.containsKey(action)) {
      val f = actions[action]?.invoke(message)
    }
    else {
      message.fail(ErrorCodes.BAD_ACTION.ordinal, "Bad action: "+action)
    }
  }

  private fun ifActionPresent(message: Message<JsonObject>, block: (Message<JsonObject>) -> Unit) {
    if (!message.headers().contains("action")) {
      logger.error("No action header specified for message with headers {} and body {}",
        message.headers(), message.body().encodePrettily())
      message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal, "No action header specified")
    } else {
      block(message)
    }
  }
}



