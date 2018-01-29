package fi.johannes.web.factory

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

/**
 * Johannes on 29.1.2018.
 */
class WebClientFactoryImpl(val vertx: Vertx) : WebClientFactory{

  override fun newClient(options: WebClientOptions): WebClient {
    return WebClient.create(vertx, options)
  }
}
