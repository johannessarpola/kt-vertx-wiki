package fi.johannes.handlers.wiki.Index

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine
import io.vertx.ext.web.templ.TemplateEngine
import java.util.stream.Collectors

/**
 * Johannes on 6.1.2018.
 */
class IndexImpl (val dbClient: SQLClient, val templateEngine: TemplateEngine): Index {

  val SQL_ALL_PAGES = "select Name from Pages" // todo move

  override fun handler(context: RoutingContext) {
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
}
