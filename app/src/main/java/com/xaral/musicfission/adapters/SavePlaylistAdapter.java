package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;

import java.io.Serializable;
import java.util.List;

public class SavePlaylistAdapter extends RecyclerView.Adapter<SavePlaylistAdapter.ViewHolder>{

    private final Context view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Playlist> playlists;

    public SavePlaylistAdapter(Context view, List<MusicRepository.Playlist> playlists) {
        this.playlists = playlists;
        this.view = view;
        this.inflater = LayoutInflater.from(view);
    }
    @Override
    public SavePlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_save_playlist, parent, false);
        return new SavePlaylistAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SavePlaylistAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Playlist playlist = playlists.get(position);
        holder.title.setText(playlist.getTitle());
        holder.artist.setText(playlist.getArtist());
        holder.trackCount.setText(playlist.getItemCount());
        if (position == 0)
            holder.image.setImageResource(R.drawable.ic_like_playlist_24dp);
        else if (position == 1)
            holder.image.setImageResource(R.drawable.ic_history_playlist);
        else if (position == 2)
            holder.image.setImageResource(R.drawable.ic_download_playlist);
        else if (playlist.getImage().equals(""))
            holder.image.setImageResource(R.drawable.default_song_image);
        else
            Picasso.get().load(playlist.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", playlist.getTitle());
                bundle.putString("author", playlist.getArtist());
                bundle.putString("browseId", playlist.getBrowseId());
                bundle.putString("image", playlist.getImage());

                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_save_playlist, bundle, navOptions);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void updateRequestList(List<MusicRepository.Playlist> newRequests) {
        playlists = newRequests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, artist, trackCount;
        private final ImageView image;
        private final CardView cardView;

        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            title = view.findViewById(R.id.titlePlaylist);
            artist = view.findViewById(R.id.authorPlaylist);
            image = view.findViewById(R.id.image);
            cardView = view.findViewById(R.id.cardView21);
            trackCount = view.findViewById(R.id.trackCount);
        }
    }
}
