# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SpeakerYou is a MicYou plugin that enables using a mobile phone as a computer speaker. Audio streams from the phone to desktop via WiFi, Bluetooth, or USB connection.

## Build Commands

```bash
# Windows - package plugin
./gradlew.bat packagePlugin

# Output location
# build/distributions/SpeakerYou.micyou-plugin.zip
```

The plugin package includes:
- `plugin.json` - plugin metadata
- `plugin.jar` - compiled classes
- `lib/` - runtime dependencies

## Architecture

**Plugin System**: The main class `SpeakerPlugin` implements three MicYou plugin interfaces:
- `Plugin` - lifecycle management (onLoad, onEnable, onDisable, onUnload)
- `PluginUIProvider` - provides Compose UI via `MainWindow()` and `SettingsContent()`
- `PluginSettingsProvider` - settings integration

**Plugin API**: The `libs/plugin-api-jvm-1.0.0.jar` is a compile-only dependency providing:
- `PluginContext` - access to settings storage, logging, and host interface
- `PluginHost` - streaming control (`startStream`, `stopStream`) and UI utilities (`showSnackbar`)
- `StreamState` - state flow (Idle, Connecting, Streaming, Error)
- `ConnectionMode` - enum (Wifi, Bluetooth, Usb)

**UI**: Built with JetBrains Compose (Material3). The main window provides connection mode selection, IP/port configuration, and streaming controls.

## Key Files

- `src/main/kotlin/com/wongwingchun/speakeryou/SpeakerPlugin.kt` - main plugin implementation
- `src/main/resources/plugin.json` - plugin metadata (must match manifest in code)
- `libs/plugin-api-jvm-1.0.0.jar` - MicYou plugin API (compile-only)

## Notes

- Project is WIP (development stage) - README notes not to use it yet
- Uses Kotlin 2.2.20 with Compose 1.7.3
- Settings are persisted via `PluginContext` (getString/putString, getInt/putInt, getBoolean/putBoolean)