package fi.johannes.web.handlers.wiki.index

import fi.johannes.web.handlers.wiki.common.WikiControllerComponents
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 6.1.2018.
 */
class IndexImpl(val components: WikiControllerComponents) : Index {

  override fun get(context: RoutingContext) {
    val options = DeliveryOptions().addHeader("action", "all-pages")

    components.sendToDatabaseQueue<JsonObject>(JsonObject(), options, Handler { reply ->
      if (reply.succeeded()) {
        val body = reply.result().body()
        context.put("title", "Wiki home");
        context.put("pages", body.getJsonArray("pages").getList());
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
