package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.DownloadService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    private final View view;
    private final Context context;
    private final LayoutInflater inflater;
    private List<MusicRepository.Track> tracks;
    private final boolean isAlbum;

    public SongAdapter(View view, List<MusicRepository.Track> tracks) {
        this.tracks = tracks;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
        this.context = view.getContext();
        this.isAlbum = false;
    }

    public SongAdapter(View view, List<MusicRepository.Track> tracks, boolean isAlbum) {
        this.tracks = tracks;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
        this.context = view.getContext();
        this.isAlbum = isAlbum;
    }

    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_song, parent, false);
        return new SongAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Track track = tracks.get(position);
        holder.title.setText(track.getTitle());
        holder.artist.setText(track.getArtist());
        Picasso.get().load(track.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAlbum)
                    MainActivity.startMusic(track);
                else {
                    PlayerService.musicRepository.newData(tracks);
                    MainActivity.startMusic(position);
                }
            }
        });
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.options);
                popupMenu.getMenuInflater().inflate(R.menu.search_track_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_add_playlist) {
                            MainActivity.addToPlaylistDialog(view.getContext(), track);
                        }
                        if (item.getItemId() == R.id.menu_add_end) {
                            PlayerService.musicRepository.addEnd(track);
                            Toast.makeText(context, context.getString(R.string.added_to_the_end), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_play_playlist) {
                            PlayerService.musicRepository.playNext(track);
                            Toast.makeText(context, context.getString(R.string.will_be_played_next), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_download) {
                            DownloadService.addFileToQueue(track);
                            Toast.makeText(context, context.getString(R.string.start_downloading), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
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
        private final TextView title, artist;
        private final ImageView image, options;
        //private final View view;
        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            title = view.findViewById(R.id.title);
            artist = view.findViewById(R.id.artist);
            image = view.findViewById(R.id.image);
            options = view.findViewById(R.id.button);
        }
    }
}

