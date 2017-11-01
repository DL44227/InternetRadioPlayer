package io.github.vladimirmi.radius.model.entity

import android.net.Uri

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

data class Media(val title: String,
                 val uri: Uri,
                 val dirName: String,
                 val path: String,
                 val fav: Boolean = false)

