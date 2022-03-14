import org.hibernate.validator.internal.engine.DefaultClockProvider
import org.springframework.core.LocalVariableTableParameterNameDiscoverer
import org.springframework.core.PrioritizedParameterNameDiscoverer
import org.springframework.core.StandardReflectionParameterNameDiscoverer
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import javax.validation.ClockProvider
import javax.validation.Configuration
import javax.validation.ParameterNameProvider

/**
 * By: pschichtel
 * Gist url: https://gist.github.com/pschichtel/830b7943ea43b7cb58cadd984b54b903
 *
 * This class is part of the workaround for a bug in hibernate-validation.
 *
 * It post-processes the Hibernate configuration to use our customized parameter name discoverer.
 *
 * See:
 *  * Spring issue: https://github.com/spring-projects/spring-framework/issues/23499
 *  * Hibernate issue: https://hibernate.atlassian.net/browse/HV-1638
 */
class CustomLocalValidatorFactoryBean : LocalValidatorFactoryBean() {
    override fun getClockProvider(): ClockProvider = DefaultClockProvider.INSTANCE

    override fun postProcessConfiguration(configuration: Configuration<*>) {
        super.postProcessConfiguration(configuration)

        val discoverer = PrioritizedParameterNameDiscoverer().apply {
            addDiscoverer(SuspendAwareKotlinParameterNameDiscoverer())
            addDiscoverer(StandardReflectionParameterNameDiscoverer())
            addDiscoverer(LocalVariableTableParameterNameDiscoverer())
        }

        val defaultProvider = configuration.defaultParameterNameProvider
        configuration.parameterNameProvider(object : ParameterNameProvider {
            override fun getParameterNames(constructor: Constructor<*>): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(constructor)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(constructor)
            }

            override fun getParameterNames(method: Method): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(method)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(method)
            }
        })
    }
}
