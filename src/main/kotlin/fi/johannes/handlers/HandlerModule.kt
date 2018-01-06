package fi.johannes.handlers

import com.github.salomonbrys.kodein.*
import fi.johannes.handlers.wiki.index.Index
import fi.johannes.handlers.wiki.index.IndexImpl
import fi.johannes.handlers.wiki.page.Page
import fi.johannes.handlers.wiki.page.PageImpl

/**
 * Johannes on 6.1.2018.
 */

class HandlerModule(private val main: Kodein) {
  val handlers = Kodein {
    extend(main)
    bind<Index>() with singleton { IndexImpl(instance(), instance()) }
    bind<Page>() with singleton { PageImpl(instance(), instance()) }
  }
}
