package fi.johannes.web.handlers.wiki.base

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

/**
 * Johannes on 3.2.2018.
 */
interface ApiControllerBase {

  fun logger(): Logger

  fun badRequest(): JsonObject {
    return JsonObject().put("success", false).put("error", "Bad request payload")
  }

  fun badRequestResponse(json: JsonObject, routingContext: RoutingContext) {
    logger().error("Bad page creation JSON payload: " + json.encodePrettily() + " from " + routingContext.request().remoteAddress())
    routingContext.response().setStatusCode(400)
      .putHeader("Content-Type", "application/json")
      .end(badRequest().encode())
  }

  fun ok(): JsonObject {
    return JsonObject()
      .put("success", true)
  }

  fun <T> okResponseWith(routingContext: RoutingContext, key: String, result: T, statusCode: Int = 200) {
    routingContext.response()
      .setStatusCode(statusCode)
      .putHeader("Content-Type", "application/json")
      .end(ok().put(key, result).encode())
  }

  fun okResponse(routingContext: RoutingContext, statusCode: Int = 200) {
    routingContext.response()
      .setStatusCode(statusCode)
      .putHeader("Content-Type", "application/json")
      .end(ok().encode())
  }

  fun error(errorMessage: String?): JsonObject {
    return JsonObject()
      .put("success", false)
      .put("error", errorMessage)
  }
  fun errorResponse(routingContext: RoutingContext, errorMessage: String?, errorCode: Int = 500) {
    routingContext.response().setStatusCode(errorCode)
      .putHeader("Content-Type", "application/json")
      .end(error(errorMessage).encode())
  }

  fun handleSimpleResponse(routingContext: RoutingContext,
                           successCode: Int = 200,
                           errorCode: Int = 500,
                           reply: AsyncResult<Void>) {
    if(reply.succeeded()) okResponse(routingContext, successCode)
    else errorResponse(routingContext, reply.cause().message, errorCode)
  }

  fun simpleHandler(context: RoutingContext,
                    successCode: Int = 200,
                    errorCode: Int = 500): Handler<AsyncResult<Void>> = Handler { reply -> handleSimpleResponse(context, successCode, errorCode, reply) }

}
