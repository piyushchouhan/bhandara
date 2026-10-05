package com.example.bhandara.resources

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The shared strings files are read by Compose Multiplatform, not Android: an Android-style \' isn't unescaped
 * there, so users would see the backslash ("isn\'t"). A plain ' is fine in these files.
 */
class StringResourcesTest {

    private val files = File("src/commonMain/composeResources").walkTopDown().filter { it.name == "strings.xml" }.toList()

    @Test
    fun theStringsFilesAreFound() {
        assertTrue(files.size >= 3, "found ${files.map { it.path }}")
    }

    @Test
    fun everyStringsFileIsValidXml() {
        // e.g. a bare "&" (write "&amp;") breaks the whole file
        val parser = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
        files.forEach { file ->
            runCatching { parser.parse(file) }.onFailure { throw AssertionError("${file.parentFile.name}/strings.xml: ${it.message}") }
        }
    }

    @Test
    fun noStringUsesAnAndroidOnlyEscapedApostrophe() {
        val offending = files.flatMap { file ->
            Regex("""<string name="([^"]+)"[^>]*>([^<]*)</string>""").findAll(file.readText())
                .filter { "\\'" in it.groupValues[2] }
                .map { "${file.parentFile.name}/${it.groupValues[1]}" }
                .toList()
        }
        assertTrue(offending.isEmpty(), "Use ' instead of \\' in: $offending")
    }
}
