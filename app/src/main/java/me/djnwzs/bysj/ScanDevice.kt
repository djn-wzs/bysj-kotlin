package me.djnwzs.bysj

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.Toast
import me.djnwzs.bysj.MainActivity.Companion
import me.djnwzs.bysj.MainActivity.Companion.mactivity

object ScanDevice {

    //10秒搜索时间
    private val SCAN_PERIOD: Long = 10000

    var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            var str = ""
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            if (device != null) {
                str = device.name + ": " + device.address
            }
            Log.e("BroadcastReceiver", "action:" + action!!)
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.e("BroadcastReceiver", "发现蓝牙设备")
                    Log.e("BroadcastReceiver", "btInfo:$str")
                    if (!Companion.address_list.contains(device!!.address)) {
                        Companion.setTextView(str)
                        Companion.name_list.add(device.name)
                        Companion.address_list.add(device.address)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.e("BroadcastReceiver", "搜索完毕")
                    if (!Companion.textView!!.getText().toString().endsWith("搜索完毕\n")) {
                        Companion.setTextView("搜索完毕")
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> Companion.setTextViewState("$str 已连接")
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> Companion.setTextViewState("$str 已断开")
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                    when (blueState) {
                        BluetoothAdapter.STATE_TURNING_ON -> {
                            Companion.setTextViewState("正在打开")
                            Log.e("BluetoothState", "STATE_TURNING_ON")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            Companion.setTextViewState("打开")
                            Log.e("BluetoothState", "STATE_ON")
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            Companion.setTextViewState("正在关闭")
                            Log.e("BluetoothState", "STATE_TURNING_OFF")
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            Companion.setTextViewState("关闭")
                            Log.e("BluetoothState", "STATE_OFF")
                        }
                    }
                }
            }

        }
    }

    //region Android M 以上的回调
    private val scanCallback = object : ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val rssi = result.rssi
            //获取rssi
            // 这里写你自己的逻辑
            if (!Companion.address_list.contains(device.address)) {
                Companion.setTextView(device.name + ": " + device.address)
                Companion.name_list.add(device.name)
                Companion.address_list.add(device.address)
            }
            Log.e(device.name + ": " + device.address, "run: ")
        }
    }

    //region Android M 以下的回调
    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        //这里是个子线程，下面把它转换成主线程处理
        mactivity?.runOnUiThread(Runnable {
            //在这里可以把搜索到的设备保存起来
            //bluetoothdevice.getName();获取蓝牙设备名字
            //bluetoothdevice.getAddress();获取蓝牙设备mac地址
            //这里的rssi即信号强度，即手机与设备之间的信号强度。
            if (!Companion.address_list.contains(device.address)) {
                Companion.setTextView(device.name + ": " + device.address)
                Companion.name_list.add(device.name)
                Companion.address_list.add(device.address)
            }
            Log.e(device.name + ": " + device.address, "run: ")
        })
    }

    fun scanDevice(enable: Boolean) {
        if (!Companion.isBlueEnable) {
            Log.e("scanDevice", "Bluetooth not enable!")
            return
        } else if (enable) {//true
            //15秒后停止搜索
            Companion.mHandler.postDelayed(Runnable {
                //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                scanstop()
            }, SCAN_PERIOD)
            //mBluetoothAdapter.startLeScan(mLeScanCallback); //开始搜索
            scanstart()
        } else {
            return
        }
        /*else {
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);//停止搜索
            scanstop();
        }*/
    }


    fun scanstart() {
        Companion.isScanning=true
        Companion.name_list.clear()
        Companion.address_list.clear()
        if (!Companion.isBlueEnable) {
            Log.e("scanstart", "Bluetooth not enable!")
            Companion.isScanning=false
            return
        }
        if (Companion.mBluetoothAdapter!!.isDiscovering()) {
            Companion.mBluetoothAdapter!!.cancelDiscovery()
        }
        if (Companion.isClassicBluetooth) {
            scanBlue()
        }
        if (!Companion.isClassicBluetooth && Companion.isScanning) {
            Companion.setTextView("开始扫描ble设备")
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //安卓6.0及以下版本BLE操作的代码
            Companion.mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            // 安卓7.0及以上版本BLE操作的代码
            Companion.scanner!!.startScan(scanCallback)
        }
    }

    fun scanstop() {
        if (!Companion.isBlueEnable) {
            Log.e("scanstop", "Bluetooth not enable!")
            Companion.isScanning=false
            return
        }
        if (Companion.mBluetoothAdapter!!.isDiscovering()) {
            Companion.mBluetoothAdapter!!.cancelDiscovery()
            //this.unregisterReceiver(mReceiver);
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //安卓6.0及以下版本BLE操作的代码
            Companion.mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        } else {
            // 安卓7.0及以上版本BLE操作的代码
            Companion.scanner!!.stopScan(scanCallback)
        }
        if (!Companion.isClassicBluetooth && Companion.isScanning) {
            Companion.setTextView("扫描完毕")
        }
        Companion.isScanning=false
    }


    fun scanBlue() {
        if (!Companion.isBlueEnable) {
            Log.e("scanBlue", "Bluetooth not enable!")
            return
        }
        //当前是否在扫描，如果是就取消当前的扫描，重新扫描
        if (Companion.mBluetoothAdapter!!.isDiscovering()) {
            Companion.mBluetoothAdapter!!.cancelDiscovery()
        }
        //注册设备被发现时的广播
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        Companion.mactivity?.registerReceiver(mReceiver, filter)
        //注册一个搜索结束时的广播
        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        Companion.mactivity?.registerReceiver(mReceiver, filter2)
        Companion.setTextView("开始搜索蓝牙设备")
        //此方法是个异步操作，一般搜索12秒
        Companion.mBluetoothAdapter!!.startDiscovery()
    }
}
