package com.shykial.kScrapperCore.config.libraryBugFixes

import org.springframework.core.KotlinReflectionParameterNameDiscoverer
import org.springframework.core.ParameterNameDiscoverer
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.jvm.kotlinFunction

/**
 * By: pschichtel
 * Gist url: https://gist.github.com/pschichtel/830b7943ea43b7cb58cadd984b54b903
 *
 * This class is part of the workaround for a bug in hibernate-validation.
 *
 * It appends an additional (empty) parameter name in case of suspend functions
 *
 * See:
 *  * Spring issue: https://github.com/spring-projects/spring-framework/issues/23499
 *  * Hibernate issue: https://hibernate.atlassian.net/browse/HV-1638
 */
class SuspendAwareKotlinParameterNameDiscoverer : ParameterNameDiscoverer {

    private val defaultProvider = KotlinReflectionParameterNameDiscoverer()

    override fun getParameterNames(constructor: Constructor<*>): Array<String>? =
        defaultProvider.getParameterNames(constructor)

    override fun getParameterNames(method: Method): Array<String>? {
        val defaultNames = defaultProvider.getParameterNames(method) ?: return null
        val function = method.kotlinFunction
        return if (function != null && function.isSuspend) {
            defaultNames + ""
        } else defaultNames
    }
}
