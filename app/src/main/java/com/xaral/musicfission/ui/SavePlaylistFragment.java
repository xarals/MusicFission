package com.xaral.musicfission.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.SavePlaylistTrackAdapter;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SavePlaylistFragment extends Fragment {
    public static TextView titlePlaylist, statusPlaylistTitle, artistPlaylist;
    private ImageView more, playlistImage, line, playPlaylistShuffle, playPlaylist, renamePlaylist;
    private RecyclerView tracksView;
    private ScrollView playlistScroll;
    private SavePlaylistTrackAdapter savePlaylistTrackAdapter;
    private String title;
    private String author;
    private String browseId;
    private String image;
    private List<MusicRepository.Track> songsList;
    private AlertDialog alertDialog;
    private static final int[] position = {0};
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_save_playlist, container, false);

        statusPlaylistTitle = root.findViewById(R.id.statusPlaylistTitle);
        more = root.findViewById(R.id.more);
        line = root.findViewById(R.id.line);
        tracksView = root.findViewById(R.id.tracksView);

        Bundle arguments = getArguments();
        author = arguments.getString("author");
        browseId = arguments.getString("browseId");
        image = arguments.getString("image");
        if (!browseId.equals("") && !browseId.equals("-1") && !browseId.equals("-2"))
            title = FavoriteService.getPlaylistTitle(browseId);
        else
            title = arguments.getString("title");

        if (browseId.equals("")) {
            songsList = FavoriteService.getLiked();
        }
        else if (browseId.equals("-1")) {
            songsList = FavoriteService.getFromHistory();
        }
        else if (browseId.equals("-2")) {
            songsList = FavoriteService.getDownloads();
        }
        else {
            songsList = FavoriteService.getPlaylistSongs(browseId);
        }

        statusPlaylistTitle.setText(title);
        statusPlaylistTitle.setSelected(true);

        savePlaylistTrackAdapter = new SavePlaylistTrackAdapter(root.getContext(), songsList, arguments);
        tracksView.setAdapter(savePlaylistTrackAdapter);

        RecyclerView.OnScrollChangeListener scrollListener = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            scrollListener = new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    position[0] -= i3;
                    float albumTitleHeight;
                    try {
                        albumTitleHeight = getResources().getDimension(R.dimen.albumTitleHeight);
                    } catch (Exception ex) {
                        //Log.e("scroll", ex.toString());
                        return;
                    }
                    //Log.i("scroll", Integer.toString(position[0]));
                    if (position[0] == 0)
                        line.setAlpha(0f);
                    else
                        line.setAlpha(1f);
                    if (position[0] <= 0.8 * albumTitleHeight)
                        statusPlaylistTitle.setAlpha(0);
                    else if (position[0] < 1.2 * albumTitleHeight)
                        statusPlaylistTitle.setAlpha(2.5f / albumTitleHeight * position[0] - 2);
                    else
                        statusPlaylistTitle.setAlpha(1);
                }
            };
            tracksView.setOnScrollChangeListener(scrollListener);
        }

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(root.getContext(), more);
                popupMenu.getMenuInflater().inflate(R.menu.save_playlist_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_add_end) {
                            if (songsList.size() == 0)
                                return false;
                            for (MusicRepository.Track track : songsList) {
                                PlayerService.musicRepository.addEnd(track);
                            }
                            MainActivity.playlistUpdate();
                            Toast.makeText(root.getContext(), root.getContext().getString(R.string.added_to_the_end), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_play_playlist) {
                            for (int i = songsList.size() - 1; i >= 0; i--) {
                                PlayerService.musicRepository.playNext(songsList.get(i));
                            }
                            //Log.i("size", Integer.toString(songsList.size()));
                            MainActivity.playlistUpdate();
                            Toast.makeText(root.getContext(), root.getContext().getString(R.string.will_be_played_next), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_add_end_shuffle) {
                            List<MusicRepository.Track> list2 = songsList;
                            Collections.shuffle(list2);
                            for (MusicRepository.Track track : list2) {
                                PlayerService.musicRepository.addEnd(track);
                            }
                            MainActivity.playlistUpdate();
                            Toast.makeText(root.getContext(), root.getContext().getString(R.string.added_to_the_end_shuffled), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_play_playlist_shuffle) {
                            List<MusicRepository.Track> tracks2 = songsList;
                            Collections.shuffle(tracks2);
                            for (int i = tracks2.size() - 1; i >= 0; i--) {
                                PlayerService.musicRepository.playNext(tracks2.get(i));
                            }
                            MainActivity.playlistUpdate();
                            Toast.makeText(root.getContext(), root.getContext().getString(R.string.will_be_played_next_shuffled), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_delete_playlist && !browseId.equals("") && !browseId.equals("-1") && !browseId.equals("-2")) {
                            FavoriteService.deletePlaylist(browseId);
                            if (getActivity() == null) return false;
                            getActivity().onBackPressed();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        Bundle outState = getArguments();
        if (outState != null)
            position[0] = outState.getInt("position", 0);
        else
            position[0] = 0;

        return root;
    }

    @Override
    public void onPause() {
        Bundle outState = getArguments();
        if (outState == null) outState = new Bundle();
        outState.putInt("position", position[0]);
        setArguments(outState);
        super.onPause();
    }
}
