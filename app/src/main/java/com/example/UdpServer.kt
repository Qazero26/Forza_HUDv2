package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UdpServer {
    private var socket: DatagramSocket? = null
    var isRunning = false
        private set

    suspend fun startListening(port: Int, onDataReceived: (TelemetryData) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                socket = DatagramSocket(null)
                socket?.reuseAddress = true
                socket?.bind(InetSocketAddress(port))
                socket?.soTimeout = 2000
                isRunning = true

                val buffer = ByteArray(500)
                val packet = DatagramPacket(buffer, buffer.size)

                Log.d("UdpServer", "Started listening on port $port")

                while (isRunning) {
                    try {
                        socket?.receive(packet)
                        if (packet.length >= 300) {
                            val data = parsePacket(buffer, packet.length)
                            onDataReceived(data)
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        // Just timeout, loop again and check isRunning
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e("UdpServer", "Receive error", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UdpServer", "Socket bind error", e)
            } finally {
                socket?.close()
                socket = null
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        socket?.close()
        socket = null
    }

    private fun parsePacket(bytes: ByteArray, length: Int): TelemetryData {
        if (length < 20) return TelemetryData() // Prevent buffer underflow

        val bb = ByteBuffer.wrap(bytes, 0, length)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        // The "Dash" (v2) packet struct.
        // Some offsets (standard fh4/fh5 dash):
        // 0: s32 IsRaceOn
        // 4: u32 TimestampMS
        // 8: f32 EngineMaxRpm
        // 12: f32 EngineIdleRpm
        // 16: f32 CurrentEngineRpm
        // 256: f32 Speed (meters per second)
        // 331: u8 Gear (sometimes 331 depending on padding. Let's check 319 vs 331 safely)
        
        val isRaceOn = bb.getInt(0) == 1
        val timestampMs = bb.getInt(4).toLong() and 0xFFFFFFFFL
        val maxRpm = bb.getFloat(8)
        val idleRpm = bb.getFloat(12)
        val currentRpm = bb.getFloat(16)
        
        // Speed in m/s -> km/h
        val speedMs = if (length >= 260) bb.getFloat(256) else 0f
        val speedKmh = speedMs * 3.6f

        // Gear is at offset 319 in the standard FH4/FH5 Dash struct
        var gear = 0
        var throttle = 0
        var brake = 0
        var steering = 0
        if (length >= 320) {
            val gearValue = bb.get(319).toInt() and 0xFF
            gear = gearValue
            val throttleValue = bb.get(315).toInt() and 0xFF
            throttle = throttleValue
            val brakeValue = bb.get(316).toInt() and 0xFF
            brake = brakeValue
            val steeringValue = bb.get(318).toInt()
            steering = steeringValue
        }

        return TelemetryData(
            isRaceOn = isRaceOn,
            timestampMs = timestampMs,
            engineMaxRpm = maxRpm,
            engineIdleRpm = idleRpm,
            currentEngineRpm = currentRpm,
            speedKmh = speedKmh,
            gear = gear,
            throttle = throttle,
            brake = brake,
            steering = steering
        )
    }
}
