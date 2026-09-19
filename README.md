# AvatarCam v0.1

AvatarCam is an Android prototype for selecting a photo avatar and probing the device's real virtual-camera capability before attempting system-wide camera replacement.

## What works in v0.1
- Pick an avatar from the Android document picker.
- Copy the selected avatar into app-private storage so it remains available.
- Preview the avatar locally.
- Request front camera permission for later live animation/face tracking.
- Runtime-test whether the Android 17 virtual-camera extension is present and whether the device reports virtual-camera support.
- Check whether the app actually holds Android's privileged `CREATE_VIRTUAL_DEVICE` permission.
- Avoid falsely reporting “activated” when Android has blocked the system-wide path.

## Important Android limitation
Android virtual cameras sit behind the Virtual Device Manager. Creating a virtual device requires Android's `CREATE_VIRTUAL_DEVICE` permission, which normal third-party APKs cannot simply request at runtime. The device may support the camera framework while still refusing a regular APK permission to create the host virtual device.

That means the eventual system-wide mode needs one of these routes on the target phone:
1. an OEM/system-supported role that grants the virtual-device capability, or
2. a device-level/root/system helper.

The app is deliberately structured so the avatar renderer/UI stays separate from that privileged helper.

## Build
Current project settings target Android 17 / API 37 with Android Gradle Plugin 9.4.0 and Gradle 9.6.0.

A GitHub Actions workflow is included. On every push it builds a debug APK and publishes it as the `AvatarCam-debug-apk` workflow artifact.

## Safety
Use another real person's image only with their permission. AvatarCam is not intended to defeat biometric, liveness, or identity verification systems.
