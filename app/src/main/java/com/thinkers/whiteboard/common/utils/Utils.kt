package com.thinkers.whiteboard.common.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object Utils {
    fun showAlertDialog(
        context: Context,
        title: String,
        text: String,
        positiveText: String,
        negativeText: String,
        onPositiveClicked: (() -> Unit)?,
        onNegativeClicked: (() -> Unit)?,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            setMessage(text)
            setPositiveButton(positiveText,
                DialogInterface.OnClickListener { dialog, id ->
                    onPositiveClicked?.invoke()
                })
            setNegativeButton(negativeText,
                DialogInterface.OnClickListener { dialog, id ->
                    onNegativeClicked?.invoke()
                })
        }
        builder.create().show()
    }

    fun showAlertDialogWithArguments(
        context: Context,
        title: String,
        text: String,
        positiveText: String,
        negativeText: String,
        onPositiveClicked: ((AlertDialogArguments) -> Unit)?,
        onNegativeClicked: ((AlertDialogArguments) -> Unit)?,
        arguments: AlertDialogArguments
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            setMessage(text)
            setPositiveButton(positiveText) { _, _ ->
                onPositiveClicked?.invoke(arguments)
            }
            setNegativeButton(negativeText) { _, _ ->
                onNegativeClicked?.invoke(arguments)
            }
        }
        builder.create().show()
    }
}
