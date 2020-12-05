package com.theothertim.shadowlog

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.kv
import net.logstash.logback.argument.StructuredArguments.v
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.CodeSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Aspect
@Component
public class ShadowLogAspect {

    private val proceed = ThreadLocal.withInitial<Any> { Object() }

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
            (joinPoint.signature as CodeSignature).parameterTypes
                .asSequence()
                .zip(joinPoint.args.asSequence())
                .filter { it.first.isAnnotationPresent(ShadowLogItem::class.java) || it.first.isAnnotationPresent(ShadowLogId::class.java) }
                .map { if (it.first.annotations.contains(ShadowLogId::class.java))
                    v("reference", it.second.toString())
                else
                    v(((it.first as Class).getAnnotation(ShadowLogItem::class.java) as ShadowLogItem).value, it.second.toString())
                }
                .toList()
                .toTypedArray()

    private fun getAllStructuredArguments(obj: Any): Array<StructuredArgument> =
        obj.javaClass
                .declaredFields
                .filter { it.isAnnotationPresent(ShadowLogItem::class.java) || it.isAnnotationPresent(ShadowLogId::class.java) }
                .map { if (it.annotations.contains(ShadowLogId::class.java))
                    v("reference", it.toString())
                else
                    v(it.name, it.toString())
                }
                .toList()
                .toTypedArray()
}