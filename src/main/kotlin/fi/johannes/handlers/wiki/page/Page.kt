package fi.johannes.handlers.wiki.page

import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 6.1.2018.
 */
interface Page {

  /**
   * Gets a single page using page's name
   */
  fun get(context: RoutingContext)

  /**
   * Saves using id if updating otherwise creates new
   */
  fun save(context: RoutingContext)

  /**
   * Redirects to edit view which uses saving
   */
  fun create(context: RoutingContext)

  /**
   * Deletes a page using id
   */
  fun delete(context: RoutingContext)

}
