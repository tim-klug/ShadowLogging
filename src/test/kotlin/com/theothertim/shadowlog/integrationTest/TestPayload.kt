package com.theothertim.shadowlog.integrationTest

import com.theothertim.shadowlog.ShadowLogId
import com.theothertim.shadowlog.ShadowLogItem
import net.logstash.logback.encoder.org.apache.commons.lang3.StringEscapeUtils
import java.util.*

class TestPayload {

    @field:ShadowLogId
    var id: UUID = UUID.randomUUID()

    @field:ShadowLogItem
    var name: String = ""

    @field:ShadowLogItem("tenant")
    var tenantReference: UUID = UUID.randomUUID()

    var ignoredField: String = "ignore me"
}