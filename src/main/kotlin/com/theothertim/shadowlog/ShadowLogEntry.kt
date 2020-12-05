package com.theothertim.shadowlog

import org.slf4j.event.Level


public annotation class ShadowLogEntry(
        val message: String = "",
        val arguments: Array<ShadowLogV> = [],
        val logLevel: Level = Level.DEBUG
)
