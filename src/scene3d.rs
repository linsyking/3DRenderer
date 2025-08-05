//! A simple 3D scene with light shining over a cube sitting on a plane.

use bevy::prelude::*;


#[derive(Resource, Default)]
pub struct TouchInput {
    pub touch: Option<Vec2>,
}

#[derive(Resource, Default)]
pub struct LastTouchInput {
    pub touch: Option<Vec2>,
}

#[derive(Resource)]
struct OrbitCamera {
    azimuth: f32,   // Horizontal angle
    elevation: f32, // Vertical angle
    radius: f32,    // Distance from the target
}

#[derive(Resource)]
pub struct MyPluginConfig {
    pub env_lightcolor: Color,
    pub move_strength: f32,
}

pub struct Scene3DPlugin {
    pub env_lightcolor: Color,
    pub move_strength: f32,
    pub meshes: Vec<Mesh>,
}

impl Plugin for Scene3DPlugin {
    fn build(&self, app: &mut App) {
        app.insert_resource(TouchInput::default())
            .insert_resource(LastTouchInput::default())
            .insert_resource(MyPluginConfig {
                env_lightcolor: self.env_lightcolor,
                move_strength: self.move_strength,
            })
            .insert_resource(OrbitCamera {
                azimuth: 0.0,
                elevation: 0.0,
                radius: 10.0,
            })
            .add_systems(Startup, setup)
            .add_systems(Update, move_camera);
    }
}

/// set up a simple 3D scene
fn setup(
    mut commands: Commands,
    mut meshes: ResMut<Assets<Mesh>>,
    mut materials: ResMut<Assets<StandardMaterial>>,
    config: Res<MyPluginConfig>,
) {
    // cube
    commands.spawn((
        Mesh3d(meshes.add(Cuboid::new(1.0, 1.0, 1.0))),
        MeshMaterial3d(materials.add(Color::srgb_u8(124, 144, 255))),
        Transform::from_xyz(0.0, 0.0, 0.0),
    ));
    commands.spawn((
        Mesh3d(meshes.add(Plane3d::default().mesh().size(100.0, 100.0))),
        MeshMaterial3d(materials.add(Color::srgb_u8(200, 200, 200))),
        Transform::from_xyz(0.0, -1.0, 0.0),
    ));
    // light
    commands.spawn((
        PointLight {
            shadows_enabled: true,
            color: config.env_lightcolor,
            ..default()
        },
        Transform::from_xyz(0.0, 8.0, 0.0),
    ));
    commands.spawn((
        Camera3d::default(),
        Transform::from_xyz(-2.5, 4.5, 9.0).looking_at(Vec3::ZERO, Vec3::Y),
    ));
}

fn move_camera(
    mut query: Query<&mut Transform, With<Camera3d>>,
    input: Res<TouchInput>,
    mut lastinput: ResMut<LastTouchInput>,
    mut orbit: ResMut<OrbitCamera>,
    config: Res<MyPluginConfig>,
) {
    if let Some(cpos) = input.touch {
        if let Some(lastpos) = lastinput.touch {
            let delta = cpos - lastpos;
            for mut transform in &mut query {
                let rotate_speed = config.move_strength;

                orbit.azimuth -= delta.x * rotate_speed;
                orbit.elevation += delta.y * rotate_speed;

                // Clamp elevation to avoid flipping
                orbit.elevation = orbit.elevation.clamp(
                    -std::f32::consts::FRAC_PI_2 + 0.01,
                    std::f32::consts::FRAC_PI_2 - 0.01,
                );

                // Convert spherical coordinates to cartesian
                let x = orbit.radius * orbit.elevation.cos() * orbit.azimuth.sin();
                let y = orbit.radius * orbit.elevation.sin();
                let z = orbit.radius * orbit.elevation.cos() * orbit.azimuth.cos();

                let position = Vec3::new(x, y, z);
                let target = Vec3::ZERO;
                transform.translation = position;
                transform.look_at(target, Vec3::Y);
            }
        }
        // Update last touch input
        lastinput.touch = Some(cpos);
    }
}
