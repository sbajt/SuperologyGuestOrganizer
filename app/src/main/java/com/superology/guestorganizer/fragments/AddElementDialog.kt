package com.superology.guestorganizer.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import com.superology.guestorganizer.R
import com.superology.guestorganizer.data.models.Element
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import java.util.UUID

class AddElementDialog(private val host: String, private val listener: DialogActionListener) : DialogFragment() {

    private val disposable = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.add_element_action_bar_title)
            .setView(R.layout.dialog_add_element_text)
            .setPositiveButton(R.string.dialog_button_confirm) { _, _ ->
                Log.d(TAG, UUID.randomUUID().toString())
                val initialElement = Element(UUID.randomUUID().toString(), "", host, DateTime(0), "")
                initialElement.run {
                    guest = dialog?.findViewById<TextView>(R.id.guestView)?.text.toString()
                    description = dialog?.findViewById<TextView>(R.id.descriptionView)?.text.toString()
                }
                listener.add(initialElement)
            }
            .setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog?.dismiss() }
            .show()

        initViews(dialog)
        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun initViews(dialog: AlertDialog) {
        val guestObservable = RxTextView.textChangeEvents(dialog.findViewById(R.id.guestView)).skip(1).map { it.text() }
        val descriptionObservable = RxTextView.textChangeEvents(dialog.findViewById(R.id.descriptionView)).skip(1).map { it.text() }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        disposable.add(Observable.combineLatest(guestObservable, descriptionObservable) { name, status -> Pair(name.toString(), status.toString()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = it.first.trim().isNotBlank() && it.second.trim().isNotBlank()
            }) { Log.e(TAG, it.message.toString()) })
    }

    companion object {

        val TAG = AddElementDialog::class.java.canonicalName

        fun getInstance(host: String, listener: DialogActionListener) = AddElementDialog(host, listener)
    }
}