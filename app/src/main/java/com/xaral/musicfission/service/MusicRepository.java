package com.xaral.musicfission.service;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.ytmusicapi.YTMusicApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class MusicRepository {
    public List<Track> data = new ArrayList<Track>();
    public List<Track> relatedData = new ArrayList<>();
    private int maxIndex = -1;
    private int currentItemIndex = -1;
    public int repeat = 0;
    public boolean req = false;
    public Track getNext() {
        if (currentItemIndex == maxIndex && repeat == 1 && maxIndex > -1){
            currentItemIndex = 0;
            return getCurrent();
        } else if (currentItemIndex == maxIndex && relatedData.size() > 0) {
            data.add(relatedData.get(0));
            relatedData.remove(0);
            currentItemIndex++;
            maxIndex++;
        } else if (currentItemIndex == maxIndex) {
            newRelatedData();
            return new Track("", "", "", "", "", 0);
        }
        else
            currentItemIndex++;
        return getCurrent();
    }

    public Track willBeNext() {
        if (currentItemIndex == maxIndex && maxIndex == -1)
            return null;
        else if (repeat == 2)
            return data.get(currentItemIndex);
        else if (currentItemIndex == maxIndex && repeat == 1 && maxIndex > -1)
            return data.get(0);
        else if (currentItemIndex == maxIndex && relatedData.size() > 0)
            return relatedData.get(0);
        else if (maxIndex > currentItemIndex)
            return data.get(currentItemIndex + 1);
        else
            return null;

    }

    public Track getPrevious() {
        if (currentItemIndex == 0)
            currentItemIndex = maxIndex;
        else if (currentItemIndex == -1)
            return new Track("", "", "", "", "", 0);
        else
            currentItemIndex--;
        return getCurrent();
    }

    public Track getCurrent() {
        maxIndex = data.size() - 1;
        if ((maxIndex - currentItemIndex < 5 && currentItemIndex > -1 && relatedData.size() < 3 && !req) || (currentItemIndex > -1 && relatedData.size() == 0 && !req)) {
            List<Track> liked = FavoriteService.getLiked();
            List<Track> history = FavoriteService.getFromHistory("end");
            List<Track> request = new ArrayList<>();
            for (int i = maxIndex; i >= 0 && i > maxIndex - 3; i--) {
                request.add(data.get(i));
            }
            if (request.size() < 3 && history.size() > 0) {
                request.add(history.get(0));
            }
            if (request.size() < 3 && liked.size() > 0) {
                request.add(liked.get(liked.size() - 1));
            }
            updateRelatedList(request);
        }
        if (currentItemIndex == -1 && maxIndex < 0)
            return new Track("", "", "", "", "", 0);
        else if (currentItemIndex == -1)
            return getNext();
        if (currentItemIndex > maxIndex)
            return data.get(maxIndex);
        else if (currentItemIndex < 0 && maxIndex >= 0)
            return data.get(0);
        return data.get(currentItemIndex);
    }

    public void setCurrentBigImage(Uri uri) {
        data.get(currentItemIndex).setBigImage(uri.toString());
    }

    public int getCurrentItemIndex() {
        return currentItemIndex;
    }

    public int getMaxIndex() { return maxIndex; }

    public int getRelatedDataSize() { return relatedData.size(); }
    public void newRelatedData() {
        List<Track> liked = FavoriteService.getLiked();
        List<Track> history = FavoriteService.getFromHistory("end");
        List<Track> request = new ArrayList<>();
        for (int i = maxIndex; i >= 0 && i > maxIndex - 3; i--) {
            request.add(data.get(i));
        }
        if (request.size() < 3 && history.size() > 0) {
            request.add(history.get(0));
        }
        if (request.size() < 3 && liked.size() > 0) {
            request.add(liked.get(liked.size() - 1));
        }
        updateRelatedList(request);
    }

    public void playLast(){
        currentItemIndex = maxIndex;
    }

    public void addEnd(Track track){
        data.add(track);
        maxIndex = data.size() - 1;
    }

    public void playNext(Track track){
        data.add(currentItemIndex + 1, track);
        maxIndex = data.size() - 1;
    }

    public List<Track> getData(){
        return data;
    }

    public void newData(List<Track> tracks) {
        data = new ArrayList<>();
        data.addAll(tracks);
        maxIndex = data.size() - 1;
        currentItemIndex = -1;
    }

    public void setCurrentItemIndex(int index){
        currentItemIndex = index;
    }

    public void deleteTrack(int index){
        data.remove(index);
        if (currentItemIndex >= index) currentItemIndex--;
        maxIndex = data.size() - 1;;
    }

    public Track getNextSecondPlayer() {
        Track track;
        if (currentItemIndex == maxIndex && relatedData.size() == 0)
            return null;
        else if (currentItemIndex == maxIndex)
            return relatedData.get(0);
        else
            return data.get(currentItemIndex + 1);
    }

    private void updateRelatedList(List<Track> trackList) {
        req = true;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    relatedData = new ArrayList<>();
                    List<String> linkList = new ArrayList<>();
                    for (Track track : data)
                        linkList.add(track.getUri());
                    for (int i = 0; i < trackList.size(); i++) {
                        String data = trackList.get(i).getUri().split("\\?v=")[1];
                        //Log.i("data", data);
                        JSONArray responseBody = YTMusicApi.getRelatedSongs(data);
                        List<JSONObject> tracksObject = new ArrayList<>();
                        for (int j = 0; j < responseBody.length(); j++)
                            tracksObject.add(responseBody.getJSONObject(j));
                        for (JSONObject trackObject : tracksObject) {
                            if (relatedData.size() >= 15) break;
                            if (linkList.contains(trackObject.getString("url"))) continue;
                            relatedData.add(new Track(trackObject.getString("title"), trackObject.getString("artist"), trackObject.getString("artistId"), trackObject.getString("url"), trackObject.getString("image"), 0, trackObject.getString("image")));
                            linkList.add(trackObject.getString("url"));
                        }
                    }
                    FavoriteService.updateRelatedSongs(relatedData);
                    req = false;
                } catch (Exception e) {
                    req = false;
                    //Log.e("Error", e.toString());
                }
            }
        };
        thread.start();
    }

    public static class Track implements Serializable {

        private String title;
        private String artist;
        private String artistId;
        private String uri, image, bigImage;
        private long duration;

        public Track(String title, String artist, String artistId, String uri, String image, long duration) {
            this.title = title;
            this.artist = artist;
            this.artistId = artistId;
            this.uri = uri;
            this.image = image;
            this.duration = duration;
            this.bigImage = "";
        }

        public Track(String title, String artist, String artistId, String uri, String image, long duration, String bigImage) {
            this.title = title;
            this.artist = artist;
            this.artistId = artistId;
            this.uri = uri;
            this.image = image;
            this.duration = duration;
            this.bigImage = bigImage;
        }

        public static Bundle saveToBundle(Bundle bundle, String key, List<Track> list) {
            bundle.putSerializable(key, (Serializable) list);
            return bundle;
        }

        public static List<Track> restoreFromBundle(String key, Bundle bundle) {
            List<Track> list = new ArrayList<>();
            if (bundle != null && bundle.containsKey(key)) {
                list = (List<Track>) bundle.getSerializable(key);
            }
            return list;
        }

        public String getBigImage() { return bigImage; }
        public void setBigImage(String bigImage) { this.bigImage = bigImage; }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

        public String getArtistId() { return artistId; }

        public String getUri() {
            return uri;
        }

        public String getImage() { return image; }

        public long getDuration() { return duration; }
    }

    public static class Artist implements Serializable {
        private String name;
        private String id;
        private String subscribers;
        private String image;
        private String views;

        public Artist(String name, String id, String subscribers, String image) {
            this.name = name;
            this.id = id;
            this.subscribers = subscribers;
            this.image = image;
            this.views = "";
        }

        public Artist(String name, String id, String subscribers, String image, String views) {
            this.name = name;
            this.id = id;
            this.subscribers = subscribers;
            this.image = image;
            this.views = views;
        }

        public static Bundle saveToBundle(Bundle bundle, String key, List<Artist> list) {
            bundle.putSerializable(key, (Serializable) list);
            return bundle;
        }

        public static List<Artist> restoreFromBundle(String key, Bundle bundle) {
            List<Artist> list = new ArrayList<>();
            if (bundle != null && bundle.containsKey(key)) {
                list = (List<Artist>) bundle.getSerializable(key);
            }
            return list;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getImage() {
            return image;
        }

        public String getSubscribers() {
            return subscribers;
        }

        public String getViews() { return views; }
    }

    public static class Album implements Serializable {
        private String title;
        private String artist;
        private String artistId;
        private String browseId;
        private String year;
        private String image;

        public Album(String title, String artist, String artistId, String browseId, String year, String image) {
            this.title = title;
            this.artist = artist;
            this.artistId = artistId;
            this.browseId = browseId;
            this.year = year;
            this.image = image;
        }

        public static Bundle saveToBundle(Bundle bundle, String key, List<Album> list) {
            bundle.putSerializable(key, (Serializable) list);
            return bundle;
        }

        public static List<Album> restoreFromBundle(String key, Bundle bundle) {
            List<Album> list = new ArrayList<>();
            if (bundle != null && bundle.containsKey(key)) {
                list = (List<Album>) bundle.getSerializable(key);
            }
            return list;
        }

        public String getArtist() {
            return artist;
        }

        public String getArtistId() {
            return artistId;
        }

        public String getBrowseId() {
            return browseId;
        }

        public String getTitle() {
            return title;
        }

        public String getYear() {
            return year;
        }

        public String getImage() {
            return image;
        }
    }

    public static class Playlist implements Serializable {
        private String title;
        private String artist;
        private String browseId;
        private String itemCount;
        private String image;

        public Playlist(String title, String artist, String browseId, String itemCount, String image) {
            this.title = title;
            this.artist = artist;
            this.browseId = browseId;
            this.itemCount = itemCount;
            this.image = image;
        }

        public static Bundle saveToBundle(Bundle bundle, String key, List<Playlist> list) {
            bundle.putSerializable(key, (Serializable) list);
            return bundle;
        }

        public static List<Playlist> restoreFromBundle(String key, Bundle bundle) {
            List<Playlist> list = new ArrayList<>();
            if (bundle != null && bundle.containsKey(key)) {
                list = (List<Playlist>) bundle.getSerializable(key);
            }
            return list;
        }

        public String getImage() {
            return image;
        }

        public String getBrowseId() {
            return browseId;
        }

        public String getArtist() {
            return artist;
        }

        public String getTitle() {
            return title;
        }

        public String getItemCount() {
            return itemCount;
        }
    }

    public static class Video implements Serializable {
        private String title;
        private String artist;
        private String artistId;
        private String url;
        private String image;

        public Video(String title, String artist, String artistId, String url, String image) {
            this.title = title;
            this.artist = artist;
            this.artistId = artistId;
            this.url = url;
            this.image = image;
        }

        public static Bundle saveToBundle(Bundle bundle, String key, List<Video> list) {
            bundle.putSerializable(key, (Serializable) list);
            return bundle;
        }

        public static List<Video> restoreFromBundle(String key, Bundle bundle) {
            List<Video> list = new ArrayList<>();
            if (bundle != null && bundle.containsKey(key)) {
                list = (List<Video>) bundle.getSerializable(key);
            }
            return list;
        }

        public String getArtist() {
            return artist;
        }

        public String getUrl() {
            return url;
        }

        public String getArtistId() {
            return artistId;
        }

        public String getTitle() {
            return title;
        }

        public String getImage() {
            return image;
        }
    }

}
