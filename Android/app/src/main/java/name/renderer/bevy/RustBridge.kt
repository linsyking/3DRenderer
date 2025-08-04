package name.renderer.bevy

import android.view.Surface
import android.content.Context
import android.content.res.AssetManager


object RustBridge {
    init {
        System.loadLibrary("bevy_in_app")
    }

    external fun init_ndk_context(ctx: Context)
    external fun create_bevy_app(asset_manager: AssetManager, surface: Surface, scale_factor: Float, bg_r: Float, bg_g: Float, bg_b: Float, l_r: Float, l_g: Float, l_b: Float, ms : Float): Long
    external fun enter_frame(bevy_app: Long)
    external fun device_motion(bevy_app: Long, x: Float, y: Float, z: Float)
    external fun device_enter_touch(bevy_app: Long, x: Float, y: Float)
    external fun device_touch_move(bevy_app: Long, x: Float, y: Float)
    external fun device_exit_touch(bevy_app: Long)
    external fun release_bevy_app(bevy_app: Long)
}
