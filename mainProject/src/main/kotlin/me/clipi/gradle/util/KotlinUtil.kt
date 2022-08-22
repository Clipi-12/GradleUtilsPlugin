package me.clipi.gradle.util

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

// Cannot be reified as it would do nothing in an object cast to Any
public fun <T : Any> T.getPrivateField(property: String): Any? {
    val finder: (KProperty<*>) -> Boolean = finder@{ it.name == property }


    // Search the field in instance fields of this class and superclasses

    val tClass = this::class

    @Suppress("UNCHECKED_CAST")
    val fieldNotStatic = tClass.memberProperties.find(finder) as KProperty1<in T, *>?

    if (fieldNotStatic != null) {
        fieldNotStatic.isAccessible = true

        return fieldNotStatic.get(this)
    }


    // Search the field in static fields of this class and superclasses

    val fieldStatic = this::class.staticProperties.find(finder)

    if (fieldStatic != null) {
        fieldStatic.isAccessible = true
        return fieldStatic.get()
    }


    throw NoSuchFieldException()
}

@Suppress("unused")
public// Cannot be reified as it would do nothing in an object cast to Any
fun <T : Any, V : Any?> T.setPrivateField(property: String, value: V) {
    val finder: (KMutableProperty<*>) -> Boolean = finder@{ it.name == property }


    // Search the field in instance fields of this class and superclasses

    val fieldNotStatic = this::class.memberProperties.filterIsInstance<KMutableProperty1<in T, in V>>().find(finder)

    if (fieldNotStatic != null) {
        fieldNotStatic.isAccessible = true
        fieldNotStatic.set(this, value)
        return
    }


    // Search the field in static fields of this class and superclasses

    val fieldStatic = this::class.staticProperties.filterIsInstance<KMutableProperty0<in V>>().find(finder)

    if (fieldStatic != null) {
        fieldStatic.isAccessible = true
        fieldStatic.set(value)
        return
    }


    throw NoSuchFieldException()
}

// Cannot be reified as it would do nothing in an object cast to Any
public fun <T : Any> T.callPrivateMethod(method: String, vararg arguments: Any?): Any? {
    val func = this::class.functions.find { f ->
        if (f.name != method) return@find false
        val isValid: (Iterator<Any?>, KParameter) -> Boolean = valid@{ args: Iterator<Any?>, param ->
            if (!args.hasNext()) return@valid param.isOptional
            val arg = args.next()
            if (correctType(arg, param)) return@valid true
            if (param.isVararg) {
                @Suppress("UNCHECKED_CAST")
                val varargsType = (param.type.classifier as KClass<Array<*>>).java.componentType.kotlin

                return@valid correctType(arg, varargsType) && args.asSequence().all { correctType(it, varargsType) }
            }
            return@valid false
        }

        val iter = arguments.iterator()
        f.valueParameters.forEach {
            if (!isValid(iter, it)) return@find false
        }
        return@find true
    } ?: throw NoSuchMethodException("${this::class.java.name}.$method(${arguments.joinToString()})")

    func.returnType
    func.isAccessible = true
    if (func.findAnnotation<JvmStatic>() != null) {
        return func.call(null, *arguments)
    }
    if (func.instanceParameter == null) {
        return func.call(*arguments)
    }
    return func.call(this, *arguments)
}

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun correctType(arg: Any?, param: KParameter): Boolean {
    return correctType(arg, param.type.classifier as KClass<*>)
}

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun correctType(arg: Any?, type: KClass<*>): Boolean {
    return arg == null || type.isInstance(arg)
}
