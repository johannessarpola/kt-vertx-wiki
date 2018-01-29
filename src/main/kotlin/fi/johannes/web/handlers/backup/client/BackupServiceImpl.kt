package fi.johannes.web.handlers.backup.client

import fi.johannes.web.dto.PageDto
import fi.johannes.web.utils.WebService
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

/**
 * Johannes on 26.1.2018.
 */
class BackupServiceImpl(private val webService: WebService,
                        private val createClient: (WebClientOptions) -> WebClient) : BackupService {

  override fun saveBackup(page: PageDto, resultHandler: Handler<AsyncResult<Void>>) {
    val options = WebClientOptions()
    val client = createClient(options)
    client.post(webService.port, webService.url, "/backup/save")
      .putHeader("apiKey", webService.configuration.get("apiKey").orEmpty())
      .sendJson(page, { result ->
        if (result.succeeded()) {
          resultHandler.handle(Future.succeededFuture())
        } else {
          resultHandler.handle(Future.failedFuture(result.cause()))
        }
      })
    client.close()
  }

}
