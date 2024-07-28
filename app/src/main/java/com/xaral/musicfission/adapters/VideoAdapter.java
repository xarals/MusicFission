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

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.DownloadService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder>{

    private final View view;
    private final Context context;
    private final LayoutInflater inflater;
    private List<MusicRepository.Video> videos;
    private final boolean isPlaylist;

    public VideoAdapter(View view, List<MusicRepository.Video> videos) {
        this.videos = videos;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
        this.context = view.getContext();
        this.isPlaylist = false;
    }

    public VideoAdapter(View view, List<MusicRepository.Video> videos, boolean isPlaylist) {
        this.videos = videos;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
        this.context = view.getContext();
        this.isPlaylist = isPlaylist;
    }

    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_video, parent, false);
        return new VideoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicRepository.Video video = videos.get(position);
        holder.title.setText(video.getTitle());
        holder.artist.setText(video.getArtist());
        Picasso.get().load(video.getImage()).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaylist)
                    MainActivity.startMusic(video);
                else {
                    List<MusicRepository.Track> tracks = new ArrayList<>();
                    for (MusicRepository.Video video1 : videos)
                        tracks.add(new MusicRepository.Track(video1.getTitle(), video1.getArtist(), video1.getArtistId(), video1.getUrl(), video1.getImage(), -1));
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
                MusicRepository.Track track = new MusicRepository.Track(video.getTitle(), video.getArtist(), video.getArtistId(), video.getUrl(), video.getImage(), -1);
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
        return videos.size();
    }

    public void updateRequestList(List<MusicRepository.Video> newRequests) {
        videos = newRequests;
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