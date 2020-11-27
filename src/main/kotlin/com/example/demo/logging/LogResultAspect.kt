package com.example.demo.logging

import net.logstash.logback.argument.StructuredArguments.v
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.CodeSignature
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

        val arguments = (joinPoint.signature as CodeSignature).parameterNames
                .asSequence()
                .zip(joinPoint.args.asSequence())
                .filter {
                    logResult.structuredArgumentGeneral.asSequence().any { l -> l.field == it.first }
                }
                .toList()


        val loggingArguments = arrayOf(
                logResult.structuredArgumentGeneral
                        .asSequence()
                        .map { parameter ->
                            Pair(parameter.key,
                                    arguments.find { a -> a.first == parameter.field }
                                            ?.let {
                                                it.second::class.memberProperties
                                                        .find { it.name == parameter.field }?.getter?.call(it.second).toString()
                                            }
                            )
                        }
                        .map { v(it.first, it.second) }
                        .toList()
                        .toTypedArray(),
                logResult.structuredParameterGeneral
                        .asSequence()
                        .map { p -> v(p.key, p.value) }
                        .toList()
                        .toTypedArray()
        )

        logger.debug(logResult.value, *logResult.logMessageParameter, *loggingArguments)

        // get the data from the method argument for failure logging


        when (val result = proceed.get()) {
            is Optional<*> -> result.ifPresentOrElse({ payload -> logger.debug(logResult.value, *logResult.logMessageParameter, *getStructuredArguments(logResult, payload)) }, Runnable { logger.warn(logResult.value) })
            is Collection<*> -> if (result.isNotEmpty()) logger.debug(logResult.value, result.size) else logger.warn(logResult.value)
            else -> logger.debug(logResult.value, result)
        }

        return proceed.get()
    }

    private fun getStructuredArguments(logResult: LogResult, payload: Any): Array<Any> {
        return arrayOf(
                logResult.structuredParameterForResult
                        .asSequence()
                        .map { p ->
                            v(p.key, payload::class.memberProperties
                                    .find { it.name == p.value }?.getter?.call(payload).toString())
                        }
                        .toList()
                        .toTypedArray(),
                logResult.structuredParameterGeneral
                        .asSequence()
                        .map { p -> v(p.key, p.value) }
                        .toList()
                        .toTypedArray()
        )
    }
}