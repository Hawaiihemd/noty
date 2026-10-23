package com.noty.app.ui

import android.content.Context
import android.text.format.DateUtils

/**
 * A reminder timestamp as the user should read it. Shared because both the note card and the
 * editor sheet display one, and they must agree — a card reading "Mar 3, 09:00" beside a
 * sheet reading something else is the kind of mismatch nobody reports and everybody notices.
 */
internal fun formatReminderTime(context: Context, millis: Long): String =
    DateUtils.formatDateTime(
        context, millis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH
    )
