package fi.johannes.web.utils

import io.vertx.core.http.HttpServerRequest

/**
 * Johannes on 6.1.2018.
 */
object RequestUtils {

  fun getParam (name: String, request: HttpServerRequest, default: String = ""): String {
    val p = request.getParam(name)
    if(!p.isNullOrEmpty()) return p
    else if(default.isNotEmpty()) return default
    else throw NoSuchFieldError()
  }

  fun getParamInt (name: String, request: HttpServerRequest, default: Int? = null): Int {
    val p = request.getParam(name).toIntOrNull()
    if(p != null) return p
    else if(default != null) return default
    else throw NoSuchFieldError()
  }

}
