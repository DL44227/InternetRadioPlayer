package io.github.vladimirmi.radius.presentation.mediaList

import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class MediaListPresenter
@Inject constructor(private val repository: StationRepository,
                    private val mediaBrowserController: MediaBrowserController,
                    private val router: Router)
    : BasePresenter<MediaListView>() {


    override fun onFirstViewAttach() {
        repository.groupedStationList.observe()
                .subscribeBy { viewState.setMediaList(it) }
                .addTo(compDisp)

        repository.selected
                .subscribeBy { viewState.select(it, mediaBrowserController.isPlaying(it)) }
                .addTo(compDisp)

        mediaBrowserController.playbackState
                .subscribeBy {
                    val station = repository.selected.value ?: return@subscribeBy
                    if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                        viewState.select(station, playing = true)
                    } else {
                        viewState.select(station, playing = false)
                    }
                }.addTo(compDisp)
        initToolbar()
    }

    private fun initToolbar() {
        val builder = ToolbarBuilder()
                .setToolbarTitleId(R.string.app_name)
        viewState.buildToolbar(builder)
    }

    fun select(station: Station) {
        repository.setSelected(station)
    }

    fun selectGroup(group: String) {
        if (repository.groupedStationList.isGroupVisible(group)) {
            repository.groupedStationList.hideGroup(group)
        } else {
            repository.groupedStationList.showGroup(group)
        }
        viewState.notifyList()
    }

    fun addStation(uri: Uri) {
        repository.parseStation(uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { viewState.openAddDialog(it) },
                        onComplete = { viewState.showToast(R.string.toast_add_error) })
                .addTo(compDisp)
    }

    fun addStation(station: Station) {
        if (repository.add(station)) {
            viewState.closeAddDialog()
            viewState.showToast(R.string.toast_add_success)
            select(station)
        } else {
            viewState.showToast(R.string.toast_add_force)
        }
    }

    fun removeStation(station: Station, submit: Boolean = false) {
        viewState.openRemoveDialog(station)
    }

    fun showStation(station: Station) {
        router.navigateTo(Screens.STATION_SCREEN, station)
    }
}


