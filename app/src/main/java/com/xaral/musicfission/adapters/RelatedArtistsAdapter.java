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

public class RelatedArtistsAdapter extends RecyclerView.Adapter<RelatedArtistsAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Artist> artists;
    private boolean even;

    public RelatedArtistsAdapter(View view, List<MusicRepository.Artist> artists) {
        this.artists = artists;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
        this.even = artists.size() % 2 == 0;
    }
    @Override
    public RelatedArtistsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_artists, parent, false);
        return new RelatedArtistsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RelatedArtistsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Artist artist = artists.get(2 * position);
        holder.name.setText(artist.getName());
        Picasso.get().load(artist.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        MusicRepository.Artist finalArtist = artist;
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("artistId", finalArtist.getId());
                bundle.putString("image", finalArtist.getImage().toString());

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
        holder.name.setVisibility(View.VISIBLE);
        holder.image.setVisibility(View.VISIBLE);
        if (2 * position == artists.size() - 1) return;
        artist = artists.get(2 * position + 1);
        holder.name2.setText(artist.getName());
        Picasso.get().load(artist.getImage()).placeholder(R.drawable.background_void).into(holder.image2);
        MusicRepository.Artist finalArtist1 = artist;
        holder.image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("artistId", finalArtist1.getId());
                bundle.putString("image", finalArtist1.getImage().toString());

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
        holder.name2.setVisibility(View.VISIBLE);
        holder.image2.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        if (even)
            return artists.size() / 2;
        else
            return (artists.size() + 1) / 2;
    }

    public void updateRequestList(List<MusicRepository.Artist> newRequests) {
        artists = newRequests;
        even = artists.size() % 2 == 0;
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
