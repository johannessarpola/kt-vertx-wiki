package fi.johannes.data.dao

import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet

/**
 * Johannes on 10.1.2018.
 */
interface PageDao {
  fun fetchAllPages(success: (ResultSet) -> Unit,
                    error: (Throwable) -> Unit)

  fun fetchPage(params: JsonArray,
                success: (ResultSet) -> Unit,
                error: (Throwable) -> Unit)

  fun insertPage(params: JsonArray,
                 success: () -> Unit,
                 error: (Throwable) -> Unit)

  fun updatePage(params: JsonArray,
                 success: () -> Unit,
                 error: (Throwable) -> Unit)

  fun deletePage(params: JsonArray,
                 success: () -> Unit,
                 error: (Throwable) -> Unit)

  fun createTable(success: () -> Unit,
                  connectionError: (Throwable) -> Unit,
                  createError: (Throwable) -> Unit)
}
