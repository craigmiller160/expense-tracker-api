package db.dev.migration

import java.util.UUID
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

class V1_20221110__Set_Category_Colors : BaseJavaMigration() {
  companion object {
    private const val UPDATE_COLORS =
        """
            UPDATE categories
            SET color = :color
            WHERE id = :id
        """

    private const val GET_CATEGORY_IDS =
        """
            SELECT *
            FROM categories
        """
  }

  override fun migrate(context: Context) {
    val jdbcTemplate =
        NamedParameterJdbcTemplate(SingleConnectionDataSource(context.connection, true))
    jdbcTemplate
        .query(GET_CATEGORY_IDS, MapSqlParameterSource()) { rs, _ ->
          CategoryRecord(id = UUID.fromString(rs.getString("id")), name = rs.getString("name"))
        }
        .forEach { category ->
          val params =
              MapSqlParameterSource()
                  .addValue("id", category.id)
                  .addValue("color", stringToColor(category.name))
          jdbcTemplate.update(UPDATE_COLORS, params)
        }
  }

  private data class CategoryRecord(val id: UUID, val name: String)

  private fun stringToColor(value: String): String =
      String.format("#%06x", 0xFFFFFF and value.hashCode())
}
