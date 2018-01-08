package fi.johannes.web.handlers.wiki

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.templ.TemplateEngine

/**
 * Johannes on 7.1.2018.
 */
class WikiControllerComponetsImpl(val templateEngine: TemplateEngine,
                                  val dbQueue: String,
                                  val eventBus: EventBus): WikiControllerComponents {
  override fun <T> sendToDatabaseQueue(message: Any,
                                       options: DeliveryOptions,
                                       replyHandler: Handler<AsyncResult<Message<T>>>) {
    eventBus.send(dbQueue, message, options, replyHandler)
  }

  override fun templateEngine(): TemplateEngine {
    return templateEngine
  }
  override fun wikiDatabaseQueue(): String {
    return dbQueue
  }
  override fun eventBus(): EventBus {
    return eventBus
  }

}
