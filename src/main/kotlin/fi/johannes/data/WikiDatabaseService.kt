package fi.johannes.data

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

/**
 * Johannes on 8.1.2018.
 */
interface WikiDatabaseService {
  fun fetchAllPages(message: Message<JsonObject>)
  fun fetchPage(message: Message<JsonObject>)
  fun createPage(message: Message<JsonObject>)
  fun savePage(message: Message<JsonObject>)
  fun deletePage(message: Message<JsonObject>)
}
