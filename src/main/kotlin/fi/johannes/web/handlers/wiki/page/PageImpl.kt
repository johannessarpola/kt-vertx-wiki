package fi.johannes.web.handlers.wiki.page

import com.github.rjeschke.txtmark.Processor
import fi.johannes.web.handlers.wiki.common.WikiControllerComponents
import fi.johannes.web.utils.RequestUtils.getParam
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.util.*


/**
 * Johannes on 6.1.2018.
 */
class PageImpl(val components: WikiControllerComponents):Page {

  val EMPTY_PAGE_MARKDOWN = "# A new page\n\n Feel-free to write in Markdown!\n";

  override fun get(context: RoutingContext) {
    val page = context.request().getParam("page");
    val request = JsonObject().put("page", page)
    val options = DeliveryOptions().addHeader("action", "get-page")

    components.sendToDatabaseQueue<JsonObject>(request, options, Handler { reply ->

      if (reply.succeeded()) {
        val body = reply.result().body() as JsonObject

        val found = body.getBoolean("found")!!
        val rawContent = body.getString("rawContent", EMPTY_PAGE_MARKDOWN)
        context.put("title", page)
          .put("id", body.getInteger("id", -1))
          .put("newPage", if (found) "no" else "yes")
          .put("rawContent", rawContent)
          .put("content", Processor.process(rawContent))
          .put("timestamp", Date().toString())

        components.templateEngine().render(context, "templates", "/page.ftl", { ar ->
          if (ar.succeeded()) {
            context.response()
              .putHeader("Content-Type", "text/html")
              .end(ar.result())
          } else {
            context.fail(ar.cause())
          }
        })

      } else {
        context.fail(reply.cause())
      }
    })
  }

  override fun save(context: RoutingContext) {
    val request = context.request()

    val title = getParam("title", request)
    val jsonRequest = JsonObject()
      .put("id", getParam("id", request))
      .put("title", title)
      .put("markdown", getParam("markdown", request))

    val options = DeliveryOptions()
    if ("yes" == getParam("newPage", request)) {
      options.addHeader("action", "create-page")
    } else {
      options.addHeader("action", "save-page")
    }

    components.sendToDatabaseQueue<JsonObject>(jsonRequest, options, Handler { reply ->
      if (reply.succeeded()) {
        val response = context.response()
          .setStatusCode(303)
          .putHeader("Location", "/wiki/" + title)
          .end()
      } else {
        context.fail(reply.cause())
      }
    })
  }

  override fun create(context: RoutingContext) {
    val pageName = getParam(name = "page", request = context.request());

    val location = if (pageName.isEmpty()) "/" else "/wiki/" + pageName;

    context.response()
      .setStatusCode(303)
      .putHeader("Location", location)
      .end()
  }

  override fun delete(context: RoutingContext) {
    val request = context.request()

    val id = getParam(name = "id", request = request)

    val jsonRequest = JsonObject().put("id", id)
    val options = DeliveryOptions().addHeader("action", "delete-page")
    components.sendToDatabaseQueue<JsonObject>(jsonRequest, options, Handler { reply ->
      if (reply.succeeded()) {
        context.response().setStatusCode(303)
          .putHeader("Location", "/")
          .end()
      } else {
        context.fail(reply.cause())
      }
    })
  }
}
