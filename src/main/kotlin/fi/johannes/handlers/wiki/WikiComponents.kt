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
interface WikiComponents {

  fun databaseEventBusQueue(): String
  fun eventBus(): EventBus
  fun dbClient(): SQLClient
  fun templateEngine(): TemplateEngine
  fun <T> sendToDatabaseQueue(message: Any,
                              options: DeliveryOptions,
                              replyHandler: Handler<AsyncResult<Message<T>>>)


}
