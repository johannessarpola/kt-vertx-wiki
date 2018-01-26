package fi.johannes.web.utils

import io.vertx.ext.web.client.WebClient

/**
 * Johannes on 26.1.2018.
 */
class WebService(val configuration: Map<String, String>,
                 val protocol: String,
                 val url: String,
                 val port: Int)
