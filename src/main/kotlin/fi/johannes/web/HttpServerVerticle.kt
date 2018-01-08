package fi.johannes.web

import com.github.salomonbrys.kodein.*
import fi.johannes.web.handlers.wiki.WikiControllers
import fi.johannes.web.handlers.wiki.index.Index
import fi.johannes.web.handlers.wiki.page.Page
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.Logger
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

/**
 * Johannes on 8.1.2018.
 */
class HttpServerVerticle : AbstractVerticle() {

  private val logger: Logger by lazy {
    io.vertx.core.logging.LoggerFactory.getLogger("WikiDatabaseVerticle")
  }

  val CONFIG_HTTP_SERVER_PORT = "web.server.port"
  val CONFIG_WIKIDB_QUEUE = "wikidb.queue"

  private val wikiDbQueue by lazy {
    config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue")
  }

  private val eventBus by lazy {
    vertx.eventBus()
  }

  val httpModule by lazy {
    Kodein {
      bind<Logger>() with singleton { logger }
      bind<EventBus>() with singleton { eventBus }
      constant("wikiDatabaseQueue") with wikiDbQueue
    }
  }

  private fun setupRouter(router: Router): Router {

    val controllers = WikiControllers(wikiDbQueue, eventBus)

    // todo could use string tags to even less coupling
    val indexController = controllers.injector.instance<Index>()
    router.get("/").handler(indexController::get);

    // todo could use string tags to even less coupling
    val pageController = controllers.injector.instance<Page>()
    router.get("/wiki/:page").handler(pageController::get)
    router.post().handler(BodyHandler.create())
    router.post("/wiki/create").handler(pageController::create)
    router.post("/wiki/:id/save").handler(pageController::save)
    router.post("/wiki/:id/delete").handler(pageController::delete)

    return router
  }

  override fun start(startFuture: Future<Void>) {

    val server = vertx.createHttpServer()

    val router = Router.router(vertx)
    setupRouter(router)

    val portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
    server
      .requestHandler(router::accept)
      .listen(portNumber, { ar ->
        if (ar.succeeded()) {
          logger.info("HTTP server running on port " + portNumber);
          startFuture.complete();
        } else {
          logger.error("Could not start a HTTP server", ar.cause());
          startFuture.fail(ar.cause());
        }
      });
  }


}
