#[cfg(any(target_os = "android", target_os = "ios"))]
fn main() {}

#[cfg(not(any(target_os = "android", target_os = "ios")))]
fn main() {
    use bevy::color::Color;

    let mut bevy_app = bevy_in_app::create_breakout_app(Color::WHITE, Color::WHITE);
    bevy_app.run();
}
