package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.SQLConnection


class MainVerticle : AbstractVerticle() {
  private val SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity primary key, Name varchar(255) unique, Content clob)"
  private val SQL_GET_PAGE = "select Id, Content from Pages where Name = ?"
  private val SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)"
  private val SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?"
  private val SQL_ALL_PAGES = "select Name from Pages"
  private val SQL_DELETE_PAGE = "delete from Pages where Id = ?"

  private val LOGGER = LoggerFactory.getLogger("MainVerticle")
  private var dbClient: JDBCClient? = null // todo remove


  override fun start(startFuture: Future<Void>) {
    val steps = prepareDatabase().compose { v -> startHttpServer() }
    steps.setHandler{ ar ->
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
          SQLConnection connection = ar.result ()
          connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
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

    return future
  }

  private fun startHttpServer(): Future<Void> {
    val future = Future.future<Void>()

    vertx.createHttpServer()
      .requestHandler { req -> req.response().end("Hello Vert.x!") }
      .listen(8080)

    return future
  }

}
