package com.example.rubikssolver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class rvLeaderboardAdapter extends RecyclerView.Adapter<rvLeaderboardAdapter.SolveViewHolder> {
    private List<Solve> solves = new ArrayList<>();
    Context context;

    public rvLeaderboardAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public SolveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.recycler_solve, parent, false);
        return new SolveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolveViewHolder holder, int position) {
        Solve solve = solves.get(position);
        String time;
        time = String.valueOf(solve.durationMinutes)+ ":" +String.valueOf(solve.durationSeconds)+ ":" +String.valueOf(solve.durationMilliseconds);
        holder.LeaderboardTimer.setText(time);
        holder.LeaderboardUsername.setText(solve.username);
        holder.LeaderboardRank.setText("Rank "+String.valueOf(position+1));

        if(position==0){
            holder.imgTrophy.setImageResource(R.drawable.gold);
        }
        else if(position==1){
            holder.imgTrophy.setImageResource(R.drawable.silver);
        }
        else if(position==2){
            holder.imgTrophy.setImageResource(R.drawable.bronze);
        }
        else {
            holder.imgTrophy.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return solves.size();
    }
    public void updateData(List<Solve> newSolves){
        solves.clear();
        solves.addAll(newSolves);
        notifyDataSetChanged();
    }


    public static class SolveViewHolder extends RecyclerView.ViewHolder{
        public TextView LeaderboardUsername;
        public TextView LeaderboardTimer;
        public TextView LeaderboardRank;
        public ImageView imgTrophy;

        public SolveViewHolder(@NonNull View itemView) {
            super(itemView);
            LeaderboardUsername = itemView.findViewById(R.id.txtViewLeaderboardUsername);
            LeaderboardTimer = itemView.findViewById(R.id.txtViewLeaderboardTime);
            LeaderboardRank = itemView.findViewById(R.id.txtViewRank);
            imgTrophy = itemView.findViewById(R.id.imgTrophy);

        }
    }
}
