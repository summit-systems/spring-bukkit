package kr.summitsystems.springbukkit.view

import kr.summitsystems.springbukkit.core.scheduler.BukkitTaskScheduler
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
class NavigatorImpl(
    private val bukkitTaskScheduler: BukkitTaskScheduler,
    private val viewFactory: ViewFactory
) : Navigator {
    private val views: ConcurrentHashMap<UUID, Stack<NamedView>> = ConcurrentHashMap()

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val view = event.inventory.holder
        val player = event.player

        if (player !is Player || view !is View<*>) {
            return
        }

        if (!view.isStandby() && !view.isDisposed()) {
            popView(player)
            return
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        views.remove(event.player.uniqueId)
    }

    override fun <C : ViewInitializationContext> pushView(viewer: Player, view: Class<out View<C>>, context: C, name: String?) {
        val currentView = findPlayerCurrentView(viewer.uniqueId)?.original
        @Suppress("IfThenToSafeAccess")
        if (currentView != null) {
            currentView.standby()
        }
        val instantiatedView = viewFactory.create(viewer, view, context)
        val views = views.computeIfAbsent(viewer.uniqueId) {
            Stack()
        }
        views.push(NamedView(name, instantiatedView))
        openInventory(viewer, instantiatedView.inventory)
    }

    override fun popView(viewer: Player) {
        popView(viewer, true)
    }

    private fun popView(viewer: Player, sendInventory: Boolean) {
        val viewStack = views[viewer.uniqueId]
        if (viewStack.isNullOrEmpty()) {
            return
        }

        viewStack.pop().original.dispose()
        if (viewStack.isNotEmpty()) {
            val previousView = viewStack.peek()
            previousView.original.active()
            if (sendInventory) {
                closeInventory(viewer)
                openInventory(viewer, previousView.original.inventory, 1L)
            }
        }
    }

    override fun popViewAll(viewer: Player) {
        val viewStack = views[viewer.uniqueId]
        if (viewStack.isNullOrEmpty()) {
            return
        }

        while (viewStack.size >= 1) {
            popView(viewer, false)
        }
        closeInventory(viewer)
    }

    override fun popViewUntilFirst(viewer: Player) {
        val viewStack = views[viewer.uniqueId]
        if (viewStack.isNullOrEmpty()) {
            return
        }
        while (viewStack.size >= 3) {
            popView(viewer, false)
        }
        popView(viewer, true)
    }

    override fun popViewUntilNamed(viewer: Player, name: String) {
        val viewStack = views[viewer.uniqueId]
        if (viewStack.isNullOrEmpty()) {
            return
        }
        while (viewStack.size >= 1) {
            if (findPlayerPreviousView(viewer.uniqueId)?.name == name) {
                popView(viewer, true)
                return
            } else {
                popView(viewer, false)
            }
        }
        closeInventory(viewer)
    }

    private fun closeInventory(viewer: Player) {
        bukkitTaskScheduler.schedule {
            viewer.closeInventory()
        }
    }

    private fun openInventory(viewer: Player, inventory: Inventory, delay: Long? = null) {
        if (delay == null) {
            bukkitTaskScheduler.schedule {
                viewer.openInventory(inventory)
            }
        } else {
            bukkitTaskScheduler.scheduleWithFixedDelay(delay) {
                viewer.openInventory(inventory)
            }
        }
    }

    private fun findPlayerCurrentView(viewerId: UUID): NamedView? {
        val viewStack = views[viewerId]
        if (viewStack.isNullOrEmpty()) {
            return null
        }
        return viewStack.peek()
    }

    private fun findPlayerPreviousView(viewerId: UUID): NamedView? {
        val viewStack = views[viewerId]
        if (viewStack.isNullOrEmpty()) {
            return null
        }
        return viewStack.getOrNull(viewStack.size - 2)
    }

    private data class NamedView(val name: String?, val original: View<*>)
}