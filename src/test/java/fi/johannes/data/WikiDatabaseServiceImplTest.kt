package fi.johannes.data

import fi.johannes.data.ext.WikiDatabaseServiceExt
import fi.johannes.data.ext.WikiDatabaseServiceExtFactory
import fi.johannes.data.ext.WikiDatabaseServiceExtImpl
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.close
import io.vertx.core.Handler
import org.junit.After
import io.vertx.core.impl.VertxImpl.context


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

    service?.createPage("Test", "Some content", context.asyncAssertSuccess { v1 ->

      service?.fetchPage("Test", context.asyncAssertSuccess { json1 ->
        context.assertTrue(json1.getBoolean("found"))
        context.assertTrue(json1.containsKey("id"))
        context.assertEquals("Some content", json1.getString("rawContent"))

        service?.savePage(json1.getInteger("id"), "Yo!", context.asyncAssertSuccess { v2 ->

          service?.fetchAllPages(context.asyncAssertSuccess { array1 ->
            context.assertEquals(1, array1.size())

            service?.fetchPage("Test", context.asyncAssertSuccess { json2 ->
              context.assertEquals("Yo!", json2.getString("rawContent"))

              service?.deletePage(json1.getInteger("id"), Handler { v3 ->

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
