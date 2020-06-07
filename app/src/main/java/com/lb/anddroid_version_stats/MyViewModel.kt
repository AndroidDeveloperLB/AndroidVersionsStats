package com.lb.anddroid_version_stats

import android.app.Application
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext!!
    val stateLiveData = MutableLiveData<State>()

    class VersionItem(val versionNickName: String, val version: String, val apiLevel: Int, val marketSharePercentage: Double)

    sealed class State {
        object Loading : State()
        class Success(val isFromInternet: Boolean, val versionItems: List<VersionItem>) : State()
    }

    @UiThread
    fun load() {
        if (stateLiveData.value is State.Success)
            return
        stateLiveData.value = State.Loading
        thread {
            // https://cs.android.com/android/platform/superproject/+/studio-master-dev:tools/adt/idea/android/src/com/android/tools/idea/stats/DistributionService.java
            var root: JsonArray
            var isFromInternet = true
            try {
                HttpURLConnection.setFollowRedirects(true)
                val statsUrl = "https://dl.google.com/android/studio/metadata/distributions.json"
                val url = URL(statsUrl)
                val request: HttpURLConnection = url.openConnection() as HttpURLConnection
                request.connectTimeout = 3000
                request.connect()
                InputStreamReader(request.content as InputStream).use {
                    root = JsonParser.parseReader(it).asJsonArray
                }
            } catch (e: Exception) {
                isFromInternet = false
//                Log.d("AppLog", "error while loading from Internet, so using fallback")
                e.printStackTrace()
                InputStreamReader(context.resources.openRawResource(R.raw.distributions)).use {
                    root = JsonParser.parseReader(it).asJsonArray
                }
            }
//            Log.d("AppLog", "result:")
            val versionItems = ArrayList<VersionItem>()
            root.forEach {
                val androidVersionInfo = it.asJsonObject
                val versionNickName = androidVersionInfo.get("name").asString
                val versionName = androidVersionInfo.get("version").asString
                val versionApiLevel = androidVersionInfo.get("apiLevel").asInt
                val marketSharePercentage = androidVersionInfo.get("distributionPercentage").asDouble
                versionItems.add(VersionItem(versionNickName, versionName, versionApiLevel, marketSharePercentage))
            }
            stateLiveData.postValue(State.Success(isFromInternet, versionItems))
        }
    }
}
