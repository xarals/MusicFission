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

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Album> albums;
    private boolean even;

    public AlbumsAdapter(View view, List<MusicRepository.Album> albums) {
        this.albums = albums;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
        this.even = albums.size() % 2 == 0;
    }
    @Override
    public AlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_albums, parent, false);
        return new AlbumsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Album album = albums.get(2 * position);
        holder.name.setText(album.getTitle());
        Picasso.get().load(album.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        MusicRepository.Album finalAlbum = album;
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", finalAlbum.getTitle());
                bundle.putString("artist", finalAlbum.getArtist());
                bundle.putString("browseId", finalAlbum.getBrowseId());
                bundle.putString("year", finalAlbum.getYear());
                bundle.putString("image", finalAlbum.getImage().toString());

                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_album, bundle, navOptions);
            }
        });
        holder.name.setVisibility(View.VISIBLE);
        holder.image.setVisibility(View.VISIBLE);
        if (2 * position == albums.size() - 1) return;
        album = albums.get(2 * position + 1);
        holder.name2.setText(album.getTitle());
        Picasso.get().load(album.getImage()).placeholder(R.drawable.background_void).into(holder.image2);
        MusicRepository.Album finalAlbum1 = album;
        holder.image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", finalAlbum1.getTitle());
                bundle.putString("artist", finalAlbum1.getArtist());
                bundle.putString("browseId", finalAlbum1.getBrowseId());
                bundle.putString("year", finalAlbum1.getYear());
                bundle.putString("image", finalAlbum1.getImage().toString());

                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_album, bundle, navOptions);
            }
        });
        holder.name2.setVisibility(View.VISIBLE);
        holder.image2.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        if (even)
            return albums.size() / 2;
        else
            return (albums.size() + 1) / 2;
    }

    public void updateRequestList(List<MusicRepository.Album> newRequests) {
        albums = newRequests;
        even = albums.size() % 2 == 0;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, name2;
        private final ImageView image, image2;
        //private final View view;
        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            name = view.findViewById(R.id.name);
            image = view.findViewById(R.id.image);
            name2 = view.findViewById(R.id.name2);
            image2 = view.findViewById(R.id.image2);
        }
    }
}