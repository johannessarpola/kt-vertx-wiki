package fi.johannes.web.handlers.wiki.index

import fi.johannes.web.handlers.wiki.WikiControllerComponents
import fi.johannes.web.handlers.wiki.WikiControllerComponentsExt
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 6.1.2018.
 */
class IndexImplExt(val components: WikiControllerComponentsExt) : Index {

  override fun get(context: RoutingContext) {
    val options = DeliveryOptions().addHeader("action", "all-pages")

    components.dbService().fetchAllPages( Handler { reply ->
      if (reply.succeeded()) {
        context.put("title", "Wiki home");
        context.put("pages", reply.result().getList());
        components
          .templateEngine()
          .render(context, "templates", "/index.ftl", { ar ->
            if (ar.succeeded()) {
              context.response().putHeader("Content-Type", "text/html");
              context.response().end(ar.result());
            } else {
              context.fail(ar.cause());
            }
          });
      } else {
        context.fail(reply.cause());
      }
    })
  }
}
