package io.github.vladimirmi.internetradioplayer.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.recorder.RecorderService
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

private const val RECORDS_DIRECTORY = "records"
private const val RECORD_EXT = "mp3"

class RecordsRepository
@Inject constructor(private val context: Context,
                    private val mediaRepository: MediaRepository) {

    private val recordsDirectory: File by lazy {
        val dir = File(context.getExternalFilesDir(null), RECORDS_DIRECTORY)
        dir.mkdir()
        dir
    }

    private val currentRecording = HashSet<String>()

    val recordsObs by lazy {
        BehaviorRelay.createDefault(initRecords())
    }

    fun startCurrentRecord() {
        val station = mediaRepository.currentMedia as? Station ?: return
        val name = getNewRecordName(station.name)

        val intent = Intent(context, RecorderService::class.java).apply {
            if (currentRecording.contains(station.name)) {
                putExtra(RecorderService.EXTRA_STOP_RECORD, name)
                currentRecording.remove(station.name)
            } else {
                putExtra(RecorderService.EXTRA_START_RECORD, name)
                currentRecording.add(station.name)
            }
            data = station.uri.toUri()
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun createNewRecord(name: String): Record {
        Timber.e("createNewRecord: $name")
        val file = File(recordsDirectory, "$name.$RECORD_EXT")
        return Record.fromFile(file)
    }

    fun commitRecord(record: Record) {
        val newRecord = record.copy(createdAt = System.currentTimeMillis())
        Timber.e("commitRecord: $newRecord")
        val list = (recordsObs.value ?: emptyList()) + newRecord
        recordsObs.accept(list)
    }

    fun deleteRecord(record: Record) {
        Timber.e("deleteRecord: $record")
    }

    private fun initRecords(): List<Record> {
        return recordsDirectory
                .listFiles { pathname -> pathname.extension == RECORD_EXT }
                .map { Record.fromFile(it) }
    }

    private fun getNewRecordName(stationName: String): String {
        val regex = "^$stationName(_\\d)?".toRegex()
        val list = recordsObs.value?.filter { it.name.matches(regex) } ?: emptyList()
        return if (list.isEmpty()) stationName
        else "${stationName}_${list.size}"
    }
}
