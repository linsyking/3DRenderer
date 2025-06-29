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

### Storymap  
A visual overview of our app’s user interaction and development stages is provided below ![storymap](fig/storymap.png).

### Engine Architecture

Our application features a custom rendering engine designed to convert user sketches into high-quality 3D visualizations in real-time. Below is a diagram of the engine architecture (`fig/engine.png`), followed by detailed descriptions of each component and their roles:

![Engine Architecture](fig/engine.png)
---

#### Component Overview
1. **User Sketch Input**  
   - **Mesh / Curve Sketches**: Users draw 2D strokes which are captured using pointer events (e.g., mouse or touch input). These are sampled into point sequences and fitted into parametric curves or polygonal outlines.
   - **Mesh Modifiers**: Implemented as functions that operate on vertex buffers and face lists. For example, smoothing is achieved using Laplacian averaging; extrusion is done via face normal offsetting.

2. **Imported Mesh**  
   - Meshes are loaded from `.obj` or `.glb` files using a 3D asset loader (e.g., `three.js`'s `GLTFLoader` or custom parsing logic). The loader reconstructs vertex and face data into a scene graph or internal mesh data structure.

3. **Integrated Mesh**  
   - Acts as the central data store for all mesh operations. Implemented as a shared mesh object that holds vertex arrays, edge connectivity, and attributes like normals, UVs, and colors.
   - Editing operations (e.g., merging sketches and imports) are handled by Boolean mesh operations or vertex-level stitching algorithms.

4. **Exported Mesh**  
   - Once editing is complete, the mesh can be serialized into formats like `.obj`, `.glb`, or `.ply`. This is implemented by traversing the internal mesh structure and writing to disk or memory.

5. **Rendering Config & User Render Modifiers**  
   - Settings like lighting, material type (Phong, Lambertian, etc.), and wireframe/solid mode are defined by the user. These are passed as uniforms and flags to the rendering pipeline (e.g., WebGL shader programs or Three.js renderer configuration).

6. **Canvas**  
   - The render output is drawn to a WebGL canvas or GPU-accelerated framebuffer. A render loop updates the view in real-time using `requestAnimationFrame`, applying the current camera transform, mesh geometry, and lighting model.

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
- Yuqi Meng
- Yiming Xiang
- Yufan Wang
- Yinong He
