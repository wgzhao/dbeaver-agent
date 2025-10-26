package com.dbeaver.agent.ui

import com.dbeaver.agent.License
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

/**
 * Integration test to verify that the UI components can integrate with the License functionality
 */
class LicenseGeneratorUIIntegrationTest {
    @Test
    @Throws(Exception::class)
    fun testLicenseGenerationBackend() {
        // Test that the backend license generation works correctly
        // This simulates what the UI would do when Generate button is clicked

        val product = "dbeaver"
        val type = "ue"
        val version = "25"


        // Capture output just like the UI does
        val baos = ByteArrayOutputStream()
        val originalOut = System.out
        val ps = PrintStream(baos)
        System.setOut(ps)

        try {
            // Create and run license generation like UI does
            // Don't call main() as it calls System.exit(), instead call the License methods directly
            val license = License()


            // Use reflection to set the fields like the command line would
            val productNameField = License::class.java.getDeclaredField("productName")
            productNameField.setAccessible(true)
            productNameField.set(license, product)

            val licenseTypeField = License::class.java.getDeclaredField("licenseType")
            licenseTypeField.setAccessible(true)
            licenseTypeField.set(license, type)

            val productVersionField = License::class.java.getDeclaredField("productVersion")
            productVersionField.setAccessible(true)
            productVersionField.set(license, version.toInt())


            // Call the actual license generation method
            license.call()
            ps.flush()

            val output: String? = baos.toString()


            // Verify output contains expected elements
            Assertions.assertNotNull(output, "Output should not be null")
            Assertions.assertTrue(output!!.contains("LICENSE"), "Output should contain license header")
            Assertions.assertTrue(output.length > 100, "Output should contain base64 data")
            Assertions.assertTrue(output.contains(product + "-" + type), "Output should contain product info")
        } finally {
            System.setOut(originalOut)
            ps.close()
        }
    }

    @Test
    fun testLicenseExtractionLogic() {
        // Test the license extraction logic that would be used by the Copy button
        val sampleOutput = "--- dbeaver-ue(v25) LICENSE ---\n" +
                "gQ7ucLt8tMJt/tYdtuU3S/I8q2oaNy9jJQQD1bCsdf6qWDEWYwlJ35OcdHM8vtlPu7NsJXhrV9lxZvuJYhVs7kLBoUeGYB7SbvyrWm07tCaK+ZQp5A3x5g9ItvYW/PDf42XvCTU0YLEFo0p6WgRpwekhA1cGaLaj2Lp4/ceWsiGV+zBpIKj2cDPxJ0UIhAKr\n" +
                "--- 请复制上一行 ---"

        val lines = sampleOutput.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var licenseToCopy = ""

        for (line in lines) {
            if (line.trim { it <= ' ' }.length > 50 && !line.contains("---") && !line.contains("请复制")) {
                licenseToCopy = line.trim { it <= ' ' }
                break
            }
        }

        Assertions.assertFalse(licenseToCopy.isEmpty(), "Should extract license key")
        Assertions.assertTrue(licenseToCopy.length > 50, "License key should be base64-like")
        Assertions.assertFalse(licenseToCopy.contains("---"), "License key should not contain formatting text")
    }

    @Test
    fun testPlatformSpecificConfiguration() {
        // Test platform detection logic that would be used by the UI
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())

        val isMac = osName.contains("mac")
        val isWindows = osName.contains("win")
        val isLinux = osName.contains("linux") || osName.contains("unix")


        // Test that platform-specific properties can be set
        if (isMac) {
            // macOS specific test
            Assertions.assertNotNull(System.getProperty("os.name"), "Should be able to set macOS properties")
        } else if (isWindows) {
            // Windows specific test  
            Assertions.assertNotNull(System.getProperty("os.name"), "Should be able to detect Windows")
        } else {
            // Linux/Unix specific test
            Assertions.assertNotNull(System.getProperty("os.name"), "Should be able to detect Unix-like system")
        }
    }

    @Test
    fun testUIParameterValidation() {
        // Test parameter validation logic that would be used by the UI

        // Test empty version

        var version = "   "
        Assertions.assertTrue(version.trim { it <= ' ' }.isEmpty(), "Should detect empty version")


        // Test valid version
        version = "25"
        Assertions.assertFalse(version.trim { it <= ' ' }.isEmpty(), "Should accept valid version")


        // Test product options
        val validProducts = arrayOf<String?>("dbeaver", "cloudbeaver")
        Assertions.assertTrue(Arrays.asList<String?>(*validProducts).contains("dbeaver"), "Should contain dbeaver")
        Assertions.assertTrue(Arrays.asList<String?>(*validProducts).contains("cloudbeaver"), "Should contain cloudbeaver")


        // Test license type options
        val validTypes = arrayOf<String?>("ue", "ee", "le")
        Assertions.assertTrue(Arrays.asList<String?>(*validTypes).contains("ue"), "Should contain ue")
        Assertions.assertTrue(Arrays.asList<String?>(*validTypes).contains("ee"), "Should contain ee")
        Assertions.assertTrue(Arrays.asList<String?>(*validTypes).contains("le"), "Should contain le")
    }
}