package fi.johannes.utils

import io.vertx.core.http.HttpServerRequest

/**
 * Johannes on 6.1.2018.
 */
object RequestUtils {

  fun getParam (name: String, request: HttpServerRequest, default: String = ""): String {
    val p = request.getParam(name)
    if(p.isNotEmpty()) return p
    else if(default.isNotEmpty()) return default
    else throw NoSuchFieldError()
  }
}
