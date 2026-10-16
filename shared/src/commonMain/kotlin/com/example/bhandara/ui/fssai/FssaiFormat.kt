package com.example.bhandara.ui.fssai

import kotlinx.datetime.LocalDate

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "12 Mar 2028", as on the certificate */
fun fssaiDate(date: LocalDate): String = "${date.day} ${MONTHS[date.month.ordinal]} ${date.year}"

/** "Mar 2028" */
fun fssaiMonth(date: LocalDate): String = "${MONTHS[date.month.ordinal]} ${date.year}"

/** "12 Mar 2028" from the backend's "2028-03-12"; the text as it is if it isn't a date */
fun fssaiDate(isoDate: String): String = runCatching { fssaiDate(LocalDate.parse(isoDate)) }.getOrDefault(isoDate)

/** "11523026000123" → "1152 3026 0001 23" */
fun fssaiNumber(digits: String): String = digits.chunked(4).joinToString(" ")
