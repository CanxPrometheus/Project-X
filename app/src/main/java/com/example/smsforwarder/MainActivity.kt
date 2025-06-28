package com.example.smsforwarder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.smsforwarder.data.TokenResponse
import com.example.smsforwarder.databinding.ActivityMainBinding
import com.example.smsforwarder.network.RetrofitClient
import com.example.smsforwarder.service.SMSService
import com.example.smsforwarder.utils.PreferencesManager
import com.example.smsforwarder.utils.SIMCardDetector
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 100
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var simCardDetector: SIMCardDetector
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        simCardDetector = SIMCardDetector(this)
        
        setupUI()
        checkPermissions()
        loadToken()
        updateServiceStatus()
        displaySIMInfo()
    }
    
    private fun setupUI() {
        binding.btnRegenerateToken.setOnClickListener {
            generateToken()
        }
        
        binding.btnStartService.setOnClickListener {
            if (checkPermissions()) {
                startSMSService()
            }
        }
        
        binding.btnStopService.setOnClickListener {
            stopSMSService()
        }
    }
    
    private fun checkPermissions(): Boolean {
        val permissionsToRequest = mutableListOf<String>()
        
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
            return false
        }
        
        return true
    }
    
    private fun loadToken() {
        val token = preferencesManager.getDeviceToken()
        if (token != null) {
            binding.tvToken.text = token
        } else {
            binding.tvToken.text = "Token bulunamadı"
            generateToken()
        }
    }
    
    private fun generateToken() {
        binding.btnRegenerateToken.isEnabled = false
        binding.tvToken.text = "Token oluşturuluyor..."
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getToken()
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        val fullUrl = tokenResponse.deviceToken
                        val token = fullUrl.substringAfterLast("/")
                        
                        preferencesManager.saveDeviceToken(token)
                        binding.tvToken.text = token
                        Toast.makeText(this@MainActivity, R.string.token_generated, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Token generated successfully: $token")
                        Log.d(TAG, "Full URL: $fullUrl")
                    } else {
                        binding.tvToken.text = "Token oluşturulamadı"
                        Toast.makeText(this@MainActivity, R.string.token_error, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Token response is null")
                    }
                } else {
                    binding.tvToken.text = "Token oluşturulamadı"
                    Toast.makeText(this@MainActivity, R.string.token_error, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Token generation failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                binding.tvToken.text = "Token oluşturulamadı"
                Toast.makeText(this@MainActivity, R.string.token_error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error generating token", e)
            } finally {
                binding.btnRegenerateToken.isEnabled = true
            }
        }
    }
    
    private fun startSMSService() {
        val serviceIntent = Intent(this, SMSService::class.java)
        startForegroundService(serviceIntent)
        updateServiceStatus()
        Toast.makeText(this, "SMS servisi başlatıldı", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "SMS service started")
    }
    
    private fun stopSMSService() {
        val serviceIntent = Intent(this, SMSService::class.java)
        stopService(serviceIntent)
        updateServiceStatus()
        Toast.makeText(this, "SMS servisi durduruldu", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "SMS service stopped")
    }
    
    private fun updateServiceStatus() {
        val isRunning = preferencesManager.isServiceRunning()
        if (isRunning) {
            binding.tvServiceStatus.text = getString(R.string.service_running)
            binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            binding.tvServiceStatus.text = getString(R.string.service_stopped)
            binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
    }
    
    private fun displaySIMInfo() {
        val simCards = simCardDetector.getAllSIMInfo()
        if (simCards.isNotEmpty()) {
            val simInfoText = StringBuilder()
            simInfoText.append("SIM Kartları:\n")
            simCards.forEach { simInfo ->
                simInfoText.append("Slot ${simInfo.slotIndex}: ${simInfo.carrierName} (${simInfo.number})\n")
            }
            addLog(simInfoText.toString())
        } else {
            addLog("SIM kart bilgisi alınamadı")
        }
    }
    
    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] $message\n"
        binding.tvLogs.append(logEntry)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            
            if (allGranted) {
                Toast.makeText(this, "İzinler verildi", Toast.LENGTH_SHORT).show()
                loadToken()
                displaySIMInfo()
            } else {
                showPermissionDialog()
            }
        }
    }
    
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permissions_required)
            .setMessage(R.string.permissions_message)
            .setPositiveButton(R.string.grant_permissions) { _, _ ->
                checkPermissions()
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
} 