# SPORS: Secure Phone Ownership and Recovery System

SPORS is a comprehensive Android application designed to be a modern-day solution for locating lost devices. It transforms your smartphone into a powerful tool for finding not only your own misplaced items but also for contributing to a community-based network to help others recover their lost devices. The application leverages a robust set of technologies, including real-time location tracking, Bluetooth proximity scanning, and a secure, anonymous communication platform, to create a reliable and user-friendly experience.

## Table of Contents

  - [Key Features](#key-features)
  - [How It Works](#how-it-works)
  - [Architecture](#architecture)
  - [Technology Stack](#technology-stack)
  - [Permissions Explained](#permissions-explained)
  - [Getting Started](#getting-started)
  - [Project Structure](#project-structure)
  - [Contributing](#contributing)
  - [License](#license)

-----

## Key Features

  - **Real-Time Location Tracking**: The app uses both fine and coarse location data, with support for background location updates, to provide the last known location of your device on an integrated Google Map.
  - **Bluetooth Proximity Detection**: By utilizing Bluetooth Low Energy (BLE), the app can scan for nearby devices and beacons, helping you pinpoint the location of a lost item when you are in close proximity.
  - **Community-Powered Search**: The "Help Find" feature allows users to participate in the search for other users' lost devices. When a user's device comes within range of a lost item, its location is anonymously updated, increasing the chances of recovery.
  - **Lost and Found Reporting**: Users can easily report their devices as lost or report a device they have found. This initiates the tracking process and notifies the community.
  - **Secure Anonymous Chat**: An in-app anonymous chat feature allows the finder of a lost device to communicate securely with the owner without revealing any personal information, ensuring privacy and safety for both parties.
  - **Persistent Background Services**: The app runs as a foreground service, ensuring that location and Bluetooth scanning continue to operate reliably even when the app is not in the foreground. It also includes a `BootReceiver` to automatically restart services when the device is powered on.
  - **User Authentication and Profile Management**: Secure user authentication is provided through email and password, with options for password recovery. Users can also manage their profile information within the app.

-----

## How It Works

1.  **Sign Up and Set Up**: Users create an account and grant the necessary permissions for location and Bluetooth access.
2.  **Background Tracking**: The app's `LocationService` and `BeaconService` run in the background, periodically updating the device's location and scanning for other devices in the network.
3.  **Report a Lost Device**: When a device is lost, the user can mark it as "lost" from their account. The app will then display its last known location on a map.
4.  **Community Assistance**: Other users running the app will anonymously detect the lost device if they come near it, and the location will be updated for the owner.
5.  **Report a Found Device**: If a user finds a lost device, they can report it as "found" and initiate an anonymous chat with the owner to coordinate its return.

-----

## Architecture

This application follows a modern Android architecture, loosely based on the **Model-View-ViewModel (MVVM)** pattern.

  - **View**: The UI is built using Activities and Fragments, which are responsible for displaying data and capturing user input.
  - **ViewModel**: The use of `lifecycle-viewmodel-ktx` suggests that ViewModels are used to hold and manage UI-related data, surviving configuration changes.
  - **Model**: The model layer consists of data classes (like `ChatModels`), a network layer for API communication (`ApiService` with Retrofit), and services for background tasks.
  - **Services**: `LocationService` and `BeaconService` handle the heavy lifting of background tracking, independent of the UI.
  - **Repository**: Although not explicitly defined, a repository pattern is likely used to abstract the data sources (network and local database) from the ViewModels.

-----

## Technology Stack

  - **Language**: [Kotlin](https://kotlinlang.org/)
  - **Asynchronous Programming**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
  - **Architecture Components**:
      - [AndroidX](https://developer.android.com/jetpack/androidx)
      - [Lifecycle (ViewModel & LiveData)](https://developer.android.com/topic/libraries/architecture/lifecycle)
      - [View Binding](https://developer.android.com/topic/libraries/view-binding)
  - **Networking**:
      - [Retrofit 2](https://square.github.io/retrofit/) for REST API communication
      - [OkHttp 3](https://square.github.io/okhttp/) as the HTTP client
      - [Gson](https://github.com/google/gson) for JSON serialization
  - **UI**:
      - [Material Design Components](https://material.io/develop/android)
      - [SmoothBottomBar](https://github.com/ibrahimsn98/SmoothBottomBar) for navigation
  - **Location and Maps**:
      - [Google Maps Platform](https://developers.google.com/maps/documentation/android-sdk/start)
      - [Google Play Services for Location](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary)
  - **Authentication**:
      - [Google Play Services for Auth](https://developers.google.com/android/reference/com/google/android/gms/auth/api/package-summary)

-----

## Permissions Explained

The application requests a number of permissions to provide its core functionality. Here’s why they are needed:

  - **`CAMERA`**: To allow users to take and upload photos of found items.
  - **`INTERNET` & `ACCESS_NETWORK_STATE`**: Required for all network communication, including user authentication, location reporting, and chat.
  - **`ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`**: Essential for accurately determining the device's location.
  - **`ACCESS_BACKGROUND_LOCATION`**: Crucial for the app to track the device's location even when it's not in use, which is a core feature for finding a lost phone.
  - **`BLUETOOTH`, `BLUETOOTH_ADMIN`, `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`**: Needed for the proximity detection feature, allowing the app to scan for, connect to, and advertise its presence to other devices.
  - **`READ_PHONE_STATE`, `READ_PHONE_NUMBERS`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`**: These permissions are likely used for user verification or for features that help identify the device or its owner.
  - **`USE_BIOMETRIC`**: To provide a secure and convenient way for users to authenticate within the app.
  - **`FOREGROUND_SERVICE`**: Required to run the location and beacon services reliably in the background.
  - **`RECEIVE_BOOT_COMPLETED`**: To automatically start the app's services when the device is turned on, ensuring continuous protection.

-----

## Getting Started

To get started with the project, you can clone the repository and set it up in Android Studio.

### Prerequisites

  - Android Studio (latest version recommended)
  - Android SDK
  - An Android device or emulator

### Installation

1.  **Clone the repository**:
    ```sh
    git clone https://github.com/normie69k/SPORS.git
    ```
2.  **Open in Android Studio**: Open the cloned project in Android Studio.
3.  **API Key**: You will need to provide your own Google Maps API key. Add it to your `local.properties` file:
    ```
    MAPS_API_KEY=YOUR_API_KEY
    ```
    Then, reference this in your `AndroidManifest.xml` where the key is specified as `@string/google_maps_key`.
4.  **Build and Run**: Build the project to download all the dependencies, and then run it on an emulator or a physical device.

-----

## Project Structure

The project is organized into the following main packages:

  - **`adapters`**: Contains `RecyclerView` adapters for displaying lists of data.
  - **`bluetooth`**: Includes the `BluetoothScanner` class for handling BLE scanning.
  - **`fragments`**: Contains all the UI fragments for different screens of the app.
  - **`models`**: Defines the data models used in the application (e.g., `ChatModels`).
  - **`network`**: Contains the `ApiService` interface for Retrofit.
  - **`services`**: Includes the background services like `LocationService` and `BeaconService`.
  - **`utils`**: Utility classes and helper functions.

-----

## Contributing

Contributions are welcome\! If you would like to contribute to this project, please follow these steps:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/your-feature-name`).
3.  Make your changes and commit them (`git commit -m 'Add some feature'`).
4.  Push to the branch (`git push origin feature/your-feature-name`).
5.  Open a Pull Request.

-----

## License

This project is licensed under the MIT License. See the `LICENSE` file for more details.
