package fi.johannes.web.handlers.wiki.common

import fi.johannes.data.services.proxy.WikiDatabaseServiceExt
import io.vertx.ext.web.templ.TemplateEngine

/**
 * Johannes on 7.1.2018.
 */
interface WikiControllerComponentsExt {

  fun templateEngine(): TemplateEngine
  fun dbService(): WikiDatabaseServiceExt

}
