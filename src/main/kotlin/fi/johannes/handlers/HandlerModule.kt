package fi.johannes.handlers

import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.bindings.Binding
import fi.johannes.handlers.wiki.Index.Index
import fi.johannes.handlers.wiki.Index.IndexImpl
import fi.johannes.starter.MainVerticle

/**
 * Johannes on 6.1.2018.
 */

class HandlerModule(private val main: Kodein) {
  val handlers = Kodein {
    extend(main)
    bind<Index>() with singleton { IndexImpl(instance(), instance()) }
  }
}
