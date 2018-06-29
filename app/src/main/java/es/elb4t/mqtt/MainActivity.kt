package es.elb4t.mqtt

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.io.IOException




/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity(), MqttCallback {

    companion object {
        private val TAG = "Things"
        private val topic = "elllabel/test"
        private val hello = "Hello world! Android Things conectada."
        private val qos = 1
        private val broker = "tcp://iot.eclipse.org:1883"
        private val clientId = "lens_M8gAXD2AXDuhTkq8y7XzrW6yDkQ"
        private val topic_gestion = "elllabel/gestion"
        private val topic_led = "elllabel/led"
        private val topic_boton = "elllabel/boton"
    }

    private val PIN_LED = "BCM18"
    private val PIN_BUTTON = "BCM23"
    var mLedGpio: Gpio? = null
    private var mButtonGpio: Gpio? = null
    lateinit var client: MqttClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val service = PeripheralManager.getInstance()
        try {
            mLedGpio = service.openGpio(PIN_LED)
            mLedGpio?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            mButtonGpio = service.openGpio(PIN_BUTTON)
            mButtonGpio?.setDirection(Gpio.DIRECTION_IN)
            mButtonGpio?.setActiveType(Gpio.ACTIVE_LOW)
            mButtonGpio?.setEdgeTriggerType(Gpio.EDGE_FALLING)
            mButtonGpio?.registerGpioCallback(mCallback)
        } catch (e: IOException) {
            Log.e(TAG, "Error en el API PeripheralIO", e)
        }

        try {
            val clientId = MqttClient.generateClientId()
            client = MqttClient(broker, clientId, MemoryPersistence())
            client.setCallback(this)
            val connOpts = MqttConnectOptions()
            connOpts.isCleanSession = true
            connOpts.keepAliveInterval = 60
            connOpts.setWill(topic_gestion, "Android Things desconectada!"
                    .toByteArray(), qos, false)
            Log.i(TAG, "Conectando al broker $broker")
            client.connect(connOpts)
            Log.i(TAG, "Conectado")
            Log.i(TAG, "Publicando mensaje: $hello")
            val message = MqttMessage(hello.toByteArray())
            message.qos = qos
            client.publish(topic_gestion, message)
            Log.i(TAG, "Mensaje publicado")
            client.subscribe(topic_led, qos)
            Log.i(TAG, "Suscrito a $topic_led")
        } catch (e: MqttException) {
            Log.e(TAG, "Error en MQTT.", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (client != null && client.isConnected) {
                client.disconnect()
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Error en MQTT.", e)
        }
        if (mLedGpio != null) {
            try {
                mLedGpio?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error en el API PeripheralIO", e)
            } finally {
                mLedGpio = null
            }
        }
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        val payload = String(message.payload)
        Log.d(TAG, payload)
        when (payload) {
            "ON" -> {
                mLedGpio?.value = true
                Log.d(TAG, "LED ON!")
            }
            "OFF" -> {
                mLedGpio?.value = false
                Log.d(TAG, "LED OFF!")
            }
            "Shake!" -> {
                Log.d(TAG, "Parpadeo!")
                for (i in 0..3) {
                    mLedGpio?.value = true
                    Thread.sleep(500)
                    mLedGpio?.value = false
                    Thread.sleep(500)
                }
            }
            else -> Log.d(TAG, "Comando no soportado")
        }
    }

    override fun connectionLost(cause: Throwable?) {
        Log.d(TAG, "Conexión perdida...")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.d(TAG, "Entrega completa!")
    }

    private val mCallback = GpioCallback {
        Log.i(TAG, "Botón pulsado!")
        try {
            val mensaje = "click!"
            Log.i(TAG, "Publicando mensaje: $mensaje")
            val message = MqttMessage(mensaje.toByteArray())
            message.qos = qos
            client.publish(topic_boton, message)
            Log.i(TAG, "Mensaje publicado")
        } catch (e: MqttException) {
            Log.e(TAG, "Error en MQTT.", e)
        }
        true // Mantenemos el callback activo
    }
}
