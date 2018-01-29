package fi.johannes.web.handlers.wiki.api

import com.github.rjeschke.txtmark.Processor
import fi.johannes.web.handlers.wiki.WikiControllerComponentsExt
import fi.johannes.web.utils.RequestUtils
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.core.json.JsonObject


/**
 * Johannes on 29.1.2018.
 */
class WikiApiImpl(val components: WikiControllerComponentsExt): WikiApi {
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

  override fun savePage(context: RoutingContext) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createPage(context: RoutingContext) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deletePage(context: RoutingContext) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


}
