package com.sabrina.listabluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnLoadDevices: Button
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var adapter: DevicesAdapter

    private val devices = mutableListOf<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerDevices)
        btnLoadDevices = findViewById(R.id.btnLoadDevices)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DevicesAdapter()
        recyclerView.adapter = adapter

        val tempAdapter = BluetoothAdapter.getDefaultAdapter()
        if (tempAdapter != null) {
            bluetoothAdapter = tempAdapter
        } else {
            btnLoadDevices.isEnabled = false
            return
        }

        btnLoadDevices.setOnClickListener {
            loadPairedDevices()
        }
    }

    // ================= CARGAR DISPOSITIVOS =================
    @SuppressLint("MissingPermission")
    private fun loadPairedDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
                return
            }
        }

        if (!::bluetoothAdapter.isInitialized || bluetoothAdapter == null) {
            android.widget.Toast.makeText(this, "Este dispositivo no tiene Bluetooth", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            android.widget.Toast.makeText(this, "Activa el Bluetooth para ver dispositivos", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        try {
            devices.clear()
            devices.addAll(bluetoothAdapter.bondedDevices)
            adapter.notifyDataSetChanged()

            if (devices.isEmpty()) {
                android.widget.Toast.makeText(this, "No hay dispositivos vinculados", android.widget.Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            // Evita que la app se cierre si falla el permiso en versiones viejas
            android.widget.Toast.makeText(this, "Error de permisos Bluetooth", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadPairedDevices()
        }
    }

    // ================= ADAPTER =================
    inner class DevicesAdapter :
        RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

        inner class DeviceViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {

            val name: TextView = view.findViewById(android.R.id.text1)
            val details: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return DeviceViewHolder(view)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            val device = devices[position]

            holder.name.text = device.name ?: "Dispositivo desconocido"
            holder.details.text = "MAC: ${device.address}"
            holder.details.setTextColor(Color.GRAY)
        }

        override fun getItemCount(): Int = devices.size
    }
}
