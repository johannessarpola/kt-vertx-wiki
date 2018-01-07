package fi.johannes.starter;

import com.github.salomonbrys.kodein.*
import fi.johannes.handlers.wiki.WikiHandlers
import fi.johannes.handlers.wiki.index.Index
import fi.johannes.handlers.wiki.page.Page
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine
import io.vertx.ext.web.templ.TemplateEngine

class MainVerticle : AbstractVerticle() {
  private val SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity primary key, Name varchar(255) unique, Content clob)"

  private val logger: Logger  by lazy {
    LoggerFactory.getLogger("MainVerticle")
  }

  private val dbClient: JDBCClient by lazy {
    JDBCClient.createShared(vertx, JsonObject()
      .put("url", "jdbc:hsqldb:file:db/wiki")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30))
  }

  private val templateEngine by lazy {
    FreeMarkerTemplateEngine.create()
  }

  private val eventBus by lazy {
    vertx.eventBus()
  }

  private val wikiDbQueue = "wikidb.queue";

  val appModule = Kodein {
    bind<SQLClient>() with singleton { dbClient }
    bind<TemplateEngine>() with singleton { templateEngine }
    bind<Logger>() with singleton { logger }
    bind<EventBus>() with singleton { eventBus }
    constant("wikiDatabaseQueue") with wikiDbQueue
  }
  val handlerModule = WikiHandlers(appModule)




  val CONFIG_HTTP_SERVER_PORT = "http.server.port";
  val CONFIG_WIKIDB_QUEUE = "wikidb.queue";


  override fun start(startFuture: Future<Void>) {
    val steps = prepareDatabase().compose { v -> startHttpServer() }
    steps.setHandler { ar ->
      when (ar.succeeded()) {
        true -> startFuture.complete()
        false -> startFuture.fail(ar.cause())
      }
    }
  }

  private fun prepareDatabase(): Future<Void> {
    val future = Future.future<Void>()

    dbClient.getConnection { ar ->
      when (ar.failed()) {
        true -> {
          logger.error("Could not open a database connection", ar.cause())
          future.fail(ar.cause())
        }
        false -> {
          val connection = ar.result()
          connection.execute(SQL_CREATE_PAGES_TABLE, { create ->
            connection.close()
            if (create.failed()) {
              logger.error("Database preparation error", create.cause())
              future.fail(create.cause())
            } else {
              future.complete()
            }
          })
        }
      }
    }
    return future
  }

  private fun setupRouter(router:Router): Router {

    // todo could use string tags to even less coupling
    val indexController = handlerModule.injector.instance<Index>()
    router.get("/").handler(indexController::get);

    val pageController = handlerModule.injector.instance<Page>()
    router.get("/wiki/:page").handler(pageController::get)
    router.post().handler(BodyHandler.create())
    router.post("/wiki/create").handler(pageController::create)
    router.post("/wiki/:id/save").handler(pageController::save)
    router.post("/wiki/:id/delete").handler(pageController::delete)

    return router
  }

  private fun startHttpServer(): Future<Void> {
    val future = Future.future<Void>()

    val router: Router = setupRouter(Router.router(vertx))
    val server = vertx.createHttpServer();

    val portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);

    server
      .requestHandler(router::accept)
      .listen(portNumber, { ar ->
        if (ar.succeeded()) {
          logger.info("HTTP server running on port 8080");
          future.complete();
        } else {
          logger.error("Could not start a HTTP server", ar.cause());
          future.fail(ar.cause());
        }
      });
    return future
  }

}


