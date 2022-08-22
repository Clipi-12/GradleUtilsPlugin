package me.clipi.gradle

@FunctionalInterface
public interface Relocator : (Array<String>) -> Array<String>? {
    /**
     * Returns a new file path (or null if the path doesn't change)
     * according to the full path of the file
     *
     * @param path the full path to the file
     */
    override fun invoke(path: Array<String>): Array<String>?
}