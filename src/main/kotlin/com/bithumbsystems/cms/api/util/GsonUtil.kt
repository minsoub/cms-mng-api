package com.bithumbsystems.cms.api.util

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

object GsonUtil {

    val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe()) // todo
        .create()
}

class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate) {
        out.value(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(value))
    }

    override fun read(input: JsonReader): LocalDate = LocalDate.parse(input.nextString())
}
