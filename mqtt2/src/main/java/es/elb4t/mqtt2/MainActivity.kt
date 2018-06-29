package es.elb4t.mqtt2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


class MainActivity : AppCompatActivity(), MqttCallback, ShakeListener.OnShakeListener {

    companion object {
        private val TAG = "Mobile"
        private val topic_gestion = "elllabel/gestion"
        private val topic_led = "elllabel/led"
        val hello = "Hello world! Android Mobile conectado."
        private val qos = 1
        private val broker = "tcp://iot.eclipse.org:1883"
    }

    var client: MqttClient? = null
    var connOpts: MqttConnectOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val test = ShakeListener(this)
        test.setOnShakeListener(this)

        try {
            val clientId = MqttClient.generateClientId()
            client = MqttClient(broker, clientId, MemoryPersistence())
            client?.setCallback(this)
            connOpts = MqttConnectOptions()
            connOpts?.isCleanSession = true
            connOpts?.keepAliveInterval = 60
            connOpts?.setWill(topic_gestion, ("Android Mobile " + "desconectado!").toByteArray(), qos, false)
        } catch (e: MqttException) {
            Log.e(TAG, "Error en MQTT.", e)
        }

        buttonConnect.setOnClickListener {
            Log.i(TAG, "Boton presionado")
            textview.text = "Conectando..."
            try {
                Log.i(TAG, "Conectando al broker $broker")
                client?.connect(connOpts)
                Log.i(TAG, "Conectado")
                Log.i(TAG, "Publicando mensaje: $hello")
                val message = MqttMessage(hello.toByteArray())
                message.qos = qos
                client?.publish(topic_gestion, message)
                Log.i(TAG, "Mensaje publicado")
                textview.text = "Conectado"
            } catch (e: MqttException) {
                Log.e(TAG, "Error en MQTT.", e)
                textview.text = "Error al conectar"
            }
        }

        buttonDisconnect.setOnClickListener {
            Log.i(TAG, "Boton presionado")
            textview.text = "Desconectando..."
            try {
                if (client != null && client?.isConnected!!) {
                    client?.disconnect()
                }
                textview.text = "Desconectado"
            } catch (e: MqttException) {
                Log.e(TAG, "Error en MQTT.", e)
                textview.text = "Error al desconectar"
            }
        }

        buttonON.setOnClickListener {
            Log.i(TAG, "Boton presionado")
            try {
                val mensaje = "ON"
                Log.i(TAG, "Publicando mensaje: $mensaje")
                val message = MqttMessage(mensaje.toByteArray())
                message.qos = qos
                client?.publish(topic_led, message)
                Log.i(TAG, "Mensaje publicado")
                textview.text = "Publicado ON"
            } catch (e: MqttException) {
                Log.e(TAG, "Error en MQTT.", e)
                textview.text = "Error al publicar"
            }
        }

        buttonOFF.setOnClickListener {
            Log.i(TAG, "Boton presionado")
            try {
                val mensaje = "OFF"
                Log.i(TAG, "Publicando mensaje: $mensaje")
                val message = MqttMessage(mensaje.toByteArray())
                message.qos = qos
                client?.publish(topic_led, message)
                Log.i(TAG, "Mensaje publicado")
                textview.text = "Publicado OFF"
            } catch (e: MqttException) {
                Log.e(TAG, "Error en MQTT.", e)
                textview.text = "Error al publicar"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (client != null && client?.isConnected!!) {
                client?.disconnect()
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Error en MQTT.", e)
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
    }

    override fun connectionLost(cause: Throwable?) {
        Log.d(TAG, "Conexión perdida...")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.d(TAG, "Entrega completa!")
    }

    override fun onShake() {
        Log.i(TAG, "Shake!")
        try {
            val mensaje = "Shake!"
            Log.i(TAG, "Publicando mensaje: $mensaje")
            val message = MqttMessage(mensaje.toByteArray())
            message.qos = qos
            client?.publish(topic_led, message)
            Log.i(TAG, "Mensaje publicado")
            textview.text = "Publicado Shake!"
        } catch (e: MqttException) {
            Log.e(TAG, "Error en MQTT.", e)
            textview.text = "Error al publicar"
        }
    }
}
