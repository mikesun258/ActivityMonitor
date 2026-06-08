package com.mikesun258.activitymonitor

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitorHook : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"

    init {
        // 模块被加载时就输出日志
        Log.i(TAG, "✅ 测试模块已被 LSPosed 加载！")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 什么都不做，只记录被 Hook 的包名
        Log.i(TAG, "📦 正在 Hook 包: ${lpparam.packageName}")
    }
}
