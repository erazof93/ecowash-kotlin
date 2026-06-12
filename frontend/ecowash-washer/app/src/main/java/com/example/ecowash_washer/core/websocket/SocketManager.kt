package com.example.ecowash_washer.core.websocket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

object SocketManager {

    private const val TAG = "SocketManager"
    private var socket: Socket? = null

    private val _nuevoPedidoFlow = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val nuevoPedidoFlow = _nuevoPedidoFlow.asSharedFlow()

    private val _ubicacionConfirmadaFlow = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val ubicacionConfirmadaFlow = _ubicacionConfirmadaFlow.asSharedFlow()

    private val _connectionStateFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val connectionStateFlow = _connectionStateFlow.asSharedFlow()

    fun connect() {
        if (socket?.connected() == true) return

        try {
            val opts = IO.Options.builder()
                .setPath("/socket.io")
                .setReconnection(true)
                .setReconnectionAttempts(10)
                .setReconnectionDelay(2000)
                .build()

            socket = IO.socket("${com.example.ecowash_washer.core.network.NetworkConstants.SOCKET_URL}${com.example.ecowash_washer.core.network.NetworkConstants.SOCKET_NAMESPACE}", opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Conectado al WebSocket")
                _connectionStateFlow.tryEmit(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Desconectado del WebSocket")
                _connectionStateFlow.tryEmit(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Error de conexion: ${args.firstOrNull()}")
                _connectionStateFlow.tryEmit(false)
            }

            socket?.on("nuevo_pedido_disponible") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    if (data != null) {
                        Log.d(TAG, "Nuevo pedido recibido: $data")
                        _nuevoPedidoFlow.tryEmit(data)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando nuevo_pedido_disponible", e)
                }
            }

            socket?.on("ubicacion_recibida") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    if (data != null) {
                        Log.d(TAG, "Ubicacion confirmada: $data")
                        _ubicacionConfirmadaFlow.tryEmit(data)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando ubicacion_recibida", e)
                }
            }

            socket?.on("error_nodo") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    Log.e(TAG, "Error del servidor: ${data?.optString("message")}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando error_nodo", e)
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar WebSocket", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun enviarUbicacion(lavadorId: String, latitud: Double, longitud: Double) {
        try {
            val data = JSONObject().apply {
                put("lavador_id", lavadorId)
                put("latitud", latitud)
                put("longitud", longitud)
            }
            socket?.emit("actualizar_ubicacion", data)
            Log.d(TAG, "Ubicacion enviada: $data")
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar ubicacion", e)
        }
    }

    fun isConnected(): Boolean = socket?.connected() == true
}
