package fi.johannes.core

import fi.johannes.data.WikiDatabaseVerticleExt
import fi.johannes.web.HttpServerVerticleExt
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.ext.web.codec.BodyCodec
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Johannes on 4.2.2018.
 */
@RunWith(VertxUnitRunner::class)
class WikiApiTest {

  val vertx = Vertx.vertx()

  val webClient by lazy {
    WebClient.create(vertx, WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(8080))
  }

  @Before
  fun prepare(context: TestContext) {
    val dbConf = JsonObject()
      .put(WikiDatabaseVerticleExt.CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:mem:testdb;shutdown=true")
      .put(WikiDatabaseVerticleExt.CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, 4)

    vertx.deployVerticle(WikiDatabaseVerticleExt(), DeploymentOptions().setConfig(dbConf), context.asyncAssertSuccess())
    vertx.deployVerticle(HttpServerVerticleExt(),context.asyncAssertSuccess())
  }
  @Test
  fun wikiApiSpec(context: TestContext) {
    val async = context.async()

    val page = JsonObject()
      .put("name", "Sample")
      .put("markdown", "# A page")

    /**
     * Create a page
     */
    val postRequest = Future.future<JsonObject>()
    webClient.post("/wiki/api/pages")
      .`as`(BodyCodec.jsonObject())
      .sendJsonObject(page, { ar ->
        if (ar.succeeded()) {
          val postResponse = ar.result()
          postRequest.complete(postResponse.body())
        } else {
          context.fail(ar.cause())
        }
      })

    /**
     * Get all pages
     */
    val getRequest = Future.future<JsonObject>()
    postRequest.compose({ h ->
      webClient.get("/wiki/api/pages")
        .`as`(BodyCodec.jsonObject())
        .send({ ar ->
          if (ar.succeeded()) {
            val getResponse = ar.result()
            getRequest.complete(getResponse.body())
          } else {
            context.fail(ar.cause())
          }
        })
    }, getRequest)

    /**
     * Update a page
     */
    val putRequest = Future.future<JsonObject>()
    getRequest.compose({ response ->
      val array = response.getJsonArray("pages")
      context.assertEquals(1, array.size())
      context.assertEquals(0, array.getJsonObject(0).getInteger("id"))
      webClient.put("/wiki/api/pages/0")
        .`as`(BodyCodec.jsonObject())
        .sendJsonObject(JsonObject()
          .put("id", 0)
          .put("markdown", "Oh Yeah!"), { ar ->
          if (ar.succeeded()) {
            val putResponse = ar.result()
            putRequest.complete(putResponse.body())
          } else {
            context.fail(ar.cause())
          }
        })
    }, putRequest)

    /**
     * Delete a page
     */
    val deleteRequest = Future.future<JsonObject>()
    putRequest.compose({ response ->
      context.assertTrue(response.getBoolean("success"))
      webClient.delete("/wiki/api/pages/0")
        .`as`(BodyCodec.jsonObject())
        .send({ ar ->
          if (ar.succeeded()) {
            val delResponse = ar.result()
            deleteRequest.complete(delResponse.body())
          } else {
            context.fail(ar.cause())
          }
        })
    }, deleteRequest)

    deleteRequest.compose({ response ->
      context.assertTrue(response.getBoolean("success"))
      async.complete()
    }, Future.failedFuture<String>("Oh?"))
  }

  @After
  fun finish(context: TestContext) {
    vertx.close(context.asyncAssertSuccess())
  }
}
