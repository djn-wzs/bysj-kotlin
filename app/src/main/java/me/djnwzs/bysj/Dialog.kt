package me.djnwzs.bysj

import android.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import me.djnwzs.bysj.GattCallback.mBluetoothGattCallback
import me.djnwzs.bysj.MainActivity.Companion.address_list
import me.djnwzs.bysj.MainActivity.Companion.bluetoothdevice
import me.djnwzs.bysj.MainActivity.Companion.isClassicBluetooth
import me.djnwzs.bysj.MainActivity.Companion.mBluetoothAdapter
import me.djnwzs.bysj.MainActivity.Companion.mBluetoothGatt
import me.djnwzs.bysj.MainActivity.Companion
import me.djnwzs.bysj.MainActivity.Companion.mactivity

object Dialog {
    var address = ""

    fun dialog_list() {
        if (address_list.isEmpty()) {
            Toast.makeText(mactivity, "无可用蓝牙设备", Toast.LENGTH_SHORT).show()
            return
        }
        val bottomView = View.inflate(mactivity, R.layout.list_dialog, null)//填充ListView布局
        val listView = bottomView.findViewById<View>(R.id.listView) as ListView
        listView.adapter = DailogAdapter(mactivity)//ListView设置适配器
        val listdialog = AlertDialog.Builder(mactivity)
                .setTitle("选择蓝牙设备").setView(bottomView)//在这里把写好的这个listview的布局加载dialog中
                .setNeutralButton("取消") { dialog, which -> dialog.dismiss() }
                .setPositiveButton("确定") { dialog, which ->
                    bluetoothdevice = mBluetoothAdapter!!.getRemoteDevice(address)
                    if (!isClassicBluetooth) {
                        mBluetoothGatt = bluetoothdevice!!.connectGatt(mactivity, false, mBluetoothGattCallback)
                    } else if (isClassicBluetooth) {
                        mBluetoothGatt = bluetoothdevice!!.connectGatt(mactivity, false, mBluetoothGattCallback)
                    }
                }.create()
        listdialog.setCanceledOnTouchOutside(false)//使除了dialog以外的地方不能被点击
        listdialog.show()
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l -> address = address_list[i] }
    }

}
