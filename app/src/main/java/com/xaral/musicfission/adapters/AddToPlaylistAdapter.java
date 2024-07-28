package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;

import java.util.List;

public class AddToPlaylistAdapter extends RecyclerView.Adapter<AddToPlaylistAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<MusicRepository.Playlist> playlists;
    private final MusicRepository.Track addTrack;

    public AddToPlaylistAdapter(Context context, List<MusicRepository.Playlist> playlists, MusicRepository.Track addTrack) {
        this.playlists = playlists;
        this.inflater = LayoutInflater.from(context);
        this.addTrack = addTrack;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Playlist playlist = playlists.get(position);
        holder.title.setText(playlist.getTitle());
        if (playlist.getBrowseId().equals(""))
            holder.image.setImageResource(R.drawable.ic_like_playlist_24dp);
        else if (!playlist.getImage().equals("")) {
            Picasso.get().load(playlist.getImage()).into(holder.image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (playlist.getBrowseId().equals(""))
                    FavoriteService.like(addTrack);
                else
                    FavoriteService.addToPlaylist(playlist.getBrowseId(), addTrack);
                MainActivity.alertDialog.dismiss();
                Toast.makeText(v.getContext(), v.getContext().getString(R.string.added_to_playlist), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final ImageView image;
        ViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.title);
            image = view.findViewById(R.id.image);
        }
    }
}

