package fi.johannes.data.ext

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import fi.johannes.data.WikiDatabaseServiceImpl
import fi.johannes.data.WikiDatabaseService
import fi.johannes.data.enums.SqlQuery
import io.vertx.ext.jdbc.JDBCClient


/**
 * Johannes on 9.1.2018.
 */
@ProxyGen
interface WikiDatabaseServiceExt {
  @Fluent
  fun fetchAllPages(resultHandler: Handler<AsyncResult<JsonArray>>): WikiDatabaseServiceExt

  @Fluent
  fun fetchPage(name: String, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseServiceExt

  @Fluent
  fun createPage(title: String, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt

  @Fluent
  fun savePage(id: Int, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt

  @Fluent
  fun deletePage(id: Int, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt
}

