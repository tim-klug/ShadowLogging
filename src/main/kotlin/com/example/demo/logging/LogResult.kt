package com.example.demo.logging

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@kotlin.annotation.Target
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class LogResult(
        val value: String = "",
        val parameter: Array<String> = [],
        val structuredParameterFromPayload: Array<LogParameter> = [],
        val structuredParameter: Array<LogParameter> = []
        )
