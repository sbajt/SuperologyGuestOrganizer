package com.superology.guestorganizer.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.superology.guestorganizer.R
import com.superology.guestorganizer.data.models.Element

class UpdateElementStatusDialog(private val listener: DialogActionListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context)
            .setCancelable(true)
            .setView(R.layout.dialog_edit_element)
            .setTitle(R.string.dialog_edit_status_title)
            .setPositiveButton(R.string.dialog_button_confirm) { _, _ ->
                arguments?.run {
                    if (this.containsKey(ARGS_ELEMENT)) {
                        listener.update(
                            this.getParcelable<Element>(ARGS_ELEMENT)?.copy(
                                guest = dialog?.findViewById<TextView>(R.id.guestView)?.text.toString(),
                                description = dialog?.findViewById<TextView>(R.id.descriptionView)?.text.toString()
                            )!!
                        )
                    }
                }
            }
            .setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog?.dismiss() }
            .show()

        initViews(dialog)
        return dialog
    }

    private fun initViews(dialog: Dialog) {
        arguments?.run {
            dialog.findViewById<TextView>(R.id.guestView).text = getParcelable<Element>(ARGS_ELEMENT)?.guest
            dialog.findViewById<TextView>(R.id.descriptionView).text = getParcelable<Element>(ARGS_ELEMENT)?.description
        }
    }

    companion object {

        val TAG = UpdateElementStatusDialog::class.java.canonicalName
        private const val ARGS_ELEMENT = "argsElement"

        fun getInstance(element: Element, listener: DialogActionListener) = UpdateElementStatusDialog(listener).also {
            it.arguments = Bundle().also {
                it.putParcelable(ARGS_ELEMENT, element)
            }
        }
    }
}