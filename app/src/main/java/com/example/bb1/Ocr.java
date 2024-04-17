package com.example.bb1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Ocr extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "Ocr";
    private ImageView imageView;
    private String encodedImage;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private PreviewView viewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr);

        imageView = findViewById(R.id.imageView);
        Button captureButton = findViewById(R.id.captureButton);
        Button captureButton0 = findViewById(R.id.captureButton0);
        Button uploadButton = findViewById(R.id.uploadButton);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.ocr);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(Ocr.this, Main.class));
                        return true;

                    case R.id.Calendar:
                        startActivity(new Intent(Ocr.this, CalendarActivity.class));
                        return true;

                    case R.id.ocr:
                        finish();
                        startActivity(new Intent(Ocr.this, Ocr.class));
                        return true;

                    case R.id.mypage:
                        startActivity(new Intent(Ocr.this, Mypage.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        viewFinder = findViewById(R.id.viewFinder);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        captureButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Ocr.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Ocr.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    requestCameraPermission();
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (encodedImage != null) {
                    uploadImageToServer(encodedImage);
                    imageView.setImageBitmap(null);
                    encodedImage = null;
                } else {
                    Toast.makeText(Ocr.this, "먼저 사진을 촬영하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Button mappingButton = findViewById(R.id.mappingButton);
        mappingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Ocr.this, Mapping.class);
                startActivity(intent);
            }
        });
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_270)
                .build();

        Preview preview = new Preview.Builder()
                .setTargetRotation(Surface.ROTATION_90)
                .build();

        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private void takePicture() {
        if (ContextCompat.checkSelfPermission(Ocr.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (imageCapture != null) {
                File photoFile = getOutputFile();
                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                processCapturedImage(photoFile);
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Log.e(TAG, "Image capture failed: " + exception.getMessage());
                                Toast.makeText(Ocr.this, "사진 촬영에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            requestCameraPermission();
        }
    }

    private void processCapturedImage(File photoFile) {
        if (photoFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            encodedImage = compressAndResizeImage(bitmap, 800, 600, 80); // 이미지를 압축하고 크기를 조절
            imageView.setImageBitmap(bitmap);
        }
    }

    private String compressAndResizeImage(Bitmap image, int maxWidth, int maxHeight, int quality) {
        Bitmap scaledBitmap;
        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            float aspectRatio = (float) image.getWidth() / (float) image.getHeight(); // Corrected
            int newWidth = maxWidth;
            int newHeight = (int) (newWidth / aspectRatio);
            scaledBitmap = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
        } else {
            scaledBitmap = image;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    private File getOutputFile() {
        File mediaDir = new File(getExternalFilesDir(null), "images");
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return null;
            }
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        return new File(mediaDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    private void uploadImageToServer(final String encodedImage) {
        String serverUrl = "http://192.168.158.177:3000/processImage";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("uploadImageToServer", "Request sent to server");
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String message = jsonResponse.getString("message");
                            Toast.makeText(Ocr.this, "이미지 업로드가 완료되었습니다", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ServerError", error.toString());
                        Toast.makeText(Ocr.this, "이미지 업로드 실패.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", encodedImage);
                return params;
            }
        };

        int timeoutMilliseconds = 60000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeoutMilliseconds,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
