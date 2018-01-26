package fi.johannes.web

import com.github.salomonbrys.kodein.*
import fi.johannes.data.services.proxy.WikiDatabaseServiceExt
import fi.johannes.data.services.proxy.WikiDatabaseServiceExtFactory
import fi.johannes.web.handlers.backup.BackupControllers
import fi.johannes.web.handlers.backup.controllers.BackupController
import fi.johannes.web.handlers.wiki.WikiControllersExt
import fi.johannes.web.handlers.wiki.index.Index
import fi.johannes.web.handlers.wiki.page.Page
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.logging.Logger
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.ext.web.handler.BodyHandler
import javax.jws.WebService

/**
 * Johannes on 8.1.2018.
 */
class HttpServerVerticleExt : AbstractVerticle() {

  private val logger: Logger by lazy {
    io.vertx.core.logging.LoggerFactory.getLogger("WikiDatabaseVerticle")
  }

  val CONFIG_HTTP_SERVER_PORT = "web.server.port"
  val CONFIG_WIKIDB_QUEUE = "wikidb.queue"

  private val wikiDbQueue by lazy {
    config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue")
  }


  val modules by lazy {
    Kodein {
      bind<Logger>() with singleton { logger }
      bind<WikiDatabaseServiceExt>() with singleton { WikiDatabaseServiceExtFactory.createProxy(vertx, wikiDbQueue) }
    }
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

  private fun wikiRouter(): Router {
    val router = Router.router(vertx)
    val wikiControllers = WikiControllersExt(modules.instance())
    // todo could use string tags to even less coupling
    val pageController = wikiControllers.injector.instance<Page>()

    router.get("/:page").handler(pageController::get)
    router.post().handler(BodyHandler.create())
    router.post("/create").handler(pageController::create)
    router.post("/:id/save").handler(pageController::save)
    router.post("/:id/delete").handler(pageController::delete)

    return router
  }

  private fun backupApiRouter(): Router {
    val router = Router.router(vertx)
    val backupControllers = BackupControllers(WebClient.create(vertx, WebClientOptions()))
    val backupController = backupControllers.injector.instance<BackupController>()

    router.post("/save").handler(backupController::saveBackup)

    return router
  }

  private fun indexRouter(): Router {
    val router = Router.router(vertx)
    val wikiControllers = WikiControllersExt(modules.instance())
    // todo could use string tags to even less coupling
    val indexController = wikiControllers.injector.instance<Index>()

    router.get("/").handler(indexController::get)

    return router
  }

  private fun setupRouter(router: Router): Router {
    router.mountSubRouter("/wiki", wikiRouter())
    router.mountSubRouter("/", indexRouter())
    router.mountSubRouter("/wiki-backup", backupApiRouter())
    return router
  }



}
