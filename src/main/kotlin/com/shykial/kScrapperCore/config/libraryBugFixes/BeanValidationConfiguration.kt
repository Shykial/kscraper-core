package com.shykial.kScrapperCore.config.libraryBugFixes

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.validation.MessageInterpolatorFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Role
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Configuration(proxyBeanMethods = false)
class BeanValidationConfiguration {
    /**
     * By: pschichtel
     * Gist url: https://gist.github.com/pschichtel/830b7943ea43b7cb58cadd984b54b903
     *
     * This bean definition is part of the workaround for a bug in hibernate-validation.
     *
     * It replaces the default validator factory bean with ours that uses the customized parameter name discoverer.
     *
     * See:
     *  * Spring issue: https://github.com/spring-projects/spring-framework/issues/23499
     *  * Hibernate issue: https://hibernate.atlassian.net/browse/HV-1638
     */
    @Primary
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun defaultValidator(): LocalValidatorFactoryBean = CustomLocalValidatorFactoryBean().apply {
        messageInterpolator = MessageInterpolatorFactory().getObject()
    }
}
