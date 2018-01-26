package fi.johannes.web.handlers.backup.controllers

import fi.johannes.web.dto.PageDto
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 26.1.2018.
 */
interface BackupController {

  fun saveBackup(context: RoutingContext)

  fun getLatestBackup(context: RoutingContext)

}
