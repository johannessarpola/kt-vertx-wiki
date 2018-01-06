package fi.johannes.starter;

import com.github.rjeschke.txtmark.Processor
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import fi.johannes.handlers.HandlerModule
import fi.johannes.handlers.wiki.Index.Index
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine
import fi.johannes.utils.RequestUtils.getParam
import io.vertx.core.logging.Logger
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.templ.TemplateEngine
import java.util.*
import java.util.stream.Collectors

class MainVerticle : AbstractVerticle() {
  private val SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity primary key, Name varchar(255) unique, Content clob)"
  private val SQL_GET_PAGE = "select Id, Content from Pages where Name = ?"
  private val SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)"
  private val SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?"
  private val SQL_ALL_PAGES = "select Name from Pages"
  private val SQL_DELETE_PAGE = "delete from Pages where Id = ?"

  private val EMPTY_PAGE_MARKDOWN = "# A new page\n\n Feel-free to write in Markdown!\n";

  private val logger: Logger  by lazy {
    LoggerFactory.getLogger("MainVerticle")
  }

  private val dbClient: JDBCClient by lazy {
    JDBCClient.createShared(vertx, JsonObject()
      .put("url", "jdbc:hsqldb:file:db/wiki")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30))
  }
  private val templateEngine by lazy {
    FreeMarkerTemplateEngine.create()
  }

  val appModule = Kodein {
    bind<SQLClient>() with singleton { dbClient }
    bind<TemplateEngine>() with singleton { templateEngine }
    bind<Logger>() with singleton { logger }
  }
  val handlerModule = HandlerModule(appModule)


  private val wikiDbQueue = "wikidb.queue";

  val CONFIG_HTTP_SERVER_PORT = "http.server.port";
  val CONFIG_WIKIDB_QUEUE = "wikidb.queue";


  override fun start(startFuture: Future<Void>) {
    val steps = prepareDatabase().compose { v -> startHttpServer() }
    steps.setHandler { ar ->
      when (ar.succeeded()) {
        true -> startFuture.complete()
        false -> startFuture.fail(ar.cause())
      }
    }
  }

  private fun prepareDatabase(): Future<Void> {
    val future = Future.future<Void>()

    dbClient.getConnection { ar ->
      when (ar.failed()) {
        true -> {
          logger.error("Could not open a database connection", ar.cause())
          future.fail(ar.cause())
        }
        false -> {
          val connection = ar.result()
          connection.execute(SQL_CREATE_PAGES_TABLE, { create ->
            connection.close()
            if (create.failed()) {
              logger.error("Database preparation error", create.cause())
              future.fail(create.cause())
            } else {
              future.complete()
            }
          })
        }
      }
    }
    return future
  }

  private fun startHttpServer(): Future<Void> {
    val future = Future.future<Void>()

    val router = Router.router(vertx);
    val server = vertx.createHttpServer();

    val index: Index = handlerModule.handlers.instance(); // todo could use string tags to even less coupling
    router.get("/").handler(index::handler);
    router.get("/wiki/:page").handler(this::pageRenderingHandler);

    router.post().handler(BodyHandler.create());
    router.post("/save").handler(this::pageUpdateHandler);
    router.post("/create").handler(this::pageCreateHandler);
    router.post("/delete").handler(this::pageDeletionHandler);

    val portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);

    server
      .requestHandler(router::accept)
      .listen(portNumber, { ar ->
        if (ar.succeeded()) {
          logger.info("HTTP server running on port 8080");
          future.complete();
        } else {
          logger.error("Could not start a HTTP server", ar.cause());
          future.fail(ar.cause());
        }
      });
    return future
  }

  private fun indexHandler(context: RoutingContext, dbClient: SQLClient) {
    dbClient.getConnection { car ->
      if (car.succeeded()) {
        val connection = car.result();
        connection.query(SQL_ALL_PAGES, { res ->
          connection.close();

          if (res.succeeded()) {
            // todo move to own endpóint and use as REST API
            val pages = res.result()
              .getResults()
              .stream()
              .map { json -> json.getString(0) }
              .sorted()
              .collect(Collectors.toList());

            context.put("title", "Wiki home");
            context.put("pages", pages);

            // fuck the template engines
            templateEngine.render(context, "templates", "/index.ftl", { ar ->
              when (ar.succeeded()) {
                true -> {
                  context.response().putHeader("Content-Type", "text/html").end(ar.result());
                }
                false -> context.fail(ar.cause());
              }
            })
          } else {
            context.fail(res.cause()); (5)
          }
        })
      } else {
        context.fail(car.cause());
      }
    }
  }

  private fun pageRenderingHandler(context: RoutingContext) {
    val page = context.request().getParam("page");

    dbClient.getConnection { car ->
      if (car.succeeded()) {
        val connection = car.result()
        connection.queryWithParams(SQL_GET_PAGE, JsonArray().add(page), { fetch ->
          connection.close()
          if (fetch.succeeded()) {

            val row = fetch.result().getResults()
              .stream()
              .findFirst()
              .orElseGet { JsonArray().add(-1).add(EMPTY_PAGE_MARKDOWN) }

            val id = row.getInteger(0)
            val rawContent = row.getString(1)

            context.put("title", page)
              .put("id", id)
              .put("newPage", if (fetch.result().getResults().size == 0) "yes" else "no")
              .put("rawContent", rawContent)
              .put("content", Processor.process(rawContent))
              .put("timestamp", Date().toString())

            templateEngine.render(context, "templates", "/page.ftl", { ar ->
              if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html").end(ar.result());
              } else {
                context.fail(ar.cause());
              }
            })
          } else {
            context.fail(fetch.cause());
          }
        })
      } else {
        context.fail(car.cause());

      }
    }
  }

  private fun pageUpdateHandler(context: RoutingContext) {
    val request = context.request()

    val id = getParam(name = "id", request = request)
    val title = getParam(name = "title", request = request)
    val markdown = getParam(name = "markdown", request = request)
    val newPage = "yes" == getParam(name = "newPage", request = request)

    dbClient.getConnection { car ->
      if (car.succeeded()) {
        val connection = car.result()
        val sql = if (newPage) SQL_CREATE_PAGE else SQL_SAVE_PAGE
        val params = JsonArray()
        if (newPage) {
          params.add(title).add(markdown)
        } else {
          params.add(markdown).add(id)
        }

        connection.updateWithParams(sql, params, { res ->
          connection.close();
          if (res.succeeded()) {
            context.response().setStatusCode(303).putHeader("Location", "/wiki/" + title).end()
          } else {
            context.fail(res.cause())
          }
        })
      } else {
        context.fail(car.cause());
      }
    }

  }

  private fun pageCreateHandler(context: RoutingContext) {
    val pageName = context.request().getParam("name");

    val location = if (pageName == null || pageName.isEmpty()) "/" else "/wiki/" + pageName;

    context.response()
      .setStatusCode(303)
      .putHeader("Location", location)
      .end()
  }

  private fun pageDeletionHandler(context: RoutingContext) {
    val request = context.request()

    val id = getParam(name = "id", request = request)

    dbClient.getConnection { car ->
      if (car.succeeded()) {
        val connection = car.result()
        connection.updateWithParams(SQL_DELETE_PAGE, JsonArray().add(id), { res ->
          connection.close();
          if (res.succeeded()) {
            context.response().setStatusCode(303).putHeader("Location", "/").end()
          } else {
            context.fail(res.cause())
          }
        });
      } else {
        context.fail(car.cause())
      }
    }
  }
}


