package com.theothertim.shadowlog

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.kv
import net.logstash.logback.argument.StructuredArguments.v
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
public class ShadowLogAspect {

    private val proceed = ThreadLocal.withInitial<Any> { Object() }
    private final val logItem = ShadowLogItem::class.java
    private final val logId = ShadowLogId::class.java

    @Around(value = "@within(shadowLog) || @annotation(shadowLog)")
    public fun log(joinPoint: ProceedingJoinPoint, shadowLog: ShadowLog): Any {
        val logger = LoggerFactory.getLogger(joinPoint.signature.declaringType)

        proceed.set(joinPoint.proceed())

        if (proceed.get() == null
                || proceed.get() is Optional<*>
                || proceed.get() is Collection<*>) {
            logger.error(
                    shadowLog.failure.message,
                    *shadowLog.failure.arguments
                            .map { v(it.key, it.value) }
                            .toList()
                            .toTypedArray(),
                    *getAllStructuredArguments(joinPoint),
                    kv("payload", proceed.get())
            )
        } else {
            logger.debug(
                    shadowLog.success.message,
                    *shadowLog.success.arguments
                            .map { v(it.key, it.value) }
                            .toList()
                            .toTypedArray(),
                    *getAllStructuredArguments(joinPoint),
                    *getAllStructuredArguments(proceed.get()),
                    kv("payload", proceed.get())
            )
        }

        return proceed.get()
    }

    private fun getAllStructuredArguments(joinPoint: ProceedingJoinPoint): Array<StructuredArgument> =
                joinPoint.args.asSequence()
                    .filter { it.javaClass.declaredFields.any { field ->  field.isAnnotationPresent(logItem) || field.isAnnotationPresent(logId) } }
                    .map {
                        it.javaClass.declaredFields
                                .filter { field ->  field.isAnnotationPresent(logItem) || field.isAnnotationPresent(logId) }
                                .onEach { field -> field.trySetAccessible()}
                                .map { field ->
                            when {
                                field.isAnnotationPresent(logId) -> v("reference", field.get(it).toString())
                                field.isAnnotationPresent(logItem) -> (field.annotations.find { annotation -> annotation is ShadowLogItem } as ShadowLogItem).value.let { value -> if (value.isNotEmpty()) v(value, field.get(it).toString()) else v(field.name, field.get(it).toString()) }
                                else -> v(field.name, field.get(field).toString())
                            }
                        }
                    }
                    .toList()
                    .flatten()
                    .toTypedArray()

    private fun getAllStructuredArguments(obj: Any): Array<StructuredArgument> =
            obj.javaClass
                    .declaredFields
                    .filter { it.isAnnotationPresent(logItem) || it.isAnnotationPresent(logId) }
                    .map {
                        if (it.annotations.contains(logId))
                            v("reference", it.toString())
                        else
                            v(it.name, it.toString())
                    }
                    .toList()
                    .toTypedArray()
}