package kr.summitsystems.springbukkit.coroutines.command.annotation

import kotlinx.coroutines.CoroutineScope
import kr.summitsystems.springbukkit.core.command.annotation.CommandConfigurer

interface CoroutinesCommandConfigurer : CommandConfigurer {
    fun getCoroutineScope(): CoroutineScope? { return null }
}