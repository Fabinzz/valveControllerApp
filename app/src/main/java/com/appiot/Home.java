package com.appiot;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Home extends AppCompatActivity {
    private static final String TAG = "MQTT";
    private static final String MQTTSERVER = "tcp://test.mosquitto.org:1883";
    private static final String VALVE_COMMAND_TOPIC = "valve/controller";
    private static final String VALVE_STATUS_TOPIC = "valve/status";
    private static final String DISTANCE_TOPIC = "sensor/distance"; // Se precisar, adicione o tópico de distância

    private MqttClient mqttClient;
    private TextView distanceTextView;
    private TextView statusTextView;
    private Button openButton;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        distanceTextView = findViewById(R.id.distanceTextView);
        statusTextView = findViewById(R.id.statusTextView);
        openButton = findViewById(R.id.openButton);
        closeButton = findViewById(R.id.closeButton);

        connectMQTT();

        // Configurações dos botões
        openButton.setOnClickListener(v -> sendCommand("abrir"));
        closeButton.setOnClickListener(v -> sendCommand("fechar"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    private void connectMQTT() {
        try {
            mqttClient = new MqttClient(MQTTSERVER, MqttClient.generateClientId(), null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.w(TAG, "Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    if (topic.equals(VALVE_STATUS_TOPIC)) {
                        runOnUiThread(() -> statusTextView.setText("Status: " + payload));
                    } else if (topic.equals(DISTANCE_TOPIC)) {
                        runOnUiThread(() -> distanceTextView.setText("Distância: " + payload + " cm"));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Não é necessário para este caso
                }
            });

            // Conectar ao broker
            mqttClient.connect(options); // Chamada correta do método connect

            // Após a conexão, inscrever nos tópicos
            mqttClient.subscribe(VALVE_STATUS_TOPIC);
            mqttClient.subscribe(DISTANCE_TOPIC);

        } catch (MqttException e) {
            Log.e(TAG, "Error connecting to MQTT", e);
        }
    }


    private void sendCommand(String command) {
        try {
            mqttClient.publish(VALVE_COMMAND_TOPIC, new MqttMessage(command.getBytes()));
            Log.d(TAG, "Command sent: " + command);
        } catch (MqttException e) {
            Log.e(TAG, "Error sending command", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                Log.e(TAG, "Error disconnecting", e);
            }
        }
    }
}