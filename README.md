# 441 Renderer

## **Android**

### Set up Android environment

Assuming your computer already has Android Studio installed, go to `Android Studio` > `Tools` > `SDK Manager` > `Android SDK` > `SDK Tools`. Check the following options for installation and click OK.

- [x] Android SDK Build-Tools 36.0.0
- [x] Android SDK Command-line Tools
- [x] NDK(Side by side) v29.0.13599879

### Add build targets

```sh
rustup target add aarch64-linux-android
```

### Build

```sh
# Install cargo-so subcommand for the first time build
cargo install cargo-so

# Build
sh ./android_build.sh --release
```
