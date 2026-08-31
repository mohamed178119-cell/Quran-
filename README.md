# Casio Classic Calculator

Android calculator app inspired by the classic Casio scientific-calculator layout. It includes arithmetic operations, scientific functions, degree/radian mode, inverse trigonometry via SHIFT, percentage, constants, delete, and clear keys.

## Build locally

```bash
gradle :app:assembleDebug --no-daemon
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## APK workflow

GitHub Actions builds the debug APK on pushes, pull requests, and manual `workflow_dispatch` runs. Download the generated `casio-classic-debug-apk` artifact from the workflow run.
