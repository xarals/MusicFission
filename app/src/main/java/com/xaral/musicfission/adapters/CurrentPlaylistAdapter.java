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

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.DownloadService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;

import java.util.List;

public class CurrentPlaylistAdapter  extends RecyclerView.Adapter<CurrentPlaylistAdapter.ViewHolder>{

    private final Context view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Track> tracks;

    public CurrentPlaylistAdapter(Context view, List<MusicRepository.Track> tracks) {
        this.tracks = tracks;
        this.view = view;
        this.inflater = LayoutInflater.from(view);
    }
    @Override
    public CurrentPlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_song, parent, false);
        return new CurrentPlaylistAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CurrentPlaylistAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Track track = tracks.get(position);
        holder.title.setText(track.getTitle());
        holder.artist.setText(track.getArtist());
        if (track.getDuration() == -2) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.image.getLayoutParams();
            layoutParams.width *= 1.76;
            holder.image.setLayoutParams(layoutParams);
        }
        if (PlayerService.musicRepository.getCurrentItemIndex() == position) {
            holder.itemView.setBackground(view.getDrawable(R.drawable.background_current_track));
        } else {
            holder.itemView.setBackground(view.getDrawable(R.drawable.background_void));
        }
        Picasso.get().load(track.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.setTrack(position);
            }
        });
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(view, holder.options);
                popupMenu.getMenuInflater().inflate(R.menu.current_playlist_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_add_playlist)
                            MainActivity.addToPlaylistDialog(view, track);
                        if (item.getItemId() == R.id.menu_delete_track) {
                            deleteTrack(position);
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_download) {
                            DownloadService.addFileToQueue(track);
                            Toast.makeText(view, view.getString(R.string.start_downloading), Toast.LENGTH_SHORT).show();
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

    public void deleteTrack(int index) {
        PlayerService.musicRepository.deleteTrack(index);
        tracks = PlayerService.musicRepository.getData();
        notifyDataSetChanged();
    }

    public void updateRequestList(List<MusicRepository.Track> newRequests) {
        tracks = newRequests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, artist;
        private final ImageView image, options;
        private final CardView cardView;
        //private final View view;
        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            title = view.findViewById(R.id.title);
            artist = view.findViewById(R.id.artist);
            image = view.findViewById(R.id.image);
            cardView = view.findViewById(R.id.cardView21);
            options = view.findViewById(R.id.button);
        }
    }
}


