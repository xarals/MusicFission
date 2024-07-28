package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;

import java.util.List;

public class RelatedSongsAdapter extends RecyclerView.Adapter<RelatedSongsAdapter.ViewHolder>{

    private final View view;
    private final Context context;
    private final LayoutInflater inflater;
    private List<MusicRepository.Track> tracks;

    public RelatedSongsAdapter(View view, List<MusicRepository.Track> tracks) {
        this.tracks = tracks;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
        this.context = view.getContext();
    }
    @Override
    public RelatedSongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_songs, parent, false);
        return new RelatedSongsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RelatedSongsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SongAdapter songAdapter;
        if (5 * position + 5 <= tracks.size())
            songAdapter = new SongAdapter(view, tracks.subList(5 * position, 5 * (position + 1)));
        else
            songAdapter = new SongAdapter(view, tracks.subList(5 * position, tracks.size()));
        holder.songsView.setAdapter(songAdapter);
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
            //view = view.findViewById(R.id.view3);
            songsView = view.findViewById(R.id.songsView);
        }
    }
}
