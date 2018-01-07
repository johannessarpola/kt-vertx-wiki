package fi.johannes.handlers.wiki

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.templ.TemplateEngine

/**
 * Johannes on 7.1.2018.
 */
class WikiComponentsImpl(val dbClient: SQLClient,
                         val templateEngine: TemplateEngine,
                         val dbQueue: Pair<String, EventBus>): WikiComponents {
  override fun <T> sendToDatabaseQueue(message: Any,
                                       options: DeliveryOptions,
                                       replyHandler: Handler<AsyncResult<Message<T>>>) {
    eventBus().send(databaseEventBusQueue(), message, options, replyHandler)
  }


  override fun dbClient(): SQLClient {
    return dbClient
  }
  override fun templateEngine(): TemplateEngine {
    return templateEngine
  }
  override fun databaseEventBusQueue(): String {
    return dbQueue.first
  }
  override fun eventBus(): EventBus {
    return dbQueue.second
  }

}
