package fi.johannes.web.handlers.wiki.common

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.templ.TemplateEngine

/**
 * Johannes on 7.1.2018.
 */
interface WikiControllerComponents {

  fun wikiDatabaseQueue(): String
  fun eventBus(): EventBus
  fun templateEngine(): TemplateEngine
  fun <T> sendToDatabaseQueue(message: Any,
                              options: DeliveryOptions,
                              replyHandler: Handler<AsyncResult<Message<T>>>)


}
