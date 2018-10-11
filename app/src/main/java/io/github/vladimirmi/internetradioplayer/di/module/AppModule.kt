package io.github.vladimirmi.internetradioplayer.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.data.service.LoadControl
import io.github.vladimirmi.internetradioplayer.data.source.StationSource
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)

        val gson = GsonBuilder().setPrettyPrinting().create()
        bind(Gson::class.java).toInstance(gson)

        val db = StationsDatabase.newInstance(context)
        bind(StationsDatabase::class.java).toInstance(db)

        bind(StationParser::class.java).singletonInScope()

        bind(ShortcutHelper::class.java).singletonInScope()

        bind(StationSource::class.java).singletonInScope()

        bind(StationListRepository::class.java).singletonInScope()

        bind(StationInteractor::class.java).singletonInScope()

        bind(PlayerControlsInteractor::class.java).singletonInScope()

        bind(LoadControl::class.java).singletonInScope()
    }
}
