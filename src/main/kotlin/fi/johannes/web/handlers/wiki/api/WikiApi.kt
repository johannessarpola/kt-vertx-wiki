package fi.johannes.web.handlers.wiki.api

import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 29.1.2018.
 */
interface WikiApi {
  fun getPage(context: RoutingContext)
  fun savePage(context: RoutingContext)
  fun createPage(context: RoutingContext)
  fun deletePage(context: RoutingContext)
}
