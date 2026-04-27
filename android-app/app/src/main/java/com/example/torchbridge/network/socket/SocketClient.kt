package com.example.torchbridge.network.socket

import android.util.Log
import com.example.torchbridge.core.model.ControlEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class SocketClient(private val ip: String, private val port: Int) {
    @Volatile
    private var socket: Socket? = null
    @Volatile
    private var outputStream: OutputStream? = null
    
    private val sendQueue = LinkedBlockingQueue<String>(100)
    @Volatile
    private var isRunning = true

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    private val senderThread = Thread {
        while (isRunning) {
            try {
                val msg = sendQueue.poll(500, TimeUnit.MILLISECONDS)
                if (msg != null) {
                    val stream = outputStream
                    if (stream != null) {
                        stream.write((msg + "\n").toByteArray())
                        stream.flush()
                    }
                }
            } catch (e: Exception) {
                Log.e("SOCKET", "Send error in thread", e)
                if (isRunning) Thread.sleep(1000)
            }
        }
    }.apply { 
        name = "SocketSender"
        priority = Thread.MAX_PRIORITY 
        start()
    }

    fun connect() {
        Thread {
            try {
                if (socket == null || socket?.isClosed == true || socket?.isConnected == false) {
                    Log.d("SOCKET", "Connecting to $ip:$port")
                    val newSocket = Socket(ip, port)
                    newSocket.tcpNoDelay = true
                    newSocket.sendBufferSize = 1024 * 8
                    socket = newSocket
                    outputStream = BufferedOutputStream(newSocket.getOutputStream())
                    Log.d("SOCKET", "Connected successfully")
                }
            } catch (e: Exception) {
                Log.e("SOCKET", "Connection failed", e)
            }
        }.start()
    }

    fun send(msg: String) {
        if (!sendQueue.offer(msg)) {
            // If queue is full, drop the oldest for high-frequency events
            sendQueue.poll()
            sendQueue.offer(msg)
        }
    }

    fun send(event: ControlEvent) {
        send(json.encodeToString(event))
    }

    fun disconnect() {
        isRunning = false
        Thread {
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
        }.start()
    }

    val isConnected: Boolean
        get() = socket?.let { it.isConnected && !it.isClosed } ?: false
}
