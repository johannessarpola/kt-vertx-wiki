package fi.johannes.data.services.proxy

import fi.johannes.data.dao.PageDao
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import java.util.stream.Collectors


/**
 * Johannes on 9.1.2018.
 */
class WikiDatabaseServiceExtImpl(val pageDao: PageDao,
                                 val readyHandler: Handler<AsyncResult<WikiDatabaseServiceExt>>) : WikiDatabaseServiceExt {

  private val LOGGER = LoggerFactory.getLogger(WikiDatabaseServiceExtImpl::class.java)

  init {
    pageDao.createTable(
      success = { ->
        readyHandler.handle(Future.succeededFuture(this))
      },
      connectionError = { err ->
        LOGGER.error("Could not open a database connection", err)
        readyHandler.handle(Future.failedFuture(err))
      },
      createError = { err ->
        LOGGER.error("Database preparation error", err)
        readyHandler.handle(Future.failedFuture(err))
      })

  }

  private fun pageFetchSuccess(result: io.vertx.ext.sql.ResultSet): JsonObject {
    val response = JsonObject()

    if (result.getNumRows() == 0) {
      response.put("found", false)
    } else {
      response.put("found", true)
      val row = result.getResults().get(0)
      response.put("id", row.getInteger(0))
        .put("name", row.getString(1))
        .put("rawContent", row.getString(2))
    }

    return response
  }

  override fun fetchPageById(id: Int, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseServiceExt {
    val params = JsonArray().add(id)

    pageDao.fetchPageById(params,
      success = { result: io.vertx.ext.sql.ResultSet ->
        val response = pageFetchSuccess(result)
        resultHandler.handle(Future.succeededFuture(response))
      },
      error = { error ->
        LOGGER.error("Database query error", error)
        resultHandler.handle(Future.failedFuture(error))
      })

    return this
  }

  override fun fetchAllPages(resultHandler: Handler<AsyncResult<JsonArray>>): WikiDatabaseServiceExt {
    pageDao.fetchAllPages(
      success = { result ->
        val pages = result
          .getResults()
          .stream()
          .map({ json -> json.getString(0) })
          .sorted()
          .collect(Collectors.toList())
        resultHandler.handle(Future.succeededFuture(JsonArray(pages)))
      },
      error = { error ->
        LOGGER.error("Database query error", error)
        resultHandler.handle(Future.failedFuture(error))
      })
    return this
  }

  override fun fetchPage(name: String, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseServiceExt {
    val params = JsonArray().add(name)

    pageDao.fetchPage(params,
      success = { result ->
        val response = pageFetchSuccess(result)
        resultHandler.handle(Future.succeededFuture(response))
      },
      error = { error ->
        LOGGER.error("Database query error", error)
        resultHandler.handle(Future.failedFuture(error))
      })

    return this
  }

  private fun daoFail(msg: String = "Database query error",
                      error: Throwable,
                      handler: Handler<AsyncResult<Void>>) {
    LOGGER.error(msg, error)
    handler.handle(Future.failedFuture(error))
  }

  private fun blankSuccess(handler: Handler<AsyncResult<Void>>) {
    handler.handle(Future.succeededFuture())
  }

  override fun createPage(title: String, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt {
    val params = JsonArray().add(title).add(markdown)

    pageDao.insertPage(params,
      success = { ->
        blankSuccess(resultHandler)
      },
      error = { error ->
        daoFail(error = error, handler = resultHandler)
      })
    return this
  }

  override fun savePage(id: Int, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt {
    val params = JsonArray().add(markdown).add(id)

    pageDao.updatePage(params,
      success = { ->
        blankSuccess(resultHandler)
      },
      error = { error ->
        daoFail(error = error, handler = resultHandler)
      })

    return this
  }

  override fun deletePage(id: Int, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseServiceExt {
    val params = JsonArray().add(id)

    pageDao.deletePage(params,
      success = { ->
        blankSuccess(resultHandler)
      },
      error = { error ->
        daoFail(error = error, handler = resultHandler)
      })
    return this
  }

}
