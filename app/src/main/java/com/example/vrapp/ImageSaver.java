package com.example.vrapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageSaver implements Runnable {

    /**
     * The JPEG image
     */
    private final Bitmap mBmp;
    /**
     * The file we save the image into.
     */
    private final File mFile;

    ImageSaver(Bitmap bmp, File file) {
        mBmp = bmp;
        mFile = file;
    }

    @Override
    public void run() {
        if (mFile.exists()) mFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(mFile);
            mBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
