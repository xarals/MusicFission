package com.xaral.musicfission.ui;

import android.net.Uri;
import android.os.Build;
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
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.AlbumAdapter;
import com.xaral.musicfission.adapters.ArtistsAdapter;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArtistFragment extends Fragment {

    private ImageView imageArtist, line, more;
    private TextView statusArtistName, artistName, subscribers, views, songMore, relatedMore, albumMore, videoMore;
    private RecyclerView albumsView, songsView, videosView, relatedView;
    private AlbumAdapter albumAdapter;
    private SongAdapter songAdapter;
    private VideoAdapter videoAdapter;
    private ArtistsAdapter artistsAdapter;
    private ConstraintLayout loadView, errorView;
    private ScrollView artistScroll;

    private List<MusicRepository.Artist> relatedList = new ArrayList<>();
    private List<MusicRepository.Album> albumsList = new ArrayList<>();
    private List<MusicRepository.Track> songsList = new ArrayList<>();
    private List<MusicRepository.Video> videosList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_artist, container, false);
        loadView = root.findViewById(R.id.loadView);
        errorView = root.findViewById(R.id.errorView);
        artistScroll = root.findViewById(R.id.artistScroll);

        line = root.findViewById(R.id.line);
        more = root.findViewById(R.id.more);

        relatedMore = root.findViewById(R.id.relatedMore);
        videoMore = root.findViewById(R.id.videoMore);
        songMore = root.findViewById(R.id.songMore);
        albumMore = root.findViewById(R.id.albumMore);

        relatedMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> artists = new ArrayList<>();
                ArrayList<String> ids = new ArrayList<>();
                ArrayList<String> subscribers = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();
                for (MusicRepository.Artist artist : relatedList) {
                    artists.add(artist.getName());
                    ids.add(artist.getId());
                    subscribers.add(artist.getSubscribers());
                    images.add(artist.getImage());
                }
                Bundle bundle = new Bundle();
                bundle.putStringArray("artists", artists.toArray(new String[0]));
                bundle.putStringArray("ids", ids.toArray(new String[0]));
                bundle.putStringArray("subscribers", subscribers.toArray(new String[0]));
                bundle.putStringArray("images", images.toArray(new String[0]));
                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(root);
                navController.navigate(R.id.navigation_related_artists, bundle, navOptions);
            }
        });

        songMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> artists = new ArrayList<>();
                ArrayList<String> artistids = new ArrayList<>();
                ArrayList<String> uris = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();
                ArrayList<Long> durations = new ArrayList<>();
                for (MusicRepository.Track song : songsList) {
                    titles.add(song.getTitle());
                    artists.add(song.getArtist());
                    artistids.add(song.getArtistId());
                    uris.add(song.getUri());
                    images.add(song.getImage());
                    durations.add(song.getDuration());
                }
                Bundle bundle = new Bundle();
                long [] durationArray = new long[durations.size()];
                for (int i = 0; i < durations.size(); i++)
                    durationArray[i] = durations.get(i);
                bundle.putStringArray("titles", titles.toArray(new String[0]));
                bundle.putStringArray("artists", artists.toArray(new String[0]));
                bundle.putStringArray("artistids", artistids.toArray(new String[0]));
                bundle.putStringArray("uris", uris.toArray(new String[0]));
                bundle.putStringArray("images", images.toArray(new String[0]));
                bundle.putLongArray("durations", durationArray);
                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(root);
                navController.navigate(R.id.navigation_songs, bundle, navOptions);
            }
        });

        videoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> artists = new ArrayList<>();
                ArrayList<String> artistids = new ArrayList<>();
                ArrayList<String> uris = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();
                for (MusicRepository.Video video : videosList) {
                    titles.add(video.getTitle());
                    artists.add(video.getArtist());
                    artistids.add(video.getArtistId());
                    uris.add(video.getUrl());
                    images.add(video.getImage());
                }
                Bundle bundle = new Bundle();
                bundle.putStringArray("titles", titles.toArray(new String[0]));
                bundle.putStringArray("artists", artists.toArray(new String[0]));
                bundle.putStringArray("artistids", artistids.toArray(new String[0]));
                bundle.putStringArray("uris", uris.toArray(new String[0]));
                bundle.putStringArray("images", images.toArray(new String[0]));
                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(root);
                navController.navigate(R.id.navigation_videos, bundle, navOptions);
            }
        });

        albumMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> artists = new ArrayList<>();
                ArrayList<String> artistids = new ArrayList<>();
                ArrayList<String> browseIds = new ArrayList<>();
                ArrayList<String> years = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();
                for (MusicRepository.Album album : albumsList) {
                    titles.add(album.getTitle());
                    artists.add(album.getArtist());
                    artistids.add(album.getArtistId());
                    browseIds.add(album.getBrowseId());
                    years.add(album.getYear());
                    images.add(album.getImage());
                }
                Bundle bundle = new Bundle();
                bundle.putStringArray("titles", titles.toArray(new String[0]));
                bundle.putStringArray("artists", artists.toArray(new String[0]));
                bundle.putStringArray("artistids", artistids.toArray(new String[0]));
                bundle.putStringArray("browseIds", browseIds.toArray(new String[0]));
                bundle.putStringArray("years", years.toArray(new String[0]));
                bundle.putStringArray("images", images.toArray(new String[0]));
                NavOptions navOptions = new NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_out_left)
                        .setPopExitAnim(R.anim.slide_in_right)
                        .build();
                NavController navController = Navigation.findNavController(root);
                navController.navigate(R.id.navigation_albums, bundle, navOptions);
            }
        });

        imageArtist = root.findViewById(R.id.artistImage);
        statusArtistName = root.findViewById(R.id.statusArtistName);
        statusArtistName.setSelected(true);
        subscribers = root.findViewById(R.id.subscribers);
        views = root.findViewById(R.id.views);
        artistName = root.findViewById(R.id.artistName);
        artistName.setSelected(true);

        albumsView = root.findViewById(R.id.albumsView);
        songsView = root.findViewById(R.id.songsView);
        videosView = root.findViewById(R.id.videosView);
        relatedView = root.findViewById(R.id.relatedView);

        albumAdapter = new AlbumAdapter(root, new ArrayList<>());
        songAdapter = new SongAdapter(root, new ArrayList<>());
        videoAdapter = new VideoAdapter(root, new ArrayList<>());
        artistsAdapter = new ArtistsAdapter(root, new ArrayList<>());

        albumsView.setAdapter(albumAdapter);
        songsView.setAdapter(songAdapter);
        videosView.setAdapter(videoAdapter);
        relatedView.setAdapter(artistsAdapter);

        Uri image = Uri.parse(getArguments().getString("image"));
        String artistId = getArguments().getString("artistId");

        Picasso.get().load(image).placeholder(R.drawable.background_void).into(imageArtist);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(root.getContext(), more);
                if (FavoriteService.isFavoriteArtist(artistId))
                    popupMenu.getMenuInflater().inflate(R.menu.artist_unsubscribe_menu, popupMenu.getMenu());
                else
                    popupMenu.getMenuInflater().inflate(R.menu.artist_subscribe_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_subscribe_artist) {
                            Bundle arg = getArguments();
                            String artistName = arg.getString("artistName");
                            String artistId = arg.getString("artistId");
                            String subscribers = arg.getString("subscribers");
                            String image = arg.getString("image");
                            String views = arg.getString("views");
                            FavoriteService.addFavoriteArtist(new MusicRepository.Artist(artistName, artistId, subscribers, image, views));
                            Toast.makeText(root.getContext(), root.getContext().getString(R.string.you_have_successfully_subscribed), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (item.getItemId() == R.id.menu_unsubscribe_artist) {
                            FavoriteService.removeFavoriteArtist(getArguments().getString("artistId"));
                            Toast.makeText(root.getContext(), root.getContext().getString(R.string.you_have_successfully_unsubscribed), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        artistScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                float artistTitleHeight;
                try {
                    artistTitleHeight = getResources().getDimension(R.dimen.artistTitleHeight);
                } catch (Exception ex) {
                    //Log.e("scroll", ex.toString());
                    return;
                }
                if (artistScroll.getScrollY() == 0)
                    line.setAlpha(0f);
                else
                    line.setAlpha(1f);
                if (artistScroll.getScrollY() <= 0.8 * artistTitleHeight)
                    statusArtistName.setAlpha(0);
                else if (artistScroll.getScrollY() < 1.2 * artistTitleHeight)
                    statusArtistName.setAlpha(2.5f / artistTitleHeight * artistScroll.getScrollY() - 2);
                else
                    statusArtistName.setAlpha(1);
            }
        });
        if (!getArguments().keySet().contains("artistName")) {
            getArtist(artistId);
        } else {
            Bundle arg = getArguments();
            statusArtistName.setText(arg.getString("artistName"));
            artistName.setText(arg.getString("artistName"));
            subscribers.setText(arg.getString("subscribers"));
            views.setText(arg.getString("views"));
            relatedList = MusicRepository.Artist.restoreFromBundle("relatedList", arg);
            albumsList = MusicRepository.Album.restoreFromBundle("albumsList", arg);
            songsList = MusicRepository.Track.restoreFromBundle("songsList", arg);
            videosList = MusicRepository.Video.restoreFromBundle("videosList", arg);
            if (albumsList.size() <= 5)
                albumAdapter.updateRequestList(albumsList);
            else
                albumAdapter.updateRequestList(albumsList.subList(0, 5));
            if (songsList.size() <= 5)
                songAdapter.updateRequestList(songsList);
            else
                songAdapter.updateRequestList(songsList.subList(0, 5));
            if (videosList.size() <= 5)
                videoAdapter.updateRequestList(videosList);
            else
                videoAdapter.updateRequestList(videosList.subList(0, 5));
            if (relatedList.size() <= 5)
                artistsAdapter.updateRequestList(relatedList);
            else
                artistsAdapter.updateRequestList(relatedList.subList(0, 5));
            loadView.setVisibility(View.INVISIBLE);
            more.setVisibility(View.VISIBLE);
        }
        return root;
    }

    @Override
    public void onStart() {
        //Log.i("start", artistName.getText().toString());
        super.onStart();
    }

    @Override
    public void onPause() {
        Bundle outState = getArguments();
        if (outState == null) outState = new Bundle();
        outState.putString("artistName", artistName.getText().toString());
        outState.putString("subscribers", subscribers.getText().toString());
        outState.putString("views", views.getText().toString());
        MusicRepository.Artist.saveToBundle(outState, "relatedList", relatedList);
        MusicRepository.Album.saveToBundle(outState, "albumsList", albumsList);
        MusicRepository.Track.saveToBundle(outState, "songsList", songsList);
        MusicRepository.Video.saveToBundle(outState, "videosList", videosList);
        setArguments(outState);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Log.i("save", "Start");
        outState.putString("artistName", artistName.getText().toString());
        outState.putString("subscribers", subscribers.getText().toString());
        outState.putString("views", views.getText().toString());
        MusicRepository.Artist.saveToBundle(outState, "relatedList", relatedList);
        MusicRepository.Album.saveToBundle(outState, "albumsList", albumsList);
        MusicRepository.Track.saveToBundle(outState, "songsList", songsList);
        MusicRepository.Video.saveToBundle(outState, "videosList", videosList);
        super.onSaveInstanceState(outState);
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null)
            return;
        statusArtistName.setText(savedInstanceState.getString("artistName"));
        artistName.setText(savedInstanceState.getString("artistName"));
        subscribers.setText(savedInstanceState.getString("subscribers"));
        views.setText(savedInstanceState.getString("views"));
        relatedList = MusicRepository.Artist.restoreFromBundle("relatedList", savedInstanceState);
        albumsList = MusicRepository.Album.restoreFromBundle("albumsList", savedInstanceState);
        songsList = MusicRepository.Track.restoreFromBundle("songsList", savedInstanceState);
        videosList = MusicRepository.Video.restoreFromBundle("videosList", savedInstanceState);
        albumAdapter.updateRequestList(albumsList);
        songAdapter.updateRequestList(songsList);
        videoAdapter.updateRequestList(videosList);
        artistsAdapter.updateRequestList(relatedList);
        loadView.setVisibility(View.INVISIBLE);
        more.setVisibility(View.VISIBLE);
    }

    private void getArtist(String artistId) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Log.i("id", artistId);
                    JSONObject mapObjects = YTMusicApi.getArtist(artistId);
                    //Log.i("id", "successful");
                    String artist = mapObjects.getString("title");
                    String view1 = mapObjects.getString("views");
                    if (!view1.equals("") && view1.length() > 6)
                        view1 = view1.substring(0, view1.length() - 6);
                    String subscribers1 = mapObjects.getString("subscribers");
                    JSONArray songsListObject = new JSONArray();
                    JSONArray albumsListObject = new JSONArray();
                    JSONArray videosListObject = new JSONArray();
                    JSONArray relatedListObject = new JSONArray();
                    if (mapObjects.has("albums") && mapObjects.getJSONArray("albums").length() > 0)
                        albumsListObject = mapObjects.getJSONArray("albums");
                    if (mapObjects.has("songs") && mapObjects.getJSONArray("songs").length() > 0)
                        songsListObject = mapObjects.getJSONArray("songs");
                    if (mapObjects.has("videos") && mapObjects.getJSONArray("videos").length() > 0)
                        videosListObject = mapObjects.getJSONArray("videos");
                    if (mapObjects.has("related") && mapObjects.getJSONArray("related").length() > 0)
                        relatedListObject = mapObjects.getJSONArray("related");
                    for (int i = 0; i < albumsListObject.length(); i++) {
                        JSONObject el = albumsListObject.getJSONObject(i);
                        albumsList.add(new MusicRepository.Album(el.getString("title"), artist, artistId, el.getString("browseId"), el.getString("year"), el.getString("image")));
                    }
                    for (int i = 0; i < songsListObject.length(); i++) {
                        JSONObject el = songsListObject.getJSONObject(i);
                        songsList.add(new MusicRepository.Track(el.getString("title"), el.getString("artist"), el.getString("artistId"), el.getString("url"), el.getString("image"), 0));
                    }
                    for (int i = 0; i < videosListObject.length(); i++) {
                        JSONObject el = videosListObject.getJSONObject(i);
                        videosList.add(new MusicRepository.Video(el.getString("title"), el.getString("artist"), el.getString("artistId"), el.getString("url"), el.getString("image")));
                    }
                    for (int i = 0; i < relatedListObject.length(); i++) {
                        JSONObject el = relatedListObject.getJSONObject(i);
                        relatedList.add(new MusicRepository.Artist(el.getString("name"), el.getString("artistId"), el.getString("subscribers"), el.getString("image")));
                    }
                    if (getActivity() == null) return;
                    String finalView = view1;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle outState = getArguments();
                            if (outState == null) outState = new Bundle();
                            outState.putString("artistName", artist);
                            outState.putString("subscribers", subscribers1);
                            outState.putString("views", finalView);
                            setArguments(outState);
                            artistName.setText(artist);
                            statusArtistName.setText(artist);
                            if (!finalView.equals("") && finalView.length() > 6)
                                views.setText(finalView);
                            if (!subscribers1.equals(""))
                                subscribers.setText(subscribers1);
                            if (albumsList.size() <= 5)
                                albumAdapter.updateRequestList(albumsList);
                            else
                                albumAdapter.updateRequestList(albumsList.subList(0, 5));
                            if (songsList.size() <= 5)
                                songAdapter.updateRequestList(songsList);
                            else
                                songAdapter.updateRequestList(songsList.subList(0, 5));
                            if (videosList.size() <= 5)
                                videoAdapter.updateRequestList(videosList);
                            else
                                videoAdapter.updateRequestList(videosList.subList(0, 5));
                            if (relatedList.size() <= 5)
                                artistsAdapter.updateRequestList(relatedList);
                            else
                                artistsAdapter.updateRequestList(relatedList.subList(0, 5));
                            loadView.setVisibility(View.INVISIBLE);
                            more.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (Exception e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorView.setVisibility(View.VISIBLE);
                            loadView.setVisibility(View.INVISIBLE);
                        }
                    });
                    //Log.e("Error", e.toString());
                }
            }
        };
        thread.start();
    }
}