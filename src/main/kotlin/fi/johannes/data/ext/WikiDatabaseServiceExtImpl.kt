package fi.johannes.data.ext

import fi.johannes.data.WikiDatabaseService
import fi.johannes.data.enums.SqlQuery
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient

/**
 * Johannes on 9.1.2018.
 */
class WikiDatabaseServiceExtImpl(val dbClient: JDBCClient,
                                 val sqlQueries: HashMap<SqlQuery, String>,
                                 val readyHandler: Handler<AsyncResult<WikiDatabaseService>>) : WikiDatabaseServiceExt {

  override fun fetchAllPages(resultHandler: Handler<AsyncResult<JsonArray>>): WikiDatabaseServiceExt {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun fetchPage(name: String, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseServiceExt {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createPage(title: String, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun savePage(id: Int, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deletePage(id: Int, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}
