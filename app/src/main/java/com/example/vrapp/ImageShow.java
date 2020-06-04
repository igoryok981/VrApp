package com.example.vrapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.Image;
import android.view.SurfaceView;

import java.nio.ByteBuffer;

public class ImageShow implements Runnable {
    private final Bitmap mBmp;
    private final SurfaceView mSurfaceView;
    private final int mHeight;

    public ImageShow(Bitmap bmp, SurfaceView surfaceView, int height) {
        mBmp = bmp;
        mSurfaceView = surfaceView;
        mHeight = height;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        try {
            canvas = mSurfaceView.getHolder().lockCanvas(null);
            synchronized (mSurfaceView.getHolder()) {
                // Clear canvas
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                // Scale the image to fit the SurfaceView
                float scale = 1.0f * mHeight / mBmp.getHeight();
                Bitmap scaleImage = Bitmap.createScaledBitmap(mBmp,
                        (int) (scale * mBmp.getWidth()), mHeight , false);
                Paint paint = new Paint();
                // Set the opacity of the image
                paint.setAlpha(200);
                // Draw the image with an offset so we only see one third of image.
                canvas.drawBitmap(scaleImage, -scaleImage.getWidth() * 1 / 3, 0, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                mSurfaceView.getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }
}
