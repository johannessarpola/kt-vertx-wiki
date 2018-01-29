package fi.johannes.web.handlers.wiki.common

import fi.johannes.data.services.proxy.WikiDatabaseServiceExt
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
