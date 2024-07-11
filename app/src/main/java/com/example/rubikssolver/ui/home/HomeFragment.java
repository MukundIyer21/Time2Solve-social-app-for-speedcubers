package com.example.rubikssolver.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rubikssolver.ApiService;
import com.example.rubikssolver.CameraPage;
import com.example.rubikssolver.Cuber;
import com.example.rubikssolver.R;
import com.example.rubikssolver.RetrofitClient;
import com.example.rubikssolver.ScrambleResponse;
import com.example.rubikssolver.Solve;
import com.example.rubikssolver.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final int CAMERA_PAGE_REQUEST_CODE = 1001;
    private List<List<String>> scrambledCube;
    private boolean isVerified;
    private ApiService apiService;
    FloatingActionButton btnPlay,btnReset;
    ProgressBar progressBarScramble;
    Button btnVerifyCube,btnVerifySolve;
    TextView txtViewTimer,txtViewScramble;
    private FragmentHomeBinding binding;
    long milliSecond,startTime,timeBuff,updateTime = 0L;
    int seconds,minutes,milliseconds;
    Handler handler;
    boolean playing = false;
    String cubeSolve;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            milliSecond = SystemClock.uptimeMillis() - startTime;
            updateTime = timeBuff+milliSecond;
            seconds = (int) (updateTime/1000);
            minutes  = seconds/60;
            seconds = seconds%60;
            milliseconds = (int) (updateTime%1000);
            txtViewTimer.setText(MessageFormat.format("{0}:{1}:{2}",minutes,String.format(Locale.getDefault(),"%02d",seconds),String.format(Locale.getDefault(),"%02d",milliseconds) ));
            handler.postDelayed(this,0);
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        initialize all the materials
        btnPlay = binding.btnPlay;
        btnVerifyCube = binding.btnVerifyCube;
        btnVerifySolve = binding.btnVerifySolve;
        txtViewTimer = binding.txtViewTimer;
        txtViewScramble = binding.txtViewScramble;
        btnReset = binding.btnReset;
        progressBarScramble = binding.progressBarScramble;


        progressBarScramble.setVisibility(View.VISIBLE);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        boolean b =  getScramble();
        progressBarScramble.setVisibility(View.GONE);

        handler = new Handler(Looper.getMainLooper());

        Intent intent = getActivity().getIntent();
        isVerified = intent.getBooleanExtra("isVerified",false);
        cubeSolve = "";
        cubeSolve = intent.getStringExtra("cubeSolve");



        btnVerifySolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> scrambledString = new ArrayList<>();
                String[] tempSolved = {"green","green","green","green","green","green","green","green","green","blue","blue","blue","blue","blue","blue","blue","blue","blue","red","red","red","red","red","red","red","red","red","orange","orange","orange","orange","orange","orange","orange","orange","orange","white","white","white","white","white","white","white","white","white","yellow","yellow","yellow","yellow","yellow","yellow","yellow","yellow","yellow"};
                scrambledString.addAll(Arrays.asList(tempSolved));
                Intent intent1 = new Intent(getActivity(),CameraPage.class);
                intent1.putStringArrayListExtra("scrambleString",scrambledString);
                intent1.putExtra("toVerifySolve",true);
                getActivity().startActivityForResult(intent1,CAMERA_PAGE_REQUEST_CODE);
            }
        });


        btnVerifyCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraPage.class);
                ArrayList<String> intentScrambledCube = new ArrayList<>();
                for(List<String> i:scrambledCube){
                    for(String j:i){
                        intentScrambledCube.add(j);
                    }
                }
                intent.putStringArrayListExtra("scrambleString",intentScrambledCube);
                Log.d("sentArrayList",intentScrambledCube.toString());
                Toast.makeText(getActivity(),"here",Toast.LENGTH_SHORT).show();
                getActivity().startActivityForResult(intent, CAMERA_PAGE_REQUEST_CODE);
            }
        });




        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isVerified){
                    Snackbar.make(getView(),"Cube not verified!, verify again if incorrect Verification",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(!playing){
                    btnPlay.setImageResource(R.drawable.ic_pause);
                    startTime = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable,0);
                    btnReset.setEnabled(false);
                    btnVerifyCube.setEnabled(true);
                    playing=true;
                }
                else{
                    btnPlay.setImageResource(R.drawable.ic_play);
                    timeBuff+=milliSecond;
                    handler.removeCallbacks(runnable);
                    btnVerifySolve.setVisibility(View.VISIBLE);

                    Snackbar.make(getView(),"Verify your solve now",Snackbar.LENGTH_SHORT).show();

                    btnPlay.setEnabled(false);
                    btnReset.setEnabled(true);
                    btnVerifyCube.setVisibility(View.GONE);

                    getScramble();

                    playing=false;
                }

            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                milliSecond =0L;
                startTime =0L;
                timeBuff = 0L;
                updateTime = 0L;
                seconds = 0;
                minutes = 0;
                milliseconds = 0;
                txtViewTimer.setText("00:00:000");
                btnPlay.setEnabled(true);
                btnReset.setEnabled(false);
                btnVerifySolve.setVisibility(View.GONE);
                btnVerifyCube.setVisibility(View.VISIBLE);
            }
        });
        txtViewTimer.setText("00:00:000");



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void handleCameraResult(boolean isVerified, String cubeSolve) {
        this.isVerified = isVerified;
        this.cubeSolve = cubeSolve;
        Log.d("HomeFragment", "handleCameraResult: isVerified = " + isVerified + ", cubeSolve = " + cubeSolve);
        if((cubeSolve!=null) && (cubeSolve.equals("solve")) && (isVerified==true)){
            Log.d("solved","saved");
            saveDataInFirebase();
            this.isVerified = false;
        }
    }
    public void saveDataInFirebase(){
        String finalTime = txtViewTimer.getText().toString();
        String[] timesSplit = finalTime.split(":");
        int[] times = new int[timesSplit.length];
        for(int i=0;i<times.length;i++){
            times[i] = Integer.valueOf(timesSplit[i]);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        String[] username = new String[1];

        FirebaseFirestore.getInstance().collection("profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            Cuber cuber = documentSnapshot.toObject(Cuber.class);
                            username[0] = cuber.getUserName();

                            FirebaseUser CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                            DocumentReference documentReference = FirebaseFirestore.getInstance()
                                    .collection("solves")
                                    .document();
                            Map<String,Object> data = new HashMap<>();
                            data.put("durationMinutes",times[0]);
                            data.put("durationSeconds", times[1]);
                            data.put("durationMilliseconds", times[2]);
                            data.put("username",username[0]);
                            data.put("solved", true);
                            data.put("userId", CurrentUser.getUid());

                            documentReference.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Snackbar.make(getView(), "The solve was saved", Snackbar.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Snackbar.make(getView(), "The solve was not saved", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                        else {
                            Snackbar.make(getView(),"The solve was not saved",Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });






    }
    private boolean getScramble(){
        boolean[] successfulApicall = {false};
        Call<ScrambleResponse> call = apiService.getScramble();
        call.enqueue(new Callback<ScrambleResponse>() {
            @Override
            public void onResponse(Call<ScrambleResponse> call, Response<ScrambleResponse> response) {
                if (response.isSuccessful()) {
                    ScrambleResponse scrambleResponse = response.body();
                    if (scrambleResponse != null) {
                        String scrambleString = scrambleResponse.getScrambleString();
                        scrambledCube = scrambleResponse.getScrambledCube();
                        txtViewScramble.setText(scrambleString);
                        successfulApicall[0]=true;
                    } else {
                        Log.d("apiError","empty response");
                        txtViewScramble.setText("Check Internet Connectivity");
                    }
                } else {
                    Log.d("apiError","Error: " + response.code());
                    txtViewScramble.setText("Check Internet Connectivity");
                }
            }

            @Override
            public void onFailure(Call<ScrambleResponse> call, Throwable t) {
                Log.d("apiError","Network Error"+t.getMessage());
            }
        });
        return successfulApicall[0];
    }
    static CollectionReference getCollectionReference(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("solves")
                .document(currentUser.getUid()).collection("my_solves");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isVerified==false && cubeSolve!=null && cubeSolve.equals("solve")){
            Snackbar.make(getView(),"The solve was not successful/verified",Snackbar.LENGTH_SHORT).show();
        }
    }


}