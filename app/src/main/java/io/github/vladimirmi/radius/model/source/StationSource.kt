package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.net.Uri
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.StationParser
import java.io.File
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationSource
@Inject constructor(context: Context,
                    private val parser: StationParser) {
    companion object {
        const val extension = "json"
    }

    private val appDir = context.getExternalFilesDir(null)

    fun getStationList(): ArrayList<Station> {
        val stationList = ArrayList<Station>()
        val treeWalk = appDir.walkTopDown()
        treeWalk.forEach { file ->
            if (!file.isDirectory && file.extension == extension) {
                parser.parseFromJsonFile(file)?.let {
                    stationList.add(it)
                }
            }
        }
        return stationList
    }

    fun saveStation(station: Station) {
        with(File(appDir, "${station.title}.$extension")) {
            if (exists() || createNewFile()) {
                clear()
                writeText(parser.toJson(station))
            }
        }
    }

    fun removeStation(station: Station) {
        File(appDir, "${station.title}.$extension").delete()
    }

    fun parseStation(uri: Uri): Station? = parser.parseFromUri(uri)
}
