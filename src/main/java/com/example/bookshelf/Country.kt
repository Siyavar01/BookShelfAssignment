package com.example.bookshelf

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Country(
    val country: String,
    val region: String
) : Parcelable