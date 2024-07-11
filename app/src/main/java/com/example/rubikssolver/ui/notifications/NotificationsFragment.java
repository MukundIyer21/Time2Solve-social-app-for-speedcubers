package com.example.rubikssolver.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rubikssolver.Solve;
import com.example.rubikssolver.databinding.FragmentNotificationsBinding;
import com.example.rubikssolver.rvLeaderboardAdapter;
import com.example.rubikssolver.ui.home.HomeFragment;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    RecyclerView rvLeaderboard;
    rvLeaderboardAdapter adapter;
    ProgressBar leaderboardProgressbar;
    private FragmentNotificationsBinding binding;
    private FirestoreRecyclerOptions<Solve> options;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        leaderboardProgressbar = binding.leaderboardProgressbar;
        leaderboardProgressbar.setVisibility(View.VISIBLE);



        rvLeaderboard = binding.recyclerLeaderBoard;
        adapter = new rvLeaderboardAdapter(getActivity().getApplicationContext());
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        rvLeaderboard.setAdapter(adapter);

        fetchTopSolves();

        leaderboardProgressbar.setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void fetchTopSolves(){
        Query query = FirebaseFirestore.getInstance()
                .collection("solves")
                .orderBy("durationMinutes", Query.Direction.ASCENDING)
                .orderBy("durationSeconds", Query.Direction.ASCENDING)
                .orderBy("durationMilliseconds", Query.Direction.ASCENDING)
                .limit(50);


        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    return;
                }
                List<Solve> solves = new ArrayList<>();
                for(DocumentSnapshot document:value.getDocuments()){
                    Solve solve = document.toObject(Solve.class);
                    solves.add(solve);
                }
                Log.d("solves",solves.toString());
                adapter.updateData(solves);
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        fetchTopSolves();
    }
}