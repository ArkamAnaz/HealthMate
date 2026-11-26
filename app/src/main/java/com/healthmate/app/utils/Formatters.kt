package com.healthmate.app.utils

import java.util.Locale

/** 7850 -> "7,850" */
fun formatNumber(value: Int): String = String.format(Locale.US, "%,d", value)
