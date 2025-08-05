use crate::AppInitOpts;
use crate::android_asset_io::AndroidAssetManager;
use crate::app_view::{AndroidViewObj, NativeWindow};
use android_logger::Config;
use bevy::input::ButtonState;
use bevy::prelude::*;
use jni::JNIEnv;
use jni::objects::JString;
use jni::sys::{jfloat, jint, jlong, jobject, jstring};
use jni_fn::jni_fn;
use log::LevelFilter;

#[link(name = "c++_shared")]
unsafe extern "C" {}

#[unsafe(no_mangle)]
pub fn android_main(_android_app: bevy::window::android_activity::AndroidApp) {
    // This maybe a bevy issue
    // `android_main` empty function is currently required, otherwise, a panic will occur:
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn init_ndk_context(env: JNIEnv, _: jobject, context: jobject) {
    log_panics::init();
    android_logger::init_once(Config::default().with_max_level(LevelFilter::Info));
    let java_vm = env.get_java_vm().unwrap();
    unsafe {
        ndk_context::initialize_android_context(java_vm.get_java_vm_pointer() as _, context as _);
    }
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn create_bevy_app(
    env: *mut jni::sys::JNIEnv,
    _: jobject,
    asset_manager: jobject,
    surface: jobject,
    scale_factor: jfloat,
    opts: jstring,
) -> jlong {
    let mut env = unsafe { JNIEnv::from_raw(env).expect("Invalid JNIEnv pointer") };

    // Convert opts (jstring) into safe JString wrapper
    let jstr = unsafe { JString::from_raw(opts) };

    // Convert Java string to Rust String
    let rust_str: String = env.get_string(&jstr).expect("Failed to get string").into();

    // log::info!("Creating Bevy App with options: {}", rust_str);
    let a_asset_manager =
        unsafe { ndk_sys::AAssetManager_fromJava(env.get_native_interface() as _, asset_manager) };

    let android_obj = AndroidViewObj {
        native_window: NativeWindow::new(env.get_native_interface() as *mut _, surface),
        scale_factor: scale_factor as _,
    };

    log::info!("Creating Bevy App with options: {}", rust_str);
    let state: AppInitOpts = serde_json::from_str(rust_str.as_str()).unwrap();
    log::info!("DEC state {:?}", state);

    let mut bevy_app = crate::create_breakout_app(AndroidAssetManager(a_asset_manager), state);
    bevy_app.insert_non_send_resource(android_obj);
    crate::app_view::create_bevy_window(&mut bevy_app);
    log::info!("Bevy App created!");

    Box::into_raw(Box::new(bevy_app)) as jlong
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn enter_frame(_env: *mut JNIEnv, _: jobject, obj: jlong) {
    let bevy_app = unsafe { &mut *(obj as *mut App) };
    bevy_app.update();
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn device_motion(_env: *mut JNIEnv, _: jobject, obj: jlong, x: jfloat, _y: jfloat, _z: jfloat) {
    let app = unsafe { &mut *(obj as *mut App) };
    let x: f32 = x as _;
    if x < -0.2 {
        crate::change_input(app, KeyCode::ArrowLeft, ButtonState::Released);
        crate::change_input(app, KeyCode::ArrowRight, ButtonState::Pressed);
    } else if x > 0.2 {
        crate::change_input(app, KeyCode::ArrowRight, ButtonState::Released);
        crate::change_input(app, KeyCode::ArrowLeft, ButtonState::Pressed);
    } else {
        crate::change_input(app, KeyCode::ArrowLeft, ButtonState::Released);
        crate::change_input(app, KeyCode::ArrowRight, ButtonState::Released);
    }
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn update_camera_offset(
    _env: *mut JNIEnv,
    _: jobject,
    obj: jlong,
    x: jfloat,
    y: jfloat,
    z: jfloat,
) {
    let app = unsafe { &mut *(obj as *mut App) };
    crate::update_camera(app, Vec3::new(x as f32, y as f32, z as f32));
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn switch_mode(_env: *mut JNIEnv, _: jobject, obj: jlong, x: jint) {
    let app = unsafe { &mut *(obj as *mut App) };
    crate::switch_mode(app, x as u32);
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn device_touch_move(_env: *mut JNIEnv, _: jobject, obj: jlong, x: jfloat, y: jfloat) {
    let app = unsafe { &mut *(obj as *mut App) };
    crate::change_touch(app, Some(vec2(x as f32, y as f32)));
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn device_enter_touch(_env: *mut JNIEnv, _: jobject, obj: jlong, x: jfloat, y: jfloat) {
    let app = unsafe { &mut *(obj as *mut App) };
    crate::touch_enter(app, Vec2::new(x as f32, y as f32));
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn device_exit_touch(_env: *mut JNIEnv, _: jobject, obj: jlong) {
    let app = unsafe { &mut *(obj as *mut App) };
    crate::touch_exit(app);
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn get_mesh(env: JNIEnv, _: jobject, obj: jlong) -> jstring {
    let app = unsafe { &mut *(obj as *mut App) };
    let rust_str = crate::get_current_mesh(app);
    let java_str = env
        .new_string(rust_str.as_str())
        .expect("Couldn't create Java string");
    java_str.into_raw()
}

#[unsafe(no_mangle)]
#[jni_fn("name.renderer.bevy.RustBridge")]
pub fn release_bevy_app(_env: *mut JNIEnv, _: jobject, obj: jlong) {
    let app: Box<App> = unsafe { Box::from_raw(obj as *mut _) };
    crate::close_bevy_window(app);
}
