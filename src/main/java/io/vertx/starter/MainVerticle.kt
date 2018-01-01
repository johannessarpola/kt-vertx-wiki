package io.vertx.starter;

import com.github.rjeschke.txtmark.Processor
import io.vertx.core.Launcher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine
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

  private val LOGGER = LoggerFactory.getLogger("MainVerticle")
  private var dbClient: JDBCClient? = null // todo remove
  private val templateEngine = FreeMarkerTemplateEngine.create()

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

    val dbClient: JDBCClient = JDBCClient.createShared(vertx, JsonObject()
      .put("url", "jdbc:hsqldb:file:db/wiki")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30))

    dbClient.getConnection { ar ->
      when (ar.failed()) {
        true -> {
          LOGGER.error("Could not open a database connection", ar.cause())
          future.fail(ar.cause())
        }
        false -> {
          val connection = ar.result()
          connection.execute(SQL_CREATE_PAGES_TABLE, { create ->
            connection.close()
            if (create.failed()) {
              LOGGER.error("Database preparation error", create.cause())
              future.fail(create.cause())
            } else {
              future.complete()
            }
          })
        }
      }
    }

    this.dbClient = dbClient

    return future
  }

  private fun indexHandler(context: RoutingContext) {
    dbClient?.getConnection { car ->
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
                  context.response().putHeader("Content-Type", "text/html")
                  context.response().end(ar.result());
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

    dbClient?.getConnection{ car ->
      if (car.succeeded()) {
        val connection = car.result()
        connection.queryWithParams(SQL_GET_PAGE,  JsonArray().add(page), { fetch ->
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
              .put("newPage", if(fetch.result().getResults().size == 0) "yes" else "no")
              .put("rawContent", rawContent)
              .put("content", Processor.process(rawContent))
              .put("timestamp", Date().toString())

            templateEngine.render(context, "templates", "/page.ftl", { ar ->
              if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
              } else {
                context.fail(ar.cause());
              }
            })
          }
          else {
            context.fail(fetch.cause());
          }
        })
      }
      else {
        context.fail(car.cause());

      }
    }
  }

  private fun pageUpdateHandler(context: RoutingContext) {

  }

  private fun pageCreateHandler(context: RoutingContext) {

  }

  private fun pageDeletionHandler(context: RoutingContext) {

  }

  private fun startHttpServer(): Future<Void> {
    val future = Future.future<Void>()

    val router = Router.router(vertx);
    val server = vertx.createHttpServer();

    router.get("/").handler(this::indexHandler);
    router.get("/wiki/:page").handler(this::pageRenderingHandler);

    router.post().handler(BodyHandler.create());
    router.post("/save").handler(this::pageUpdateHandler);
    router.post("/create").handler(this::pageCreateHandler);
    router.post("/delete").handler(this::pageDeletionHandler);

    server
      .requestHandler(router::accept)
      .listen(8080, { ar ->
        if (ar.succeeded()) {
          LOGGER.info("HTTP server running on port 8080");
          future.complete();
        } else {
          LOGGER.error("Could not start a HTTP server", ar.cause());
          future.fail(ar.cause());
        }
      });
    return future
  }

}
