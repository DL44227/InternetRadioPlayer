package io.github.vladimirmi.internetradioplayer.presentation.metadata

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.artist
import io.github.vladimirmi.internetradioplayer.data.service.title
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

@InjectViewState
class MetadataPresenter
@Inject constructor(private val controlsInteractor: PlayerControlsInteractor)
    : BasePresenter<MetadataView>() {

    override fun onFirstViewAttach() {
        controlsInteractor.playbackMetaData
                .subscribeBy { handleMeta(it) }
                .addTo(compDisp)

        controlsInteractor.playbackStateObs
                .subscribeBy { handleState(it) }
                .addTo(compDisp)
    }

    private fun handleMeta(metadata: MediaMetadataCompat) {
        viewState.showMetadata()
        viewState.setMetadata("${metadata.artist} - ${metadata.title}")
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_BUFFERING -> {
                viewState.showMetadata()
                viewState.setMetadata(R.string.metadata_buffering)
            }
            PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_STOPPED -> viewState.hideMetadata()
        }
    }
}
