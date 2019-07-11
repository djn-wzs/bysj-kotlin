package me.djnwzs.bysj

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlin.Long as Long1

class DailogAdapter(private val activity: Activity?) : BaseAdapter() {

    override fun getCount(): Int {
        return MainActivity.address_list.size
    }

    override fun getItem(i: Int): Any? {
        return MainActivity.address_list[i]
    }

    override fun getItemId(i: Int): kotlin.Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var view = view
        view = View.inflate(activity, R.layout.text_dialog, null)
        val textView = view?.findViewById<View>(R.id.dialog_text) as TextView
        textView.text = MainActivity.name_list[i] + ": " + MainActivity.address_list[i]
        return view
    }
}