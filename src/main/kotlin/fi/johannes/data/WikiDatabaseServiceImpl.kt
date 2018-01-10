package fi.johannes.data

import fi.johannes.data.dao.PageDao
import fi.johannes.data.enums.ErrorCodes
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import java.util.stream.Collectors


/**
 * Johannes on 8.1.2018.
 */
class WikiDatabaseServiceImpl(private val pageDao: PageDao,
                              private val verticleLogging: Logger) : WikiDatabaseService {

  override fun fetchAllPages(message: Message<JsonObject>) {
    pageDao.fetchAllPages(
      success = { result ->
        val pages = result
          .getResults()
          .stream()
          .map({ json -> json.getString(0) })
          .sorted()
          .collect(Collectors.toList())
        message.reply(JsonObject().put("pages", JsonArray(pages)))
      },
      error = { error ->
        reportQueryError(message, error)
      })
  }

  override fun fetchPage(message: Message<JsonObject>) {
    val requestedPage = message.body().getString("page")
    val params = JsonArray().add(requestedPage)

    pageDao.fetchPage(params,
      success = { result ->
        val response = JsonObject()
        if (result.getNumRows() == 0) {
          response.put("found", false)
        } else {
          response.put("found", true)
          val row = result.getResults().get(0)
          response.put("id", row.getInteger(0))
            .put("rawContent", row.getString(1))
        }
        message.reply(response)
      },
      error = { error ->
        reportQueryError(message, error)
      })
  }

  override fun createPage(message: Message<JsonObject>) {
    val request = message.body()
    val params = JsonArray()
      .add(request.getString("title"))
      .add(request.getString("markdown"))

    pageDao.insertPage(params,
      success = { ->
        message.reply("ok")
      },
      error = { error ->
        reportQueryError(message, error)
      })
  }

  override fun savePage(message: Message<JsonObject>) {
    val request = message.body()
    val params = JsonArray()
      .add(request.getString("markdown"))
      .add(request.getString("id"))

    pageDao.insertPage(params,
      success = { ->
        message.reply("ok")
      },
      error = { error ->
        reportQueryError(message, error)
      })
  }

  override fun deletePage(message: Message<JsonObject>) {
    val params = JsonArray().add(message.body().getString("id"))

    pageDao.deletePage(params,
      success = { ->
        message.reply("ok")
      },
      error = { error ->
        reportQueryError(message, error)
      })
  }

  private fun reportQueryError(message: Message<JsonObject>, cause: Throwable) {
    verticleLogging.error("Database query error", cause)
    message.fail(ErrorCodes.DB_ERROR.ordinal, cause.message)
  }
}
