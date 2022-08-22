package me.clipi.gradle.util

import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.util.function.Consumer

public class DelegatedProperty<T>(private val original: Property<T>, private val onSet: Consumer<T?>) :
    Property<T> by original {

    private var set = false

    override fun set(provider: Provider<out T>) {
        set = true
        original.set(provider)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun set(value: T?) {
        set = true
        original.set(value)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun get(): T {
        val result = original.get()
        if (!set) set(result)
        return result
    }

    override fun getOrElse(defaultValue: T): T {
        val result = original.getOrElse(defaultValue)
        if (!set) set(result)
        return result
    }

    override fun getOrNull(): T? {
        val result = original.orNull
        if (!set) set(result)
        return result
    }
}

public class DelegatedMultiProperty<T, DataStructure, InnerDataStructure>(
    private val original: DataStructure,
    private val onSet: Consumer<InnerDataStructure?>
) : HasMultipleValues<T> by original, Provider<InnerDataStructure> by original
        where DataStructure : HasMultipleValues<T>, DataStructure : Provider<InnerDataStructure>, InnerDataStructure : Collection<T> {

    private var set = false

    public fun set(vararg elements: T) {
        set(listOf(*elements))
    }

    override fun set(elements: Iterable<T>?) {
        set = true
        original.set(elements)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun set(provider: Provider<out Iterable<T>>) {
        set = true
        original.set(provider)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun add(element: T) {
        set = true
        original.add(element)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun add(provider: Provider<out T>) {
        set = true
        original.add(provider)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun addAll(provider: Provider<out Iterable<T>>) {
        set = true
        original.addAll(provider)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun addAll(elements: Iterable<T>) {
        set = true
        original.addAll(elements)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun addAll(vararg elements: T) {
        set = true
        original.addAll(*elements)
        original.finalizeValue()
        onSet.accept(orNull)
    }

    override fun empty(): DelegatedMultiProperty<T, DataStructure, InnerDataStructure> {
        set = true
        original.empty()
        original.finalizeValue()
        onSet.accept(orNull)
        return this
    }

    override fun get(): InnerDataStructure {
        val result = original.get()
        if (!set) set(result)
        return result
    }

    override fun getOrElse(defaultValue: InnerDataStructure): InnerDataStructure {
        val result = original.getOrElse(defaultValue)
        if (!set) set(result)
        return result
    }

    override fun getOrNull(): InnerDataStructure? {
        val result = original.orNull
        if (!set) set(result)
        return result
    }
}
