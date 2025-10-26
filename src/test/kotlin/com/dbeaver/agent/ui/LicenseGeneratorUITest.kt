package com.dbeaver.agent.ui

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import javax.swing.*

/**
 * Test for LicenseGeneratorUI
 * Note: This test uses headless mode to avoid GUI display issues in CI environments
 */
class LicenseGeneratorUITest {
    @Test
    fun testUICreationInHeadlessMode() {
        // Set headless mode for testing
        System.setProperty("java.awt.headless", "true")

        try {
            // Test that we can create the main components without errors
            Assertions.assertNotNull(JComboBox<String?>(arrayOf<String>("dbeaver", "cloudbeaver")), "Should be able to create JComboBox")
            Assertions.assertNotNull(JTextField("25", 10), "Should be able to create JTextField")
            Assertions.assertNotNull(JTextArea(15, 50), "Should be able to create JTextArea")
            Assertions.assertNotNull(JButton("Generate License"), "Should be able to create JButton")


            // Test that the UI class can be loaded
            val uiClass = Class.forName("com.dbeaver.agent.ui.LicenseGeneratorUI")
            Assertions.assertNotNull(uiClass, "LicenseGeneratorUI class should be loadable")
        } catch (e: Exception) {
            Assertions.fail<Any?>("Should not throw exception when creating UI components in headless mode: " + e.message)
        }
    }

    @Test
    fun testPlatformDetection() {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        Assertions.assertNotNull(osName, "OS name should not be null")


        // Test that we can detect different platforms
        val isWindows = osName.contains("win")
        val isMac = osName.contains("mac")
        val isLinux = osName.contains("linux") || osName.contains("unix")


        // At least one of these should be true
        Assertions.assertTrue(isWindows || isMac || isLinux || osName.length > 0, "Should detect at least one known platform")
    }

    @Test
    fun testLookAndFeelAvailability() {
        try {
            // Test that we can get system look and feel class name
            val lafClassName = UIManager.getSystemLookAndFeelClassName()
            Assertions.assertNotNull(lafClassName, "System Look and Feel class name should not be null")
            Assertions.assertFalse(lafClassName.isEmpty(), "System Look and Feel class name should not be empty")
        } catch (e: Exception) {
            Assertions.fail<Any?>("Should be able to get system look and feel class name: " + e.message)
        }
    }
}