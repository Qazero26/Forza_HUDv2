package com.example

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface

class ForzaViewModel(application: Application) : AndroidViewModel(application) {
    private val udpServer = UdpServer()
    private val appSettings = AppSettings(application)

    private val _telemetryState = MutableStateFlow(TelemetryData())
    val telemetryState: StateFlow<TelemetryData> = _telemetryState.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _localIpAddress = MutableStateFlow("Unknown")
    val localIpAddress: StateFlow<String> = _localIpAddress.asStateFlow()

    private val _isMph = MutableStateFlow(appSettings.isMph)
    val isMph: StateFlow<Boolean> = _isMph.asStateFlow()

    private val _currentTheme = MutableStateFlow(
        DashboardTheme.values().getOrElse(appSettings.themeOrdinal) { DashboardTheme.IMMERSIVE }
    )
    val currentTheme: StateFlow<DashboardTheme> = _currentTheme.asStateFlow()

    private val _currentLayout = MutableStateFlow(
        DashboardLayout.values().getOrElse(appSettings.layoutOrdinal) { DashboardLayout.CLASSIC_ARC }
    )
    val currentLayout: StateFlow<DashboardLayout> = _currentLayout.asStateFlow()

    fun setMph(mph: Boolean) {
        appSettings.isMph = mph
        _isMph.value = mph
    }

    fun setTheme(theme: DashboardTheme) {
        appSettings.themeOrdinal = theme.ordinal
        _currentTheme.value = theme
    }
    
    fun setLayout(layout: DashboardLayout) {
        appSettings.layoutOrdinal = layout.ordinal
        _currentLayout.value = layout
    }

    fun updateLocalIp(context: Context) {
        _localIpAddress.value = getLocalIpAddress(context)
    }

    fun toggleListening(port: String) {
        if (_isListening.value) {
            udpServer.stop()
            _isListening.value = false
        } else {
            val portInt = port.toIntOrNull() ?: 5300
            _isListening.value = true
            viewModelScope.launch {
                udpServer.startListening(portInt) { data ->
                    _telemetryState.value = data
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        udpServer.stop()
    }

    private fun getLocalIpAddress(context: Context): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "Unknown"
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                // Skip loopback and inactive interfaces
                if (iface.isLoopback || !iface.isUp) continue

                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: "Unknown"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Unknown"
    }
}
