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
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.DownloadService;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistTrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Video> videosList;
    private String author, image;
    private final String browseId;
    private String title;
    private String year;

    private List<String> keys = FavoriteService.getPlaylists();

    public PlaylistTrackAdapter(Context view, List<MusicRepository.Video> videosList, Bundle arguments) {
        this.videosList = videosList;
        this.view = view;
        this.inflater = LayoutInflater.from(view);
        this.title = arguments.getString("title");
        this.author = arguments.getString("author");
        this.browseId = arguments.getString("browseId");
        this.image = arguments.getString("image");
        this.year = arguments.getString("year", "");
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0)
            return 1;
        else
            return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = inflater.inflate(R.layout.header_fragment_album, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_video_playlist, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderObject, @SuppressLint("RecyclerView") int position) {
        if (position == 0) {
            HeaderViewHolder holder = (HeaderViewHolder) holderObject;
            holder.titlePlaylist.setSelected(true);
            holder.playPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videosList.size() == 0)
                        return;
                    List<MusicRepository.Track> tracks = new ArrayList<>();
                    for (MusicRepository.Video video1 : videosList)
                        tracks.add(new MusicRepository.Track(video1.getTitle(), video1.getArtist(), video1.getArtistId(), video1.getUrl(), video1.getImage(), -1));
                    PlayerService.musicRepository.newData(tracks);
                    MainActivity.startMusic(0);
                }
            });

            Picasso.get().load(image).placeholder(R.drawable.background_void).into(holder.playlistImage);

            if (keys.contains(browseId))
                holder.saveAlbum.setImageResource(R.drawable.button_remove_library_white_24dp);
            else
                holder.saveAlbum.setImageResource(R.drawable.button_add_library_white_24dp);

            holder.playPlaylistShuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videosList.size() == 0)
                        return;
                    List<MusicRepository.Track> tracks = new ArrayList<>();
                    for (MusicRepository.Video video1 : videosList)
                        tracks.add(new MusicRepository.Track(video1.getTitle(), video1.getArtist(), video1.getArtistId(), video1.getUrl(), video1.getImage(), -1));
                    Collections.shuffle(tracks);
                    PlayerService.musicRepository.newData(tracks);
                    MainActivity.startMusic(0);
                }
            });

            holder.saveAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<MusicRepository.Track> tracks = new ArrayList<>();
                    for (MusicRepository.Video video1 : videosList)
                        tracks.add(new MusicRepository.Track(video1.getTitle(), video1.getArtist(), video1.getArtistId(), video1.getUrl(), video1.getImage(), -1));
                    if (!keys.contains(browseId)) {
                        MainActivity.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FavoriteService.savePlaylistTrack(title, author, browseId, year, tracks, image);
                                Toast.makeText(view, view.getString(R.string.playlist_saved_successfully), Toast.LENGTH_SHORT).show();
                                holder.saveAlbum.setImageResource(R.drawable.button_remove_library_white_24dp);
                            }
                        });
                    } else {
                        MainActivity.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FavoriteService.deletePlaylist(browseId);
                                Toast.makeText(view, view.getString(R.string.playlist_removed_successfully), Toast.LENGTH_SHORT).show();
                                holder.saveAlbum.setImageResource(R.drawable.button_add_library_white_24dp);
                            }
                        });
                    }
                    keys = FavoriteService.getPlaylists();
                }
            });

            if (image != null && !image.equals(""))
                Picasso.get().load(image).into(holder.playlistImage);

            holder.titlePlaylist.setText(title);
            holder.titlePlaylist.setSelected(true);
            holder.artistPlaylist.setText(author);
            return;
        }
        ItemViewHolder holder = (ItemViewHolder) holderObject;
        MusicRepository.Video video = videosList.get(position - 1);
        holder.title.setText(video.getTitle());
        holder.artist.setText(video.getArtist());
        Picasso.get().load(video.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<MusicRepository.Track> tracks = new ArrayList<>();
                for (MusicRepository.Video video1 : videosList)
                    tracks.add(new MusicRepository.Track(video1.getTitle(), video1.getArtist(), video1.getArtistId(), video1.getUrl(), video1.getImage(), -1));
                PlayerService.musicRepository.newData(tracks);
                MainActivity.startMusic(position - 1);
            }
        });
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(view, holder.options);
                popupMenu.getMenuInflater().inflate(R.menu.search_track_menu, popupMenu.getMenu());
                MusicRepository.Track track = new MusicRepository.Track(video.getTitle(), video.getArtist(), video.getArtistId(), video.getUrl(), video.getImage(), -1);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_add_playlist) {
                            MainActivity.addToPlaylistDialog(view, track);
                        }
                        if (item.getItemId() == R.id.menu_add_end) {
                            PlayerService.musicRepository.addEnd(track);
                            Toast.makeText(view, view.getString(R.string.added_to_the_end), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_play_playlist) {
                            PlayerService.musicRepository.playNext(track);
                            Toast.makeText(view, view.getString(R.string.will_be_played_next), Toast.LENGTH_SHORT).show();
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
        return videosList.size() + 1;
    }

    public void updateRequestList(List<MusicRepository.Video> newRequests) {
        videosList = newRequests;
        notifyDataSetChanged();
    }

    public void updateArguments(Bundle arguments) {
        this.title = arguments.getString("title");
        this.author = arguments.getString("author");
        this.image = arguments.getString("image");
        this.year = arguments.getString("year", "");
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, artist;
        private final ImageView image, options;

        ItemViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            title = view.findViewById(R.id.title);
            artist = view.findViewById(R.id.artist);
            image = view.findViewById(R.id.image);
            options = view.findViewById(R.id.button);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView titlePlaylist, artistPlaylist;
        private final ImageView playlistImage, playPlaylistShuffle, playPlaylist, saveAlbum;

        HeaderViewHolder(View view){
            super(view);
            titlePlaylist = view.findViewById(R.id.titleAlbum);
            artistPlaylist = view.findViewById(R.id.artistName);
            playlistImage = view.findViewById(R.id.albumImage);
            playPlaylistShuffle = view.findViewById(R.id.playAlbumShuffle);
            playPlaylist = view.findViewById(R.id.playAlbum);
            saveAlbum = view.findViewById(R.id.saveAlbum);
        }
    }
}
