package fi.johannes.data

import com.github.salomonbrys.kodein.*
import fi.johannes.data.enums.ErrorCodes
import fi.johannes.data.enums.SqlQuery
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import java.util.*

/**
 * Johannes on 8.1.2018.
 */
class WikiDatabaseVerticle : AbstractVerticle() {

  val CONFIG_WIKIDB_JDBC_URL = "wikidb.jdbc.url";
  val CONFIG_WIKIDB_JDBC_DRIVER_CLASS = "wikidb.jdbc.driver_class";
  val CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE = "wikidb.jdbc.max_pool_size";
  val CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE = "wikidb.sqlqueries.resource.file";
  val CONFIG_WIKIDB_QUEUE = "wikidb.queue";

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


  val wikiDatabaseModules by lazy {
    Kodein {
      bind<SQLClient>() with singleton { dbClient }
      bind<Logger>() with singleton { logger }
      constant("sqlQueries") with sqlQueries
      bind<WikiDatabaseService>() with singleton {
        WikiDatabaseServiceImpl(instance("sqlQueries"), instance(), instance())
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
    ifAction(message, { m -> handleActions(m) })
  }

  private fun handleActions(message: Message<JsonObject>): Unit {
    val action = message.headers().get("action")
    val service: WikiDatabaseService = wikiDatabaseModules.instance()
    when (action) {
      "all-pages" -> service.fetchAllPages(message)
      "get-page" -> service.fetchPage(message)
      "create-page" -> service.createPage(message)
      "save-page" -> service.savePage(message)
      "delete-page" -> service.deletePage(message)
      else -> message.fail(ErrorCodes.BAD_ACTION.ordinal, "Bad action: " + action)
    }
  }

  private fun ifAction(message: Message<JsonObject>, block: (Message<JsonObject>) -> Unit) {
    if (!message.headers().contains("action")) {
      logger.error("No action header specified for message with headers {} and body {}",
        message.headers(), message.body().encodePrettily())
      message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal, "No action header specified")
    }
    else {
      block(message)
    }
  }
}

