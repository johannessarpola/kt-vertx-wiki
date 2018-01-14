package fi.johannes.data

import fi.johannes.data.services.proxy.WikiDatabaseServiceExt
import fi.johannes.data.services.proxy.WikiDatabaseServiceExtFactory
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import io.vertx.core.Handler
import org.junit.After


/**
 * Johannes on 13.1.2018.
 */
@RunWith(VertxUnitRunner::class)
class WikiDatabaseServiceImplTest {

  val vertx by lazy { Vertx.vertx() }
  var service: WikiDatabaseServiceExt? = null

  @Before
  fun prepare(context: TestContext) {
    val conf = JsonObject()
      .put(WikiDatabaseVerticleExt.CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:mem:test;shutdown=true")
      .put(WikiDatabaseVerticleExt.CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, 4)

    vertx.deployVerticle(
      WikiDatabaseVerticleExt(),
      DeploymentOptions().setConfig(conf),
      context.asyncAssertSuccess { id ->
        service = WikiDatabaseServiceExtFactory.createProxy(vertx, WikiDatabaseVerticleExt.CONFIG_WIKIDB_QUEUE)
      })
  }

  @Test
  fun testCrud(context: TestContext) {
    val async = context.async()
    context.assertNotNull(service)
    service?.createPage("Test", "Some content", context.asyncAssertSuccess { v1 ->

      service?.fetchPage("Test", context.asyncAssertSuccess { pageJson ->
        context.assertTrue(pageJson.getBoolean("found"))
        context.assertTrue(pageJson.containsKey("id"))
        context.assertEquals("Some content", pageJson.getString("rawContent"))

        service?.savePage(pageJson.getInteger("id"), "Yo!", context.asyncAssertSuccess { v2 ->

          service?.fetchAllPages(context.asyncAssertSuccess { array1 ->
            context.assertEquals(1, array1.size())

            service?.fetchPage("Test", context.asyncAssertSuccess { updatedPageJson ->
              context.assertEquals("Yo!", updatedPageJson.getString("rawContent"))

              service?.deletePage(pageJson.getInteger("id"), Handler { v3 ->

                service?.fetchAllPages(context.asyncAssertSuccess(Handler { array2 ->
                  context.assertTrue(array2.isEmpty)
                  async.complete()
                }))
              })
            })
          })
        })
      })
    })
    async.awaitSuccess(5000)
  }

  @After
  fun finish(context: TestContext) {
    vertx.close(context.asyncAssertSuccess())
  }
}
