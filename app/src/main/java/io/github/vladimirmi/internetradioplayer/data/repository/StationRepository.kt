package io.github.vladimirmi.internetradioplayer.data.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.11.2018.
 */

class StationRepository
@Inject constructor(private val preferences: Preferences,
                    private val stationParser: StationParser) {

    private val _currentStationObs = BehaviorRelay.create<Station>()
    val stationObs: Observable<Station> get() = _currentStationObs

    var station: Station
        get() = _currentStationObs.value ?: Station.nullObj()
        set(value) {
            _currentStationObs.accept(value)
            saveCurrentStationId(value.id)
        }

    fun createStation(uri: Uri): Single<Station> {
        return Single.fromCallable {
            stationParser.parseFromUri(uri)
        }
    }

    fun saveCurrentStationId(id: String) {
        preferences.currentStationId = id
    }

    fun getCurrentStationId() = preferences.currentStationId


}