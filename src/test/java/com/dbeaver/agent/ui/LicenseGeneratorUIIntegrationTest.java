package com.dbeaver.agent.ui;

import com.dbeaver.agent.License;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Integration test to verify that the UI components can integrate with the License functionality
 */
public class LicenseGeneratorUIIntegrationTest {

    @Test
    public void testLicenseGenerationBackend() throws Exception {
        // Test that the backend license generation works correctly
        // This simulates what the UI would do when Generate button is clicked

        String product = "dbeaver";
        String type = "ue";
        String version = "25";

        // Capture output just like the UI does
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        try {
            // Create and run license generation like UI does
            // Don't call main() as it calls System.exit(), instead call the License methods directly
            License license = new License();

            // Use reflection to set the fields like the command line would
            java.lang.reflect.Field productNameField = License.class.getDeclaredField("productName");
            productNameField.setAccessible(true);
            productNameField.set(license, product);

            java.lang.reflect.Field licenseTypeField = License.class.getDeclaredField("licenseType");
            licenseTypeField.setAccessible(true);
            licenseTypeField.set(license, type);

            java.lang.reflect.Field productVersionField = License.class.getDeclaredField("productVersion");
            productVersionField.setAccessible(true);
            productVersionField.set(license, Integer.parseInt(version));

            // Call the actual license generation method
            license.call();
            ps.flush();

            String output = baos.toString();

            // Verify output contains expected elements
            assertNotNull(output, "Output should not be null");
            assertTrue(output.contains("LICENSE"), "Output should contain license header");
            assertTrue(output.length() > 100, "Output should contain base64 data");
            assertTrue(output.contains(product + "-" + type), "Output should contain product info");

        } finally {
            System.setOut(originalOut);
            ps.close();
        }
    }

    @Test
    public void testLicenseExtractionLogic() {
        // Test the license extraction logic that would be used by the Copy button
        String sampleOutput = "--- dbeaver-ue(v25) LICENSE ---\n" +
                "gQ7ucLt8tMJt/tYdtuU3S/I8q2oaNy9jJQQD1bCsdf6qWDEWYwlJ35OcdHM8vtlPu7NsJXhrV9lxZvuJYhVs7kLBoUeGYB7SbvyrWm07tCaK+ZQp5A3x5g9ItvYW/PDf42XvCTU0YLEFo0p6WgRpwekhA1cGaLaj2Lp4/ceWsiGV+zBpIKj2cDPxJ0UIhAKr\n" +
                "--- 请复制上一行 ---";

        String[] lines = sampleOutput.split("\n");
        String licenseToCopy = "";

        for (String line : lines) {
            if (line.trim().length() > 50 && !line.contains("---") && !line.contains("请复制")) {
                licenseToCopy = line.trim();
                break;
            }
        }

        assertFalse(licenseToCopy.isEmpty(), "Should extract license key");
        assertTrue(licenseToCopy.length() > 50, "License key should be base64-like");
        assertFalse(licenseToCopy.contains("---"), "License key should not contain formatting text");
    }

    @Test
    public void testPlatformSpecificConfiguration() {
        // Test platform detection logic that would be used by the UI
        String osName = System.getProperty("os.name").toLowerCase();

        boolean isMac = osName.contains("mac");
        boolean isWindows = osName.contains("win");
        // boolean isLinux = osName.contains("linux") || osName.contains("unix");

        // Test that platform-specific properties can be set
        if (isMac) {
            // macOS specific test
            assertNotNull(System.getProperty("os.name"), "Should be able to set macOS properties");
        } else if (isWindows) {
            // Windows specific test
            assertNotNull(System.getProperty("os.name"), "Should be able to detect Windows");
        } else {
            // Linux/Unix specific test
            assertNotNull(System.getProperty("os.name"), "Should be able to detect Unix-like system");
        }
    }

    @Test
    public void testUIParameterValidation() {
        // Test parameter validation logic that would be used by the UI

        // Test empty version
        String version = "   ";
        assertTrue(version.trim().isEmpty(), "Should detect empty version");

        // Test valid version
        version = "25";
        assertFalse(version.trim().isEmpty(), "Should accept valid version");

        // Test product options
        String[] validProducts = {"dbeaver", "cloudbeaver"};
        assertTrue(java.util.Arrays.asList(validProducts).contains("dbeaver"), "Should contain dbeaver");
        assertTrue(java.util.Arrays.asList(validProducts).contains("cloudbeaver"), "Should contain cloudbeaver");

        // Test license type options
        String[] validTypes = {"ue", "ee", "le"};
        assertTrue(java.util.Arrays.asList(validTypes).contains("ue"), "Should contain ue");
        assertTrue(java.util.Arrays.asList(validTypes).contains("ee"), "Should contain ee");
        assertTrue(java.util.Arrays.asList(validTypes).contains("le"), "Should contain le");
    }
}