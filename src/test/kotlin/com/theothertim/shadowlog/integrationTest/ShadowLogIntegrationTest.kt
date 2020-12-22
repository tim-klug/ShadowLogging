package com.theothertim.shadowlog.integrationTest

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.theothertim.shadowlog.ShadowLog
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [ShadowLogIntegrationTest::class, ShadowLogIntegrationTestConfig::class])
class ShadowLogIntegrationTest {

    @Autowired
    lateinit var sut: TestCases

    val logger = LoggerFactory.getLogger(TestCases::class.java) as Logger
    val listAppender = ListAppender<ILoggingEvent>()

    @BeforeEach
    fun init() {
        listAppender.start()
        logger.addAppender(listAppender)
        logger.level = Level.ALL
    }

    @AfterEach
    fun cleanup() {
        logger.detachAppender(listAppender)
    }

    @Test
    fun test1() {
        sut.testFunction1("demo_data")

        val logsList = listAppender.list

        Assertions.assertThat(logsList).hasSize(1)
    }

    @Test
    fun `log fields from argument payload`() {
        val payload = TestPayload()
        payload.name = "Test Demo"

        sut.logFieldsFromPayload(payload)

        val logsList = listAppender.list
        assertThat(logsList).isNotEmpty
    }

    @Test
    fun `log fields from argument with multiple payload`() {
        val payload = TestPayload()
        payload.name = "Test Demo"

        sut.logFieldsFromMultiplePayload(payload, "Something")

        val logsList = listAppender.list
        assertThat(logsList).isNotEmpty
    }
}