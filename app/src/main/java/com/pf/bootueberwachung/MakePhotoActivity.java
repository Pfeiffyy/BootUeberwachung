package com.pf.bootueberwachung;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pf.bootueberwachung.mail.GMailSender;
import com.pf.bootueberwachung.photo.PhotoHandler;
import com.pf.bootueberwachung.utils.GLOBALS;

import java.util.Locale;

import de.vogella.cameara.api.R;

public class MakePhotoActivity extends Activity {
    public final static String DEBUG_TAG = "MakePhotoActivity";
    private Camera camera;
    private int cameraId = 0;
    private CountDownTimer timer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = Camera.open(cameraId);
            }
        }
        final TextView textView = (TextView) findViewById(R.id.text_view);

        timer = new CountDownTimer(GLOBALS.INTERVALLPHOTO * 1000, 20) {

            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText(String.format(Locale.getDefault(), "%d sec.", millisUntilFinished / 1000L));
            }

            @Override
            public void onFinish() {
                try {
                    camera.takePicture(null, null,
                            new PhotoHandler(getApplicationContext()));
                    if (GLOBALS.SENDMAILWITHPHOTO) {
                        new MailTask().execute();
                    }
                    timer.start();


                } catch (Exception e) {
                    Log.e("Error", "Error: " + e.toString());
                }
            }
        }.start();

    }


    public void onClick(View view) {
        camera.startPreview();
        camera.takePicture(null, null,
                new PhotoHandler(getApplicationContext()));

    }


    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//            if (info.facing == CameraInfo.CAMERA_FACING_) {
                Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }


    @Override
    protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private class MailTask extends AsyncTask<Void, Void, Void> {
        String result;

        @Override
        protected Void doInBackground(Void... voids) {
            GMailSender sender = new GMailSender("uebboot@gmail.com", "negerin12");
            try {
                sender.sendMail("This is Subject",
                        "This is Body",
                        "uebboot@gmail.com",
                        "post@pfeifferdirk.de", GLOBALS.PHOTO);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
        }

    }
}

