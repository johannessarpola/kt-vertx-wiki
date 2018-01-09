package fi.johannes.data.ext

import fi.johannes.data.enums.SqlQuery
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.jdbc.JDBCClient

/**
 * Johannes on 9.1.2018.
 */
object WikiDatabaseServiceExtFactory {
  fun create(dbClient: JDBCClient, sqlQueries: HashMap<SqlQuery, String>, readyHandler: Handler<AsyncResult<WikiDatabaseServiceExt>>): WikiDatabaseServiceExt {
    return WikiDatabaseServiceExtImpl(dbClient, sqlQueries, readyHandler)
  }

  fun createProxy(vertx: Vertx, address: String): WikiDatabaseServiceExt {
    return WikiDatabaseServiceExtVertxEBProxy(vertx, address)
  }
}
