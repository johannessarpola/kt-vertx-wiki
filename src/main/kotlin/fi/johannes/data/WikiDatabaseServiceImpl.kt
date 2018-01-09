package fi.johannes.data

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.stream.Collectors
import io.vertx.core.eventbus.Message
import io.vertx.ext.sql.SQLClient
import fi.johannes.data.enums.SqlQuery
import fi.johannes.data.enums.ErrorCodes
import fi.johannes.data.ext.WikiDatabaseServiceExt
import fi.johannes.data.ext.WikiDatabaseServiceExtImpl
import fi.johannes.data.ext.WikiDatabaseServiceExtVertxEBProxy
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.ext.jdbc.JDBCClient


/**
 * Johannes on 8.1.2018.
 */
class WikiDatabaseServiceImpl(private val sqlQueries: Map<SqlQuery, String>,
                              private val sqlClient: SQLClient,
                              private val verticleLogging: Logger) : WikiDatabaseService {

  override fun fetchAllPages(message: Message<JsonObject>) {
    sqlClient.query(sqlQueries[SqlQuery.ALL_PAGES], { res ->
      if (res.succeeded()) {
        val pages = res.result()
          .getResults()
          .stream()
          .map({ json -> json.getString(0) })
          .sorted()
          .collect(Collectors.toList())
        message.reply(JsonObject().put("pages", JsonArray(pages)))
      } else {
        reportQueryError(message, res.cause())
      }
    })
  }

  override fun fetchPage(message: Message<JsonObject>) {
    val requestedPage = message.body().getString("page")
    val params = JsonArray().add(requestedPage)

    sqlClient.queryWithParams(sqlQueries[SqlQuery.GET_PAGE], params, { fetch ->
      if (fetch.succeeded()) {
        val response = JsonObject()
        val resultSet = fetch.result()
        if (resultSet.getNumRows() == 0) {
          response.put("found", false)
        } else {
          response.put("found", true)
          val row = resultSet.getResults().get(0)
          response.put("id", row.getInteger(0))
          response.put("rawContent", row.getString(1))
        }
        message.reply(response)
      } else {
        reportQueryError(message, fetch.cause())
      }
    })
  }

  override fun createPage(message: Message<JsonObject>) {
    val request = message.body()
    val data = JsonArray()
      .add(request.getString("title"))
      .add(request.getString("markdown"))

    sqlClient.updateWithParams(sqlQueries.get(SqlQuery.CREATE_PAGE), data, { res ->
      if (res.succeeded()) {
        message.reply("ok")
      } else {
        reportQueryError(message, res.cause())
      }
    })
  }

  override fun savePage(message: Message<JsonObject>) {
    val request = message.body()
    val data = JsonArray()
      .add(request.getString("markdown"))
      .add(request.getString("id"))

    sqlClient.updateWithParams(sqlQueries[SqlQuery.SAVE_PAGE], data, { res ->
      if (res.succeeded()) {
        message.reply("ok")
      } else {
        reportQueryError(message, res.cause())
      }
    })
  }

  override fun deletePage(message: Message<JsonObject>) {
    val data = JsonArray().add(message.body().getString("id"))

    sqlClient.updateWithParams(sqlQueries[SqlQuery.DELETE_PAGE], data, { res ->
      if (res.succeeded()) {
        message.reply("ok")
      } else {
        reportQueryError(message, res.cause())
      }
    })
  }

  private fun reportQueryError(message: Message<JsonObject>, cause: Throwable) {
    verticleLogging.error("Database query error", cause)
    message.fail(ErrorCodes.DB_ERROR.ordinal, cause.message)
  }
}
