package io.github.vladimirmi.radius.presentation.media

import android.net.Uri
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.github.vladimirmi.radius.model.interactor.media.MediaInteractor
import io.github.vladimirmi.radius.service.MediaBrowserController
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class MediaPresenter
@Inject constructor(private val mediaInteractor: MediaInteractor,
                    private val router: Router,
                    private val mediaBrowserController: MediaBrowserController)
    : MvpPresenter<MediaView>() {

    override fun onFirstViewAttach() {
        viewState.setMediaList(mediaInteractor.getMediaList())
    }

    fun playPause(uri: Uri) {
        if (mediaBrowserController.isPlaying(uri)) {
            mediaBrowserController.stop()
        } else {
            mediaBrowserController.play(uri)
        }
    }

    fun onBackPressed() = router.exit()
}


