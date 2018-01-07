package fi.johannes.handlers.wiki.page

import com.github.rjeschke.txtmark.Processor
import fi.johannes.handlers.wiki.WikiComponents
import fi.johannes.utils.RequestUtils.getParam
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import java.util.*

/**
 * Johannes on 6.1.2018.
 */
class PageImpl(val components: WikiComponents):Page {

  val SQL_GET_PAGE = "select Id, Content from Pages where Name = ?"
  val SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)"
  val SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?"
  val SQL_DELETE_PAGE = "delete from Pages where Id = ?"

  val EMPTY_PAGE_MARKDOWN = "# A new page\n\n Feel-free to write in Markdown!\n";

  override fun get(context: RoutingContext) {
    val page = context.request().getParam("page");

    components.dbClient().getConnection { car ->
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

            components.templateEngine().render(context, "templates", "/page.ftl", { ar ->
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

  override fun save(context: RoutingContext) {
    val request = context.request()

    val id = getParam(name = "id", request = request)
    val title = getParam(name = "title", request = request)
    val markdown = getParam(name = "markdown", request = request)
    val newPage = "yes" == getParam(name = "newPage", request = request)

    components.dbClient().getConnection { car ->
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

  override fun create(context: RoutingContext) {
    val pageName = getParam(name = "page", request = context.request());

    val location = if (pageName.isEmpty()) "/" else "/wiki/" + pageName;

    context.response()
      .setStatusCode(303)
      .putHeader("Location", location)
      .end()
  }

  override fun delete(context: RoutingContext) {
    val request = context.request()

    val id = getParam(name = "id", request = request)

    components.dbClient().getConnection { car ->
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
