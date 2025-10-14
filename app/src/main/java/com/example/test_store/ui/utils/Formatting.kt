package com.example.test_store.ui.utils

import java.text.NumberFormat
import java.util.Locale

fun formatPrice(price: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(price)
}
