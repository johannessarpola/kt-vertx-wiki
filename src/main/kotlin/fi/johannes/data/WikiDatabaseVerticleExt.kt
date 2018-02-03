package fi.johannes.data

import com.github.salomonbrys.kodein.*
import fi.johannes.data.dao.PageDao
import fi.johannes.data.dao.PageDaoImpl
import fi.johannes.data.enums.SqlQuery
import fi.johannes.data.services.proxy.WikiDatabaseServiceExt
import fi.johannes.data.services.proxy.WikiDatabaseServiceExtFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.serviceproxy.ServiceBinder
import java.util.*


/**
 * Johannes on 8.1.2018.
 */
class WikiDatabaseVerticleExt : AbstractVerticle() {

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
    logger.info("Starting WikiDatabaseVerticleExt")
    val now = System.currentTimeMillis()
    WikiDatabaseServiceExtFactory.createService(modules.instance(), Handler { ready ->
      if(ready.succeeded()) {
        ServiceBinder(vertx)
          .setAddress(CONFIG_WIKIDB_QUEUE)
          .register(WikiDatabaseServiceExt::class.java, ready.result())
        logger.info("WikiDatabaseVerticleExt start successful in ${System.currentTimeMillis() - now} ms")
        startFuture.complete()
      } else {
        startFuture.fail(ready.cause());
      }
    })
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
      SqlQuery.ALL_PAGES_TITLES to props.getProperty("all-page-titles"),
      SqlQuery.ALL_PAGES to props.getProperty("all-pages"),
      SqlQuery.GET_PAGE to props.getProperty("get-page"),
      SqlQuery.GET_PAGE_BY_ID to props.getProperty("get-page-by-id"),
      SqlQuery.CREATE_PAGE to props.getProperty("create-page"),
      SqlQuery.SAVE_PAGE to props.getProperty("save-page"),
      SqlQuery.DELETE_PAGE to props.getProperty("delete-page")
    )
  }
}

