package com.theothertim.shadowlog.integrationTest

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan("com.theothertim.shadowlog", "com.theothertim.shadowlog.integrationTest")
class ShadowLogIntegrationTestConfig {}