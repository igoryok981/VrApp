package com.example.vrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CreatePanoActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pano);
        ImageStitch imst = new ImageStitch();
    }
}
