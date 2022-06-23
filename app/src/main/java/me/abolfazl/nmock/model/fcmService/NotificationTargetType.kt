package me.abolfazl.nmock.model.fcmService

import androidx.annotation.StringDef

const val TARGET_HOME_FRAGMENT = "HOME_FRAGMENT"
const val TARGET_EDIT_FRAGMENT = "EDIT_FRAGMENT"
const val TARGET_ARCHIVE_FRAGMENT = "ARCHIVE_FRAGMENT"

@StringDef(
    TARGET_HOME_FRAGMENT,
    TARGET_EDIT_FRAGMENT,
    TARGET_ARCHIVE_FRAGMENT
)
annotation class NotificationTargetType()
