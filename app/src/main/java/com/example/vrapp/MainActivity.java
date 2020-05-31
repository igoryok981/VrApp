package com.example.vrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button openButton, createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openButton = (Button)findViewById(R.id.open_button);
        createButton = (Button) findViewById(R.id.create_button);

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewActivity();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreatePanoActivity();
            }
        });
    }

    private void openViewActivity() {
        Intent intent = new Intent(this, ViewActivity.class);
        startActivity(intent);
    }

    private void openCreatePanoActivity() {
        Intent intent = new Intent(this, CreatePanoActivity.class);
        startActivity(intent);
    }
}
