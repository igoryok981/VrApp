package com.example.vrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

public class ViewActivity extends AppCompatActivity {

    private PLManager plManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view);
        plManager = new PLManager(this);
        plManager.setContentView((ViewGroup) findViewById(R.id.content_view));
        plManager.onCreate();

        PLSphericalPanorama panorama = new PLSphericalPanorama();
        panorama.getCamera().lookAt(30.0f, 90.0f);

        panorama.setImage(new PLImage(PLUtils.getBitmap(this, R.raw.pano2), false));
        plManager.setPanorama(panorama);
    }

    @Override
    protected void onResume() {
        super.onResume();
        plManager.onResume();
    }

    @Override
    protected void onPause() {
        plManager.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        plManager.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return plManager.onTouchEvent(event);
    }
}
