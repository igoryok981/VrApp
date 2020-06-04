package com.example.vrapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
//import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreatePanoActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private Button captureBtn, saveBtn; // used to interact with capture and save Button in UI
    private SurfaceView mSurfaceView, mSurfaceViewOnTop; // used to display the camera frame in UI
    private boolean isPreview; // Is the camera frame displaying?
    private boolean safeToTakePicture = true; // Is it safe to capture
    private List<Mat> listImage = new ArrayList<>();
    private List<String> photos = new ArrayList<>();

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler = null;
    CameraService[] myCameras = null;
    private CameraManager mCameraManager = null;
    private TextureView mImageView;
    private int pCount = 0;

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pano);

        isPreview = false;
        mSurfaceViewOnTop = (SurfaceView) findViewById(R.id.surfaceViewOnTop);
        mSurfaceViewOnTop.setZOrderOnTop(true);    // necessary
        mSurfaceViewOnTop.getHolder().setFormat(PixelFormat.TRANSPARENT);
        captureBtn = (Button) findViewById(R.id.capture);
        saveBtn = (Button) findViewById(R.id.save);
        mImageView = (TextureView) findViewById(R.id.imageView);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||
                (ContextCompat.checkSelfPermission(CreatePanoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!myCameras[0].isOpen())
                    myCameras[0].openCamera();
                else {
                    String[] lPhotos = new String[photos.size()];
                    photos.toArray(lPhotos);
                    if (stitch(lPhotos)) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Успешно", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Ошибка", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                //if (myCameras[0].isOpen()) myCameras[0].makePhoto();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (!myCameras[0].isOpen()) myCameras[0].openCamera();
                if (myCameras[0].isOpen()) myCameras[0].makePhoto();
            }
        });
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Получение списка камер с устройства
            myCameras = new CameraService[mCameraManager.getCameraIdList().length];

            //for (String cameraID : mCameraManager.getCameraIdList()) {
            //Log.i(LOG_TAG, "cameraID: "+cameraID);
            //int id = Integer.parseInt(cameraID);
            // создаем обработчик для камеры
            myCameras[0] = new CameraService(mCameraManager, "0");

            //}
        } catch (CameraAccessException e) {
            //Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native boolean stitch(String[] imageNames);

    public class CameraService {

        private File mFile = new File("storage/emulated/0/Photos/", "test" + pCount + ".jpg");
        private String mCameraID;
        private CameraDevice mCameraDevice = null;
        private CameraCaptureSession mCaptureSession;
        private ImageReader mImageReader;

        public CameraService(CameraManager cameraManager, String cameraID) {

            mCameraManager = cameraManager;
            mCameraID = cameraID;

        }

        public void makePhoto() {
            try {
                // This is the CaptureRequest.Builder that we use to take a picture.
                final CaptureRequest.Builder captureBuilder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(mImageReader.getSurface());
                CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                   @NonNull CaptureRequest request,
                                                   @NonNull TotalCaptureResult result) {

                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Фото добавлено", Toast.LENGTH_LONG);
                        toast.show();
                        createCameraPreviewSession();
                    }
                };

                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
                mCaptureSession.capture(captureBuilder.build(), CaptureCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
                = new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader reader) {
                mFile = new File("storage/emulated/0/Photos/", "test" + pCount + ".jpg");
                Image img = reader.acquireNextImage();
                ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, false);
                mBackgroundHandler.post(new ImageShow(bitmapImage, mSurfaceViewOnTop, mImageView.getHeight()));
                mBackgroundHandler.post(new ImageSaver(bitmapImage, mFile));
                photos.add(mFile.getPath());
                pCount++;


            }

        };

        private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                //Log.i(LOG_TAG, "Open camera  with id:"+mCameraDevice.getId());
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                mCameraDevice.close();
                //Log.i(LOG_TAG, "disconnect camera  with id:"+mCameraDevice.getId());
                mCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                //Log.i(LOG_TAG, "error! camera id:"+camera.getId()+" error:"+error);
            }
        };


        private void createCameraPreviewSession() {

            mImageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);

            SurfaceTexture texture = mImageView.getSurfaceTexture();

            texture.setDefaultBufferSize(1920, 1080);
            Surface surface = new Surface(texture);

            try {
                final CaptureRequest.Builder builder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                builder.addTarget(surface);

                mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mCaptureSession = session;
                                try {
                                    mCaptureSession.setRepeatingRequest(builder.build(), null, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {
                            }
                        }, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        public boolean isOpen() {
            if (mCameraDevice == null) {
                return false;
            } else {
                return true;
            }
        }

        public void openCamera() {
            try {

                //if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(CreatePanoActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mCameraManager.openCamera(mCameraID, mCameraCallback, mBackgroundHandler);
                }
                //}

            } catch (CameraAccessException e) {
                //Log.i(LOG_TAG,e.getMessage());
            }
        }

        public void closeCamera() {

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
    }


    @Override
    public void onPause() {
        if(myCameras[0].isOpen()){myCameras[0].closeCamera();}
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

}
