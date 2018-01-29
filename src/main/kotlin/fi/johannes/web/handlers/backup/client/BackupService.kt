package fi.johannes.web.handlers.backup.client

import fi.johannes.web.dto.PageDto
import io.vertx.core.AsyncResult
import io.vertx.core.Handler

/**
 * Johannes on 29.1.2018.
 */
interface BackupService {

  fun saveBackup(page: PageDto, resultHandler: Handler<AsyncResult<Void>>)
  // todo getLatest

}
