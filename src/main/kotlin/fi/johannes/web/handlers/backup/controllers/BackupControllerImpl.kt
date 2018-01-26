package fi.johannes.web.handlers.backup.controllers

import fi.johannes.web.dto.PageDto
import fi.johannes.web.handlers.backup.client.BackupClient
import fi.johannes.web.utils.RequestUtils
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 26.1.2018.
 */
class BackupControllerImpl(val client: BackupClient) : BackupController {

  override fun saveBackup(context: RoutingContext) {
    val request = context.request()

    val title = RequestUtils.getParam("title", request)
    val markdown = RequestUtils.getParam("markdown", request)

    client.saveBackup(PageDto(title, markdown), Handler { result ->
      if(result.succeeded()) {
        val responseObj = JsonObject().put("result", "ok")
        context.response().end(responseObj.encodePrettily())
      }
      else {
        val responseObj = JsonObject().put("result", "not ok")
        context.response().end(responseObj.encodePrettily())
      }
    })
  }

  override fun getLatestBackup(context: RoutingContext) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    return
  }

}
