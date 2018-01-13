package fi.johannes.web.handlers.wiki

import fi.johannes.data.ext.WikiDatabaseServiceExt
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.templ.TemplateEngine

/**
 * Johannes on 7.1.2018.
 */
class WikiControllerComponentsImplExt(val templateEngine: TemplateEngine,
                                      val dbService: WikiDatabaseServiceExt): WikiControllerComponentsExt {

  override fun templateEngine(): TemplateEngine {
    return templateEngine
  }

  override fun dbService(): WikiDatabaseServiceExt {
    return dbService
  }
}
