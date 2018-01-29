package fi.johannes.web.factory

import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

/**
 * Johannes on 29.1.2018.
 */
interface WebClientFactory {
  fun newClient(options: WebClientOptions): WebClient
}
