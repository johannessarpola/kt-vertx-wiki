package fi.johannes.web.handlers.wiki.api

import com.github.rjeschke.txtmark.Processor
import fi.johannes.web.handlers.wiki.base.ApiControllerBase
import fi.johannes.web.handlers.wiki.common.WikiControllerComponentsExt
import fi.johannes.web.utils.JsonValidation
import fi.johannes.web.utils.RequestUtils
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import kotlin.streams.toList


/**
 * Johannes on 29.1.2018.
 */
class WikiApiImpl(val components: WikiControllerComponentsExt) : WikiApi, ApiControllerBase {

  private val LOGGER = LoggerFactory.getLogger(WikiApiImpl::class.java)
  override fun logger(): Logger = LOGGER

  private fun toIndexObj(obj: JsonObject): JsonObject {
    return JsonObject()
      .put("id", obj.getInteger("ID"))
      .put("name", obj.getString("NAME"))
      .put("content", obj.getString("CONTENT"))
      .put("html", Processor.process(obj.getString("CONTENT")))
  }

  override fun getPages(context: RoutingContext) {
    components.dbService().fetchAllPages(Handler { reply ->
      if(reply.succeeded()) {
        val lst = reply.result().stream().map { toIndexObj(it) }.toList()
        okResponseWith(context, "pages", lst)
      }
      else {
        errorResponse(context, reply.cause().message)
      }
    })
  }

  override fun getPage(context: RoutingContext) {
    val ctxResponse = context.response()
    val id = RequestUtils.getParamInt("id", context.request())
    components.dbService().fetchPageById(id, Handler { reply ->
      val response = JsonObject()
      if (reply.succeeded()) {
        val dbObject = reply.result()
        if (dbObject.getBoolean("found")) {

          val payload = JsonObject()
            .put("name", dbObject.getString("name"))
            .put("id", dbObject.getInteger("id"))
            .put("markdown", dbObject.getString("rawContent"))
            .put("html", Processor.process(dbObject.getString("rawContent")))

          response
            .put("success", true)
            .put("page", payload)
          ctxResponse.statusCode = 200
        } else {
          response
            .put("success", false)
            .put("error", "There is no page with ID " + id)
          ctxResponse.statusCode = 404
        }
      } else {
        response
          .put("success", false)
          .put("error", reply.cause().message)
        ctxResponse.statusCode = 500
      }
      ctxResponse
        .putHeader("Content-Type", "application/json")
        .end(response.encode())
    })
  }

  override fun updatePage(context: RoutingContext) {
    val id = Integer.valueOf(context.request().getParam("id"))
    val page = context.bodyAsJson
    val onErr = { _: JsonObject -> badRequestResponse(page, context) }
    if (!JsonValidation.validateAgainstKeys(page, onErr, "markdown")) {
      return
    } else {
      components.dbService().savePage(id, page.getString("markdown"), simpleHandler(context))
    }
  }


  override fun createPage(context: RoutingContext) {
    val page = context.bodyAsJson
    val onErr = { _: JsonObject -> badRequestResponse(page, context) }
    if (!JsonValidation.validateAgainstKeys(page, onErr, "name", "markdown")) {
      return
    } else {
      components.dbService().createPage(page.getString("name"), page.getString("markdown"), simpleHandler(context, successCode = 201))
    }
  }

  override fun deletePage(context: RoutingContext) {
    val id = Integer.valueOf(context.request().getParam("id"))
    components.dbService().deletePage(id, simpleHandler(context))
  }


}
