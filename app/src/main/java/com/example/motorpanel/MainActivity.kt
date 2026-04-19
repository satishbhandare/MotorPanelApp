package com.example.motorpanel

import android.bluetooth.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var btAdapter: BluetoothAdapter
    private var socket: BluetoothSocket? = null
    private var out: OutputStream? = null
    private var lastSendTime = 0L

    private val UUID_SPP: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btAdapter = BluetoothAdapter.getDefaultAdapter()

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val btnEStop = findViewById<Button>(R.id.btnEStop)

        val seekRPM = findViewById<SeekBar>(R.id.seekRPM)
        val txtRPM = findViewById<TextView>(R.id.txtRPM)

        val editCurrent = findViewById<EditText>(R.id.editCurrent)
        val btnSetCurrent = findViewById<Button>(R.id.btnSetCurrent)

        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        btnConnect.setOnClickListener { connect() }

        btnStart.setOnClickListener {
            send("START")
            txtStatus.text = "RUNNING"
        }

        btnStop.setOnClickListener {
            send("STOP")
            txtStatus.text = "STOPPED"
        }

        btnEStop.setOnClickListener {
            send("ESTOP")
            txtStatus.text = "!!! EMERGENCY STOP !!!"
        }

        seekRPM.max = 500

        seekRPM.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                txtRPM.text = "RPM: $value"
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                val rpm = sb?.progress ?: 0
                send("RPM:$rpm")
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
        })

        btnSetCurrent.setOnClickListener {
            val current = editCurrent.text.toString().toIntOrNull()

            if (current == null || current < 100 || current > 3000) {
                toast("Range: 100–3000 mA")
                return@setOnClickListener
            }

            send("CUR:$current")
        }
    }

    private fun connect() {
        val device = btAdapter.bondedDevices
            .firstOrNull { it.name == "HC-05" }

        if (device == null) {
            toast("HC-05 not paired")
            return
        }

        socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
        socket?.connect()
        out = socket?.outputStream

        toast("Connected")
    }

    private fun send(cmd: String) {
        val now = System.currentTimeMillis()
        if (now - lastSendTime < 150) return
        lastSendTime = now

        try {
            out?.write((cmd + "\n").toByteArray())
        } catch (e: Exception) {
            toast("Send failed")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
