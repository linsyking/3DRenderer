# Portable 3D Renderer

## Overview

This project implements a performant 3D rendering and sketching tool on Android devices. It is designed to bridge the gap between lightweight sketching apps and full-featured 3D modeling software. The app supports hand-drawn sketches, rendering configuration, 3D transformations, and export for professional workflows.

---

## Getting Started

### Prerequisites

- Android Studio (Giraffe or newer recommended)
- Android SDK version: 33+
- Kotlin version: 1.9+
- Gradle version: 8.0+
- Minimum API level: 26

### How to Build and Run

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

### Third-Party Tools and Libraries

The project directly uses the following open-source libraries:

#### Front-End (Android)

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

### Notes

- **No backend dependencies**: This project is fully client-side.
- **Export formats**: Exported models are compatible with Blender/FBX via intermediate conversion tools (e.g., OBJ or GLTF).

---

## Model and Engine

---

## APIs and Controller

### Proposed API

#### Canvas & Window Management

| Endpoint             | Description                         | Parameters                         | Response        |
| -------------------- | ----------------------------------- | ---------------------------------- | --------------- |
| `initializeCanvas()` | Create and prepare rendering canvas | `width`, `height`, `windowOptions` | Success/Failure |
| `resizeCanvas()`     | Resize the rendering viewport       | `newWidth`, `newHeight`            | Success/Failure |
| `destroyCanvas()`    | Clean up rendering resources        | None                               | Success/Failure |

#### Mesh Loading & Management

| Endpoint       | Description             | Parameters                 | Response         |
| -------------- | ----------------------- | -------------------------- | ---------------- |
| `importMesh()` | Load mesh from OBJ file | `filePath` or `fileBuffer` | Mesh ID or Error |
| `removeMesh()` | Remove mesh from scene  | `meshID`                   | Success/Failure  |
| `listMeshes()` | Query loaded meshes     | None                       | List of Mesh IDs |

#### Sketching & 2D Curve Management

| Endpoint                | Description                             | Parameters                    | Response         |
| ----------------------- | --------------------------------------- | ----------------------------- | ---------------- |
| `startSketch()`         | Begin user sketching session            | None                          | Sketch ID        |
| `updateSketch(points)`  | Stream sketch points (2D curve)         | List of 2D points             | Success/Failure  |
| `finalizeSketch()`      | Complete sketch input                   | Sketch ID                     | Success/Failure  |
| `convertSketchToMesh()` | Convert 2D sketch to 3D mesh            | `sketchID`, optional params   | Mesh ID or Error |
| `editSketch()`          | Apply transformations (translate, etc.) | `sketchID`, `transformParams` | Success/Failure  |
| `groupSketches()`       | Group multiple sketches                 | List of Sketch IDs            | Group ID         |

#### Scene Navigation & View Control

| Endpoint              | Description                | Parameters                | Response        |
| --------------------- | -------------------------- | ------------------------- | --------------- |
| `setCameraPosition()` | Move camera to a position  | `position`, `orientation` | Success/Failure |
| `orbitCamera()`       | Orbit camera around target | `angleDelta`              | Success/Failure |
| `zoomCamera()`        | Zoom in/out the view       | `zoomFactor`              | Success/Failure |

#### Rendering & Configuration

| Endpoint                 | Description                       | Parameters      | Response        |
| ------------------------ | --------------------------------- | --------------- | --------------- |
| `setRenderingConfig()`   | Apply rendering settings          | `configOptions` | Success/Failure |
| `switchRenderingSetup()` | Toggle between multiple setups    | `setupID`       | Success/Failure |
| `renderScene()`          | Trigger manual render (if needed) | Optional flags  | Success/Failure |

#### Exporting

| Endpoint                    | Description                              | Parameters                     | Response        |
| --------------------------- | ---------------------------------------- | ------------------------------ | --------------- |
| `exportMeshOBJ()`           | Export scene or selected meshes as OBJ   | `outputPath` or `bufferTarget` | Success/Failure |
| `exportSceneConfig()`       | Save scene configuration (e.g., OpenUSD) | `outputPath`                   | Success/Failure |
| `exportAnimationSequence()` | Output animation sequence if applicable  | `outputPath` or `format`       | Success/Failure |
| `exportRenderConfig()`      | Save current rendering setup             | `outputPath`                   | Success/Failure |

### Integration with Third-Party SDKs

| SDK               | Purpose                                            | Interaction Method                                  |
| ----------------- | -------------------------------------------------- | --------------------------------------------------- |
| **WebGPU**        | Cross-platform GPU rendering backend               | Direct calls from engine; abstracted from front-end |
| **Tinyobjloader** | Efficient OBJ mesh parsing                         | Invoked during `importMesh()`                       |
| **2D Style Libs** | Optional stylization, non-photorealistic rendering | Applied during rendering based on config            |

### Future Considerations

* API Versioning for long-term compatibility
* Batch operations to reduce overhead in performance-critical scenarios
* Advanced mesh editing exposed incrementally
* WebAssembly optimizations for browser-based deployments

### Example Interaction Flow

1. `startSketch()`
2. `updateSketch()` with streamed 2D points
3. `finalizeSketch()`
4. `convertSketchToMesh()` to generate 3D geometry
5. `editSketch()` or `groupSketches()` for refinements
6. `exportMeshOBJ()` to save output

---

## View UI/UX

---

## Team Roster
