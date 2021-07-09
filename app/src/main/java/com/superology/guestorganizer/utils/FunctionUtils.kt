package com.scorealarm.functions

import android.content.Context
import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.superology.guestorganizer.R

object FunctionUtils {

    private val TAG = FunctionUtils::class.java.canonicalName

    private val functions by lazy {
        FirebaseFunctions.getInstance()
    }

    fun observeDb(context: Context) {
        functions.getHttpsCallable("observeDataChange").call()
            .addOnSuccessListener { Log.d(TAG, context.getString(R.string.firebase_function_data_change_success)) }
            .addOnFailureListener { Log.e(TAG, context.getString(R.string.firebase_function_data_change_error), it) }
    }

}