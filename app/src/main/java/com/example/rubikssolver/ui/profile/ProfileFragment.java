package com.example.rubikssolver.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rubikssolver.Cuber;
import com.example.rubikssolver.LoginActivity;
import com.example.rubikssolver.MainActivity;
import com.example.rubikssolver.R;
import com.example.rubikssolver.Solve;
import com.example.rubikssolver.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileFragment extends Fragment {
    Button btnLogout;
    TextView txtViewNoSolves,txtViewBestSolve,txtViewCuberName,txtViewLeaderboardRank;
    ProgressBar progressBarProfile;
    FragmentProfileBinding binding;
    CardView cardViewProfile;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding =  FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnLogout = binding.btnLogout;
        txtViewNoSolves = binding.txtViewNoSolves;
        txtViewBestSolve = binding.txtViewBestSolve;
        txtViewCuberName = binding.txtViewProfileCuberName;
        txtViewLeaderboardRank = binding.txtViewLeaderboardRank;
        progressBarProfile = binding.progressBarProfile;
        cardViewProfile = binding.cardViewProfile;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchAndUpdateProfile(userId);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });



        return  root;
    }
    private void fetchAndUpdateProfile(String uid){
        progressBarProfile.setVisibility(View.VISIBLE);
        cardViewProfile.setVisibility(View.GONE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("solves")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int solveCount=0;
                        long bestSolveMilliseconds = Long.MAX_VALUE;
                        int bestMinutes = Integer.MAX_VALUE;
                        int bestSeconds = Integer.MAX_VALUE;
                        int bestMilliseconds = Integer.MAX_VALUE;
                        String username = "";

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Solve solve = document.toObject(Solve.class);
                            if (solve.getSolved()) {
                                solveCount++;
                                int minutes = solve.getDurationMinutes();
                                int seconds = solve.getDurationSeconds();
                                int milliseconds = solve.getDurationMilliseconds();
                                if (minutes < bestMinutes ||
                                        (minutes == bestMinutes && seconds < bestSeconds) ||
                                        (minutes == bestMinutes && seconds == bestSeconds && milliseconds < bestMilliseconds)) {
                                    bestMinutes = minutes;
                                    bestSeconds = seconds;
                                    bestMilliseconds = milliseconds;
                                    username = solve.getUsername();
                                }
                            }
                        }
                        updateProfileDocument(username, solveCount, bestMinutes,bestSeconds,bestMilliseconds);
                        progressBarProfile.setVisibility(View.GONE);
                        cardViewProfile.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        progressBarProfile.setVisibility(View.GONE);
                        cardViewProfile.setVisibility(View.VISIBLE);
                        Log.e("Firestore", "Error fetching solves", e);
                    }
                });

    }
    private void updateProfileDocument(String username, int solveCount, int bestMinutes , int bestSeconds, int bestMilliseconds) {
            txtViewCuberName.setText(username);
            txtViewNoSolves.setText(String.valueOf(solveCount));
            txtViewBestSolve.setText(String.valueOf(bestMinutes) + ":" + String.valueOf(bestSeconds) + ":" + String.valueOf(bestMilliseconds));
            txtViewLeaderboardRank.setText("#1");
    }
}