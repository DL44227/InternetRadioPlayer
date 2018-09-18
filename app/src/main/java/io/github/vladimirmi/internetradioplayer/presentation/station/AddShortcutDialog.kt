package io.github.vladimirmi.internetradioplayer.presentation.station

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_add_shortcut.view.*

/**
 * Created by Vladimir Mikhalev 18.09.2018.
 */

class AddShortcutDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.menu_station_shortcut)
    }

    @SuppressLint("InflateParams")
    override fun getCustomDialogView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.dialog_add_shortcut, null)
    }

    override fun onPositive() {
        (parentFragment as StationFragment).presenter.addShortcut(dialogView!!.checkbox.isChecked)
    }

    override fun onNegative() {
    }
}
