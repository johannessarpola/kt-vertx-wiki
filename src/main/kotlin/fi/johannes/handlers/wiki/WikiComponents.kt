package fi.johannes.handlers.wiki

import io.vertx.core.eventbus.EventBus
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.templ.TemplateEngine

/**
 * Johannes on 7.1.2018.
 */
interface WikiComponents {

  fun eventBusQueue(): String
  fun eventBus(): EventBus
  fun dbClient(): SQLClient
  fun templateEngine(): TemplateEngine

}
