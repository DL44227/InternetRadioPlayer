package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseViewGroup
import kotlinx.android.synthetic.main.view_favorite_stations.view.*
import timber.log.Timber
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class FavoriteStationsViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseViewGroup<FavoriteStationsPresenter, FavoriteStationsView>(context, attrs, defStyleAttr),
        FavoriteStationsView, StationItemCallback {

    private val stationListAdapter by lazy { StationListAdapter(this) }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(object : ItemSwipeCallback() {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                stationListAdapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onStartDrag(position: Int) {
                stationListAdapter.onStartDrag(position)
            }

            override fun onIdle() {
                presenter.moveGroupElements(stationListAdapter.onIdle())
            }
        })
    }

    override fun providePresenter(): FavoriteStationsPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(FavoriteStationsPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {
        stationsRv.layoutManager = LinearLayoutManager(context!!)
        stationsRv.adapter = stationListAdapter
//        itemTouchHelper.attachToRecyclerView(stationsRv)
    }

    fun onContextItemSelected(item: MenuItem): Boolean {
        Timber.e("onContextItemSelected: $item ${stationListAdapter.longClickedItem}")
        return true
    }

    //region =============== StationListView ==============

    override fun setStations(stationList: FlatStationsList) {
        stationListAdapter.setData(stationList)
    }

    override fun selectStation(station: Station) {
        stationListAdapter.selectStation(station)
        val position = stationListAdapter.getPosition(station)
        if (position != -1) stationsRv.scrollToPosition(position)
    }

    //todo refactor
//    override fun showControls(visibility: Float) {
//        val pb = ((48 * (1 - visibility) + 16) * context!!.dp).toInt()
//        stationsRv.setPadding(0, stationsRv.paddingTop, 0, pb)
//    }

    override fun showPlaceholder(show: Boolean) {
        stationsRv.visible(!show)
        placeholderView.visible(show)
    }

    //endregion

    //region =============== StationItemCallback ==============

    override fun onGroupSelected(id: String) {
        presenter.selectGroup(id)
    }

    override fun onItemSelected(station: Station) {
        presenter.selectStation(station)
    }

    override fun onGroupRemove(id: String) {
        presenter.removeGroup(id)
    }

    //endregion
}