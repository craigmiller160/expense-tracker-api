package db.dev.migration

import io.craigmiller160.expensetrackerapi.extension.getUUID
import java.util.UUID
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

class V1_20221111__Mass_Add_Categories : BaseJavaMigration() {
  companion object {
    private const val GET_CATEGORY_IDS =
      """
            SELECT id
            FROM categories
        """

    private const val GET_HALF_TRANSACTIONS =
      """
            SELECT id
            FROM transactions
            ORDER BY description ASC
            LIMIT (
                SELECT COUNT(*) / 2
                FROM transactions
            )
        """

    private const val SET_CATEGORY_ON_TRANSACTIONS =
      """
            UPDATE transactions
            SET category_id = :categoryId
            WHERE id = :transactionId
        """
  }

  override fun migrate(context: Context) {
    val jdbcTemplate =
      NamedParameterJdbcTemplate(SingleConnectionDataSource(context.connection, true))
    val categoryIds = getCategoryIds(jdbcTemplate)
    val transactionIds = getTransactionIds(jdbcTemplate)
    setCategoryOnTransaction(jdbcTemplate, transactionIds, categoryIds)
  }

  private fun getTransactionIds(jdbcTemplate: NamedParameterJdbcTemplate): List<UUID> =
    jdbcTemplate.query(GET_HALF_TRANSACTIONS, MapSqlParameterSource()) { rs, _ -> rs.getUUID("id") }

  private fun setCategoryOnTransaction(
    jdbcTemplate: NamedParameterJdbcTemplate,
    transactionIds: List<UUID>,
    categoryIds: List<UUID>
  ) {
    val batchParams =
      transactionIds
        .mapIndexed { index, transactionId ->
          val categoryIndex = if (index == 0) 0 else index % categoryIds.size
          MapSqlParameterSource()
            .addValue("transactionId", transactionId)
            .addValue("categoryId", categoryIds[categoryIndex])
        }
        .toTypedArray()
    jdbcTemplate.batchUpdate(SET_CATEGORY_ON_TRANSACTIONS, batchParams)
  }

  private fun getCategoryIds(jdbcTemplate: NamedParameterJdbcTemplate): List<UUID> =
    jdbcTemplate.query(GET_CATEGORY_IDS, MapSqlParameterSource()) { rs, _ ->
      UUID.fromString(rs.getString("id"))
    }
}
