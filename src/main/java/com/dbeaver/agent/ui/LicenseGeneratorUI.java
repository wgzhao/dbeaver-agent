package com.dbeaver.agent.ui;

import com.dbeaver.agent.License;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Platform-dependent UI for DBeaver license generation
 */
public class LicenseGeneratorUI extends JFrame
{
    private JComboBox<String> productComboBox;
    private JComboBox<String> licenseTypeComboBox;
    private JTextField versionField;
    private JTextArea licenseOutputArea;
    private JButton generateButton;
    private JButton copyButton;
    private JLabel statusLabel;

    public LicenseGeneratorUI() {
        initializeUI();
        setupPlatformSpecificBehavior();
    }

    private void initializeUI() {
        setTitle("DBeaver License Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Product selection
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Product:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        productComboBox = new JComboBox<>(new String[]{"dbeaver", "cloudbeaver"});
        mainPanel.add(productComboBox, gbc);

        // License type selection
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("License Type:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        licenseTypeComboBox = new JComboBox<>(new String[]{"ue", "ee", "le"});
        licenseTypeComboBox.setSelectedItem("ue"); // Default to Ultimate Edition
        mainPanel.add(licenseTypeComboBox, gbc);

        // Version field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Version:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        versionField = new JTextField("25", 10);
        mainPanel.add(versionField, gbc);

        // Generate button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        generateButton = new JButton("Generate License");
        generateButton.addActionListener(new GenerateLicenseAction());
        mainPanel.add(generateButton, gbc);

        // License output area
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        licenseOutputArea = new JTextArea(15, 50);
        licenseOutputArea.setEditable(false);
        licenseOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        licenseOutputArea.setBackground(getBackground());
        licenseOutputArea.setBorder(BorderFactory.createTitledBorder("Generated License"));
        JScrollPane scrollPane = new JScrollPane(licenseOutputArea);
        mainPanel.add(scrollPane, gbc);

        // Copy button
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        copyButton = new JButton("Copy License to Clipboard");
        copyButton.addActionListener(new CopyLicenseAction());
        copyButton.setEnabled(false);
        mainPanel.add(copyButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Ready to generate license...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        // Size and center the window
        setSize(600, 500);
        setLocationRelativeTo(null);
    }

    private void setupPlatformSpecificBehavior() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("mac")) {
            // macOS specific settings
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DBeaver License Generator");
            setIconImage(createApplicationIcon());
        } else if (osName.contains("win")) {
            // Windows specific settings
            setIconImage(createApplicationIcon());
        } else {
            // Linux/Unix specific settings
            setIconImage(createApplicationIcon());
        }

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Fall back to default look and feel
            statusLabel.setText("Warning: Could not set system look and feel");
        }
    }

    private Image createApplicationIcon() {
        // Create a simple icon for the application
        int size = 32;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) icon.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a simple database-like icon
        g.setColor(new Color(70, 130, 180));
        g.fillOval(4, 4, size-8, size-8);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        String text = "DB";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
        g.dispose();
        
        return icon;
    }

    private class GenerateLicenseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            generateButton.setEnabled(false);
            statusLabel.setText("Generating license...");
            
            // Run license generation in background thread to avoid blocking UI
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    String product = (String) productComboBox.getSelectedItem();
                    String type = (String) licenseTypeComboBox.getSelectedItem();
                    String version = versionField.getText().trim();
                    
                    if (version.isEmpty()) {
                        throw new IllegalArgumentException("Version cannot be empty");
                    }

                    // Capture the output from License generation
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
                        return baos.toString();
                    } finally {
                        System.setOut(originalOut);
                        ps.close();
                    }
                }

                @Override
                protected void done() {
                    try {
                        String output = get();
                        licenseOutputArea.setText(output);
                        copyButton.setEnabled(true);
                        statusLabel.setText("License generated successfully!");
                    } catch (Exception ex) {
                        licenseOutputArea.setText("Error generating license: " + ex.getMessage());
                        copyButton.setEnabled(false);
                        statusLabel.setText("Error: " + ex.getMessage());
                    } finally {
                        generateButton.setEnabled(true);
                    }
                }
            };
            
            worker.execute();
        }
    }

    private class CopyLicenseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String licenseText = licenseOutputArea.getText();
            if (!licenseText.isEmpty()) {
                // Extract just the license key (the base64 encoded part)
                String[] lines = licenseText.split("\n");
                String licenseToCopy = "";
                
                for (String line : lines) {
                    if (line.trim().length() > 50 && !line.contains("---") && !line.contains("请复制")) {
                        licenseToCopy = line.trim();
                        break;
                    }
                }
                
                if (licenseToCopy.isEmpty()) {
                    licenseToCopy = licenseText; // fallback to full text
                }
                
                StringSelection selection = new StringSelection(licenseToCopy);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                statusLabel.setText("License copied to clipboard!");
                
                // Reset a status message after 3 seconds
                Timer timer = new Timer(3000, evt -> statusLabel.setText("Ready to generate license..."));
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    public static void main(String[] args) {
        // Set platform-specific properties before creating UI
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new LicenseGeneratorUI().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Error starting License Generator UI: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}