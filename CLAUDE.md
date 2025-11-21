# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

kotlin-voxel is a voxel engine (like Minecraft) written in Kotlin as a programming exercise. It uses libgdx (Java game library) for graphics and input handling.

## Build and Run Commands

**Run the application:**
```bash
./gradlew desktop:run
```

**Generate IntelliJ IDEA project files:**
```bash
./gradlew idea
# Then open the .ipr file with IntelliJ
```

## Project Structure

This is a multi-module Gradle project:
- **core**: Main game logic (Kotlin + Java)
- **desktop**: Desktop launcher (Java)
- **test**: Test code (Kotlin)

## Architecture

### World Management System

The world is managed by a multi-threaded architecture with the primary goal of dynamic chunk loading/unloading based on camera position:

- **World** (gameworld/World.kt): Main world container holding chunks. Manages chunk lifecycle including creation queue, removal of distant chunks, and world dimensions (width x height x depth).

- **WorldUpdater** (gameworld/WorldUpdater.kt): Runs on separate thread. Continuously checks camera position/frustum and queues chunks for creation when they come into view. Uses synchronized blocks to safely communicate with main World thread.

- **Chunk** (gameworld/Chunk.kt): Fixed-size (16x16x16) container for voxel data. Each chunk has a CubeDataGrid for data and a ChunkMesh for rendering. Chunks are pooled for performance (see DebugPool in Main.kt).

- **CubeDataGrid** (cube/CubeDataGrid.java): 3D grid storing voxel data (CubeData instances). Handles world-to-local coordinate translation via origin/center/boundary vectors. Also pooled.

### Rendering Pipeline

- **ChunkMesh** (gameworld/ChunkMesh.kt): Builds vertex data for rendering a chunk. Optimizes by skipping hidden faces (faces adjacent to other solid cubes). Uses shared static vertex array (MAX_VERTS) to minimize allocations.

- **View** (View.kt): Manages camera, shaders, and rendering. Renders all chunks in frustum using libgdx Mesh primitives.

- **Hidden Face Culling**: Adjacent cube faces are marked as hidden during chunk refresh to reduce vertex count. Uses bitwise flags in CubeData.

### Terrain Generation

World elevation is generated using Simplex noise with multiple octaves (see World.NoiseLayerManager). Each chunk applies noise-based elevation to determine which cubes are solid (Grass) vs void.

### Object Pooling

Critical for performance - Chunk and CubeDataGrid instances are pooled (chunkPool, cdgPool in Main.kt) to avoid GC pressure. Use obtain()/free() pattern.

### Mixed Language Codebase

Core performance-critical code like CubeDataGrid is in Java. Game logic and higher-level systems are in Kotlin. When modifying existing files, match the language already in use.

## Key Global Variables

Main.kt defines global lateinit vars for major subsystems (fonts, view, hud, gameInput, assets, world, lights). These are initialized in Main.create().

## Controls

Controls are explained in the in-game HUD. Additional debug keys:
- G: OpenGL debugging info
- V: Reset view

## Dependencies

- Kotlin: 2.1.0
- libgdx: 1.12.1
- JUnit: 4.13.2 (tests)
