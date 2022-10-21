package me.abolfazl.nmock.view.editor

import androidx.annotation.IntDef

const val EDITOR_REASON_NON = 9
const val EDITOR_REASON_BUNDLE = 10
const val EDITOR_REASON_INTENT = 11

@IntDef(EDITOR_REASON_NON, EDITOR_REASON_BUNDLE, EDITOR_REASON_INTENT)
annotation class EditorOpeningReason()
