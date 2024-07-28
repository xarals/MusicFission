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
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.MusicRepository;

import java.util.List;

public class PlaylistAdapter  extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Playlist> playlists;

    public PlaylistAdapter(View view, List<MusicRepository.Playlist> playlists) {
        this.playlists = playlists;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Playlist playlist = playlists.get(position);
        holder.title.setText(playlist.getTitle());
        Picasso.get().load(playlist.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", playlist.getTitle());
                bundle.putString("author", playlist.getArtist());
                bundle.putString("browseId", playlist.getBrowseId());
                bundle.putString("image", playlist.getImage().toString());

                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_playlist, bundle, navOptions);
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
        private final TextView title;
        private final ImageView image;
        //private final View view;
        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            title = view.findViewById(R.id.title);
            image = view.findViewById(R.id.image);
        }
    }
}
