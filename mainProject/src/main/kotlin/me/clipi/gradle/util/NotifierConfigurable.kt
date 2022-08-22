package me.clipi.gradle.util

import groovy.lang.Closure
import me.clipi.bcm.Import
import me.clipi.bcm.MethodInjector
import org.gradle.api.Action
import org.gradle.kotlin.dsl.closureOf
import org.gradle.util.Configurable
import java.io.Serializable

public abstract class NotifierConfigurable<T : NotifierConfigurable<T>>(
    private val name: String,
    private val notify: Action<T>
) : Configurable<T> {

    private var configureCalled = false
    private var notified = false
    override fun configure(cl: Closure<*>): T {
        if (configureCalled) StopExecution.cause("The '${name}' extension may only be called once")

        @Suppress("UNCHECKED_CAST")
        val self = this as T

        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call(self)

        /**
         * We set it here (instead that right after the check) to allow
         * multiple calls to this configuration inside the closure itself.
         * Coincidentally that is also a requirement for making the plugin
         * compatible with the Groovy-DSL, which calls configure(Closure)
         * inside the action that it passes to
         * [ExtensionHolder.configure][org.gradle.internal.extensibility.ExtensionsStorage.ExtensionHolder.configure]
         *
         * @see NotifierConfigurable.Internal
         */
        configureCalled = true

        if (!notified) {
            notify.execute(self)
            notified = true
        }

        return self
    }

    public companion object {
        init {
            MethodInjector.delegate(
                Class.forName("org.gradle.internal.extensibility.ExtensionsStorage\$ExtensionHolder"),
                Internal::class.java,
                "configure",
                Action::class.java
            )
        }
    }

    @Import(
        "me.clipi.gradle.util.KotlinUtilKt",
        "me.clipi.gradle.util.KotlinUtilKt\$callPrivateMethod\$func$1\$isValid$1",
        "me.clipi.gradle.util.NotifierConfigurable\$Internal\$Companion\$configure\$config$1"
    )
    public class Internal : Serializable {
        public companion object {
            @Suppress("UNUSED", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            @JvmStatic
            public fun configure(self: Object, action: Action<in Object?>): Object? {
                val extension = self.callPrivateMethod("get") as Object?
                val config = closureOf<Object?> { action.execute(this) }
                if (extension is Configurable<*>) {
                    extension.configure(config)
                } else {
                    config.call(extension)
                }
                return extension
            }
        }
    }
}
