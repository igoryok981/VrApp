package com.example.vrapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class OnSwipeTouchListener implements View.OnTouchListener, Runnable {
    private final GestureDetector gestureDetector;
    private final SurfaceView mSurfaceView;
    private final Bitmap mBmp;
    private final int mHeight;
    private int k = -1;
    private int h = 0;
    Context context;
    OnSwipeTouchListener(Context ctx, View mainView, SurfaceView surfaceView, Bitmap bmp, int height) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        mainView.setOnTouchListener(this);
        context = ctx;
        mBmp = bmp;
        mSurfaceView = surfaceView;
        mHeight = height;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
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
                canvas.drawBitmap(scaleImage, scaleImage.getWidth()* 1 / 3 * k, h, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                mSurfaceView.getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    public class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
    void onSwipeRight() {
        if (k != -1)
            k = -1;
        else
            k = 100;
        run();
        this.onSwipe.swipeRight();
    }
    void onSwipeLeft() {
        if (k != -1)
            k = -1;
        else
            k = 100;
        run();
        this.onSwipe.swipeLeft();
    }
    void onSwipeTop() {
        if (h == -mHeight / 2)
            h = -mHeight;
        else
            if (h == mHeight)
                h = mHeight / 2;
            else
                if (h == mHeight / 2)
                    h = 0;
                else
                    if (h != -mHeight)
                        h = -mHeight / 2;
        run();
        this.onSwipe.swipeTop();
    }
    void onSwipeBottom() {
        if (h == mHeight / 2)
            h = mHeight;
        else
            if (h == -mHeight)
                h = -mHeight / 2;
            else
                if (h == -mHeight / 2)
                    h = 0;
                else
                    if (h != mHeight)
                        h = mHeight / 2;
        run();
        this.onSwipe.swipeBottom();
    }
    interface onSwipeListener {
        void swipeRight();
        void swipeTop();
        void swipeBottom();
        void swipeLeft();
    }
    onSwipeListener onSwipe;
}
