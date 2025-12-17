# Android SDK Setup

The project is configured to look for the Android SDK at `/opt/android-sdk` (see `local.properties`).
To install Platform 34 and the corresponding build tools when network access is available:

1. Download the Android command-line tools for Linux from Google.
2. Extract them into `/opt/android-sdk/cmdline-tools/latest` so that `sdkmanager` is available at `/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager`.
3. Run:
   ```
   yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   ```
4. Accept licenses when prompted.

Due to the current environment's network restrictions, the SDK components could not be downloaded automatically here. Once the SDK packages are available under `/opt/android-sdk`, Gradle should detect them via the checked-in `local.properties` and builds/tests can proceed normally.
