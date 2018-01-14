package fi.johannes.data.services.proxy

import fi.johannes.data.dao.PageDao
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx

/**
 * Johannes on 9.1.2018.
 */
object WikiDatabaseServiceExtFactory {
  fun createService(pageDao: PageDao, readyHandler: Handler<AsyncResult<WikiDatabaseServiceExt>>): WikiDatabaseServiceExt {
    return WikiDatabaseServiceExtImpl(pageDao, readyHandler)
  }

  fun createProxy(vertx: Vertx, address: String): WikiDatabaseServiceExt {
    return WikiDatabaseServiceExtVertxEBProxy(vertx, address)
  }
}
