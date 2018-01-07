package fi.johannes.handlers.wiki

import com.github.salomonbrys.kodein.*
import fi.johannes.handlers.wiki.index.Index
import fi.johannes.handlers.wiki.index.IndexImpl
import fi.johannes.handlers.wiki.page.Page
import fi.johannes.handlers.wiki.page.PageImpl

/**
 * Johannes on 6.1.2018.
 */

class WikiHandlers(private val main: Kodein) {
  val injector = Kodein {
    extend(main)
    bind<WikiComponents>() with singleton { WikiComponentsImpl(
      instance(),
      instance(),
      Pair(instance<String>("wikiDatabaseQueue"), instance()))
    }
    bind<Index>() with singleton { IndexImpl(instance()) }
    bind<Page>() with singleton { PageImpl(instance()) }
  }

}
