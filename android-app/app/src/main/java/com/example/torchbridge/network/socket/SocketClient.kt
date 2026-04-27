package com.example.torchbridge.network.socket

import android.util.Log
import com.example.torchbridge.core.model.ControlEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.Executors

class SocketClient(private val ip: String, private val port: Int) {
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun connect() {
        executor.execute {
            try {
                if (socket == null || socket?.isClosed == true) {
                    Log.d("SOCKET", "Connecting to $ip:$port")
                    socket = Socket(ip, port)
                    socket?.tcpNoDelay = true
                    outputStream = socket?.getOutputStream()
                    Log.d("SOCKET", "Connected successfully")
                }
            } catch (e: Exception) {
                Log.e("SOCKET", "Connection failed", e)
            }
        }
    }

    fun send(msg: String) {
        executor.execute {
            try {
                outputStream?.let {
                    Log.d("SOCKET_SEND", msg)
                    it.write((msg + "\n").toByteArray())
                    it.flush()
                } ?: Log.e("SOCKET", "OutputStream is null, cannot send. Attempting reconnect...")
            } catch (e: Exception) {
                Log.e("SOCKET", "Send error", e)
            }
        }
    }

    fun send(event: ControlEvent) {
        send(Json.encodeToString(event))
    }

    fun disconnect() {
        executor.execute {
            try {
                outputStream?.close()
                socket?.close()
                Log.d("SOCKET", "Disconnected")
            } catch (e: Exception) {
                Log.e("SOCKET", "Disconnect error", e)
            } finally {
                outputStream = null
                socket = null
            }
        }
    }

    val isConnected: Boolean
        get() = socket?.let { it.isConnected && !it.isClosed } ?: false
}
