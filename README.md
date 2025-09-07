````markdown
# APkARiS  

APkARiS is an Android application designed for analyzing APK files and retrieving essential device information in a lightweight, user-friendly way.  
It combines **APK metadata analysis**, **permission inspection**, and **device info display** into a single app with a clean, gradient-based interface.  
The application is structured with only **5 activities**, keeping it simple, fast, and efficient.  

---

## 🚀 Features & Functionality  

### 1. Splash Screen  
- Displays app logo, name, and description with animations.  
- Gradient background for modern UI experience.  
- Auto-redirects to main dashboard after a short delay.  

### 2. Main Dashboard  
- Serves as the home screen of the app.  
- Provides navigation to the two core functions:  
  - **APK Analysis**  
  - **Device Information**  
- Includes quick access to Settings/About page.  

### 3. APK Analysis  
- Allows users to select APK files from device storage.  
- Extracts and displays:  
  - App name  
  - Package name  
  - Version name & version code  
  - File size  
- Lists all declared permissions.  
- Categorizes permissions as **Normal**, **Dangerous**, or **Special** for quick security insights.  
- Provides a summary view so users can quickly judge APK safety.  

### 4. Device Information  
- Fetches device-specific information such as:  
  - Device model & manufacturer  
  - Android version & SDK level  
  - Hardware details (CPU, RAM info where available)  
  - IMEI / Serial number (restricted on newer Android versions due to permissions).  
- Helps in device-level debugging and verification.  

### 5. Settings / About  
- Displays app version and developer details.  
- Provides link to project repository.  
- May include theme toggles or simple preferences.  

---

## 📸 Screenshots  

### Splash Screen  
*(Placeholder – add screenshot here)*  

### Main Dashboard  
*(Placeholder – add screenshot here)*  

### APK Analysis Screen  
*(Placeholder – add screenshot here)*  

### Device Information Screen  
*(Placeholder – add screenshot here)*  

### Settings/About Screen  
*(Placeholder – add screenshot here)*  

---

## 📊 App Flow Diagram  

```mermaid
flowchart TD
    A[Splash Screen] --> B[Main Dashboard]
    B --> C[APK Analysis]
    B --> D[Device Information]
    B --> E[Settings/About]

    C --> C1[Select APK File]
    C1 --> C2[Extract Metadata]
    C2 --> C3[Analyze Permissions]
    C3 --> C4[Show APK Report]

    D --> D1[Fetch Device Properties]
    D1 --> D2[Show Device Report]

    E --> E1[App Info & Preferences]
````

---

## 📂 Project Structure

```
APkARiS/
│── app/                     
│   ├── src/main/java/...    # Kotlin source code
│   ├── res/                 # Layouts, drawables, animations, etc.
│   ├── AndroidManifest.xml  # App manifest
│── gradle/                  
│── build.gradle             # App-level gradle
│── settings.gradle.kts      
```

---

## 🛠️ Tech Stack

* **Language**: Kotlin
* **Framework**: Android SDK
* **UI**: XML Layouts + Material Design + Animations
* **Version Control**: Git + GitHub

---

## ⚙️ How It Works

1. **App Launch**

    * User opens the app → Splash Screen animates logo, name, and description.
    * After \~5 seconds, the app transitions to the **Main Dashboard**.

2. **Main Dashboard**

    * Central hub with navigation options for:

        * APK Analysis
        * Device Information
        * Settings/About

3. **APK Analysis Flow**

    * User selects an APK file from storage.
    * The app parses the APK’s `AndroidManifest.xml`.
    * Extracts metadata such as app name, version, package name.
    * Reads declared permissions and groups them by severity.
    * Displays results in a structured, readable format.

4. **Device Information Flow**

    * On selecting “Device Info”, the app queries system properties.
    * Uses Android APIs (`Build`, `TelephonyManager`, etc.) to fetch:

        * Model, manufacturer, hardware info.
        * Android version & SDK.
        * IMEI/serial (if accessible).
    * Displays info for user review or troubleshooting.

5. **Settings/About**

    * Provides app metadata and developer details.
    * Optional customization options (e.g., theme, preferences).

---

## 🔧 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/Normie69K/APkARiS.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle dependencies.
4. Run the app on an emulator or physical device.

---

## 👨‍💻 Contributors

* **Karan Singh** ([@Normie69K](https://github.com/Normie69K))

---

## 📜 License

This project is licensed under the **MIT License**.

```

This version gives both **features explanation** and a **visual flow diagram** for the app. Do you want me to also add a **sequence diagram** for APK analysis (step-by-step parsing process)?
```
