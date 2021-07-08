package com.superology.guestorganizer.data

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.superology.guestorganizer.R
import com.superology.guestorganizer.data.models.Element
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import org.joda.time.DateTime
import org.joda.time.Interval

object DataService {

    private val TAG = DataService::class.java.canonicalName
    private val dataSubject = ReplaySubject.create<List<Element>>(1)
    private val dbRef by lazy { FirebaseDatabase.getInstance().reference }
    private val items = mutableListOf<Element>()

    init {
        dbRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.children.map {
                    val tokens = it.value.toString().split(',').map { it.trim() }
                    Element(
                        key = it.key ?: "",
                        guest = tokens[0],
                        host = tokens[1],
                        dateTime = DateTime.parse(tokens[2]),
                        description = tokens[3]
                    )
                }
                onDataFetch(data.filterToday())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message, error.toException())
            }
        })
    }

    fun observeData(): Observable<List<Element>> = dataSubject.subscribeOn(Schedulers.newThread())

    fun addElement(context: Context?, element: Element?) {
        if (context != null && element != null)
            dbRef.child(element.key).setValue(toDbString(element))
                .addOnCompleteListener { Log.d(TAG, context.getString(R.string.firebase_add_element_success)) }
                .addOnFailureListener { Log.e(TAG, context.getString(R.string.firebase_add_element_error)) }
    }

    fun updateElement(context: Context?, element: Element?) {
        if (context != null && element != null)
            dbRef.child(element.key).setValue(toDbString(element))
                .addOnFailureListener { Log.e(TAG, context.getString(R.string.firebase_change_element_error)) }
    }

    fun removeElement(context: Context?, element: Element?) {
        if (context != null && element != null)
            dbRef.child(element.key).removeValue()
                .addOnFailureListener { Log.e(TAG, context.getString(R.string.firebase_remove_element_error)) }
    }

    private fun onDataFetch(records: List<Element>) {
        items.clear()
        items.addAll(records)
        dataSubject.onNext(records)
    }

    private fun List<Element>.filterToday(): List<Element> {
        return this.filter { Interval(DateTime.now().withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay().plusHours(24)).contains(it.dateTime) }
    }

    private fun toDbString(element: Element) = "${element.guest}, ${element.host}, ${element.dateTime}, ${element.description}"
}