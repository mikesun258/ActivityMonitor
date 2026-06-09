package com.mikesun258.activitymonitor

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.callbacks.XC_ZygoteInit

class ActivityMonitorHook : IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private val TAG = "AppMonitor"
    private lateinit var prefs: SharedPreferences

    // ===================== 广播 Action 定义 =====================
    private val BROADCAST_VIDEO_SWITCH = "com.mikesun258.activitymonitor.VIDEO_SWITCH"
    private val BROADCAST_ACTIVITY_CREATE = "com.mikesun258.activitymonitor.ACTIVITY_CREATE"
    private val BROADCAST_ACTIVITY_START = "com.mikesun258.activitymonitor.ACTIVITY_START"
    private val BROADCAST_ACTIVITY_RESUME = "com.mikesun258.activitymonitor.ACTIVITY_RESUME"
    private val BROADCAST_ACTIVITY_PAUSE = "com.mikesun258.activitymonitor.ACTIVITY_PAUSE"
    private val BROADCAST_ACTIVITY_STOP = "com.mikesun258.activitymonitor.ACTIVITY_STOP"

    // 短视频目标包名列表
    private val targetVideoPackages = listOf(
        "com.bytedance.douyin",
        "com.bytedance.douyin.lite",
        "com.bytedance.douyin.extreme",
        "com.bytedance.douyin3",
        "com.bytedance.douyin2",
        "com.bytedance.douyinselected",
        "com.ik.mang",
        "com.ik.shortdrama",
        "com.hippo.drama",
        "com.kuaishou.nebula",
        "com.huolong.mangju",
        "com.kylin.read"
    )

    // 配置键
    private val KEY_ENABLE_ACTIVITY = "enable_activity_monitor"
    private val KEY_ENABLE_VIDEO = "enable_video_monitor"

    override fun initZygote(param: XC_ZygoteInit.ZygoteInitParam) {
        prefs = XposedBridge.getSharedPreferences("activity_monitor_config", Context.MODE_PRIVATE)
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName == "com.mikesun258.activitymonitor") {
            resparam.res.setReplacement("com.mikesun258.activitymonitor", "string", "settings_title", "监控总设置")
            resparam.res.setReplacement("com.mikesun258.activitymonitor", "string", "tip_activity", "开启后监控所有应用 Activity 页面切换")
            resparam.res.setReplacement("com.mikesun258.activitymonitor", "string", "tip_video", "开启后监控短视频 App 滑动切换视频")
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val pkgName = lpparam.packageName
        Log.i(TAG, "📦 加载包: $pkgName")

        // 读取双开关状态，默认全部开启
        val enableActivity = prefs.getBoolean(KEY_ENABLE_ACTIVITY, true)
        val enableVideo = prefs.getBoolean(KEY_ENABLE_VIDEO, true)

        // 按需启用 Activity 监控
        if (enableActivity) {
            hookActivityLifecycle(lpparam)
            Log.d(TAG, "✅ Activity 监控已启用")
        } else {
            Log.d(TAG, "❌ Activity 监控已关闭")
        }

        // 按需启用短视频滑动监控
        if (enableVideo && pkgName in targetVideoPackages) {
            hookVideoRecyclerView(lpparam)
            Log.d(TAG, "✅ 短视频滑动监控已启用")
        } else if (!enableVideo) {
            Log.d(TAG, "❌ 短视频滑动监控已关闭")
        }
    }

    // ===================== Activity 生命周期 Hook =====================
    private fun hookActivityLifecycle(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val instrumentationClass = lpparam.classLoader.loadClass("android.app.Instrumentation")

            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnCreate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendActivityBroadcast(act, BROADCAST_ACTIVITY_CREATE, "onCreate")
                }
            })

            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnStart", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendActivityBroadcast(act, BROADCAST_ACTIVITY_START, "onStart")
                }
            })

            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendActivityBroadcast(act, BROADCAST_ACTIVITY_RESUME, "onResume")
                }
            })

            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnPause", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendActivityBroadcast(act, BROADCAST_ACTIVITY_PAUSE, "onPause")
                }
            })

            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnStop", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendActivityBroadcast(act, BROADCAST_ACTIVITY_STOP, "onStop")
                }
            })

        } catch (e: Throwable) {
            Log.e(TAG, "Activity Hook 失败: ${lpparam.packageName}", e)
        }
    }

    // ===================== 短视频滑动监听（滑动停止才触发） =====================
    private fun hookVideoRecyclerView(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val recyclerViewClass = lpparam.classLoader.loadClass("androidx.recyclerview.widget.RecyclerView")

            XposedBridge.hookAllMethods(recyclerViewClass, "addOnScrollListener", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val originalListener = param.args[0] as RecyclerView.OnScrollListener?
                    val recyclerView = param.thisObject as RecyclerView

                    val hookedListener = object : RecyclerView.OnScrollListener() {
                        private var lastVisiblePosition = -1
                        private var isFirstIdle = true

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val layoutManager = recyclerView.layoutManager
                            if (layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
                                lastVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                            }
                            originalListener?.onScrolled(recyclerView, dx, dy)
                        }

                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                if (isFirstIdle || lastVisiblePosition != -1) {
                                    isFirstIdle = false
                                    sendVideoSwitchBroadcast(recyclerView, lastVisiblePosition)
                                }
                            }
                            originalListener?.onScrollStateChanged(recyclerView, newState)
                        }
                    }
                    recyclerView.addOnScrollListener(hookedListener)
                }
            })

        } catch (e: Throwable) {
            Log.e(TAG, "RecyclerView Hook 失败: ${lpparam.packageName}", e)
        }
    }

    // ===================== 广播发送工具方法 =====================
    private fun sendActivityBroadcast(activity: Activity, action: String, type: String) {
        val pkgName = activity.packageName
        val actName = activity.javaClass.name
        Log.d(TAG, "📱 $type | $pkgName / $actName")

        val intent = Intent(action).apply {
            putExtra("pkg_name", pkgName)
            putExtra("act_name", actName)
            putExtra("event_type", type)
        }
        activity.sendBroadcast(intent)
    }

    private fun sendVideoSwitchBroadcast(view: View, position: Int) {
        val pkgName = view.context.packageName
        Log.d(TAG, "🎬 视频切换 | 包名: $pkgName | 位置: $position")

        val intent = Intent(BROADCAST_VIDEO_SWITCH).apply {
            putExtra("pkg_name", pkgName)
            putExtra("video_position", position)
            putExtra("view_id", view.id)
        }
        view.context.sendBroadcast(intent)
    }
}
