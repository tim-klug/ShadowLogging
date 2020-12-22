package com.theothertim.shadowlog.integrationTest

import com.theothertim.shadowlog.ShadowLog
import com.theothertim.shadowlog.ShadowLogEntry
import com.theothertim.shadowlog.ShadowLogV
import org.slf4j.event.Level
import org.springframework.stereotype.Component

@Component
class TestCases {

    @ShadowLog(
            success = ShadowLogEntry(
                    "This is a test", [ShadowLogV("--key--", "--value--")], Level.DEBUG),
            failure = ShadowLogEntry("There was an error", [ShadowLogV("--key--", "--value--")])
    )
    fun testFunction1(item: String): String {
        return "test"
    }

    @ShadowLog(success = ShadowLogEntry("Success"), failure = ShadowLogEntry("Failure"))
    fun logFieldsFromPayload(payload: TestPayload): String {
        return payload.name
    }
}