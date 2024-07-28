package com.xaral.musicfission.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.SavePlaylistAdapter;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LibraryFragment extends Fragment {

    private RecyclerView savePlaylistView;
    private SavePlaylistAdapter savePlaylistAdapter;
    private List<MusicRepository.Playlist> playlistList = new ArrayList<>();
    private ImageView newPlaylist, line;
    private AlertDialog alertDialog;
    private static final int[] position = {0};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_library, container, false);

        /*try {
            AdView adView = root.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } catch (Exception ignored) {}*/

        playlistList = new ArrayList<>();

        savePlaylistView = root.findViewById(R.id.savePlaylistView);
        newPlaylist = root.findViewById(R.id.newPlaylist);
        line = root.findViewById(R.id.line);

        RecyclerView.OnScrollChangeListener scrollListener = new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                position[0] -= i3;
                if (position[0] == 0)
                    line.setAlpha(0f);
                else
                    line.setAlpha(1f);
            }
        };
        savePlaylistView.setOnScrollChangeListener(scrollListener);

        newPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlaylist(root);
            }
        });

        List<String> playlistsId = FavoriteService.getPlaylists();

        playlistList.add(new MusicRepository.Playlist(getString(R.string.liked), getString(R.string.you), "", Integer.toString(FavoriteService.getLiked().size()), ""));
        playlistList.add(new MusicRepository.Playlist(getString(R.string.history), getString(R.string.you), "-1", Integer.toString(FavoriteService.getFromHistory().size()), ""));
        playlistList.add(new MusicRepository.Playlist(getString(R.string.downloaded), getString(R.string.you), "-2", Integer.toString(FavoriteService.getDownloads().size()), ""));

        for (String browseId : playlistsId) {
            String title = FavoriteService.getPlaylistTitle(browseId);
            String author = FavoriteService.getPlaylistAuthor(browseId);
            String image = FavoriteService.getPlaylistUriImage(browseId);
            int itemCount = FavoriteService.getPlaylistSongs(browseId).size();
            playlistList.add(new MusicRepository.Playlist(title, author, browseId, Integer.toString(itemCount), image));
        }

        savePlaylistAdapter = new SavePlaylistAdapter(root.getContext(), playlistList);
        savePlaylistView.setAdapter(savePlaylistAdapter);


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

    public void createPlaylist(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        View promptView = layoutInflater.inflate(R.layout.input_new_title_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setView(promptView);

        final EditText editText = promptView.findViewById(R.id.edittext);
        final Button positiveButton = promptView.findViewById(R.id.positive_button);
        final Button negativeButton = promptView.findViewById(R.id.negative_button);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editText.getText().toString();
                if (newName.equals("")) {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.error_text_is_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                FavoriteService.createPlaylist(editText.getText().toString());
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playlistList = new ArrayList<>();
                        List<String> playlistsId = FavoriteService.getPlaylists();
                        playlistList.add(new MusicRepository.Playlist(getString(R.string.liked), getString(R.string.you), "", Integer.toString(FavoriteService.getLiked().size()), ""));
                        playlistList.add(new MusicRepository.Playlist(getString(R.string.history), getString(R.string.you), "-1", Integer.toString(FavoriteService.getFromHistory().size()), ""));
                        playlistList.add(new MusicRepository.Playlist(getString(R.string.downloaded), getString(R.string.you), "-2", Integer.toString(FavoriteService.getDownloads().size()), ""));

                        for (String browseId : playlistsId) {
                            String title = FavoriteService.getPlaylistTitle(browseId);
                            String author = FavoriteService.getPlaylistAuthor(browseId);
                            String image = FavoriteService.getPlaylistUriImage(browseId);
                            int itemCount = FavoriteService.getPlaylistSongs(browseId).size();
                            playlistList.add(new MusicRepository.Playlist(title, author, browseId, Integer.toString(itemCount), image));
                        }
                        savePlaylistAdapter.updateRequestList(playlistList);
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

        alertDialog = alertDialogBuilder.create();
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