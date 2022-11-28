package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
import com.opencsv.CSVReader
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.io.InputStream
import java.io.InputStreamReader

object CsvParser {
  fun parse2(stream: InputStream): TryEither<Sequence<Array<String>>> =
    Either.catch { CSVReader(InputStreamReader(stream)).readAll().asSequence().drop(1) }

  fun parse(stream: InputStream): TryEither<CsvData> =
    Either.catch { CSVReader(InputStreamReader(stream)).readAll() }
      .map { allRows ->
        CsvData(header = allRows.first(), records = allRows.subList(0, allRows.size))
      }
}
