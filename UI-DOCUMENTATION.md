# DBeaver License Generator UI

This document describes the new platform-dependent graphical user interface for generating DBeaver licenses.

## Overview

The DBeaver License Generator now includes a cross-platform GUI application that provides an easy-to-use interface for generating licenses without requiring command-line interaction.

## Features

### Platform-Dependent UI
- **Windows**: Native Windows look and feel
- **macOS**: Native macOS appearance with system menu bar integration
- **Linux/Unix**: System-appropriate look and feel

### GUI Components
- **Product Selection**: Dropdown to choose between DBeaver and CloudBeaver
- **License Type**: Dropdown for license types (Ultimate Edition, Enterprise Edition, Lite Edition)
- **Version Input**: Text field for specifying the product version
- **Generate Button**: Triggers license generation
- **License Display**: Large text area showing the generated license
- **Copy to Clipboard**: One-click copying of the license key
- **Status Bar**: Shows current operation status and messages

### User Experience
- **Background Processing**: License generation runs in a separate thread to keep UI responsive
- **Progress Feedback**: Status messages inform user of operation progress
- **Error Handling**: Clear error messages for invalid inputs or generation failures
- **Platform Icons**: Custom application icons that adapt to the platform

## Usage

### Starting the GUI

#### On Windows:
```cmd
gen-license-ui.bat
```

#### On Linux/macOS:
```bash
./gen-license-ui.sh
```

### Generating a License

1. **Select Product**: Choose "dbeaver" or "cloudbeaver" from the Product dropdown
2. **Choose License Type**: 
   - `ue` - Ultimate Edition (default)
   - `ee` - Enterprise Edition  
   - `le` - Lite Edition
3. **Enter Version**: Specify the product version (default: 25)
4. **Click Generate**: Press the "Generate License" button
5. **Copy License**: Use the "Copy License to Clipboard" button to copy the license key

### Platform-Specific Features

#### macOS
- Menu bar integration with native macOS appearance
- System-style dialogs and components
- Native keyboard shortcuts

#### Windows
- Windows look and feel
- System notification area integration
- Native file dialogs

#### Linux/Unix
- GTK+ or system-appropriate theme
- Desktop environment integration
- Standard Linux UI conventions

## Technical Implementation

### Architecture
- Built using Java Swing for cross-platform compatibility
- Utilizes `UIManager.getSystemLookAndFeelClassName()` for native appearance
- Platform detection via `System.getProperty("os.name")`
- Background processing with `SwingWorker` for responsive UI

### License Generation Backend
- Reuses existing `License` class functionality
- Direct method invocation (avoiding `System.exit()` calls)
- Reflection-based parameter setting for compatibility
- Output capture and parsing for display

### Error Handling
- Input validation for all form fields
- Graceful handling of license generation errors
- User-friendly error messages
- Fallback behaviors for platform-specific features

## Testing

The UI includes comprehensive testing:
- **Unit tests** for component creation and platform detection
- **Integration tests** for license generation workflow
- **Headless testing** for CI/CD environments
- **Platform compatibility** validation

## File Structure

```
src/main/java/com/dbeaver/agent/ui/
└── LicenseGeneratorUI.java          # Main UI class

src/test/java/com/dbeaver/agent/ui/
├── LicenseGeneratorUITest.java      # UI component tests
└── LicenseGeneratorUIIntegrationTest.java  # Integration tests

gen-license-ui.sh                    # Unix launcher script
gen-license-ui.bat                   # Windows launcher script
```

## Benefits

1. **User-Friendly**: No command-line knowledge required
2. **Platform-Native**: Feels natural on each operating system  
3. **Error-Resistant**: Input validation prevents common mistakes
4. **Efficient**: One-click copying and visual feedback
5. **Accessible**: Standard GUI controls work with accessibility tools
6. **Maintainable**: Reuses existing license generation logic

## Future Enhancements

Potential improvements could include:
- License validation and preview
- Batch license generation
- License export to file
- Custom branding and themes
- Advanced license options configuration