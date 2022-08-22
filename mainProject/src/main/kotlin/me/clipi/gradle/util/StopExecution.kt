package me.clipi.gradle.util

import org.gradle.api.GradleException

@Suppress("unused")
public class StopExecution : GradleException {
    public constructor() : super()
    public constructor(message: String) : super(message)
    public constructor(message: String, cause: Throwable?) : super(message, cause)

    public companion object {
        public fun cause(cause: Throwable?): Nothing {
            if (cause == null) throw StopExecution()
            throw StopExecution(cause.message ?: "", cause)
        }

        public fun cause(cause: String?): Nothing {
            if (cause == null) throw StopExecution()
            throw StopExecution(cause)
        }

        public fun cause(cause: Any?): Nothing {
            if (cause == null) throw StopExecution()
            cause(cause.toString())
        }
    }
}
