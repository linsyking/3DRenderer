use bevy::prelude::*;
use serde::Deserialize;

#[cfg(any(target_os = "android", target_os = "ios"))]
use bevy::ecs::{
    entity::Entity,
    system::{Commands, Query, SystemState},
};
#[cfg(any(target_os = "android", target_os = "ios"))]
use bevy::input::{
    ButtonState,
    keyboard::{Key, KeyboardInput},
};

#[cfg(any(target_os = "android", target_os = "ios"))]
mod app_view;

#[cfg(any(target_os = "android", target_os = "ios"))]
mod ffi;
#[cfg(any(target_os = "android", target_os = "ios"))]
pub use ffi::*;

use crate::scene3d::{LastTouchInput, MeshConfig, TouchInput};

// #[cfg(target_os = "android")]
mod android_asset_io;

mod breakout_game;
mod lighting_demo;
mod scene3d;
mod shapes_demo;
mod stepping;

mod file_io;
mod geometry;

#[derive(Deserialize, Debug)]
struct ObjConfig {
    data: String,
    #[serde(rename = "type")]
    objtype: String,
    label: String,
    #[serde(default)]
    color: Vec<f32>,
    #[serde(default)]
    pos: Vec<f32>,
    scale: f32,
}

#[derive(Deserialize, Debug)]
struct SceneConfig {
    #[serde(default)]
    objects: Vec<ObjConfig>,

    #[serde(default, rename = "cameraPos")]
    camera_pos: Vec<f32>,
}

#[derive(Deserialize, Debug)]
struct AppInitOpts {
    #[serde(rename = "backgroundColor")]
    background_color: Vec<f32>,

    #[serde(rename = "environmentLightColor")]
    light_color: Vec<f32>,

    #[serde(rename = "moveStrength")]
    move_strength: f32,

    scene: SceneConfig,
}

#[allow(unused_variables)]
pub fn create_breakout_app(
    #[cfg(target_os = "android")] android_asset_manager: android_asset_io::AndroidAssetManager,
    opts: AppInitOpts,
) -> App {
    let bg = &opts.background_color;
    let bg_color = Color::srgb(bg[0], bg[1], bg[2]);
    #[allow(unused_imports)]
    use bevy::winit::WinitPlugin;

    let mut bevy_app = App::new();

    #[allow(unused_mut)]
    let mut default_plugins = DefaultPlugins.build();

    #[cfg(any(target_os = "android", target_os = "ios"))]
    {
        default_plugins = default_plugins
            .disable::<WinitPlugin>()
            .set(WindowPlugin::default());
    }

    #[cfg(target_os = "android")]
    {
        bevy_app.insert_non_send_resource(android_asset_manager);

        use bevy::render::{
            RenderPlugin,
            settings::{RenderCreation, WgpuSettings},
        };
        default_plugins = default_plugins.set(RenderPlugin {
            render_creation: RenderCreation::Automatic(WgpuSettings {
                backends: Some(wgpu::Backends::VULKAN),
                ..default()
            }),
            ..default()
        });

        // the custom asset io plugin must be inserted in-between the
        // `CorePlugin' and `AssetPlugin`. It needs to be after the
        // CorePlugin, so that the IO task pool has already been constructed.
        // And it must be before the `AssetPlugin` so that the asset plugin
        // doesn't create another instance of an asset server. In general,
        // the AssetPlugin should still run so that other aspects of the
        // asset system are initialized correctly.
        //
        // 2023/11/04, Bevy v0.12:
        // In the Android, Bevy's AssetPlugin relies on winit, which we are not using.
        // If a custom AssetPlugin plugin is not provided,  it will crash at runtime:
        // thread '<unnamed>' panicked at 'Bevy must be setup with the #[bevy_main] macro on Android'
        default_plugins = default_plugins
            .add_before::<bevy::asset::AssetPlugin>(android_asset_io::AndroidAssetIoPlugin);
    }
    bevy_app
        .insert_resource(ClearColor(bg_color))
        .add_plugins(default_plugins);

    #[cfg(any(target_os = "android", target_os = "ios"))]
    bevy_app.add_plugins(app_view::AppViewPlugin);

    // bevy_app.add_plugins(breakout_game::BreakoutGamePlugin);
    // bevy_app.add_plugins(lighting_demo::LightingDemoPlugin);
    bevy_app.add_plugins(to_plugin_opts(opts));
    // bevy_app.add_plugins(shapes_demo::ShapesDemoPlugin);

    // In this scenario, need to call the setup() of the plugins that have been registered
    // in the App manually.
    // https://github.com/bevyengine/bevy/issues/7576
    // bevy 0.11 changed: https://github.com/bevyengine/bevy/pull/8336
    #[cfg(any(target_os = "android", target_os = "ios"))]
    {
        use bevy::app::PluginsState;
        if bevy_app.plugins_state() == PluginsState::Ready {}
        bevy_app.finish();
        bevy_app.cleanup();
    }

    bevy_app
}

pub(crate) fn to_plugin_opts(opts: AppInitOpts) -> scene3d::Scene3DPlugin {
    let mut meshes = vec![];
    for mesh_config in opts.scene.objects {
        let mesh_data = mesh_config.data;
        // log::info!("Importing mesh data: {}", mesh_data);
        let res = file_io::load_obj(mesh_data).unwrap();
        let mymesh = file_io::to_bevy_mesh(&res);
        let mm = MeshConfig {
            mesh: mymesh,
            transform: Transform::from_xyz(
                mesh_config.pos[0],
                mesh_config.pos[1],
                mesh_config.pos[2],
            )
            .with_scale(Vec3::splat(mesh_config.scale)),
            color: Color::srgb(mesh_config.color[0], mesh_config.color[1], mesh_config.color[2]),
        };
        meshes.push(mm);
    }

    let light = opts.light_color;
    let env_lightcolor = Color::srgb(light[0], light[1], light[2]);
    let move_strength = opts.move_strength;

    scene3d::Scene3DPlugin {
        env_lightcolor: env_lightcolor,
        move_strength: move_strength,
        meshes: meshes,
        camera_pos: opts.scene.camera_pos,
    }
}

pub(crate) fn change_touch(app: &mut App, pos: Option<Vec2>) {
    let mut touch_input = app.world_mut().resource_mut::<TouchInput>();
    touch_input.touch = pos;
}

pub(crate) fn change_last_touch(app: &mut App, pos: Option<Vec2>) {
    let mut touch_input = app.world_mut().resource_mut::<LastTouchInput>();
    touch_input.touch = pos;
}

#[cfg(any(target_os = "android", target_os = "ios"))]
pub(crate) fn change_input(app: &mut App, key_code: KeyCode, state: ButtonState) {
    let mut windows_system_state: SystemState<Query<(Entity, &mut Window)>> =
        SystemState::from_world(app.world_mut());
    let windows = windows_system_state.get_mut(app.world_mut());
    if let Ok((entity, _)) = windows.single() {
        let input = KeyboardInput {
            logical_key: if key_code == KeyCode::ArrowLeft {
                Key::ArrowLeft
            } else {
                Key::ArrowRight
            },
            state,
            key_code: key_code,
            window: entity,
            repeat: false,
            text: None,
        };
        app.world_mut().send_event(input);
    }
}

#[cfg(any(target_os = "android", target_os = "ios"))]
#[allow(clippy::type_complexity)]
pub(crate) fn close_bevy_window(mut app: Box<App>) {
    let mut windows_state: SystemState<(
        Commands,
        Query<(Entity, &mut Window)>,
        EventWriter<AppExit>,
    )> = SystemState::from_world(app.world_mut());
    let (mut commands, windows, mut app_exit_events) = windows_state.get_mut(app.world_mut());
    for (window, _focus) in windows.iter() {
        commands.entity(window).despawn();
    }
    app_exit_events.write(AppExit::Success);
    windows_state.apply(app.world_mut());

    app.update();
}
