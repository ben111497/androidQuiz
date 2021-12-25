package com.example.androidquiz

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class DialogManager private constructor(){
    companion object {
        val instance : DialogManager by lazy { DialogManager() }
    }

    private var loadingDialog: Dialog? = null
    private var dialog: Dialog? = null

    fun dismissAll() {
        loadingDialog?.dismiss()
        dialog?.dismiss()
    }

    fun showCustom(activity: Activity, layout: View, keyboard: Boolean = false, gravityPosition: Int = -1): View?{
        if(!activity.isDestroyed) {
            try {
                dialog?.dismiss()

                dialog = AlertDialog.Builder(activity, R.style.Theme_Dialog).create()
                dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

                if (gravityPosition != -1) {
                    val wlp = dialog?.window?.attributes
                    wlp?.gravity = gravityPosition
                    wlp?.flags = wlp?.flags?.and(WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv())
                    dialog?.window?.attributes = wlp
                }

                dialog?.show()
                if(keyboard) {
                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

                    dialog?.setOnDismissListener {
                        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

                        (activity as? AppCompatActivity)?.let {
                            Method.hideKeyBoard(it)
                        }
                    }
                }

                dialog?.setContentView(layout)
                return layout
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    fun cancelDialog() = dialog?.dismiss()

    fun getDialog() = dialog
}