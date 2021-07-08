package com.superology.guestorganizer.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class Element(
    var key: String,
    var guest: String,
    var host: String,
    var dateTime: DateTime,
    var description: String
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Element

        if (host != other.host) return false
        if (dateTime != other.dateTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + dateTime.hashCode()
        return result
    }
}
