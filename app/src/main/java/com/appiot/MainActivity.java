package com.appiot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialize o botão
        startButton = findViewById(R.id.startButton); // Certifique-se de que o ID corresponde ao botão no layout

        startButton.setOnClickListener(v -> {
            Intent it = new Intent(this, Home.class); // Troque por Home.class se desejar ir para Home
            startActivity(it);
        });
    }
}
