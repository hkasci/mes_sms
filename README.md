# MES_SMS

Android app MES_SMS — Emergency SMS system (minimal, fast, background-capable).

CI Build
--------
This repository includes a GitHub Actions workflow to build the debug APK on each push or via manual dispatch.

To trigger CI:
1. Commit & push your changes to `main` (or open a PR targeting `main`).
2. Go to the GitHub repo -> Actions -> select "Android CI Build" -> run or view the workflow.

Artifacts
---------
If the build succeeds, the generated debug APK is available in the Actions run under "Artifacts" named `app-debug-apk`.

Local build notes
-----------------
If you prefer to build locally, install JDK 17 and Android SDK (platform 36, build-tools 36.0.0) and run:

```bash
./gradlew assembleDebug
```

If you have any CI or build errors, open an issue or paste the workflow logs and I will fix them and push patches.

