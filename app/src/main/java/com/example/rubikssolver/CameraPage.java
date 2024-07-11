package com.example.rubikssolver;

import static androidx.core.graphics.drawable.DrawableKt.toBitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.ScriptGroup;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rubikssolver.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraPage extends AppCompatActivity {
    private boolean isAppInForeground = false;
    private long cameraPreviewStartTime = 0;
    private ApiService apiService;
    private List<List<String>> checkingCube;
    private int frameCounter = 0;
    private int noFacesDetected =0;
    private boolean isProcessingFrame = false;
    private long lastProcessedTimestamp = 0;
    private static final long PROCESSING_INTERVAL = 1000;
    private String facesDetected = "";
    TextView txtViewFacesDetected;
    Context context;
    CameraDevice cameraDevice;
    TextureView textureViewCamera;
    CameraManager cameraManager;
    Handler handler;
    Boolean toVerifySolve;
    Map<String,List<String>> cube;
    List<List<String>> detectedCube;
    SurfaceTexture surfaceTexture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);


            initializeVariables();
            initializeViews();

            Intent intent = getIntent();
            ArrayList<String> scrambledList =intent.getStringArrayListExtra("scrambleString");
            toVerifySolve = intent.getBooleanExtra("toVerifySolve",false);
            Log.d("toVerifySolve","toVerifySolve");


            if(scrambledList != null && !scrambledList.isEmpty()){
                checkingCube = makeCubeFromIntent(scrambledList);
                Log.d("cubeMade", checkingCube.toString());
            }
            else {
                Toast.makeText(this, "Check Internet connection and restart app", Toast.LENGTH_SHORT).show();
                finish();
//                return;
            }
            resetState();
            setupCamera();


            return insets;
        });
    }

    private void setupCamera() {
        HandlerThread handlerThread = new HandlerThread("videoThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        textureViewCamera.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                try {
                    open_camera();
                } catch (CameraAccessException e) {
                    Toast.makeText(CameraPage.this, "Error Accessing the camera", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                stopCameraPreview();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

                if (!isAppInForeground) {
                    return;
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - cameraPreviewStartTime < 2000) {
                    return;  // Ignore frames in the first second
                }

                if (cube.size() == 6) {
                    boolean isVerified = checkCubeIsSame(cube, checkingCube,toVerifySolve);
                    Log.d("cube",cube.toString());
                    Log.d("cube1",String.valueOf(isVerified));
                    finishWithResult(isVerified);
                    return;
                }

                long currentTimestamp = System.currentTimeMillis();
                if (!isProcessingFrame && (currentTimestamp - lastProcessedTimestamp) >= PROCESSING_INTERVAL) {
                    processFrame();
                }
            }
        });
    }


    private void initializeViews() {
        txtViewFacesDetected = findViewById(R.id.txtViewFacesDetected);
        textureViewCamera = findViewById(R.id.textureCameraView);
    }

    private void initializeVariables() {
        context = this;
        cube = new HashMap<>();
        detectedCube = new ArrayList<>();
        apiService = RetrofitClient.getClient().create(ApiService.class);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    private void finishWithResult(boolean isVerified) {
        Log.d("CameraPage", "Finishing with result: isVerified = " + isVerified + ", toVerifySolve = " + toVerifySolve);
        stopCameraPreview();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isVerified", isVerified);
        resultIntent.putExtra("cubeSolve", toVerifySolve ? "solve" : "cube");
        setResult(RESULT_OK, resultIntent);
        Log.d("CameraPage", "setResult called with RESULT_OK");
        finish();
    }



    @SuppressLint("MissingPermission")
    void open_camera() throws CameraAccessException {

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        cameraManager.openCamera(cameraManager.getCameraIdList()[0], new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                surfaceTexture = textureViewCamera.getSurfaceTexture();
                Surface surface = new Surface(surfaceTexture);
                try {
                    CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    captureRequestBuilder.addTarget(surface);

                    // You can now use the captureRequestBuilder to build your capture request
                    CaptureRequest captureRequest = captureRequestBuilder.build();
                    List<Surface> surfaceList = new ArrayList<>();
                    surfaceList.add(surface);
                    cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.setRepeatingRequest(captureRequest, null, null);
                                cameraPreviewStartTime = System.currentTimeMillis();
                            } catch (CameraAccessException e) {
                                Toast.makeText(getApplicationContext(), "Error Accessing the camera", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Error configuring the camera", Toast.LENGTH_SHORT).show();
                        }
                    }, handler);


                } catch (CameraAccessException e) {
                    Toast.makeText(getApplicationContext(), "Error Accessing the camera", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                stopCameraPreview();
                resetState();
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }, handler);
    }





        @Override
        protected void onDestroy() {
            super.onDestroy();
            stopCameraPreview();
            if (cameraDevice != null) {
                cameraDevice.close();
            }
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            if (textureViewCamera != null) {
                textureViewCamera.setSurfaceTextureListener(null);
                textureViewCamera = null;
            }
        }
    private void processFrame() {
        if (textureViewCamera.getBitmap() != null) {
            isProcessingFrame = true;
            Bitmap bitmap = textureViewCamera.getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                makeApiCall(encodedImage);
            } finally {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    Log.e("CameraPage", "Error closing ByteArrayOutputStream", e);
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        } else {
            Toast.makeText(context, "Error obtaining image from camera", Toast.LENGTH_SHORT).show();
        }
    }
  private void makeApiCall(String encodedImage){
      ImageRequest imageRequest = new ImageRequest(encodedImage);
      Call<ApiResponse> call = apiService.predictImage(imageRequest);
      call.enqueue(new Callback<ApiResponse>() {
          @Override
          public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
              if (response.isSuccessful()) {
                  ApiResponse apiResponse = response.body();
                  if (apiResponse != null && apiResponse.isSuccess()) {
                      List<String> detections = apiResponse.getDetections();
                      Log.d("detections",detections.toString());
                      if (!detections.isEmpty()) {
                          String cubeCenter = detections.get(4);
                          if (!cube.containsKey(cubeCenter)) {
                              cube.put(cubeCenter, detections);
                              detectedCube.add(detections);
                              runOnUiThread(() -> {
                                  txtViewFacesDetected.setText("Faces Detected - " + (cube.size()));
                              });
                          }
                      }
                  } else {
//                      Toast.makeText(getApplicationContext(), "API call failed", Toast.LENGTH_SHORT).show();
                      Log.d("fail","failureAPI");
                  }
              } else {
//                  Toast.makeText(getApplicationContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                  Log.d("fail","failure"+response.code());
              }
              isProcessingFrame = false;
              lastProcessedTimestamp = System.currentTimeMillis();

          }

          @Override
          public void onFailure(Call<ApiResponse> call, Throwable t) {
//              Toast.makeText(getApplicationContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
              Log.d("fail","failureNetwork");
              isProcessingFrame = false;
              lastProcessedTimestamp = System.currentTimeMillis();
          }
      });
  }
    private List<List<String>> makeCubeFromIntent(ArrayList<String> intentScrambledCube){
        List<List<String>> scrambledCube = new ArrayList<>();
        List<String> tempString = new ArrayList<>();
        for (int i = 0; i < intentScrambledCube.size(); i++) {
            tempString.add(intentScrambledCube.get(i));
            if ((i + 1) % 9 == 0) {
                scrambledCube.add(new ArrayList<>(tempString));
                tempString.clear();
            }
        }
        return scrambledCube;
    }
    private boolean checkCubeIsSame(Map<String,List<String>> detectedCube,List<List<String>> checkingCube,Boolean toVerifySolve){
        for(int i=0;i<checkingCube.size();i++){
            Map<String,Integer> temp1 = new HashMap<>();
            Map<String,Integer> temp2 = new HashMap<>();
            String center = checkingCube.get(i).get(4);
            for(int j =0;j<checkingCube.get(0).size();j++){
                String str1 = detectedCube.get(center).get(j);
                String str2 = checkingCube.get(i).get(j);
                if(temp1.containsKey(str1)){
                    temp1.put(str1,temp1.get(str1)+1);
                }
                else{
                    temp1.put(str1,1);
                }
                if(temp2.containsKey(str2)){
                    temp2.put(str2,temp2.get(str2)+1);
                }
                else{
                    temp2.put(str2,1);
                }
            }
            if(!toVerifySolve){
                if(temp1.equals(temp2)){
                    return true;
                }
            }
            else{
                if(!temp1.equals(temp2)){
                    return false;
                }
            }

            temp1.clear();
            temp2.clear();
        }
        return toVerifySolve;
    }
    private void stopCameraPreview() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if (textureViewCamera != null) {
            textureViewCamera.setVisibility(View.INVISIBLE);
            textureViewCamera.setVisibility(View.VISIBLE);
        }
        cameraPreviewStartTime = 0;
    }
    private void resetState() {
        cube.clear();
        detectedCube.clear();
        isProcessingFrame = false;
        lastProcessedTimestamp = 0;
        frameCounter = 0;
        noFacesDetected = 0;
        facesDetected = "";
        cameraPreviewStartTime = 0;
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopCameraPreview();
        isAppInForeground = false;
        if (textureViewCamera != null) {
            textureViewCamera.setVisibility(View.INVISIBLE);
            textureViewCamera.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        isAppInForeground = true;
        super.onResume();
        if (textureViewCamera != null) {
            textureViewCamera.setVisibility(View.INVISIBLE);
            textureViewCamera.setVisibility(View.VISIBLE);
            if (textureViewCamera.isAvailable()) {
                try {
                    open_camera();
                } catch (CameraAccessException e) {
                    Log.e("CameraPage", "Failed to open camera", e);
                }
            }
        }
    }
}