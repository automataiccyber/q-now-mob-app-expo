# Q-Now - Mobile Queue Management App

A comprehensive mobile application designed for efficient queue management and appointment scheduling that helps patients and service-seekers manage their wait times and book appointments with real-time updates and instant notifications.

## 📱 App Overview

Q-Now is a mobile application developed to provide a seamless and organized experience for managing queues and appointments in various establishments. It features a robust system for users to join queues remotely, monitor their status, and book appointments ahead of time. The app integrates with Firebase for real-time synchronization, secure authentication, and cloud-based notifications, ensuring users are always informed about their service status.

## ✨ Key Features

### 🕒 Queue Management
- **Remote Queue Joining**: Join queues from anywhere without being physically present at the establishment.
- **Real-time Status Monitoring**: Live updates on your position in the queue and estimated waiting times.
- **Smart Arrival Index**: Track your active service status and receive notifications when it's your turn.
- **Remote Exit**: Easily leave a queue if your plans change, maintaining system efficiency.

### 📅 Appointment Scheduling
- **Instant Booking**: Schedule appointments with various establishments directly through the app.
- **Rescheduling & Cancellation**: Flexible management of existing appointments with integrated request systems.
- **Upcoming Overview**: Centralized view of all future appointments on the main dashboard.
- **Service Availability**: View available time slots and services offered by establishments.

### 🔍 Discovery & Profile
- **Establishment Search**: Search and browse nearby or specific centers to view their services and availability.
- **Comprehensive Profiles**: Securely manage personal information to streamline service registration.
- **Recently Visited**: Quick access to history of visited establishments for easy re-queueing.
- **HealthPass Integration**: (In Development) Secure identity verification for streamlined check-ins.

### 🔔 Notifications & Communication
- **Instant Alerts**: Real-time push notifications via Firebase Cloud Messaging (FCM) for queue and appointment updates.
- **In-App Inbox**: A centralized location to view all past notifications and status changes.
- **Dynamic Messaging**: Automated updates on arrival status, reschedule confirmations, and more.

## 🛠️ Technical Stack

### Core Technologies
- **Language**: Java (Android SDK).
- **Platform**: Android (Min SDK 24, Target SDK 35).
- **Backend/Database**: Firebase (Firestore & Realtime Database).
- **Architecture**: MVVM-inspired architecture with specialized adapters and repositories.

### Key Dependencies
- **Firebase SDK**: Authentication, Firestore, Realtime Database, Cloud Messaging, and Analytics.
- **Material Design**: Modern UI components for a clean and professional user experience.
- **AndroidX**: Modern library support for better compatibility and performance.
- **Gradle (Kotlin DSL)**: Robust build and dependency management.

### Architecture
- **Home Dashboard**: Central hub for navigation, active queues, and upcoming appointments.
- **Queue Engine**: Logic for joining, monitoring, and exiting queues in real-time.
- **Appointment Manager**: Comprehensive system for scheduling and managing service requests.
- **Firebase Sync Layer**: Real-time data synchronization between the mobile client and cloud services.

## 📂 Project Structure

```
q-now-mob-app-expo-main/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/signin/
│   │   │   ├── HomeActivity.java          # Main Dashboard & Navigation
│   │   │   ├── MainActivity.java          # Login & Entry Point
│   │   │   ├── JoinQueueActivity.java     # Remote Queue Entry
│   │   │   ├── BookAppointmentActivity.java # Appointment Booking
│   │   │   ├── SearchEstablishmentActivity.java # Center Discovery
│   │   │   ├── PatientInfo.java           # Profile Management
│   │   │   ├── MyFirebaseMessagingService.java # Notification Handling
│   │   │   └── ... (Adapters & Models)
│   │   ├── res/layout/
│   │   │   ├── availability.xml           # Home Screen Layout
│   │   │   ├── signin.xml                 # Login UI
│   │   │   ├── activity_join_queue.xml    # Queue UI
│   │   │   └── ... (Other UI Layouts)
│   │   └── AndroidManifest.xml            # App Configuration & Permissions
│   ├── build.gradle.kts                   # Dependencies & Build Config
│   └── google-services.json               # Firebase Config
└── ...
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (Ladybug or newer recommended).
- Android Device or Emulator (API 24+).
- Firebase Project with Firestore and Realtime Database enabled.

### Installation

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd q-now-mob-app-expo-main
   ```

2. **Set up Firebase**
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.signin`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Enable Authentication, Firestore, and Realtime Database.

3. **Build & Run**
   - Open the project in Android Studio.
   - Sync project with Gradle files.
   - Run the app on your connected device or emulator.

### Configuration

#### Firebase Setup
1. **Firestore**: Ensure collections like `patients`, `establishments`, and `inbox` are structured correctly.
2. **Realtime Database**: Set up nodes for `arrivalIndex` and `establishments` to handle live queue updates.
3. **Cloud Messaging**: Configure the server key in the Firebase Console for push notifications.

#### Permissions
The app requires the following permissions:
- `INTERNET`: For Firebase communication.
- `POST_NOTIFICATIONS`: For receiving queue and appointment updates (Android 13+).

## 📱 Screenshots

<img width="900" height="2030" alt="image" src="https://github.com/user-attachments/assets/c598bae0-36bc-46ee-9cc2-cc1b5d567bc2" />
<img width="900" height="2030" alt="image" src="https://github.com/user-attachments/assets/a45c7177-2256-48de-b025-b4bdec723b8a" />
<img width="900" height="2030" alt="image" src="https://github.com/user-attachments/assets/3cf43a38-2246-4a87-8d2f-31e210607db8" />
<img width="900" height="2030" alt="image" src="https://github.com/user-attachments/assets/75c34fc4-85c9-4287-8088-62fe28a46ce9" />
<img width="900" height="2030" alt="image" src="https://github.com/user-attachments/assets/e6ad7018-3a3c-42cb-baf6-dd0e7416698b" />

## 🧪 Testing

### Manual Testing
- **Authentication**: Test login, signup, and "Remember Me" functionality.
- **Queueing**: Join a queue and verify status updates in the `Active Queues` section.
- **Appointments**: Book an appointment and check if it appears in the `Upcoming Appointments` list.
- **Notifications**: Trigger a status change in Firebase and verify the push notification receipt.

## 📄 License

This project is proprietary software. All rights reserved.

## 📞 Contact

For support or inquiries:
- **Email**: lieldarrenfajutagana@gmail.com
- **Developer**: Liel Darren Fajutagana

## 🔄 Version History

- **v1.0**: Initial release
  - Core Dashboard and Navigation
  - Firebase Authentication & Firestore Integration
  - Remote Queue Management & Arrival Tracking
  - Appointment Booking & Scheduling System
  - Push Notification Support

## 🤝 Contributing

This is a proprietary application. For feature requests or bug reports, please contact the developer.

---

**Q-Now** - Your smart solution for real-time queue management and appointment scheduling.
