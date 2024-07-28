package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.MusicRepository;

import java.util.List;

public class HistoryLineAdapter extends RecyclerView.Adapter<HistoryLineAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Track> tracks;

    public HistoryLineAdapter(View view, List<MusicRepository.Track> tracks) {
        this.tracks = tracks;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
    }
    @Override
    public HistoryLineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_history_song, parent, false);
        return new HistoryLineAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryLineAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Track track = tracks.get(position);
        holder.title.setText(track.getTitle());
        if (track.getBigImage().equals(""))
            Picasso.get().load(track.getImage()).into(holder.image);
        else
            Picasso.get().load(track.getBigImage()).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.startMusic(track);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void updateRequestList(List<MusicRepository.Track> newRequests) {
        tracks = newRequests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView image;
        ViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.title);
            image = view.findViewById(R.id.image);
        }
    }
}
