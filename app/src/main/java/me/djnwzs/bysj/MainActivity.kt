package me.djnwzs.bysj

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import me.djnwzs.bysj.Dialog.dialog_list

import java.io.IOException
import java.util.ArrayList
import java.util.UUID
import me.djnwzs.bysj.ScanDevice.mReceiver
import me.djnwzs.bysj.ScanDevice.scanDevice
import me.djnwzs.bysj.ScanDevice.scanstop


class MainActivity : AppCompatActivity() {



    private val readPH = object : Runnable {
        override fun run() {
            mHandler.postDelayed(this, 1000)//设置延迟时间，此处是1秒
            if (mCharacteristic2 == null || mBluetoothGatt == null) {
                textViewPH?.text = ""
                textViewPHT?.text = ""
                isReadPH = false
                mHandler.removeCallbacksAndMessages(null)
                return
            }
            if (mCharacteristic2 != null && mBluetoothGatt != null) {
                try {
                    SendData.byteSend(PHS)
                    isReadPH = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            //            mBluetoothGatt.setCharacteristicNotification(mCharacteristic2, true);
            mBluetoothGatt?.readCharacteristic(mCharacteristic2)
            //            textViewPH.setText(mCharacteristic2.getValue().toString());
            if (mCharacteristic2!!.value != null && isReadPH == true) {
                try {
                    val bytes = mCharacteristic2!!.value
                    if (bytes[0] != 0x06.toByte() || bytes[1] != 0x03.toByte() || bytes[2] != 0x08.toByte()) return
                    var PH: Double
                    var PHT: Double
                    val n: Int
                    var m: Int
                    n = bytes[6].toInt()
                    PH = ((bytes[3].toInt() shl 8) + (bytes[4].toInt())).toDouble()
                    PH = PH / Math.pow(10.0, n.toDouble())
                    m = bytes[10].toInt()
                    PHT = ((bytes[7].toInt() shl 8) + (bytes[8].toInt())).toDouble()
                    PHT = PHT / Math.pow(10.0, m.toDouble())
                    textViewPH?.text = PH.toString()
                    textViewPHT?.text = PHT.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                textViewPH?.text = ""
                textViewPHT?.text = ""
                isReadPH = false
                return
            }
        }
    }

    private val readConductivity = object : Runnable {
        override fun run() {
            mHandler.postDelayed(this, 1000)//设置延迟时间，此处是1秒
            if (mCharacteristic2 == null || mBluetoothGatt == null) {
                textViewC?.text = ""
                textViewCT?.text = ""
                isReadConductivity = false
                mHandler.removeCallbacksAndMessages(null)
                return
            }
            if (mCharacteristic2 != null && mBluetoothGatt != null) {
                try {
                    SendData.byteSend(CS)
                    isReadConductivity = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            //mBluetoothGatt.setCharacteristicNotification(mCharacteristic2, true);
            mBluetoothGatt?.readCharacteristic(mCharacteristic2)
            if (mCharacteristic2!!.value != null && isReadConductivity == true) {
                try {
                    val bytes = mCharacteristic2!!.value
                    if (bytes[0] != 0x01.toByte() || bytes[1] != 0x03.toByte() || bytes[2] != 0x08.toByte()) return
                    var C: Double
                    var CT: Double
                    val n: Int
                    var m: Int
                    n = bytes[6].toInt()
                    C = ((bytes[3].toInt() shl 8) + bytes[4].toInt()).toDouble()
                    C = C / Math.pow(10.0, n.toDouble())
                    m = bytes[10].toInt()

                    CT = ((bytes[7].toInt() shl 8 ) + bytes[8].toInt()).toDouble()
                    CT = CT / Math.pow(10.0, m.toDouble())
                    textViewC?.text = C.toString()
                    textViewCT?.text = CT.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                textViewC?.text = ""
                textViewCT?.text = ""
                isReadConductivity = false
                return
            }
        }
    }

    /**
     * 检查权限
     */
    private val REQUEST_CODE_PERMISSION_LOCATION = 1

    /**
     * 开启GPS
     *
     * @param permission
     */
    private val REQUEST_CODE_OPEN_GPS = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mactivity=this
        checkPermissions()
        View()
        bluetooth_enable()
        if (isBlueEnable) {
            setTextViewState("打开")
        } else {
            setTextViewState("关闭")
        }
        val stateChangeFilter = IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED)
        val connectedFilter = IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED)
        val disConnectedFilter = IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED)
        mactivity?.registerReceiver(mReceiver, stateChangeFilter)
        mactivity?.registerReceiver(mReceiver, connectedFilter)
        mactivity?.registerReceiver(mReceiver, disConnectedFilter)
        button?.setOnClickListener {
            bluetooth_enable()
            scanDevice(!isScanning)
        }
        button1?.setOnClickListener {
            if (isScanning) {
                scanstop()
                Toast.makeText(this@MainActivity, "已暂停扫描", Toast.LENGTH_SHORT).show()
            }
            dialog_list()
            //bluetoothdevice = mBluetoothAdapter.getRemoteDevice(address);
            //mBluetoothGatt = bluetoothdevice.connectGatt(MainActivity.this, false, mBluetoothGattCallback);
        }
        button2?.setOnClickListener {
            if (mBluetoothGatt != null) {
                try {
                    mBluetoothGatt?.disconnect()//主动断开连接
                    mBluetoothGatt?.close()
                    mBluetoothGatt = null
                } catch (e: Exception) {
                    Log.e("blueTooth", "断开蓝牙失败")
                    Toast.makeText(this@MainActivity, "断开蓝牙失败", Toast.LENGTH_SHORT).show()
                }

            }
            if (isClassicBluetooth) {
                try {
                    val socket = bluetoothdevice?.createRfcommSocketToServiceRecord(UUID
                            .fromString("00001101-0000-1000-8000-00805F9B34FB"))
                    //停止搜索
                    mBluetoothAdapter?.cancelDiscovery()
                    //连接
                    socket?.close()
                    //创建蓝牙客户端线程
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        button3?.setOnClickListener {
            //runPH();
            if (mCharacteristic2 != null && !isReadPH) {
                mHandler.post(readPH)
            }
        }
        button4?.setOnClickListener {
            //runC();
            if (mCharacteristic2 != null && !isReadConductivity) {
                mHandler.post(readConductivity)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val setting = menu.findItem(R.id.action_settings)
        if (isClassicBluetooth) {
            setting.title = "切换至低功耗蓝牙"
        } else {
            setting.title = "切换至经典蓝牙"
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            if (isScanning) {
                scanstop()
                name_list.clear()
                address_list.clear()
                Toast.makeText(this@MainActivity, "已暂停扫描", Toast.LENGTH_SHORT).show()
            }
            isClassicBluetooth = !isClassicBluetooth
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    private fun bluetooth_enable() {
        if (!isBlueEnable) {
            Log.d("00000", "Bluetooth is NOT switched on")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(enableBtIntent, 1)
            //this.startActivity(enableBtIntent);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = mBluetoothAdapter?.bluetoothLeScanner
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionDeniedList = ArrayList<String>()
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION)
        }
    }

    /**
     * 权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> if (grantResults.size > 0) {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted(permissions[i])
                    }
                }
            }
        }
    }

    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("当前手机扫描蓝牙需要打开定位功能。")
                        .setNegativeButton("取消"
                        ) { dialog, which -> finish() }
                        .setPositiveButton("前往设置"
                        ) { dialog, which ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                        }

                        .setCancelable(false)
                        .show()
            } else {
                //GPS已经开启了
            }
        }
    }

    /**
     * 检查GPS是否打开
     *
     * @return
     */
    private fun checkGPSIsOpen(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                ?: return false
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    private fun View() {
        textView = findViewById<View>(R.id.textView) as TextView
        textViewPH = findViewById<View>(R.id.textView3) as TextView
        textViewPHT = findViewById<View>(R.id.textView4) as TextView
        textViewC = findViewById<View>(R.id.textView5) as TextView
        textViewCT = findViewById<View>(R.id.textView6) as TextView
        textViewState = findViewById<View>(R.id.textView7) as TextView
        button = findViewById<View>(R.id.button) as Button
        button1 = findViewById<View>(R.id.button1) as Button
        button2 = findViewById<View>(R.id.button2) as Button
        button3 = findViewById<View>(R.id.button3) as Button
        button4 = findViewById<View>(R.id.button4) as Button
        scrollView = findViewById<View>(R.id.scrollView) as ScrollView
    }

    companion object {

        var mactivity: MainActivity?  = null

        var PHS = byteArrayOf(0x06.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x45.toByte(), 0xBE.toByte())
        var CS = byteArrayOf(0x01.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x44.toByte(), 0x09.toByte())

        var mBluetoothAdapter: BluetoothAdapter? = null
        var mHandler = Handler()
        var mCharacteristic: BluetoothGattCharacteristic? = null
        var mCharacteristic2: BluetoothGattCharacteristic? = null
        var bluetoothdevice: BluetoothDevice? = null
        var mBluetoothGatt: BluetoothGatt? = null


        var textView: TextView? = null
        var textViewPH: TextView? = null
        var textViewPHT: TextView? = null
        var textViewC: TextView? = null
        var textViewCT: TextView? = null
        var textViewState: TextView? = null
        var button: Button? = null
        var button1: Button? = null
        var button2: Button? = null
        var button3: Button? = null
        var button4: Button? = null
        var scrollView: ScrollView? = null

        var isScanning: Boolean = false//是否正在搜索
        var isClassicBluetooth: Boolean = false
        var isReadPH: Boolean = false
        var isReadConductivity: Boolean = false
        var name_list = ArrayList<String>()
        var address_list = ArrayList<String>()
        var serviceList: List<BluetoothGattService>? = null//服务
        var characterList: List<BluetoothGattCharacteristic>? = null//特征
        var scanner: BluetoothLeScanner? = null//用过单例的方式获取实例

        fun setTextViewState(string: String) {
            val setTextViewState = Runnable { textViewState?.text = "蓝牙状态: $string" }
            mHandler.post(setTextViewState)
        }

        fun setTextView(string: String) {
            textView?.append(string + "\n")
            scrollView?.fullScroll(ScrollView.FOCUS_DOWN)
        }




        /**
         * 设备是否支持蓝牙  true为支持
         *
         * @return
         */
        val isSupportBlue: Boolean
            get() = mBluetoothAdapter != null

        /**
         * 蓝牙是否打开   true为打开
         *
         * @return
         */
        val isBlueEnable: Boolean
            get() = isSupportBlue && mBluetoothAdapter!!.isEnabled
    }

}
