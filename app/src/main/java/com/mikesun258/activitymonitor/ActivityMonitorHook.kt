package com.mikesun258.activitymonitor

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitorHook : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"

    // 模块被加载时立刻输出日志
    init {
        Log.i(TAG, "✅ 模块已被 LSPosed 加载！")
    }

    // Hook 所有应用时都会调用这个方法
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Log.i(TAG, "📦 正在 Hook 包: ${lpparam.packageName}")
    }
}
