//! A simple 3D scene with light shining over a cube sitting on a plane.

use bevy::prelude::*;

use crate::{
    file_io::to_bevy_mesh,
    geometry::{curvify, meshify},
};

#[derive(Resource, Default)]
pub struct TouchInput {
    pub touch: Option<Vec2>,
}

#[derive(Resource, Default)]
pub struct LastTouchInput {
    pub touch: Option<Vec2>,
}

#[derive(Debug, Clone)]
pub struct MeshConfig {
    pub mesh: Mesh,
    pub transform: Transform,
    pub color: Color,
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
    pub meshes: Vec<MeshConfig>,
    pub camera_pos: Vec<f32>,
    pub sketch: bool, // Whether to use sketch mode
}

pub struct Scene3DPlugin {
    pub env_lightcolor: Color,
    pub move_strength: f32,
    pub meshes: Vec<MeshConfig>,
    pub camera_pos: Vec<f32>,
}

impl Plugin for Scene3DPlugin {
    fn build(&self, app: &mut App) {
        app.insert_resource(TouchInput::default())
            .insert_resource(LastTouchInput::default())
            .insert_resource(MyPluginConfig {
                env_lightcolor: self.env_lightcolor,
                move_strength: self.move_strength,
                meshes: self.meshes.clone(),
                camera_pos: self.camera_pos.clone(),
                sketch: true,
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
    // commands.spawn((
    //     Mesh3d(meshes.add(Cuboid::new(1.0, 1.0, 1.0))),
    //     MeshMaterial3d(materials.add(Color::srgb_u8(124, 144, 255))),
    //     Transform::from_xyz(0.0, 0.0, 0.0),
    // ));
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
        Transform::from_xyz(0.0, 0.0, 10.0).looking_at(Vec3::ZERO, Vec3::Y),
    ));

    // Spawn meshes from the config
    for meshconfig in &config.meshes {
        // log::info!("Spawning mesh: {:?}", mesh);
        let mesh_handle = meshes.add(meshconfig.mesh.clone());
        commands.spawn((
            Mesh3d(mesh_handle),
            MeshMaterial3d(materials.add(meshconfig.color)),
            meshconfig.transform.clone(),
        ));
    }

    // Test curve mesh
    let ps = vec![glam::Vec3::new(-2., 0.0, 0.0), glam::Vec3::new(2., 4., 0.0)];
    let cc = curvify(&ps);
    let mm = meshify(&cc, glam::Vec3::new(-2.5, 4.5, 9.0));
    let rmm = to_bevy_mesh(&mm);

    commands.spawn((
        Mesh3d(meshes.add(rmm)),
        MeshMaterial3d(materials.add(Color::srgb_u8(255, 0, 0))),
        Transform::from_xyz(0.0, 0.0, 0.0),
    ));
}

fn move_camera(
    mut commands: Commands,
    mut query: Query<&mut Transform, With<Camera3d>>,
    cameras: Query<(&Camera, &GlobalTransform)>,
    input: Res<TouchInput>,
    mut lastinput: ResMut<LastTouchInput>,
    mut orbit: ResMut<OrbitCamera>,
    config: Res<MyPluginConfig>,
    mut meshes: ResMut<Assets<Mesh>>,
    mut materials: ResMut<Assets<StandardMaterial>>,
    windows: Query<&Window>,
) {
    if config.sketch {
        // If sketch mode is enabled, we don't move the camera

        if let Some(cpos) = input.touch {
            if let Some(lastpos) = lastinput.touch {
                let delta = cpos - lastpos;
            } else {
                // Fisrt time
                let window = windows.single().unwrap();
                let (camera, camera_transform) = cameras.single().unwrap();
                let ccpos = cpos / window.scale_factor();

                if let Ok(ray) = camera.viewport_to_world(camera_transform, ccpos) {
                    let cam_pos = camera_transform.translation();
                    let cam_forward = camera_transform.forward() * 10.0; // +Z in Bevy

                    // Plane 1 unit in front of camera
                    let plane_origin = cam_pos + cam_forward;
                    let plane_normal = cam_forward;

                    let ray_origin = ray.origin;
                    let ray_dir = ray.direction;

                    // Ray-plane intersection: (P - plane_origin) ⋅ n = 0
                    // Solve for t: (O + tD - plane_origin) ⋅ n = 0
                    let denom = ray_dir.dot(plane_normal);
                    let mut mshp = Sphere::default();
                    mshp.radius = 0.5;
                    if denom.abs() > f32::EPSILON {
                        let t = (plane_origin - ray_origin).dot(plane_normal) / denom;
                        if t >= 0.0 {
                            let intersection = ray_origin + ray_dir * t;
                            commands.spawn((
                                Mesh3d(meshes.add(Mesh::from(mshp.mesh().ico(5).unwrap()))),
                                MeshMaterial3d(materials.add(Color::srgb_u8(200, 200, 200))),
                                Transform::from_translation(intersection),
                            ));
                            log::info!("Hit point 1 unit in front of camera: {:?}", intersection);
                        } else {
                            log::info!("Intersection behind the camera");
                        }
                    } else {
                        log::info!("Ray is parallel to the plane");
                    }
                }
            }
            lastinput.touch = Some(cpos);
        }
        return;
    } else {
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
                    let x = orbit.radius * orbit.elevation.cos() * orbit.azimuth.sin()
                        + config.camera_pos[0];
                    let y = orbit.radius * orbit.elevation.sin() + config.camera_pos[1];
                    let z = orbit.radius * orbit.elevation.cos() * orbit.azimuth.cos()
                        + config.camera_pos[2];

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
}
