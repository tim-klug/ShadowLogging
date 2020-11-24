package com.example.demo.logging

import net.logstash.logback.argument.StructuredArguments.v
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import kotlin.reflect.full.memberProperties

@Aspect
@Component
class LogResultAspect {

    private val proceed = ThreadLocal.withInitial<Any> { Object() }

    @Around(value = "@within(logThis) || @annotation(logThis)")
    fun log(joinPoint: ProceedingJoinPoint, logResult: LogResult): Any {
        val logger = LoggerFactory.getLogger(joinPoint.signature.declaringType)

        proceed.set(joinPoint.proceed())


        // get the data from the method argument for failure logging

        when (val result = proceed.get()) {
            is Optional<*> -> result.ifPresentOrElse({ payload -> logger.debug(logResult.value, *logResult.parameter, *getStructuredArguments(logResult, payload)) }, Runnable { logger.warn(logResult.value) })
            is Collection<*> -> if (result.isNotEmpty()) logger.debug(logResult.value, result.size) else logger.warn(logResult.value)
            else -> logger.debug(logResult.value, result)
        }

        return proceed.get()
    }

    private fun getStructuredArguments(logResult: LogResult, payload: Any): Array<Any> {
        return arrayOf(
                logResult.structuredParameterFromPayload
                .asSequence()
                .map { p -> v(p.key, payload::class.memberProperties
                        .find { it.name == p.value }?.getter?.call(payload) ?: "" ) }
                .toList()
                .toTypedArray(),
                logResult.structuredParameter
                        .asSequence()
                        .map { p -> v(p.key, p.value) }
                        .toList()
                        .toTypedArray()
        )
    }
}