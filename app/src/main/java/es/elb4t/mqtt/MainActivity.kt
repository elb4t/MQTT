package es.elb4t.mqtt

import android.app.Activity
import android.os.Bundle
import android.util.Log
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence



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
class MainActivity : Activity() {

    companion object {
        private val TAG = "Things"
        private val topic = "elllabel/test"
        private val hello = "Hello world!"
        private val qos = 1
        private val broker = "tcp://iot.eclipse.org:1883"
        private val clientId = "lens_M8gAXD2AXDuhTkq8y7XzrW6yDkQ"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val client = MqttClient(broker, clientId, MemoryPersistence())
            Log.i(TAG, "Conectando al broker $broker")
            client.connect()
            Log.i(TAG, "Conectado")
            Log.i(TAG, "Publicando mensaje: $hello")
            val message = MqttMessage(hello.toByteArray())
            message.qos = qos
            client.publish(topic, message)
            Log.i(TAG, "Mensaje publicado")
            client.disconnect()
            Log.i(TAG, "Desconectado")
        } catch (e: MqttException) {
            Log.e(TAG, "Error en MQTT.", e)
        }

    }
}
