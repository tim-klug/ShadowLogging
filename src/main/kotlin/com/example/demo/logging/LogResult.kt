package com.example.demo.logging

@kotlin.annotation.Target
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class LogResult(
        val value: String = "",
        val logMessageParameter: Array<String> = [],
        val structuredParameterForResult: Array<LogParameter> = [],
        val structuredParameterGeneral: Array<LogParameter> = [],
        val structuredArgumentGeneral: Array<LogArgumentParameter> = []
)
