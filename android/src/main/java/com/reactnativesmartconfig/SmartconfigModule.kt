package com.reactnativesmartconfig

import android.os.AsyncTask
import android.util.Log
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.IEsptouchResult
import com.espressif.iot.esptouch.IEsptouchTask
import com.facebook.react.bridge.*


class SmartconfigModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private companion object {
    val TAG = "SmartconfigModule"
  }

  private var mEsptouchTask: IEsptouchTask? = null

  override fun getName(): String {
    return "Smartconfig"
  }

  @ReactMethod
  fun stop() {
    if (mEsptouchTask != null) {
      mEsptouchTask!!.interrupt()
      Log.d(TAG, "cancel task")
    }
  }

  @ReactMethod
  fun start(options: ReadableMap, promise: Promise) {
    val ssid = options.getString("ssid")
    val pass = options.getString("password")
    val hidden = false
    Log.d(TAG, "ssid $ssid | pass $pass")
    stop()
    EsptouchAsyncTask(object : TaskListener {
      override fun onFinished(result: List<IEsptouchResult>?) {
        // Do Something after the task has finished
        val ret = Arguments.createArray()
        var resolved = false
        if (result != null) {
          for (resultInList in result) {
            if (!resultInList.isCancelled && resultInList.bssid != null) {
              val map = Arguments.createMap()
              map.putString("bssid", resultInList.bssid)
              map.putString("ipv4", resultInList.inetAddress.hostAddress)
              ret.pushMap(map)
              resolved = true
              if (!resultInList.isSuc) break
            }
          }
        }
        if (resolved) {
          Log.d(TAG, "Success run smartconfig")
          promise.resolve(ret)
        } else {
          Log.d(TAG, "Error run smartconfig")
          promise.reject("new IllegalViewOperationException()")
        }
      }

    }).execute(ssid, "", pass, "YES", "1")
  }


  interface TaskListener {
    fun onFinished(result: List<IEsptouchResult>?)
  }

  inner class EsptouchAsyncTask(val taskListener: TaskListener) : AsyncTask<String?, Void?, List<IEsptouchResult>>() {

    private val mLock = Any()

    override fun doInBackground(vararg params: String?): List<IEsptouchResult> {
      Log.d(TAG, "doing task")
      var taskResultCount = -1
      synchronized(mLock) {
        val apSsid = params[0]
        val apBssid = params[1]
        val apPassword = params[2]
        val isSsidHiddenStr = params[3]
        val taskResultCountStr = params[4]
        var isSsidHidden = false
        if (isSsidHiddenStr == "YES") {
          isSsidHidden = true
        }
        if (taskResultCountStr != null) {
          taskResultCount = taskResultCountStr.toInt()
        }
        mEsptouchTask = EsptouchTask(apSsid, apBssid, apPassword,
          isSsidHidden, currentActivity)
      }
      return mEsptouchTask!!.executeForResults(taskResultCount)
    }

    override fun onPostExecute(result: List<IEsptouchResult>) {
      val firstResult = result[0]
      // check whether the task is cancelled and no results received
      if (!firstResult.isCancelled) {
        if (taskListener != null) {
          // And if it is we call the callback function on it.
          taskListener.onFinished(result)
        }
      }
    }


//    private val taskListener: TaskListener?) : AsyncTask<String?, Void?, List<IEsptouchResult>>() {

    // without the lock, if the user tap confirm and cancel quickly enough,
    // the bug will arise. the reason is follows:
    // 0. task is starting created, but not finished
    // 1. the task is cancel for the task hasn't been created, it do nothing
    // 2. task is created
    // 3. Oops, the task should be cancelled, but it is running
//    private val mLock = Any()
//    override fun onPreExecute() {
//      Log.d(TAG, "Begin task")
//    }
//
//    protected override fun doInBackground(vararg params: String): List<IEsptouchResult> {
//      Log.d(TAG, "doing task")
//      var taskResultCount = -1
//      synchronized(mLock) {
//        val apSsid = params[0]
//        val apBssid = params[1]
//        val apPassword = params[2]
//        val isSsidHiddenStr = params[3]
//        val taskResultCountStr = params[4]
//        var isSsidHidden = false
//        if (isSsidHiddenStr == "YES") {
//          isSsidHidden = true
//        }
//        taskResultCount = taskResultCountStr.toInt()
//        mEsptouchTask = EsptouchTask(apSsid, apBssid, apPassword,
//          isSsidHidden, getCurrentActivity())
//      }
//      return mEsptouchTask.executeForResults(taskResultCount)
//    }
//
//    override fun onPostExecute(result: List<IEsptouchResult>) {
//      val firstResult = result[0]
//      // check whether the task is cancelled and no results received
//      if (!firstResult.isCancelled) {
//        if (taskListener != null) {
//
//          // And if it is we call the callback function on it.
//          taskListener.onFinished(result)
//        }
//      }
//    }

  }
}
