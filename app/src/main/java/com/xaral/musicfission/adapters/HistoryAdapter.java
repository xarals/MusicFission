package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.MusicRepository;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Track> tracks;

    public HistoryAdapter(View view, List<MusicRepository.Track> tracks) {
        this.tracks = tracks;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
    }
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_history, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        HistoryLineAdapter historyLineAdapter;
        if (5 * position + 5 <= tracks.size())
            historyLineAdapter = new HistoryLineAdapter(view, tracks.subList(5 * position, 5 * (position + 1)));
        else
            historyLineAdapter = new HistoryLineAdapter(view, tracks.subList(5 * position, tracks.size()));
        holder.songsView.setAdapter(historyLineAdapter);
    }

    @Override
    public int getItemCount() {
        if (tracks.size() % 5 == 0)
            return tracks.size() / 5;
        else
            return tracks.size() / 5 + 1;
    }

    public void updateRequestList(List<MusicRepository.Track> newRequests) {
        tracks = newRequests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView songsView;
        //private final View view;
        ViewHolder(View view){
            super(view);
            songsView = view.findViewById(R.id.songsView);
        }
    }
}
