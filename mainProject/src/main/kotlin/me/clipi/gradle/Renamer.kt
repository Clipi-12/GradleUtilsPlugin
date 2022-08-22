package me.clipi.gradle

@FunctionalInterface
public interface Renamer : (String, String) -> String? {
    /**
     * Returns a new file name (or null if the file name doesn't change)
     * according to the full path of the file and its name
     *
     * @param fullName the full path to the file
     * @param fileName the name of the file
     */
    override fun invoke(fullName: String, fileName: String): String?
}
