package io.github.vladimirmi.internetradioplayer.presentation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.manager.BACKUP_TYPE
import io.github.vladimirmi.internetradioplayer.data.manager.BackupRestoreHelper
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.startActivitySafe
import io.github.vladimirmi.internetradioplayer.ui.SeekBarDialogPreference
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 30.09.2018.
 */

private const val PICK_BACKUP_REQUEST_CODE = 999

class SettingsFragment : PreferenceFragmentCompat() {

    private val backupRestoreHelper = Scopes.app.getInstance(BackupRestoreHelper::class.java)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_screen)

        findPreference("BACKUP_STATIONS").setOnPreferenceClickListener {
            val uri = backupRestoreHelper.createBackup()
            val intent = ShareCompat.IntentBuilder.from(activity)
                    .setType(BACKUP_TYPE)
                    .setSubject(getString(R.string.full_app_name))
                    .setStream(uri)
                    .setChooserTitle(getString(R.string.chooser_backup))
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context!!.startActivitySafe(intent)
            true
        }
        findPreference("RESTORE_STATIONS").setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = BACKUP_TYPE
            if (context!!.packageManager.resolveActivity(intent, 0) != null) {
                startActivityForResult(intent, PICK_BACKUP_REQUEST_CODE)
            }
            true
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is SeekBarDialogPreference) {
            val fragment = SeekBarDialogFragment.newInstance(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(fragmentManager, "SeekBarDialogFragment")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK
                && data?.data != null) {
            backupRestoreHelper.restoreBackup(context!!.contentResolver.openInputStream(data.data))
                    .ioToMain()
                    .subscribeBy(onError = { Timber.e(it) })
        }
    }
}
