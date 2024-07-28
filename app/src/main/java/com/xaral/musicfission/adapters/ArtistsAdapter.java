package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.MusicRepository;

import java.util.List;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Artist> artists;

    public ArtistsAdapter(View view, List<MusicRepository.Artist> artists) {
        this.artists = artists;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Artist artist = artists.get(position);
        holder.name.setText(artist.getName());
        Picasso.get().load(artist.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("artistId", artist.getId());
                bundle.putString("image", artist.getImage().toString());

                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_artist, bundle, navOptions);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public void updateRequestList(List<MusicRepository.Artist> newRequests) {
        artists = newRequests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final ImageView image;
        //private final View view;
        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            name = view.findViewById(R.id.name);
            image = view.findViewById(R.id.image);
        }
    }
}
