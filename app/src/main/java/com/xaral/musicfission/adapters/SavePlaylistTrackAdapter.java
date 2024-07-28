package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.xaral.musicfission.ui.SavePlaylistFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SavePlaylistTrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Track> songsList;
    private final String author, browseId, image;
    private String title;
    private List<Integer> removedPosition = new ArrayList<>();

    public SavePlaylistTrackAdapter(Context view, List<MusicRepository.Track> songsList, Bundle arguments) {
        this.songsList = songsList;
        this.view = view;
        this.inflater = LayoutInflater.from(view);
        this.title = arguments.getString("title");
        this.author = arguments.getString("author");
        this.browseId = arguments.getString("browseId");
        this.image = arguments.getString("image");
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
            View view = inflater.inflate(R.layout.header_fragment_playlist, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_song_album, parent, false);
            return new SavePlaylistTrackAdapter.ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderObject, @SuppressLint("RecyclerView") int position) {
        if (position == 0) {
            HeaderViewHolder holder = (HeaderViewHolder) holderObject;
            holder.playPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (songsList.size() == 0)
                        return;
                    PlayerService.musicRepository.newData(songsList);
                    MainActivity.startMusic(0);
                }
            });

            holder.playPlaylistShuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (songsList.size() == 0)
                        return;
                    List<MusicRepository.Track> shuffleList = songsList;
                    Collections.shuffle(shuffleList);
                    PlayerService.musicRepository.newData(shuffleList);
                    MainActivity.startMusic(0);
                }
            });

            holder.renamePlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    renamePlaylistClick(view, browseId, holder);
                }
            });

            if (browseId.equals("")) {
                holder.playlistImage.setImageResource(R.drawable.ic_like_playlist_24dp);
            }
            else if (browseId.equals("-1")) {
                holder.playlistImage.setImageResource(R.drawable.ic_history_playlist);
            }
            else if (browseId.equals("-2")) {
                holder.playlistImage.setImageResource(R.drawable.ic_download_playlist);
            }
            else {
                if (image != null && !image.equals(""))
                    Picasso.get().load(image).into(holder.playlistImage);
            }

            holder.titlePlaylist.setText(title);
            holder.titlePlaylist.setSelected(true);
            holder.artistPlaylist.setText(author);
            return;
        }
        ItemViewHolder holder = (ItemViewHolder) holderObject;
        MusicRepository.Track song = songsList.get(position - 1);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        Picasso.get().load(song.getImage()).placeholder(R.drawable.background_void).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 0;
                for (int k : removedPosition)
                    if (position - 1 > k) i++;
                PlayerService.musicRepository.newData(songsList);
                MainActivity.startMusic(position - i - 1);
            }
        });
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 0;
                for (int k : removedPosition)
                    if (position - 1 > k) i++;
                PopupMenu popupMenu = new PopupMenu(view, holder.options);
                popupMenu.getMenuInflater().inflate(R.menu.save_playlist_track_menu, popupMenu.getMenu());

                int finalI = i;
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_add_end) {
                            PlayerService.musicRepository.addEnd(song);
                            Toast.makeText(view, view.getString(R.string.added_to_the_end), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_play_playlist) {
                            PlayerService.musicRepository.playNext(song);
                            Toast.makeText(view, view.getString(R.string.will_be_played_next), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_download) {
                            DownloadService.addFileToQueue(song);
                            Toast.makeText(view, view.getString(R.string.start_downloading), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_delete_track) {
                            songsList.remove(position - finalI - 1);
                            if (browseId.equals("")) {
                                FavoriteService.dislike(song);
                                notifyItemRemoved(position - finalI);
                                removedPosition.add(position - 1);
                                return true;
                            }
                            else if (browseId.equals("-1")) {
                                FavoriteService.deleteTrackHistory(song.getUri());
                                notifyItemRemoved(position - finalI);
                                removedPosition.add(position - 1);
                                return true;
                            }
                            else if (browseId.equals("-2")) {
                                FavoriteService.deleteDownload(song);
                                notifyItemRemoved(position - finalI);
                                removedPosition.add(position - 1);
                                return true;
                            }
                            String title = FavoriteService.getPlaylistTitle(browseId);
                            String author = FavoriteService.getPlaylistAuthor(browseId);
                            String year = FavoriteService.getPlaylistYear(browseId);
                            String image = FavoriteService.getPlaylistUriImage(browseId);
                            FavoriteService.savePlaylistTrack(title, author, browseId, year, songsList, image);
                            notifyItemRemoved(position - finalI);
                            removedPosition.add(position - 1);
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
        return songsList.size() + 1;
    }

    public void updateRequestList(List<MusicRepository.Track> newRequests) {
        songsList = newRequests;
        notifyDataSetChanged();
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
        private final ImageView playlistImage, playPlaylistShuffle, playPlaylist, renamePlaylist;

        HeaderViewHolder(View view){
            super(view);
            titlePlaylist = view.findViewById(R.id.titlePlaylist);
            titlePlaylist.setSelected(true);
            artistPlaylist = view.findViewById(R.id.artistPlaylist);
            playlistImage = view.findViewById(R.id.playlistImage);
            playPlaylistShuffle = view.findViewById(R.id.playPlaylistShuffle);
            playPlaylist = view.findViewById(R.id.playPlaylist);
            renamePlaylist = view.findViewById(R.id.renamePlaylist);
        }
    }

    private void renamePlaylistClick(Context view, String browseId, HeaderViewHolder holder) {
        AlertDialog alertDialog;
        if (browseId.equals("") || browseId.equals("-1") || browseId.equals("-2")) {
            Toast.makeText(view, view.getString(R.string.you_cannot_rename_this_playlist), Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater layoutInflater = LayoutInflater.from(view);
        View promptView = layoutInflater.inflate(R.layout.input_new_title_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view);
        alertDialogBuilder.setView(promptView);

        alertDialog = alertDialogBuilder.create();

        final EditText editText = promptView.findViewById(R.id.edittext);
        final Button positiveButton = promptView.findViewById(R.id.positive_button);
        final Button negativeButton = promptView.findViewById(R.id.negative_button);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editText.getText().toString();
                if (newName.equals("")) {
                    Toast.makeText(view, view.getString(R.string.error_text_is_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                FavoriteService.renamePlaylist(browseId, newName);
                MainActivity.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        title = newName;
                        holder.titlePlaylist.setText(newName);
                        SavePlaylistFragment.statusPlaylistTitle.setText(newName);
                    }
                });
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    positiveButton.performClick();
                    return true;
                }
                return false;
            }
        });
        alertDialog.show();
    }
}
