package fi.johannes.data.services.proxy

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject


/**
 * Johannes on 9.1.2018.
 */
@ProxyGen
interface WikiDatabaseServiceExt {
  @Fluent
  fun fetchAllPageTitles(resultHandler: Handler<AsyncResult<JsonArray>>): WikiDatabaseServiceExt

  @Fluent
  fun fetchAllPages(resultHandler: Handler<AsyncResult<List<JsonObject>>>): WikiDatabaseServiceExt

  @Fluent
  fun fetchPageById(id: Int, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseServiceExt

  @Fluent
  fun fetchPage(name: String, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseServiceExt

  @Fluent
  fun createPage(title: String, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt

  @Fluent
  fun savePage(id: Int, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt

  @Fluent
  fun deletePage(id: Int, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt
}

