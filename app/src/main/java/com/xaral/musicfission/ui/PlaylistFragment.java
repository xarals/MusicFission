package com.xaral.musicfission.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.PlaylistTrackAdapter;
import com.xaral.musicfission.adapters.SongAdapter;
import com.xaral.musicfission.adapters.VideoAdapter;
import com.xaral.musicfission.service.ConvertorService;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;
import com.xaral.musicfission.ytmusicapi.YTMusicApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlaylistFragment extends Fragment {

    private TextView titlePlaylist, artistPlaylist, statusTitlePlaylist;
    private ImageView playlistImage, line, playPlaylist, savePlaylist, playPlaylistShuffle, more;
    private RecyclerView videosView;
    private ConstraintLayout loadView, errorView;
    private ScrollView playlistScroll;

    private PlaylistTrackAdapter videoAdapter;
    private List<MusicRepository.Video> videosList = new ArrayList<>();
    private List<String> keys = FavoriteService.getPlaylists();

    private static final int[] position = {0};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        statusTitlePlaylist = root.findViewById(R.id.statusPlaylistTitle);
        statusTitlePlaylist.setSelected(true);
        line = root.findViewById(R.id.line);
        more = root.findViewById(R.id.more);

        loadView = root.findViewById(R.id.loadView);
        errorView = root.findViewById(R.id.errorView);

        videosView = root.findViewById(R.id.videosView);

        Bundle arguments = getArguments();

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(root.getContext(), more);
                popupMenu.getMenuInflater().inflate(R.menu.search_playlist_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<MusicRepository.Track> songsList = new ArrayList<>();
                        if (videosList.size() == 0)
                            return false;
                        for (MusicRepository.Video video : videosList)
                            songsList.add(new MusicRepository.Track(video.getTitle(), video.getArtist(), video.getArtistId(), video.getUrl(), video.getImage(), -1));
                        if (item.getItemId() == R.id.menu_add_end) {
                            if (videosList.size() == 0)
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
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        statusTitlePlaylist.setText(arguments.getString("title"));

        videoAdapter = new PlaylistTrackAdapter(root.getContext(), videosList, arguments);
        videosView.setAdapter(videoAdapter);

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
                        statusTitlePlaylist.setAlpha(0);
                    else if (position[0] < 1.2 * albumTitleHeight)
                        statusTitlePlaylist.setAlpha(2.5f / albumTitleHeight * position[0] - 2);
                    else
                        statusTitlePlaylist.setAlpha(1);
                }
            };
            videosView.setOnScrollChangeListener(scrollListener);
        }

        if (arguments.get("videosList") != null) {
            videoAdapter.updateRequestList(videosList);
            loadView.setVisibility(View.INVISIBLE);
            position[0] = arguments.getInt("position", 0);
        } else {
            position[0] = 0;
            getVideos(arguments.getString("browseId"));
        }

        return root;
    }

    @Override
    public void onPause() {
        Bundle outState = getArguments();
        if (outState == null) outState = new Bundle();
        outState.putInt("position", position[0]);
        MusicRepository.Video.saveToBundle(outState, "videosList", videosList);
        setArguments(outState);
        super.onPause();
    }

    private void getVideos(String browseId) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    JSONObject responseBody = YTMusicApi.getPlaylist(browseId);;
                    JSONArray listVideosObject = new JSONArray();
                    videosList = new ArrayList<>();
                    String author = "";
                    if (responseBody.has("author"))
                        author = responseBody.getString("author");
                    if (responseBody.has("videos"))
                        listVideosObject = responseBody.getJSONArray("videos");
                    for (int i = 0; i < listVideosObject.length(); i++) {
                        JSONObject video = listVideosObject.getJSONObject(i);
                        videosList.add(new MusicRepository.Video(video.getString("title"), video.getString("artist"), video.getString("artistId"), video.getString("url"), video.getString("image")));
                    }
                    String finalAuthor = author;
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle outState = getArguments();
                            outState.putString("author", finalAuthor);
                            setArguments(outState);
                            videoAdapter.updateRequestList(videosList);
                            videoAdapter.updateArguments(outState);
                            more.setVisibility(View.VISIBLE);
                            loadView.setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (Exception ex) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorView.setVisibility(View.VISIBLE);
                            loadView.setVisibility(View.INVISIBLE);
                        }
                    });
                    //Log.e("Exception", ex.toString());
                }
            }
        };
        thread.start();
    }
}