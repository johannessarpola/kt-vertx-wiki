package fi.johannes.web.handlers.wiki.page

import com.github.rjeschke.txtmark.Processor
import fi.johannes.web.handlers.wiki.WikiControllerComponents
import fi.johannes.web.handlers.wiki.WikiControllerComponentsExt
import fi.johannes.web.utils.RequestUtils.getParam
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.util.*
import io.vertx.core.impl.VertxImpl.context
import io.vertx.core.AsyncResult




/**
 * Johannes on 6.1.2018.
 */
class PageImplExt(val components: WikiControllerComponentsExt):Page {

  val EMPTY_PAGE_MARKDOWN = "# A new page\n\n Feel-free to write in Markdown!\n";

  override fun get(context: RoutingContext) {
    val page = getParam("page", context.request());

    components.dbService().fetchPage(page, Handler { reply ->

      if (reply.succeeded()) {
        val fetchResult = reply.result()

        val found = fetchResult.getBoolean("found")!!
        val rawContent = fetchResult.getString("rawContent", EMPTY_PAGE_MARKDOWN)
        context.put("title", page)
          .put("id", fetchResult.getInteger("id", -1))
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

    val id = Integer.valueOf(getParam("id", request))
    val title = getParam("title", request)
    val markdown =  getParam("markdown", request)

    val handler = Handler<AsyncResult<Void>> { reply ->
      if (reply.succeeded()) {
        context.response().setStatusCode(303)
          .putHeader("Location", "/wiki/" + title)
          .end()
      } else {
        context.fail(reply.cause())
      }
    }

    if ("yes" == getParam("newPage", request)) {
      components.dbService().createPage(title, markdown, handler)
    } else {
      components.dbService().savePage(id, markdown, handler)
    }

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

    val id = Integer.valueOf(getParam(name = "id", request = request))

    components.dbService().deletePage(id, Handler { reply ->
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
