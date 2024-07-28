package com.xaral.musicfission.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.AlbumAdapter;
import com.xaral.musicfission.adapters.ArtistsAdapter;
import com.xaral.musicfission.adapters.PlaylistAdapter;
import com.xaral.musicfission.adapters.RequestHistoryAdapter;
import com.xaral.musicfission.adapters.SearchResponseAdapter;
import com.xaral.musicfission.adapters.SongAdapter;
import com.xaral.musicfission.adapters.VideoAdapter;
import com.xaral.musicfission.service.ConvertorService;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.ytmusicapi.YTMusicApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchFragment extends Fragment {
    private ConstraintLayout requestsView, loadView, responseView, errorView;
    private SearchView searchView;
    private RecyclerView requestHistory, artistsView;
    private RequestHistoryAdapter requestHistoryAdapter;
    private ArtistsAdapter artistsAdapter;
    private PlaylistAdapter playlistAdapter;
    private SongAdapter songAdapter;
    private VideoAdapter videoAdapter;
    private AlbumAdapter albumAdapter;
    private SearchResponseAdapter searchResponseAdapter;
    private List<MusicRepository.Artist> artistsList = new ArrayList<>();
    private List<MusicRepository.Album> albumsList = new ArrayList<>();
    private List<MusicRepository.Playlist> playlistsList = new ArrayList<>();
    private List<MusicRepository.Track> tracksList = new ArrayList<>();
    private List<MusicRepository.Video> videosList = new ArrayList<>();
    private int requests = 0;
    private String topResult = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        /*try {
            AdView adView = root.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } catch (Exception ignored) {}*/

        requestsView = root.findViewById(R.id.layoutHistory);
        loadView = root.findViewById(R.id.loadView);
        errorView = root.findViewById(R.id.errorView);
        responseView = root.findViewById(R.id.responseView);
        requestHistory = root.findViewById(R.id.requestHistory);
        artistsView = root.findViewById(R.id.artistsView);
        artistsAdapter = new ArtistsAdapter(root, new ArrayList<>());
        playlistAdapter = new PlaylistAdapter(root, new ArrayList<>());
        songAdapter = new SongAdapter(root, new ArrayList<>());
        videoAdapter = new VideoAdapter(root, new ArrayList<>());
        albumAdapter = new AlbumAdapter(root, new ArrayList<>());
        searchResponseAdapter = new SearchResponseAdapter(root, new LinkedHashMap<>());
        artistsView.setAdapter(searchResponseAdapter);
        requestHistoryAdapter = new RequestHistoryAdapter(root, FavoriteService.getRequests());
        requestHistory.setAdapter(requestHistoryAdapter);
        searchView = root.findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if (query.length() < 1) {
                    Toast.makeText(root.getContext(), root.getContext().getString(R.string.the_request_is_too_short), Toast.LENGTH_SHORT).show();
                    return false;
                }
                requests++;
                doSearch(query, requests);
                FavoriteService.saveRequest(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<String> requests = FavoriteService.getRequests();
                List<String> newRequests = new ArrayList<>();
                for (String req : requests) {
                    if (req.contains(newText))
                        newRequests.add(req);
                }
                requestHistoryAdapter.updateRequestList(newRequests);
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    requestHistory.setVisibility(View.VISIBLE);
                else
                    requestHistory.setVisibility(View.INVISIBLE);
            }
        });
        if (getArguments() == null)
            return root;
        if (getArguments().get("isResponse") == null)
            return root;
        if (!(boolean) getArguments().get("isResponse")) {
            searchView.setQuery(getArguments().get("searchText").toString(), false);
            return root;
        }
        Map<String, RecyclerView.Adapter> map = new LinkedHashMap<>();
        switch (getArguments().get("topResult").toString()) {
            case ("artist"):
                if (!artistsList.isEmpty()) {
                    artistsAdapter.updateRequestList(artistsList.subList(0, 1));
                    map.put("Artists", artistsAdapter);
                }
                break;
            case ("album"):
                if (!albumsList.isEmpty()) {
                    albumAdapter.updateRequestList(albumsList);
                    map.put("Albums", albumAdapter);
                }
                break;
            case ("playlist"):
                if (!playlistsList.isEmpty()) {
                    playlistAdapter.updateRequestList(playlistsList);
                    map.put("Playlists", playlistAdapter);
                }
                break;
            case ("song"):
                if (!tracksList.isEmpty()) {
                    songAdapter.updateRequestList(tracksList);
                    map.put("Songs", songAdapter);
                }
                break;
            case ("video"):
                if (!tracksList.isEmpty()) {
                    songAdapter.updateRequestList(tracksList);
                    map.put("Songs", songAdapter);
                }
                if (!videosList.isEmpty()) {
                    videoAdapter.updateRequestList(videosList);
                    map.put("Videos", videoAdapter);
                }
                break;
        }
        if (!artistsList.isEmpty() && !getArguments().get("topResult").toString().equals("artist")) {
            artistsAdapter.updateRequestList(artistsList);
            map.put("Artists", artistsAdapter);
        }
        if (!albumsList.isEmpty() && !getArguments().get("topResult").toString().equals("album")) {
            albumAdapter.updateRequestList(albumsList);
            map.put("Albums", albumAdapter);
        }
        if (!playlistsList.isEmpty() && !getArguments().get("topResult").toString().equals("playlist")) {
            playlistAdapter.updateRequestList(playlistsList);
            map.put("Playlists", playlistAdapter);
        }
        if (!tracksList.isEmpty() && !getArguments().get("topResult").toString().equals("song") && !getArguments().get("topResult").toString().equals("video")) {
            songAdapter.updateRequestList(tracksList);
            map.put("Songs", songAdapter);
        }
        if (!videosList.isEmpty() && !getArguments().get("topResult").toString().equals("video")) {
            videoAdapter.updateRequestList(videosList);
            map.put("Videos", videoAdapter);
        }
        searchResponseAdapter.updateRequestList(map);
        responseView.setVisibility(View.VISIBLE);
        loadView.setVisibility(View.INVISIBLE);
        requestHistory.setVisibility(View.INVISIBLE);
        searchView.setQuery(getArguments().get("searchText").toString(), false);
        return root;
    }

    @Override
    public void onPause() {
        Bundle args = getArguments();
        if (args == null) args = new Bundle();
        args.putString("searchText", searchView.getQuery().toString());
        args.putString("topResult", topResult);
        args.putBoolean("isResponse", responseView.getVisibility() == View.VISIBLE);
        MusicRepository.Artist.saveToBundle(args, "artistsList", artistsList);
        MusicRepository.Album.saveToBundle(args, "albumsList", albumsList);
        MusicRepository.Playlist.saveToBundle(args, "playlistsList", playlistsList);
        MusicRepository.Track.saveToBundle(args, "tracksList", tracksList);
        MusicRepository.Video.saveToBundle(args, "videosList", videosList);
        super.onPause();
    }

    private void doSearch(String requestText, int indexRequest) {
        requestHistory.setVisibility(View.INVISIBLE);
        loadView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.INVISIBLE);
        responseView.setVisibility(View.INVISIBLE);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    JSONObject answer = YTMusicApi.search(requestText);
                    JSONArray artistsListObject = new JSONArray();
                    JSONArray albumsListObject = new JSONArray();
                    JSONArray playlistsListObject = new JSONArray();
                    JSONArray tracksListObject = new JSONArray();
                    JSONArray videosListObject = new JSONArray();
                    artistsList = new ArrayList<>();
                    albumsList = new ArrayList<>();
                    playlistsList = new ArrayList<>();
                    tracksList = new ArrayList<>();
                    videosList = new ArrayList<>();
                    if (answer.has("artists") && answer.getJSONArray("artists").length() > 0) {
                        artistsListObject = answer.getJSONArray("artists");
                        for (int i = 0; i < artistsListObject.length(); i++) {
                            JSONObject artist = artistsListObject.getJSONObject(i);
                            artistsList.add(new MusicRepository.Artist(artist.getString("name"), artist.getString("artistId"), "", artist.getString("image")));
                        }
                    }
                    if (answer.has("albums") && answer.getJSONArray("albums").length() > 0) {
                        albumsListObject = answer.getJSONArray("albums");
                        for (int i = 0; i < albumsListObject.length(); i++) {
                            JSONObject album = albumsListObject.getJSONObject(i);
                            albumsList.add(new MusicRepository.Album(album.getString("title"), "", "", album.getString("browseId"), "", album.getString("image")));
                        }
                    }
                    if (answer.has("playlists") && answer.getJSONArray("playlists").length() > 0) {
                        playlistsListObject = answer.getJSONArray("playlists");
                        for (int i = 0; i < playlistsListObject.length(); i++) {
                            JSONObject playlist = playlistsListObject.getJSONObject(i);
                            playlistsList.add(new MusicRepository.Playlist(playlist.getString("title"), "", playlist.getString("browseId"), "", playlist.getString("image")));
                        }
                    }
                    if (answer.has("songs") && answer.getJSONArray("songs").length() > 0) {
                        tracksListObject = answer.getJSONArray("songs");
                        for (int i = 0; i < tracksListObject.length(); i++) {
                            JSONObject song = tracksListObject.getJSONObject(i);
                            tracksList.add(new MusicRepository.Track(song.getString("title"), song.getString("artist"), song.getString("artistId"), song.getString("url"), song.getString("image"), 0));
                        }
                    }
                    if (answer.has("videos") && answer.getJSONArray("videos").length() > 0) {
                        videosListObject = answer.getJSONArray("videos");
                        for (int i = 0; i < videosListObject.length(); i++) {
                            JSONObject video = videosListObject.getJSONObject(i);
                            videosList.add(new MusicRepository.Video(video.getString("title"), video.getString("artist"), video.getString("artistId"), video.getString("url"), video.getString("image")));
                        }
                    }
                    Map<String, RecyclerView.Adapter> map = new LinkedHashMap<>();
                    if (answer.has("topResult"))
                        topResult = answer.getString("topResult");
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (topResult) {
                                case ("artist"):
                                    if (!artistsList.isEmpty()) {
                                        artistsAdapter.updateRequestList(artistsList.subList(0, 1));
                                        map.put(getString(R.string.artists), artistsAdapter);
                                    }
                                    break;
                                case ("album"):
                                    if (!albumsList.isEmpty()) {
                                        albumAdapter.updateRequestList(albumsList);
                                        map.put(getString(R.string.albums), albumAdapter);
                                    }
                                    break;
                                case ("playlist"):
                                    if (!playlistsList.isEmpty()) {
                                        playlistAdapter.updateRequestList(playlistsList);
                                        map.put(getString(R.string.playlists), playlistAdapter);
                                    }
                                    break;
                                case ("song"):
                                    if (!tracksList.isEmpty()) {
                                        songAdapter.updateRequestList(tracksList);
                                        map.put(getString(R.string.songs), songAdapter);
                                    }
                                    break;
                                case ("video"):
                                    if (!tracksList.isEmpty()) {
                                        songAdapter.updateRequestList(tracksList);
                                        map.put(getString(R.string.songs), songAdapter);
                                    }
                                    if (!videosList.isEmpty()) {
                                        videoAdapter.updateRequestList(videosList);
                                        map.put(getString(R.string.videos), videoAdapter);
                                    }
                                    break;
                            }
                            if (!artistsList.isEmpty() && !topResult.equals("artist")) {
                                artistsAdapter.updateRequestList(artistsList);
                                map.put(getString(R.string.artists), artistsAdapter);
                            }
                            if (!albumsList.isEmpty() && !topResult.equals("album")) {
                                albumAdapter.updateRequestList(albumsList);
                                map.put(getString(R.string.albums), albumAdapter);
                            }
                            if (!playlistsList.isEmpty() && !topResult.equals("playlist")) {
                                playlistAdapter.updateRequestList(playlistsList);
                                map.put(getString(R.string.playlists), playlistAdapter);
                            }
                            if (!tracksList.isEmpty() && !topResult.equals("song") && !topResult.equals("video")) {
                                songAdapter.updateRequestList(tracksList);
                                map.put(getString(R.string.songs), songAdapter);
                            }
                            if (!videosList.isEmpty() && !topResult.equals("video")) {
                                videoAdapter.updateRequestList(videosList);
                                map.put(getString(R.string.videos), videoAdapter);
                            }
                            if (indexRequest != requests) return;
                            searchResponseAdapter.updateRequestList(map);
                            requestHistory.setVisibility(View.INVISIBLE);
                            responseView.setVisibility(View.VISIBLE);
                            loadView.setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (Exception e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            responseView.setVisibility(View.INVISIBLE);
                            loadView.setVisibility(View.INVISIBLE);
                            requestHistory.setVisibility(View.INVISIBLE);
                            errorView.setVisibility(View.VISIBLE);
                        }
                    });
                    //Log.e("Error", e.toString());
                }
            }
        };
        thread.start();
    }
}
