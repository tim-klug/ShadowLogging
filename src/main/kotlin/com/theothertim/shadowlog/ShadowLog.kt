package com.theothertim.shadowlog

@kotlin.annotation.Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
public annotation class ShadowLog(
        val success: ShadowLogEntry = ShadowLogEntry(),
        val failure: ShadowLogEntry = ShadowLogEntry()
)
