# TorchBridge 🔦🌉

TorchBridge is a high-performance system that transforms your Android device into a professional-grade, fully customizable game controller for your PC over WiFi. Designed with low latency and modern Android practices, it allows you to control FPS and action games with ease.

## 🚀 Key Features

- **Dynamic Joystick**: Responsive movement control with zero-deadzone logic.
- **Precision Aiming**: High-frequency touch-to-mouse translation for pixel-perfect aim.
- **Gyroscope Support**: Use your phone's tilt sensors for immersive fine-aiming.
- **Multi-Touch HUD**: Optimized layout for firing, peeking, jumping, and crouching.
- **Zero-Lag Architecture**: Uses a dedicated high-priority threading system and optimized network buffering to ensure millisecond-level responsiveness.
- **Customize Controls**: On-the-fly configuration of server IP and sensor settings.

## 📂 Project Structure

- `android-app/`: Modern Jetpack Compose application with optimized high-frequency input handling.
- `pc-listener/`: Python-based server that translates network packets into OS-level keyboard and mouse events using `pynput`.

## 🛠️ Setup Instructions

### 1. PC Listener (Server)
1. Ensure you have **Python 3.8+** installed.
2. Install the necessary dependencies:
   ```bash
   pip install pynput
   ```
3. Launch the listener:
   ```bash
   python pc-listener/listener.py
   ```
4. The server will start listening on port `12345`. Note your PC's Local IP address (use `ipconfig` on Windows).

### 2. Android App (Client)
1. Open the project in **Android Studio**.
2. Build and deploy to your Android device (Android 8.0+ recommended).
3. Connect your phone to the same WiFi network as your PC.
4. Launch the app, tap the **Settings** icon, enter your PC's IP, and hit **Connect**.

## 🎮 Default Controls Mapping

| Action | Android Button | PC Input |
| :--- | :--- | :--- |
| **Movement** | Left Joystick | `W`, `A`, `S`, `D` |
| **Aiming** | Right Screen Area / Gyro | Mouse Movement |
| **Fire** | Fire Icon (Top Left) | `Left Click` |
| **ADS (Scope)** | Scope Icon (Right) | `Right Click` |
| **Jump** | Jump Icon | `Space` |
| **Crouch** | Crouch Icon | `C` |
| **Prone** | Prone Icon | `Z` |
| **Reload** | Reload Icon | `R` |
| **Peeks** | Q / E Icons | `Q` / `E` |
| **Map** | Map Icon | `M` |

## 🔧 Technical Details

- **Networking**: TCP-based communication with `TCP_NODELAY` enabled for minimal packet latency.
- **Conveyor System**: The Android client uses a `LinkedBlockingQueue` to handle rapid touch events without blocking the Main (UI) thread.
- **Sensors**: Leverages the Android `Sensor.TYPE_GYROSCOPE` with a moving average filter for smooth, non-jittery aiming.

## 🛡️ Troubleshooting

- **Firewall**: Ensure port `12345` (TCP) is open on your PC's firewall.
- **Network Lag**: 5GHz WiFi is highly recommended for the best experience.
- **Permissions**: On Windows, run the Python script in a terminal with sufficient privileges to simulate inputs.
