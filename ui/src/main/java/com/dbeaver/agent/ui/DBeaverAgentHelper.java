package com.dbeaver.agent.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

public class DBeaverAgentHelper
        extends Application
{

    private TextField dbeaverPathField;
    private ComboBox<String> productComboBox;
    private ComboBox<String> licenseTypeComboBox;
    private TextField versionField;
    private TextArea logArea;
    private CheckBox modifyHostsCheckBox;

    @Override
    public void start(Stage primaryStage)
    {
        // macOS workaround to ensure window appears
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            primaryStage.setOpacity(0.0);  // Start invisible
            primaryStage.show();
            primaryStage.hide();
            primaryStage.setOpacity(1.0);  // Make visible again
        }
        primaryStage.setTitle("DBeaver Agent Helper");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // DBeaver installation path selector
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label dbeaverPathLabel = new Label("DBeaver Installation Path:");
        grid.add(dbeaverPathLabel, 0, 0);

        dbeaverPathField = new TextField();
        dbeaverPathField.setPrefWidth(300);
        grid.add(dbeaverPathField, 1, 0);

        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // On macOS, use a FileChooser instead to allow selecting .app bundles
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select DBeaver Application");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Applications", "*.app")
                );
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    // For macOS .app bundles, navigate to the Contents/MacOS directory
                    File macOSDir = new File(selectedFile, "Contents/MacOS");
                    if (macOSDir.exists() && macOSDir.isDirectory()) {
                        dbeaverPathField.setText(macOSDir.getAbsolutePath());
                    } else {
                        dbeaverPathField.setText(selectedFile.getAbsolutePath());
                    }
                }
            } else {
                // For other platforms, use DirectoryChooser as before
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select DBeaver Installation Directory");
                File selectedDirectory = directoryChooser.showDialog(primaryStage);
                if (selectedDirectory != null) {
                    dbeaverPathField.setText(selectedDirectory.getAbsolutePath());
                }
            }
        });
        grid.add(browseButton, 2, 0);

        Button detectButton = new Button("Auto Detect");
        detectButton.setOnAction(e -> detectDBeaverInstallation());
        grid.add(detectButton, 3, 0);

        // License generation options
        Label productLabel = new Label("Product:");
        grid.add(productLabel, 0, 1);

        productComboBox = new ComboBox<>();
        productComboBox.getItems().addAll("dbeaver", "cloudbeaver");
        productComboBox.setValue("dbeaver");
        grid.add(productComboBox, 1, 1);

        Label licenseTypeLabel = new Label("License Type:");
        grid.add(licenseTypeLabel, 0, 2);

        licenseTypeComboBox = new ComboBox<>();
        licenseTypeComboBox.getItems().addAll("le (Lite)", "ee (Enterprise)", "ue (Ultimate)");
        licenseTypeComboBox.setValue("ue (Ultimate)");
        grid.add(licenseTypeComboBox, 1, 2);

        Label versionLabel = new Label("Version:");
        grid.add(versionLabel, 0, 3);

        versionField = new TextField("25.0");
        grid.add(versionField, 1, 3);

        // Hosts file modification option
        modifyHostsCheckBox = new CheckBox("Modify hosts file to block stats.dbeaver.com");
        modifyHostsCheckBox.setSelected(true);

        // Action buttons
        Button installButton = new Button("Install Agent");
        installButton.setOnAction(e -> installAgent());

        Button generateLicenseButton = new Button("Generate License Key");
        generateLicenseButton.setOnAction(e -> generateLicense());

        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setWrapText(true);

        root.getChildren().addAll(grid, modifyHostsCheckBox, installButton,
                generateLicenseButton, new Label("Log:"), logArea);

        Scene scene = new Scene(root, 600, 450);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Try to detect DBeaver installation on startup
        detectDBeaverInstallation();
    }

    private void detectDBeaverInstallation()
    {
        log("Attempting to detect DBeaver installation...");

        // Common installation paths to check
        String[] possiblePaths = {
                // Windows paths
                "C:\\Program Files\\DBeaver",
                "C:\\Program Files (x86)\\DBeaver",
                // macOS paths
                "/Applications/DBeaver.app/Contents/MacOS",
                // Linux paths
                "/usr/share/dbeaver",
                "/opt/dbeaver",
                System.getProperty("user.home") + "/.dbeaver"
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && isValidDBeaverInstallation(dir)) {
                dbeaverPathField.setText(dir.getAbsolutePath());
                log("Found DBeaver installation at: " + dir.getAbsolutePath());
                return;
            }
        }

        log("No DBeaver installation detected automatically. Please select manually.");
    }

    private boolean isValidDBeaverInstallation(File dir)
    {
        // Check for key files that indicate a DBeaver installation
        File iniFile = new File(dir, "dbeaver.ini");
        File exeFile = new File(dir, "dbeaver.exe"); // Windows
        File appBin = new File(dir, "dbeaver"); // Linux/macOS executable

        return iniFile.exists() || exeFile.exists() || appBin.exists();
    }

    private void installAgent()
    {
        String dbeaverPath = dbeaverPathField.getText();
        if (dbeaverPath.isEmpty()) {
            showError("Error", "Please select DBeaver installation path");
            return;
        }

        File dbeaverDir = new File(dbeaverPath);
        if (!dbeaverDir.exists() || !dbeaverDir.isDirectory()) {
            showError("Error", "Invalid DBeaver installation path");
            return;
        }

        try {
            // Step 1: Build the agent JAR (assume it's already built or download it)
            log("Checking for DBeaver agent JAR...");
            File agentJar = new File("target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar");

            if (!agentJar.exists()) {
                log("Agent JAR not found. Please build it first using 'mvn package'");
                showError("Error", "Agent JAR not found. Please build it first using 'mvn package'");
                return;
            }

            // Step 2: Copy agent JAR to DBeaver installation
            File targetJar = new File(dbeaverDir, "dbeaver-agent.jar");
            log("Copying agent JAR to " + targetJar.getAbsolutePath());
            Files.copy(agentJar.toPath(), targetJar.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // Step 3: Modify dbeaver.ini
            File iniFile = findIniFile(dbeaverDir);
            if (iniFile != null && iniFile.exists()) {
                modifyIniFile(iniFile, targetJar.getAbsolutePath());
            }
            else {
                log("Warning: dbeaver.ini not found. Manual configuration required.");
            }

            // Step 4: Modify hosts file if requested
            if (modifyHostsCheckBox.isSelected()) {
                modifyHostsFile();
            }

            log("Installation completed successfully!");
            showInfo("Success", "DBeaver Agent installed successfully!");
        }
        catch (Exception e) {
            log("Error during installation: " + e.getMessage());
            showError("Installation Error", e.getMessage());
        }
    }

    private File findIniFile(File dbeaverDir)
    {
        // Check for dbeaver.ini in common locations
        File directIni = new File(dbeaverDir, "dbeaver.ini");
        if (directIni.exists()) {
            return directIni;
        }

        // For macOS
        File macOsIni = new File(dbeaverDir, "Contents/Eclipse/dbeaver.ini");
        if (macOsIni.exists()) {
            return macOsIni;
        }

        // For other locations
        File[] files = dbeaverDir.listFiles((dir, name) -> name.endsWith(".ini"));
        if (files != null && files.length > 0) {
            return files[0];
        }

        return null;
    }

    private void modifyIniFile(File iniFile, String agentPath)
            throws IOException
    {
        log("Modifying " + iniFile.getName() + "...");

        List<String> lines = Files.readAllLines(iniFile.toPath());
        boolean vmArgsFound = false;
        boolean agentAlreadyAdded = false;

        // Check if agent is already configured
        for (String line : lines) {
            if (line.contains("-javaagent:") && line.contains("dbeaver-agent.jar")) {
                agentAlreadyAdded = true;
                log("Agent already configured in " + iniFile.getName());
                break;
            }
        }

        if (!agentAlreadyAdded) {
            // Create a new list with modified content
            StringBuilder newContent = new StringBuilder();
            for (String line : lines) {
                newContent.append(line).append(System.lineSeparator());
                if (line.trim().equals("-vmargs")) {
                    vmArgsFound = true;
                    // Add our parameters right after -vmargs
                    newContent.append("-javaagent:").append(agentPath).append(System.lineSeparator());
                    newContent.append("-Xbootclasspath/a: ").append(agentPath).append(System.lineSeparator());
                }
            }

            // If -vmargs wasn't found, add it with our parameters
            if (!vmArgsFound) {
                newContent.append("-vmargs").append(System.lineSeparator());
                newContent.append("-javaagent:").append(agentPath).append(System.lineSeparator());
                newContent.append("-Xbootclasspath/a: ").append(agentPath).append(System.lineSeparator());
            }

            // Backup the original file
            Files.copy(iniFile.toPath(),
                    Paths.get(iniFile.getParent(), iniFile.getName() + ".backup"),
                    StandardCopyOption.REPLACE_EXISTING);

            // Write the modified content
            Files.write(iniFile.toPath(), newContent.toString().getBytes());
            log("Successfully updated " + iniFile.getName());
        }
    }

    private void modifyHostsFile()
    {
        log("Attempting to modify hosts file...");

        String hostsPath;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            hostsPath = "C:\\Windows\\System32\\drivers\\etc\\hosts";
        }
        else {
            hostsPath = "/etc/hosts";
        }

        File hostsFile = new File(hostsPath);
        if (!hostsFile.exists()) {
            log("Hosts file not found at " + hostsPath);
            return;
        }

        try {
            boolean needsAdmin = !hostsFile.canWrite();

            if (needsAdmin) {
                log("Hosts file requires administrator/sudo privileges to modify.");
                log("Please add the following line to your hosts file manually:");
                log("127.0.0.1 stats.dbeaver.com");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Administrator Privileges Required");
                alert.setHeaderText("Hosts File Modification");
                alert.setContentText("The hosts file requires admin privileges to modify.\n\n" +
                        "Please add the following line to your hosts file manually:\n" +
                        "127.0.0.1 stats.dbeaver.com\n\n" +
                        "File location: " + hostsPath);
                alert.showAndWait();
                return;
            }

            // Check if entry already exists
            List<String> lines = Files.readAllLines(hostsFile.toPath());
            boolean entryExists = lines.stream()
                    .anyMatch(line -> line.contains("stats.dbeaver.com"));

            if (entryExists) {
                log("Entry for stats.dbeaver.com already exists in hosts file.");
                return;
            }

            // Backup hosts file
            Files.copy(hostsFile.toPath(),
                    Paths.get(hostsFile.getParent(), "hosts.backup"),
                    StandardCopyOption.REPLACE_EXISTING);

            // Add the entry
            Files.write(hostsFile.toPath(),
                    ("\n127.0.0.1 stats.dbeaver.com # Added by DBeaver Agent Helper\n")
                            .getBytes(),
                    StandardOpenOption.APPEND);

            log("Successfully added stats.dbeaver.com entry to hosts file.");
        }
        catch (Exception e) {
            log("Error modifying hosts file: " + e.getMessage());
        }
    }

    private void generateLicense()
    {
        log("Generating license key...");

        try {
            // Get selected options
            String product = productComboBox.getValue();
            String licenseType = licenseTypeComboBox.getValue().split(" ")[0]; // Extract just the code
            String version = versionField.getText();

            // Build the command
            ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    "-cp", "libs/*:./target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar",
                    "com.dbeaver.agent.License",
                    "-p=" + product,
                    "-t=" + licenseType,
                    "-v=" + version
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the output
            StringBuilder output = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Show the license key in a dialog for easy copying
                TextArea textArea = new TextArea(output.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("License Key Generated");
                alert.setHeaderText("Your DBeaver License Key");
                alert.getDialogPane().setContent(textArea);
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(550, 300);
                alert.showAndWait();
            }
            else {
                log("License generation failed with exit code: " + exitCode);
            }
        }
        catch (Exception e) {
            log("Error generating license: " + e.getMessage());
            showError("License Generation Error", e.getMessage());
        }
    }

    private void log(String message)
    {
        logArea.appendText(message + "\n");
    }

    private void showError(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}