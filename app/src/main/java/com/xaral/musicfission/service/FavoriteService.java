package com.xaral.musicfission.service;

import android.content.SharedPreferences;
import android.net.Uri;

import com.google.android.exoplayer2.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;

public class FavoriteService {
    public static SharedPreferences mSettings;;

    public static void like(MusicRepository.Track track) {
        List<Object> songs = ConvertorService.stringToList(mSettings.getString("like", ""));
        Map<String, String> map = new HashMap<>();
        map.put("title", track.getTitle());
        map.put("artist", track.getArtist());
        map.put("artistId", track.getArtistId());
        map.put("url", track.getUri().toString());
        map.put("image", track.getImage().toString());
        map.put("duration", Long.toString(track.getDuration()));
        map.put("bigImage", track.getBigImage().toString());
        songs.add(map);
        //Log.i("liked", songs.toString());
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("like", ConvertorService.listToString(songs));
        editor.apply();
    }

    public static void dislike(MusicRepository.Track track) {
        List<Object> songs = (List<Object>) ConvertorService.stringToList(mSettings.getString("like", ""));
        for (Object song : songs) {
            Map<String, String> mapSong = (Map<String, String>) song;
            if (track.getUri().toString().equals(mapSong.get("url"))) {
                songs.remove(song);
                break;
            }
        }
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("like", ConvertorService.listToString(songs));
        editor.apply();
    }

    public static void createPlaylist(String title) {
        List<Object> songs = new ArrayList<>();
        Map<String, Object> playlists = ConvertorService.stringToMap(mSettings.getString("playlists", ""));
        Set<String> keys = playlists.keySet();
        Map<String, Object> playlist = new HashMap<>();
        int browseId = 0;
        while (keys.contains(Integer.toString(browseId)))
            browseId++;
        playlist.put("author", "You");
        playlist.put("title", title);
        playlist.put("year", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        playlist.put("songs", songs);
        playlist.put("image", "");
        playlists.put(Integer.toString(browseId), playlist);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("playlists", ConvertorService.mapToString(playlists));
        editor.apply();
    }

    public static void addToPlaylist(String browseId, MusicRepository.Track track) {
        Map<String, Object> playlists = ConvertorService.stringToMap(mSettings.getString("playlists", ""));
        Map<String, Object> playlist = (Map<String, Object>) playlists.get(browseId);
        List<Object> songs = (List<Object>) playlist.get("songs");
        Map<String, String> map = new HashMap<>();
        map.put("title", track.getTitle());
        map.put("artist", track.getArtist());
        map.put("artistId", track.getArtistId());
        map.put("url", track.getUri().toString());
        map.put("image", track.getImage().toString());
        map.put("duration", Long.toString(track.getDuration()));
        map.put("bigImage", track.getBigImage().toString());
        songs.add(map);
        playlist.put("songs", songs);
        playlists.put(browseId, playlist);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("playlists", ConvertorService.mapToString(playlists));
        editor.apply();
    }

    public static void savePlaylistTrack(String title, String author, String browseId, String year, List<MusicRepository.Track> songs, String image) {
        List<Object> newSongs = new ArrayList<>();
        Map<String, Object> playlist = new HashMap<>();
        for (MusicRepository.Track track : songs) {
            Map<String, String> map = new HashMap<>();
            map.put("title", track.getTitle());
            map.put("artist", track.getArtist());
            map.put("artistId", track.getArtistId());
            map.put("url", track.getUri().toString());
            map.put("image", track.getImage().toString());
            map.put("duration", Long.toString(track.getDuration()));
            map.put("bigImage", track.getBigImage().toString());
            newSongs.add(map);
        }
        playlist.put("author", author);
        playlist.put("title", title);
        playlist.put("year", year);
        playlist.put("songs", newSongs);
        playlist.put("image", image);
        SharedPreferences.Editor editor = mSettings.edit();
        Map<String, Object> playlists = ConvertorService.stringToMap(mSettings.getString("playlists", ""));
        playlists.put(browseId, playlist);
        editor.putString("playlists", ConvertorService.mapToString(playlists));
        editor.apply();
    }

    public static Map<String, Object> getPlaylist(String browseId) {
        Map<String, Object> playlist = (Map<String, Object>) ConvertorService.stringToMap(mSettings.getString("playlists", "")).get(browseId);
        return playlist;
    }

    public static String getPlaylistAuthor(String browseId) {
        Map<String, Object> playlist = getPlaylist(browseId);
        return (String) playlist.get("author");
    }

    public static String getPlaylistTitle(String browseId) {
        Map<String, Object> playlist = getPlaylist(browseId);
        return (String) playlist.get("title");
    }

    public static String getPlaylistYear(String browseId) {
        Map<String, Object> playlist = getPlaylist(browseId);
        return (String) playlist.get("year");
    }

    public static String getPlaylistUriImage(String browseId) {
        Map<String, Object> playlist = getPlaylist(browseId);
        return (String) playlist.get("image");
    }

    public static List<MusicRepository.Track> getPlaylistSongs(String browseId) {
        Map<String, Object> playlist = getPlaylist(browseId);
        List<MusicRepository.Track> songs = new ArrayList<>();
        List<Object> playlistSongs = (List<Object>) playlist.get("songs");
        for (Object song1 : playlistSongs){
            if (song1.equals("")) continue;
            Map<String, String> song = (Map<String, String>) song1;
            songs.add(new MusicRepository.Track(song.get("title"), song.get("artist"), song.get("artistId"), song.get("url"), song.get("image"), 0, song.get("bigImage")));
        }
        return songs;
    }

    public static List<MusicRepository.Track> getLiked() {
        List<MusicRepository.Track> tracks = new ArrayList<>();
        List<Object> playlistSongs = ConvertorService.stringToList(mSettings.getString("like", ""));
        for (Object playlistSong : playlistSongs) {
            if (playlistSong.equals("")) continue;
            Map<String, String> song = (Map<String, String>) playlistSong;
            MusicRepository.Track track = new MusicRepository.Track(song.get("title"), song.get("artist"), song.get("artistId"), song.get("url"), song.get("image"), 0, song.get("bigImage"));
            tracks.add(track);
        }
        return tracks;
    }

    public static List<String> getPlaylists() {
        Set<String> playlists = ConvertorService.stringToMap(mSettings.getString("playlists", "")).keySet();
        return new ArrayList<>(playlists);
    }

    public static void renamePlaylist(String browseId, String newName) {
        Map<String, Object> playlists = ConvertorService.stringToMap(mSettings.getString("playlists", ""));
        if (playlists.get(browseId) == null)
            return;
        Map<String, Object> playlist = (Map<String, Object>) playlists.get(browseId);
        playlist.put("title", newName);
        playlists.put(browseId, playlist);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("playlists", ConvertorService.mapToString(playlists));
        editor.apply();
    }

    public static void deletePlaylist(String browseId) {
        Map<String, Object> playlists = ConvertorService.stringToMap(mSettings.getString("playlists", ""));
        playlists.remove(browseId);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("playlists", ConvertorService.mapToString(playlists));
        editor.apply();
    }

    public static boolean isLiked(String uri) {
        List<MusicRepository.Track> liked = getLiked();
        for (MusicRepository.Track song : liked) {
            if (song.getUri().toString().equals(uri)) return true;
        }
        return false;
    }

    public static void saveRequest(String request) {
        List<Object> requestHistory = ConvertorService.stringToList(mSettings.getString("requestHistory", ""));
        requestHistory.remove(request);
        requestHistory.add(0, request);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("requestHistory", ConvertorService.listToString(requestHistory));
        editor.apply();
    }

    public static List<String> getRequests() {
        List<Object> listObject = ConvertorService.stringToList(mSettings.getString("requestHistory", ""));
        List<String> listString = new ArrayList<>();
        for (Object request : listObject)
            listString.add((String) request);
        return listString;
    }

    public static void saveToHistory(MusicRepository.Track track, String state) {
        List<Object> trackHistory = new ArrayList<>();
        if (!mSettings.getString("trackHistory", "").equals(""))
            trackHistory = ConvertorService.stringToList(mSettings.getString("trackHistory", ""));
        Map<String, String> trackMap = new HashMap<>();
        trackMap.put("title", track.getTitle());
        trackMap.put("artist", track.getArtist());
        trackMap.put("artistId", track.getArtistId());
        trackMap.put("url", track.getUri().toString());
        trackMap.put("image", track.getImage().toString());
        trackMap.put("duration", Long.toString(track.getDuration()));
        trackMap.put("bigImage", track.getBigImage().toString());
        Map<String, Object> trackHistoryMap = new HashMap<>();
        trackHistoryMap.put("track", trackMap);
        trackHistoryMap.put("state", state);
        for (Object obj :trackHistory) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, String> song = (Map<String, String>) map.get("track");
            if (song.get("url").equals(track.getUri())) {
                trackHistory.remove(obj);
                break;
            }
        }
        trackHistory.add(0, trackHistoryMap);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("trackHistory", ConvertorService.listToString(trackHistory));
        editor.apply();
    }

    public static List<MusicRepository.Track> getFromHistory() {
        List<MusicRepository.Track> trackList = new ArrayList<>();
        if (mSettings.getString("trackHistory", "").equals(""))
            return trackList;
        List<Object> trackHistory = ConvertorService.stringToList(mSettings.getString("trackHistory", ""));
        for (Object track : trackHistory) {
            Map<String, String> trackHistoryMap = (Map<String, String>) (((Map<String, Object>) track).get("track"));
            MusicRepository.Track song = new MusicRepository.Track(trackHistoryMap.get("title"), trackHistoryMap.get("artist"), trackHistoryMap.get("artistId"), trackHistoryMap.get("url"), trackHistoryMap.get("image"), 0, trackHistoryMap.get("bigImage"));
            trackList.add(song);
        }
        return trackList;
    }

    public static List<MusicRepository.Track> getFromHistory(String state) {
        List<MusicRepository.Track> trackList = new ArrayList<>();
        if (mSettings.getString("trackHistory", "").equals(""))
            return trackList;
        List<Object> trackHistoryObjects = ConvertorService.stringToList(mSettings.getString("trackHistory", ""));
        for (Object track : trackHistoryObjects) {
            Map<String, Object> trackHistory = (Map<String, Object>) track;
            if (!trackHistory.get("state").toString().equals(state))
                continue;
            Map<String, String> trackHistoryMap = (Map<String, String>) (trackHistory.get("track"));
            MusicRepository.Track song = new MusicRepository.Track(trackHistoryMap.get("title"), trackHistoryMap.get("artist"), trackHistoryMap.get("artistId"), trackHistoryMap.get("url"), trackHistoryMap.get("image"), 0, trackHistoryMap.get("bigImage"));
            trackList.add(song);
        }
        return trackList;
    }

    public static void deleteTrackHistory(String url) {
        List<Object> trackHistory = new ArrayList<>();
        if (!mSettings.getString("trackHistory", "").equals(""))
            trackHistory = ConvertorService.stringToList(mSettings.getString("trackHistory", ""));
        for (Object obj :trackHistory) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, String> song = (Map<String, String>) map.get("track");
            if (song.get("url").equals(url)) {
                trackHistory.remove(obj);
                break;
            }
        }
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("trackHistory", ConvertorService.listToString(trackHistory));
        editor.apply();
    }

    public static void addFavoriteArtist(MusicRepository.Artist artist) {
        List<Object> favoriteArtistsObjects = new ArrayList<>();
        if (!mSettings.getString("favoriteArtists", "").equals("") && !mSettings.getString("favoriteArtists", "").equals("[]"))
            favoriteArtistsObjects = ConvertorService.stringToList(mSettings.getString("favoriteArtists", ""));
        Map<String, String> newArtist = new HashMap<>();
        newArtist.put("artistName", artist.getName());
        newArtist.put("artistId", artist.getId());
        newArtist.put("subscribers", artist.getSubscribers());
        newArtist.put("image", artist.getImage());
        newArtist.put("views", artist.getViews());
        favoriteArtistsObjects.add(0, newArtist);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("favoriteArtists", ConvertorService.listToString(favoriteArtistsObjects));
        editor.apply();
    }

    public static void removeFavoriteArtist(String artistId) {
        List<MusicRepository.Artist> artistList = getFavoriteArtist();
        for (MusicRepository.Artist artist : artistList) {
            if (artist.getId().equals(artistId)) {
                artistList.remove(artist);
                break;
            }
        }
        List<Object> artistListMap = new ArrayList<>();
        for (MusicRepository.Artist artist : artistList) {
            Map<String, String> artistMap = new HashMap<>();
            artistMap.put("artistName", artist.getName());
            artistMap.put("artistId", artist.getId());
            artistMap.put("subscribers", artist.getSubscribers());
            artistMap.put("image", artist.getImage());
            artistMap.put("views", artist.getViews());
            artistListMap.add(artistMap);
        }
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("favoriteArtists", ConvertorService.listToString(artistListMap));
        editor.apply();
    }

    public static List<MusicRepository.Artist> getFavoriteArtist() {
        List<MusicRepository.Artist> artistList = new ArrayList<>();
        List<Object> favoriteArtistsObjects;
        if (!mSettings.getString("favoriteArtists", "").equals(""))
            favoriteArtistsObjects = ConvertorService.stringToList(mSettings.getString("favoriteArtists", ""));
        else
            return new ArrayList<>();
        for (Object artistObject : favoriteArtistsObjects) {
            if (artistObject.equals("")) continue;
            Map<String, String> artist = (Map<String, String>) artistObject;
            artistList.add(new MusicRepository.Artist(artist.get("artistName"), artist.get("artistId"), artist.get("subscribers"), artist.get("image"), artist.get("views")));
        }
        return artistList;
    }

    public static boolean isFavoriteArtist(String artistId) {
        List<MusicRepository.Artist> artistList = getFavoriteArtist();
        for (MusicRepository.Artist artist : artistList) {
            if (artist.getId().equals(artistId))
                return true;
        }
        return false;
    }

    public static void updateRelatedSongs(List<MusicRepository.Track> trackList) {
        List<Object> trackListMap = new ArrayList<>();
        for (MusicRepository.Track track : trackList) {
            Map<String, String> trackMap = new HashMap<>();
            trackMap.put("title", track.getTitle());
            trackMap.put("artist", track.getArtist());
            trackMap.put("artistId", track.getArtistId());
            trackMap.put("url", track.getUri().toString());
            trackMap.put("image", track.getImage().toString());
            trackMap.put("duration", Long.toString(track.getDuration()));
            trackMap.put("bigImage", track.getBigImage().toString());
            trackListMap.add(trackMap);
        }
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("relatedSongs", ConvertorService.listToString(trackListMap));
        editor.apply();
    }

    public static List<MusicRepository.Track> getRelatedSongs() {
        List<MusicRepository.Track> tracks = new ArrayList<>();
        List<Object> playlistSongs = ConvertorService.stringToList(mSettings.getString("relatedSongs", ""));
        for (Object playlistSong : playlistSongs) {
            if (playlistSong.equals("")) continue;
            Map<String, String> song = (Map<String, String>) playlistSong;
            MusicRepository.Track track = new MusicRepository.Track(song.get("title"), song.get("artist"), song.get("artistId"), song.get("url"), song.get("image"), 0, song.get("bigImage"));
            tracks.add(track);
        }
        return tracks;
    }

    public static boolean isFirstStart() {
        if (mSettings.getString("isFirstStart", "").equals("")) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString("isFirstStart", "false");
            editor.apply();
            return true;
        }
        return false;
    }

    public static void saveDownloads(MusicRepository.Track track) {
        List<Object> trackDownloads = new ArrayList<>();
        if (!mSettings.getString("trackDownloads", "").equals(""))
            trackDownloads = ConvertorService.stringToList(mSettings.getString("trackDownloads", ""));
        Map<String, String> trackMap = new HashMap<>();
        trackMap.put("title", track.getTitle());
        trackMap.put("artist", track.getArtist());
        trackMap.put("artistId", track.getArtistId());
        trackMap.put("url", track.getUri().toString());
        trackMap.put("image", track.getImage().toString());
        trackMap.put("duration", Long.toString(track.getDuration()));
        trackMap.put("bigImage", track.getBigImage().toString());
        trackDownloads.add(0, trackMap);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("trackDownloads", ConvertorService.listToString(trackDownloads));
        editor.apply();
    }

    public static List<MusicRepository.Track> getDownloads() {
        List<MusicRepository.Track> tracks = new ArrayList<>();
        List<Object> playlistSongs = ConvertorService.stringToList(mSettings.getString("trackDownloads", ""));
        for (Object playlistSong : playlistSongs) {
            if (playlistSong.equals("")) continue;
            Map<String, String> song = (Map<String, String>) playlistSong;
            MusicRepository.Track track = new MusicRepository.Track(song.get("title"), song.get("artist"), song.get("artistId"), song.get("url"), song.get("image"), 0, song.get("bigImage"));
            tracks.add(track);
        }
        return tracks;
    }

    public static void deleteDownload(MusicRepository.Track track) {
        List<Object> songs = ConvertorService.stringToList(mSettings.getString("trackDownloads", ""));
        for (Object song : songs) {
            Map<String, String> mapSong = (Map<String, String>) song;
            if (track.getUri().toString().equals(mapSong.get("url"))) {
                songs.remove(song);
                break;
            }
        }
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("trackDownloads", ConvertorService.listToString(songs));
        editor.apply();
    }

    public static void setLocale(String languageCode) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("locale", languageCode);
        editor.apply();
    }

    public static String getLocale() {
        return mSettings.getString("locale", "");
    }

    public static void setFolder(String path) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("downloadFolder", path);
        editor.apply();
    }

    public static String getFolder() {
        return mSettings.getString("downloadFolder", "");
    }
}
