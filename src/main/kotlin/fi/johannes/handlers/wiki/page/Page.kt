package fi.johannes.handlers.wiki.page

import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 6.1.2018.
 */
interface Page {

  fun get(context: RoutingContext)
  fun save(context: RoutingContext) // PUT
  fun create(context: RoutingContext)
  fun delete(context: RoutingContext) // DELETE

}
