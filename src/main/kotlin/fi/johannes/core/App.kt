import io.vertx.core.Vertx
import fi.johannes.core.AppVerticle

fun main(args: Array<String>) {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(AppVerticle()) { ar ->
    if (ar.succeeded()) {
      println("Application started")
    } else {
      println("Could not start application")
      ar.cause().printStackTrace()
    }
  }
}

