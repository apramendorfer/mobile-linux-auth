# Multi-Device Linux Authentication: Unlock Your Computer with Your Smartphone or Smartwatch

### Overview

This project provides a prototype for a user-friendly alternative to password-based Linux authentication used in a scientific evaluation. When you attempt to log in to your Linux machine, an authentication request is sent to your mobile device(s). You can approve the request via a confirmation button or biometric verification on your phone or watch. Notifications are delivered using Firebase Cloud Messaging (FCM).

Use this project at your own risk. The software is provided "as is", without warranty of any kind. 



### Features

- **Passwordless Login**: Unlock your Linux computer using your phone or smartwatch.
- **Multi-Device Support**: Authenticate via Android phone or Wear OS watch.
- **Biometric Integration**: Configure to use biometric sensor for authentication. Includes a faked fingerprint sensor for the Wear OS application. 
- **Push Notifications**: Real-time authentication requests via Firebase.
- **Configurable Backend**: Easily set up your own API server.

### How It Works

> **Note:** This is a research prototype. Use with caution.

1. **Setup**: Install the mobile authenticator app on your Android phone and (optionally) the companion app on your Wear OS smartwatch.
2. **Configuration**: Register your device's public key and device token with the backend API.
3. **Authentication Flow**: When you log in to your Linux machine, a request is sent to your phone/watch. Approve the request to unlock your computer.

### Installation

#### Requirements

- Linux computer (with PAM support)
- Android phone
- (Optional) Wear OS smartwatch
- Firebase project for push notifications
- .NET Core SDK for backend API

#### Steps

1. **Clone the Repository**

2. **Set Up Firebase for Notifications**

   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app to your Firebase project and download the `google-services.json` file.
   - Place `google-services.json` in the `authenticator/app/` directory.
   - In the Firebase Console, enable Cloud Messaging.

3. **Configure Smartphone Application**

   - Configure username and url of api [`here`](authenticator/app/src/main/java/at/apramendorfer/authenticator/config/Constants.kt).

4. **Register Device Public Key and Token**

   - Start the mobile authenticator app. The log will display your device token.
   - The public key can be found on the settings screen of the application.
   - Add these values to the user entry in [`AuthenticationRequestsService`](api/Services/AuthenticationRequestsService.cs) in the backend API.

5. **Install PAM Module and Script**

   - Make sure to have [`pam_python`](https://github.com/castlabs/pam-python) installed on your PC.
   - Edit the username in [`pam/login.py`](pam/login.py) to match the username on your mobile device.
   - Edit the backend api url in [`pam/login.py`](pam/login.py) to match where you have your api deployed.
   - Copy the contents of [`pam/common-auth`](pam/common-auth) to `/etc/pam.d/common-auth` on your Linux machine.
   - Place [`pam/login.py`](pam/login.py) in `/opt/` on your Linux machine.

6. **Start the Backend API**

   - Open the `api` directory in your preferred .NET IDE.
   - Build and run the API project.

7. **Test the System**
   - Interact with your Linux login screen. You should receive a notification on your phone/watch to approve the authentication request.

### Usage

- Approve authentication requests via notification on your phone or smartwatch.
- If biometrics are enabled, confirm with your fingerprint.
- If you do not receive a notification, open the app manually to view pending requests.

### License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
