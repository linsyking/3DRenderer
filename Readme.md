# Portable 3D Renderer

## Overview

This project implements a performant 3D rendering and sketching tool on Android devices. It is designed to bridge the gap between lightweight sketching apps and full-featured 3D modeling software. The app supports hand-drawn sketches, rendering configuration, 3D transformations, and export for professional workflows.

---

## Prerequisites

- Android Studio (Giraffe or newer recommended)
- Android SDK version: 33+
- Kotlin version: 1.9+
- Gradle version: 8.0+
- Minimum API level: 26

---

## How to Build and Run

1. **Clone the Repository**

   ```bash
   git clone https://github.com/linsyking/441Renderer.git
   cd 441Renderer
   ```

2. **Open in Android Studio**\
   Open the folder in Android Studio and allow Gradle to sync.

3. **Run the App**

   - Select a physical Android device or emulator.
   - Click "Run" (▶️) in Android Studio.

---

## Third-Party Tools and Libraries

The project directly uses the following open-source libraries:

### Front-End (Android)

- [**Jetpack Compose**](https://developer.android.com/jetpack/compose)\
  For modern, declarative UI development.

- [**OpenGL ES / GLES20**](https://developer.android.com/guide/topics/graphics/opengl)\
  For low-level 3D rendering operations.

- [**AndroidX Core + Lifecycle**](https://developer.android.com/jetpack/androidx/releases/lifecycle)\
  Lifecycle-aware components for rendering lifecycle management.

- [**Skiko (JetBrains Skia Bindings)**](https://github.com/JetBrains/skiko)\
  For GPU-accelerated 2D/3D graphics via Skia.

- [**Kotlinx Serialization**](https://github.com/Kotlin/kotlinx.serialization)\
  To export/import 2D sketches and 3D model metadata.

---

## Notes

- **No backend dependencies**: This project is fully client-side.
- **Export formats**: Exported models are compatible with Blender/FBX via intermediate conversion tools (e.g., OBJ or GLTF).

