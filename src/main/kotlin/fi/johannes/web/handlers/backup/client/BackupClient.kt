package fi.johannes.web.handlers.backup.client

import fi.johannes.web.dto.PageDto
import fi.johannes.web.utils.WebService
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient

/**
 * Johannes on 26.1.2018.
 */
class BackupClient(val webService: WebService,
                   val client: WebClient) {

  fun saveBackup(page:PageDto, resultHandler: Handler<AsyncResult<Void>>) {
      client.post(webService.port, webService.url,"/backup/save")
        .putHeader("apiKey", webService.configuration.get("apiKey").orEmpty())
        .sendJson(page, Handler { result ->
          if(result.succeeded()) {
            resultHandler.handle(Future.succeededFuture())
          }
          else {
            resultHandler.handle(Future.failedFuture(result.cause()))
          }
        })
  }

  fun dispose() {
    client.close()
  }
}
