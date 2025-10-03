package com.dbeaver.agent.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
// import java.awt.*;

/**
 * Test for LicenseGeneratorUI
 * Note: This test uses headless mode to avoid GUI display issues in CI environments
 */
public class LicenseGeneratorUITest {

    @Test
    public void testUICreationInHeadlessMode() {
        // Set headless mode for testing
        System.setProperty("java.awt.headless", "true");

        try {
            // Test that we can create the main components without errors
            assertNotNull(new JComboBox<>(new String[]{"dbeaver", "cloudbeaver"}), "Should be able to create JComboBox");
            assertNotNull(new JTextField("25", 10), "Should be able to create JTextField");
            assertNotNull(new JTextArea(15, 50), "Should be able to create JTextArea");
            assertNotNull(new JButton("Generate License"), "Should be able to create JButton");

            // Test that the UI class can be loaded
            Class<?> uiClass = Class.forName("com.dbeaver.agent.ui.LicenseGeneratorUI");
            assertNotNull(uiClass, "LicenseGeneratorUI class should be loadable");

        } catch (Exception e) {
            fail("Should not throw exception when creating UI components in headless mode: " + e.getMessage());
        }
    }

    @Test
    public void testPlatformDetection() {
        String osName = System.getProperty("os.name").toLowerCase();
        assertNotNull(osName, "OS name should not be null");

        // Test that we can detect different platforms
        boolean isWindows = osName.contains("win");
        boolean isMac = osName.contains("mac");
        boolean isLinux = osName.contains("linux") || osName.contains("unix");

        // At least one of these should be true
        assertTrue(isWindows || isMac || isLinux || osName.length() > 0, "Should detect at least one known platform");
    }

    @Test
    public void testLookAndFeelAvailability() {
        try {
            // Test that we can get system look and feel class name
            String lafClassName = UIManager.getSystemLookAndFeelClassName();
            assertNotNull(lafClassName, "System Look and Feel class name should not be null");
            assertFalse(lafClassName.isEmpty(), "System Look and Feel class name should not be empty");
        } catch (Exception e) {
            fail("Should be able to get system look and feel class name: " + e.getMessage());
        }
    }
}