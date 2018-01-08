package fi.johannes.web.handlers.wiki.index

import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 6.1.2018.
 */
interface Index {

  /**
   * Gets all pages and uses index.ftl
   */
  fun get(context: RoutingContext)

}
