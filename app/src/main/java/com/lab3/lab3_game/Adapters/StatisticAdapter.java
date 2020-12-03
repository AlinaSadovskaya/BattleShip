package com.lab3.lab3_game.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lab3.lab3_game.Structures.Statistic;
import com.lab3.lab3_game.R;

import java.util.ArrayList;

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatViewHolder> {

    private final ArrayList<Statistic> gameStatistics;

    static class StatViewHolder extends RecyclerView.ViewHolder {
        final TextView player_1_nameView;
        final TextView player_2_nameView;
        final TextView score_1_view;
        final TextView score_2_view;

        final View statView;

        StatViewHolder(View v)
        {
            super(v);
            statView = v;
            player_1_nameView = v.findViewById(R.id.player1_name);
            player_2_nameView = v.findViewById(R.id.player2_name);
            score_1_view = v.findViewById(R.id.score_player1);
            score_2_view = v.findViewById(R.id.score_player2);
        }
    }

    public StatisticAdapter(ArrayList<Statistic> list)
    {
        gameStatistics = list;
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.statistic_elem, parent, false);
        return new StatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final StatViewHolder holder, int position) {
        final Statistic statItem = gameStatistics.get(position);
        holder.player_1_nameView.setText(statItem.getPlayer_1());
        holder.player_2_nameView.setText(statItem.getPlayer_2());
        holder.score_1_view.setText(String.valueOf(statItem.getScore_1()));
        holder.score_2_view.setText(String.valueOf(statItem.getScore_2()));
    }


    @Override
    public int getItemCount() {
        return gameStatistics.size();
    }
}

