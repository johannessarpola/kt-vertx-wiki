package fi.johannes.web.handlers.backup

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import fi.johannes.web.handlers.backup.client.BackupClient
import fi.johannes.web.handlers.backup.controllers.BackupController
import fi.johannes.web.handlers.backup.controllers.BackupControllerImpl
import fi.johannes.web.utils.WebService
import io.vertx.ext.web.client.WebClient
import java.util.*

/**
 * Johannes on 26.1.2018.
 */
class BackupControllers(val wsClient: WebClient) {

  private val wsConf = WebService(Collections.emptyMap(), "http", "localhost", 8090)

  val injector = Kodein {
    bind<BackupClient>() with singleton { BackupClient(wsConf, wsClient) }
    bind<BackupController>() with singleton { BackupControllerImpl(instance()) }
  }
}
