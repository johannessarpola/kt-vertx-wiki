package fi.johannes.web.handlers.backup

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import fi.johannes.web.handlers.backup.client.BackupService
import fi.johannes.web.handlers.backup.client.BackupServiceImpl
import fi.johannes.web.handlers.backup.controllers.BackupController
import fi.johannes.web.handlers.backup.controllers.BackupControllerImpl
import fi.johannes.web.utils.WebService
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import java.util.*

/**
 * Johannes on 26.1.2018.
 */
class BackupControllers(private val wsSupplier: (WebClientOptions) -> WebClient) {

  private val wsConf = WebService(Collections.emptyMap(), "http", "localhost", 8090)

  val injector = Kodein {
    bind<BackupService>() with singleton { BackupServiceImpl(wsConf, wsSupplier) }
    bind<BackupController>() with singleton { BackupControllerImpl(instance()) }
  }
}
