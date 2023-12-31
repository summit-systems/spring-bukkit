package kr.summitsystems.springbukkit.command

import kr.summitsystems.springbukkit.core.SpringBukkitException

open class CommandException(message: String? = null, cause: Throwable? = null) : SpringBukkitException(message, cause) {
    class Unexpected : CommandException()
}