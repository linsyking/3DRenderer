#[cfg(any(target_os = "android", target_os = "ios"))]
fn main() {}

#[cfg(not(any(target_os = "android", target_os = "ios")))]
fn main() {
    use bevy::color::Color;
    use bevy_in_app::{AppInitOpts, SceneConfig};

    let mut bevy_app = bevy_in_app::create_breakout_app(AppInitOpts {
        background_color: vec![0.1, 0.1, 0.1],
        light_color: vec![1.0, 1.0, 1.0],
        move_strength: 0.01,
        scene: SceneConfig {
            objects: vec![],
            camera_pos: vec![0.0, 0.0, 0.0],
        },
    });
    bevy_app.run();
}
