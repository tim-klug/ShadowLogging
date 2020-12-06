package com.theothertim.shadowlog.integrationTest

import com.theothertim.shadowlog.ShadowLogId
import com.theothertim.shadowlog.ShadowLogItem
import net.logstash.logback.encoder.org.apache.commons.lang3.StringEscapeUtils
import java.util.*

class TestPayload {

    @ShadowLogId
    val id: UUID = UUID.randomUUID()

    @ShadowLogItem
    val name: String = ""

    @ShadowLogItem("tenant")
    val tenantReference: UUID = UUID.randomUUID()
}