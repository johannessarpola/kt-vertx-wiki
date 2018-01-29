package fi.johannes.web.handlers.wiki

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import fi.johannes.data.services.proxy.WikiDatabaseServiceExt
import fi.johannes.web.handlers.wiki.api.WikiApi
import fi.johannes.web.handlers.wiki.api.WikiApiImpl
import fi.johannes.web.handlers.wiki.common.WikiControllerComponentsExt
import fi.johannes.web.handlers.wiki.common.WikiControllerComponentsImplExt
import fi.johannes.web.handlers.wiki.index.Index
import fi.johannes.web.handlers.wiki.index.IndexImplExt
import fi.johannes.web.handlers.wiki.page.Page
import fi.johannes.web.handlers.wiki.page.PageImplExt
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine

/**
 * Johannes on 6.1.2018.
 */

class WikiControllersExt(dbService: WikiDatabaseServiceExt) {

  val injector = Kodein {
    bind<WikiControllerComponentsExt>() with singleton {
      WikiControllerComponentsImplExt(
        FreeMarkerTemplateEngine.create(),
        dbService)
    }
    bind<Index>() with singleton { IndexImplExt(instance()) }
    bind<Page>() with singleton { PageImplExt(instance()) }
    bind<WikiApi>() with singleton { WikiApiImpl(instance()) }
  }


}

