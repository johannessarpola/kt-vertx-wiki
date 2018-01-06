package fi.johannes.handlers.wiki.index

import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 6.1.2018.
 */
interface Index {

  fun get(context: RoutingContext)

}
