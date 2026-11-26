package com.example.todoapp.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object OverduePolicy {

    fun todayEnd(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
}
