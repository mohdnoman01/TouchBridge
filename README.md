# TouchBridge

TouchBridge is a system that allows your Android phone to act as a customizable game controller for your PC over WiFi.

## Project Structure
- `android-app/`: The Android application source code (Kotlin + Compose).
- `pc-listener/`: Python script to receive events and simulate keyboard/mouse input.
- `protocol/`: JSON definition of the communication protocol.
- `docs/`: Documentation.

## Setup Instructions

### 1. PC Listener (Server)
1. Install Python 3.x.
2. Install the required `pynput` library:
   ```bash
   pip install pynput
   ```
3. Run the listener script:
   ```bash
   python pc-listener/listener.py
   ```
4. Note your PC's local IP address (e.g., run `ipconfig` on Windows or `ifconfig` on Linux/Mac).

### 2. Android App (Client)
1. Open the `android-app` project in Android Studio.
2. Build and run the app on your Android device.
3. Ensure your phone and PC are on the same WiFi network.
4. Enter your PC's IP address in the app and tap **Connect**.

## Controls
- **W, A, S, D**: Movement keys.
- **FIRE**: Left Mouse Click.

## Troubleshooting
- **Connection Failed**: Check if your PC's firewall is blocking port `12345`.
- **Latency**: Ensure a stable WiFi connection.
- **Permissions**: The PC listener needs permissions to control keyboard/mouse (on macOS, this might require Accessibility permissions).
