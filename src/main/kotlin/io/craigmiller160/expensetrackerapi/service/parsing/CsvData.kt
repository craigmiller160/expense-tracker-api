package io.craigmiller160.expensetrackerapi.service.parsing

data class CsvData(val header: Array<String>, val records: List<Array<String>>)
