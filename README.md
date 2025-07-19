# 441 Renderer

## **Android**

### Set up Android environment

Assuming your computer already has Android Studio installed, go to `Android Studio` > `Tools` > `SDK Manager` > `Android SDK` > `SDK Tools`. Check the following options for installation and click OK.

- [x] Android SDK Build-Tools
- [x] Android SDK Command-line Tools
- [x] NDK(Side by side)

### Add build targets

```sh
rustup target add aarch64-linux-android
```

### Build

```sh
# Install cargo-so subcommand
cargo install cargo-so

# Build
sh ./android_build.sh --release
```
