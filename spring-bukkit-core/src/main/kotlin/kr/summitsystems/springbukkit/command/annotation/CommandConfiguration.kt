package kr.summitsystems.springbukkit.command.annotation

import kotlinx.coroutines.CoroutineScope
import kr.summitsystems.springbukkit.command.*
import kr.summitsystems.springbukkit.command.convert.CommandArgumentConversionService
import kr.summitsystems.springbukkit.command.support.BukkitCommandConfiguration
import kr.summitsystems.springbukkit.command.support.DefaultCommandFeedbackSource
import kr.summitsystems.springbukkit.command.support.PaperTabCompleter
import org.bukkit.Server
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.context.annotation.*

@Import(BukkitCommandConfiguration::class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
class CommandConfiguration {
    @Autowired
    fun setConfigurers(
        commandConfigurers: ObjectProvider<CommandConfigurer>
    ) {
        val configurers = commandConfigurers.stream().toList()
        if (configurers.size > 1) {
            throw IllegalStateException("Only one CommandConfigurer may exist")
        }
    }

    @ConditionalOnClass(name = ["com.destroystokyo.paper.event.server.AsyncTabCompleteEvent"])
    @Scope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun paperTabCompleter(
        commandMappingRegistry: CommandMappingRegistry,
        commandArgumentConversionService: CommandArgumentConversionService
    ): PaperTabCompleter {
        return PaperTabCompleter(commandMappingRegistry, commandArgumentConversionService)
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandExecutor(
        commandCoroutineScope: CoroutineScope,
        commandMappingRegistry: CommandMappingRegistry,
        commandFeedbackSource: CommandFeedbackSource,
        commandContextHolder: CommandContextHolder,
        commandArgumentConversionService: CommandArgumentConversionService
    ): CommandExecutor {
        return CommandExecutorImpl(
            commandCoroutineScope,
            commandMappingRegistry,
            commandFeedbackSource,
            commandContextHolder,
            commandArgumentConversionService
        )
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandMappingAdvisor(
        commandExceptionHandlerRegistry: CommandExceptionHandlerRegistry,
        commandContextHolder: CommandContextHolder
    ): CommandMappingAdvisor {
        return CommandMappingAdvisor(commandExceptionHandlerRegistry, commandContextHolder)
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandExceptionHandlerRegistry(): CommandExceptionHandlerRegistry {
        return CommandExceptionHandlerRegistryImpl()
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandFeedbackSource(
        commandConfigurers: ObjectProvider<CommandConfigurer>,
        messageSource: MessageSource
    ): CommandFeedbackSource {
        return commandConfigurers.singleOrNull()?.getCommandFeedbackSource() ?: DefaultCommandFeedbackSource(messageSource)
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandCoroutineScope(
        pluginCoroutineScope: CoroutineScope,
        commandConfigurers: ObjectProvider<CommandConfigurer>
    ): CoroutineScope {
        return commandConfigurers.singleOrNull()?.getCoroutineScope() ?: pluginCoroutineScope
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandContextHolder(): CommandContextHolder {
        return ThreadCommandContextHolder()
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandMappingAnnotationBeanPostProcessor(
        applicationContext: ApplicationContext
    ): CommandMappingAnnotationBeanPostProcessor {
        return CommandMappingAnnotationBeanPostProcessor(applicationContext)
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandControllerAdviceAnnotationBeanPostProcessor(
        applicationContext: ApplicationContext
    ) : CommandControllerAdviceAnnotationBeanPostProcessor {
        return CommandControllerAdviceAnnotationBeanPostProcessor(applicationContext)
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun commandMappingRegistry(): CommandMappingRegistry {
        return CommandMappingRegistry()
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    fun commandArgumentConversionService(
        server: Server,
        commandConfigurers: ObjectProvider<CommandConfigurer>
    ): CommandArgumentConversionService {
        val service = CommandArgumentConversionService(server)
        commandConfigurers.forEach { configurer ->
            configurer.addCommandArgumentCompleterAdapter(service)
            configurer.addArgumentConverter(service)
        }
        return service
    }
}