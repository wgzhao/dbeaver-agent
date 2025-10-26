package com.dbeaver.agent.ui

import com.dbeaver.agent.License
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*
import javax.swing.*
import javax.swing.Timer

/**
 * Platform-dependent UI for DBeaver license generation
 */
class LicenseGeneratorUI : JFrame() {
    private var productComboBox: JComboBox<String?>? = null
    private var licenseTypeComboBox: JComboBox<String?>? = null
    private var versionField: JTextField? = null
    private var licenseOutputArea: JTextArea? = null
    private var generateButton: JButton? = null
    private var copyButton: JButton? = null
    private var statusLabel: JLabel? = null

    init {
        initializeUI()
        setupPlatformSpecificBehavior()
    }

    private fun initializeUI() {
        setTitle("DBeaver License Generator")
        setDefaultCloseOperation(EXIT_ON_CLOSE)
        setLayout(BorderLayout())

        // Create the main panel
        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.insets = Insets(5, 5, 5, 5)

        // Product selection
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.anchor = GridBagConstraints.WEST
        mainPanel.add(JLabel("Product:"), gbc)

        gbc.gridx = 1
        gbc.gridy = 0
        gbc.fill = GridBagConstraints.HORIZONTAL
        productComboBox = JComboBox<String?>(arrayOf<String>("dbeaver", "cloudbeaver"))
        mainPanel.add(productComboBox, gbc)

        // License type selection
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.fill = GridBagConstraints.NONE
        mainPanel.add(JLabel("License Type:"), gbc)

        gbc.gridx = 1
        gbc.gridy = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        licenseTypeComboBox = JComboBox<String?>(arrayOf<String>("ue", "ee", "le"))
        licenseTypeComboBox!!.setSelectedItem("ue") // Default to Ultimate Edition
        mainPanel.add(licenseTypeComboBox, gbc)

        // Version field
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.fill = GridBagConstraints.NONE
        mainPanel.add(JLabel("Version:"), gbc)

        gbc.gridx = 1
        gbc.gridy = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        versionField = JTextField("25", 10)
        mainPanel.add(versionField, gbc)

        // Generate button
        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        generateButton = JButton("Generate License")
        generateButton!!.addActionListener(GenerateLicenseAction())
        mainPanel.add(generateButton, gbc)

        // License output area
        gbc.gridx = 0
        gbc.gridy = 4
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 1.0
        gbc.weighty = 1.0

        licenseOutputArea = JTextArea(15, 50)
        licenseOutputArea!!.isEditable = false
        licenseOutputArea!!.setFont(Font(Font.MONOSPACED, Font.PLAIN, 12))
        licenseOutputArea!!.setBackground(getBackground())
        licenseOutputArea!!.setBorder(BorderFactory.createTitledBorder("Generated License"))
        val scrollPane = JScrollPane(licenseOutputArea)
        mainPanel.add(scrollPane, gbc)

        // Copy button
        gbc.gridx = 0
        gbc.gridy = 5
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weighty = 0.0
        copyButton = JButton("Copy License to Clipboard")
        copyButton!!.addActionListener(CopyLicenseAction())
        copyButton!!.setEnabled(false)
        mainPanel.add(copyButton, gbc)

        add(mainPanel, BorderLayout.CENTER)

        // Status bar
        statusLabel = JLabel("Ready to generate license...")
        statusLabel!!.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10))
        add(statusLabel, BorderLayout.SOUTH)

        // Size and center the window
        setSize(600, 500)
        setLocationRelativeTo(null)
    }

    private fun setupPlatformSpecificBehavior() {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())

        if (osName.contains("mac")) {
            // macOS specific settings
            System.setProperty("apple.laf.useScreenMenuBar", "true")
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DBeaver License Generator")
            iconImage = createApplicationIcon()
        } else if (osName.contains("win")) {
            // Windows specific settings
            iconImage = createApplicationIcon()
        } else {
            // Linux/Unix specific settings
            iconImage = createApplicationIcon()
        }

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            SwingUtilities.updateComponentTreeUI(this)
        } catch (e: Exception) {
            // Fall back to default look and feel
            statusLabel!!.setText("Warning: Could not set system look and feel")
        }
    }

    private fun createApplicationIcon(): Image {
        // Create a simple icon for the application
        val size = 32
        val icon = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = icon.getGraphics() as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)


        // Draw a simple database-like icon
        g.color = Color(70, 130, 180)
        g.fillOval(4, 4, size - 8, size - 8)
        g.color = Color.WHITE
        g.font = Font(Font.SANS_SERIF, Font.BOLD, 14)
        val fm = g.fontMetrics
        val text = "DB"
        val x = (size - fm.stringWidth(text)) / 2
        val y = (size - fm.height) / 2 + fm.ascent
        g.drawString(text, x, y)
        g.dispose()

        return icon
    }

    private inner class GenerateLicenseAction : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            generateButton!!.setEnabled(false)
            statusLabel!!.setText("Generating license...")


            // Run license generation in background thread to avoid blocking UI
            val worker: SwingWorker<String?, Void?> = object : SwingWorker<String?, Void?>() {
                @Throws(Exception::class)
                override fun doInBackground(): String? {
                    val product = productComboBox!!.selectedItem as String?
                    val type = licenseTypeComboBox!!.selectedItem as String?
                    val version = versionField!!.getText().trim { it <= ' ' }

                    require(!version.isEmpty()) { "Version cannot be empty" }

                    // Capture the output from License generation
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
                        return baos.toString()
                    } finally {
                        System.setOut(originalOut)
                        ps.close()
                    }
                }

                override fun done() {
                    try {
                        val output = get()
                        licenseOutputArea!!.text = output
                        copyButton!!.setEnabled(true)
                        statusLabel!!.setText("License generated successfully!")
                    } catch (ex: Exception) {
                        licenseOutputArea!!.text = "Error generating license: " + ex.message
                        copyButton!!.setEnabled(false)
                        statusLabel!!.setText("Error: " + ex.message)
                    } finally {
                        generateButton!!.setEnabled(true)
                    }
                }
            }

            worker.execute()
        }
    }

    private inner class CopyLicenseAction : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            val licenseText = licenseOutputArea!!.getText()
            if (!licenseText.isEmpty()) {
                // Extract just the license key (the base64 encoded part)
                val lines = licenseText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var licenseToCopy = ""

                for (line in lines) {
                    if (line.trim { it <= ' ' }.length > 50 && !line.contains("---") && !line.contains("请复制")) {
                        licenseToCopy = line.trim { it <= ' ' }
                        break
                    }
                }

                if (licenseToCopy.isEmpty()) {
                    licenseToCopy = licenseText // fallback to full text
                }

                val selection = StringSelection(licenseToCopy)
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null)
                statusLabel!!.setText("License copied to clipboard!")


                // Reset a status message after 3 seconds
                val timer = Timer(3000, ActionListener { evt: ActionEvent? -> statusLabel!!.setText("Ready to generate license...") })
                timer.setRepeats(false)
                timer.start()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Set platform-specific properties before creating UI
            if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")) {
                System.setProperty("apple.laf.useScreenMenuBar", "true")
            }

            SwingUtilities.invokeLater(Runnable {
                try {
                    LicenseGeneratorUI().setVisible(true)
                } catch (e: Exception) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Error starting License Generator UI: " + e.message,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    e.printStackTrace()
                }
            })
        }
    }
}