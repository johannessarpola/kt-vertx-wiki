package fi.johannes.data.dao

import fi.johannes.data.enums.SqlQuery
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.UpdateResult

/**
 * Johannes on 10.1.2018.
 */
class PageDaoImpl(val sqlClient: SQLClient,
                  val sqlQueries: Map<SqlQuery, String>) : PageDao {

  private val logger = LoggerFactory.getLogger(PageDaoImpl::class.java)


  override fun createTable(success: () -> Unit,
                           connectionError: (Throwable) -> Unit,
                           createError: (Throwable) -> Unit) {
    sqlClient.getConnection { ar ->
      if (ar.failed()) {
        connectionError(ar.cause())
      } else {
        logger.info("Established page database connection successfully")
        val connection = ar.result()
        connection.execute(sqlQueries[SqlQuery.CREATE_PAGES_TABLE]) { create ->
          connection.close()
          if (create.failed()) {
            createError(create.cause())
          } else {
            logger.info("Initialized page database successfully")
            success()
          }
        }
      }
    }
  }

  private fun fetchHandler(res: AsyncResult<ResultSet>,
                           success: (ResultSet) -> Unit,
                           error: (Throwable) -> Unit) {
    if (res.succeeded()) {
      success(res.result())
    } else {
      error(res.cause())
    }
  }

  private fun updateHandler(res: AsyncResult<UpdateResult>,
                            success: () -> Unit,
                            error: (Throwable) -> Unit) {
    if (res.succeeded()) {
      success()
    } else {
      error(res.cause())
    }
  }

  override fun fetchAllPages(success: (ResultSet) -> Unit,
                             error: (Throwable) -> Unit) {
    sqlClient.query(sqlQueries[SqlQuery.ALL_PAGES], { res ->
      fetchHandler(res, success, error)
    })
  }

  override fun fetchPageById(params: JsonArray, success: (ResultSet) -> Unit, error: (Throwable) -> Unit) {
    sqlClient.queryWithParams(sqlQueries[SqlQuery.GET_PAGE_BY_ID], params, { res ->
      fetchHandler(res, success, error)
    })
  }

  override fun fetchPage(params: JsonArray,
                         success: (ResultSet) -> Unit,
                         error: (Throwable) -> Unit) {
    sqlClient.queryWithParams(sqlQueries[SqlQuery.GET_PAGE], params, { res ->
      fetchHandler(res, success, error)
    })
  }

  override fun insertPage(params: JsonArray,
                          success: () -> Unit,
                          error: (Throwable) -> Unit) {
    sqlClient.updateWithParams(sqlQueries[SqlQuery.CREATE_PAGE], params, { res ->
      updateHandler(res, success, error)
    })
  }

  override fun updatePage(params: JsonArray,
                          success: () -> Unit,
                          error: (Throwable) -> Unit) {
    sqlClient.updateWithParams(sqlQueries[SqlQuery.SAVE_PAGE], params, { res ->
      updateHandler(res, success, error)
    })
  }

  override fun deletePage(params: JsonArray,
                          success: () -> Unit,
                          error: (Throwable) -> Unit) {
    sqlClient.updateWithParams(sqlQueries[SqlQuery.DELETE_PAGE], params, { res ->
      updateHandler(res, success, error)
    })
  }
}
