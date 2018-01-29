package fi.johannes.web.handlers.wiki

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import fi.johannes.web.handlers.wiki.common.WikiControllerComponents
import fi.johannes.web.handlers.wiki.common.WikiControllerComponentsImpl
import fi.johannes.web.handlers.wiki.index.Index
import fi.johannes.web.handlers.wiki.index.IndexImpl
import fi.johannes.web.handlers.wiki.page.Page
import fi.johannes.web.handlers.wiki.page.PageImpl
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine

/**
 * Johannes on 6.1.2018.
 */

class WikiControllers(val dbQueue: String,
                      val eventBus: EventBus) {
  val injector = Kodein {
    bind<WikiControllerComponents>() with singleton {
      WikiControllerComponentsImpl(
        FreeMarkerTemplateEngine.create(),
        dbQueue,
        eventBus)
    }
    bind<Index>() with singleton { IndexImpl(instance()) }
    bind<Page>() with singleton { PageImpl(instance()) }
  }


}

