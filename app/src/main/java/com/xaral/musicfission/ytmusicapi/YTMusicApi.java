package com.xaral.musicfission.ytmusicapi;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static com.xaral.musicfission.ytmusicapi.Parser.*;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YTMusicApi {
    public static JSONObject search(String query) throws JSONException {
        JSONArray searchResults = new JSONArray();
        JSONObject requestBody = new JSONObject();
        requestBody.put("query", query);
        JSONObject response = RequestService.makeRequest(searchRequestUrl, requestBody);
        if (!response.has("contents"))
            return new JSONObject();
        JSONObject resultsObject;
        if (response.getJSONObject("contents").has(searchResultsPath[1].toString()))
            resultsObject = new JSONObjectPath(response).getJSONObject(searchResultsPath);
        else
            resultsObject = response.getJSONObject("contents");
        JSONArray results = new JSONObjectPath(resultsObject).getJSONArray(sectionList);
        List<Object> resultsObjectList = new ArrayList<>();
        for (int i = 0; i < results.length(); i++)
            resultsObjectList.add(results.get(i));
        if (results.length() == 1 && resultsObjectList.contains("itemSectionRenderer"))
            return new JSONObject();

        for (int i = 0; i < results.length(); i++) {
            JSONObject res = results.getJSONObject(i);
            JSONArray arrayResult = null;
            if (res.has("musicCardShelfRenderer")) {
                JSONObject topResult = parseTopResult(res.getJSONObject("musicCardShelfRenderer"));
                if (topResult != null)
                    searchResults.put(topResult);
                arrayResult = new JSONObjectPath(res).getJSONArray(musicCardShelfRenderer);
                if (arrayResult == null) continue;
            }
            else if (res.has(musicShelfContent[0].toString())) {
                arrayResult = new JSONObjectPath(res).getJSONArray(musicShelfContent);
            }
            JSONArray parsedSearchResults = parseSearchResult(arrayResult);
            for (int j = 0; j < parsedSearchResults.length(); j++)
                searchResults.put(parsedSearchResults.get(j));
        }
        JSONArray artistsArray = new JSONArray();
        JSONArray albumsArray = new JSONArray();
        JSONArray playlistsArray = new JSONArray();
        JSONArray songsArray = new JSONArray();
        JSONArray videosArray = new JSONArray();
        JSONObject answer = new JSONObject();
        for (int i = 0; i < searchResults.length(); i++) {
            JSONObject result = searchResults.getJSONObject(i);
            if (result.has("category")) {
                answer.put("topResult", result.getString("resultType"));
                result.remove("category");
            }
            if (result.getString("resultType").equalsIgnoreCase("artist")) {
                result.remove("resultType");
                artistsArray.put(result);
            }
            else if (result.getString("resultType").equalsIgnoreCase("album")) {
                result.remove("resultType");
                albumsArray.put(result);
            }
            else if (result.getString("resultType").equalsIgnoreCase("playlist")) {
                result.remove("resultType");
                playlistsArray.put(result);
            }
            else if (result.getString("resultType").equalsIgnoreCase("song")) {
                result.remove("resultType");
                songsArray.put(result);
            }
            else if (result.getString("resultType").equalsIgnoreCase("video")) {
                result.remove("resultType");
                videosArray.put(result);
            }
        }
        answer.put("artists", artistsArray);
        answer.put("albums", albumsArray);
        answer.put("playlists", playlistsArray);
        answer.put("songs", songsArray);
        answer.put("videos", videosArray);
        return answer;
    }
    public static JSONObject getArtist(String artistId) throws JSONException {
        if (artistId.startsWith("MPLA")) artistId = artistId.substring(4);
        JSONObject requestBody = new JSONObject(String.format("{\"browseId\": \"%s\"}", artistId));
        JSONObject response = RequestService.makeRequest(artistRequestUrl, requestBody);
        JSONObject answer = new JSONObject();
        JSONObjectPath jsonObject = new JSONObjectPath(response);
        String title = jsonObject.getString(namePath);
        String subscribers = jsonObject.getString(subscribersPath);
        String views = "";
        JSONArray jsonArray = jsonObject.getJSONArray(sectionListContents);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            if (!jsonObj.has("musicDescriptionShelfRenderer")) continue;
            JSONObjectPath jsonViews = new JSONObjectPath(jsonObj);
            views = jsonViews.getString(viewsPath);
        }
        answer.put("title", title);
        answer.put("subscribers", subscribers);
        answer.put("views", views);
        answer.put("albums", parseAlbums(jsonArray));
        answer.put("songs", parsePlaylistItems(new JSONObjectPath(jsonArray.getJSONObject(0)).getJSONArray(musicShelfContent)));
        answer.put("videos", parseVideos(jsonArray));
        answer.put("related", parseRelatedArtist(jsonArray));
        return answer;
    }

    public static JSONObject getAlbum(String browseId) throws JSONException {
        JSONObject requestBody = new JSONObject(String.format("{\"browseId\": \"%s\"}", browseId));
        JSONObject response = RequestService.makeRequest(artistRequestUrl, requestBody);
        JSONObject album = parseAlbumHeader(response);
        JSONArray results = new JSONObjectPath(response).getJSONArray(albumResultPath);
        JSONArray tracks = parsePlaylistItems(results);
        album.put("songs", tracks);
        return album;
    }

    public static JSONObject getPlaylist(String browseId) throws JSONException {
        if (!browseId.startsWith("VL"))
            browseId = "VL" + browseId;
        JSONObject requestBody = new JSONObject(String.format("{\"browseId\": \"%s\"}", browseId));
        JSONObject response = RequestService.makeRequest(artistRequestUrl, requestBody);
        JSONObject results = new JSONObjectPath(response).getJSONObject(playlistResultPath);
        JSONObject header = new JSONObjectPath(response).getJSONObject(playlistHeaderDetail);
        String title = new JSONObjectPath(header).getString(titleAlbumsPath);
        String author = "";
        String year = "";
        if (new JSONObjectPath(header).getString(authorPlaylist) != null)
            author = new JSONObjectPath(header).getString(authorPlaylist);
        int runCount = new JSONObjectPath(header).getJSONArray(subtitleRunsPlaylist).length();
        if (runCount > 1) {
            year = new JSONObjectPath(header).getString(subtitle2Playlist);
        }

        JSONArray tracks = new JSONArray();

        if (results.has("contents"))
            tracks = parsePlaylistItems(results.getJSONArray("contents"));
        JSONObject playlist = new JSONObject();
        playlist.put("title", title);
        playlist.put("author", author);
        playlist.put("year", year);
        playlist.put("videos", tracks);
        return playlist;
    }

    public static String getSongImage(String videoId) throws JSONException {
        long daysSinceEpoch = 19710;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            LocalDate epoch = LocalDate.of(1970, 1, 1);
            daysSinceEpoch = ChronoUnit.DAYS.between(epoch, today);
        }
        JSONObject requestBody = new JSONObject(String.format("{\"playbackContext\": {\"contentPlaybackContext\": {\"signatureTimestamp\": \"%s\"}},\"video_id\": \"%s\"}", daysSinceEpoch, videoId));
        JSONObject response = RequestService.makeRequest(songRequestUrl, requestBody);
        JSONArray thumbnails = new JSONObjectPath(response).getJSONArray(thumbnailsSongPath);
        String image = "";
        JSONObject thumbnailMaxSize = null;
        for (int i = 0; i < thumbnails.length(); i++) {
            JSONObject thumbnail = thumbnails.getJSONObject(i);
            if (thumbnailMaxSize == null)
                thumbnailMaxSize = thumbnail;
            else if (thumbnail.getInt("height") > thumbnailMaxSize.getInt("height"))
                thumbnailMaxSize = thumbnail;
        }
        if (thumbnailMaxSize != null)
            image = thumbnailMaxSize.getString("url");
        return image;
    }

    public static JSONArray getRelatedSongs(String videoId) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("enablePersistentPlaylistPanel", true);
        requestBody.put("isAudioOnly", true);
        requestBody.put("tunerSettingValue", "AUTOMIX_SETTING_NORMAL");
        requestBody.put("videoId", videoId);
        JSONObject watchEndpointMusicConfig = new JSONObject();
        watchEndpointMusicConfig.put("hasPersistentPlaylistPanel", true);
        watchEndpointMusicConfig.put("musicVideoType", "MUSIC_VIDEO_TYPE_ATV");
        JSONObject watchEndpointMusicSupportedConfigs = new JSONObject();
        watchEndpointMusicSupportedConfigs.put("watchEndpointMusicConfig", watchEndpointMusicConfig);
        requestBody.put("watchEndpointMusicSupportedConfigs", watchEndpointMusicSupportedConfigs);
        String playlistId = "RDAMVM" + videoId;
        requestBody.put("playlistId", playlistId);
        JSONObject response = RequestService.makeRequest(relatedSongsRequestUrl, requestBody);
        JSONArray results = new JSONObjectPath(response).getJSONArray(relatedSongsResults);
        return parseWatchPlaylist(results);
    }

    public static String getSourceUrl(String url) {
        if (url == null || url.equals(""))
            return null;
        String videoId = url.split("\\?v=")[1];
        YoutubeDownloader downloader = new YoutubeDownloader();
        RequestVideoInfo request = new RequestVideoInfo(videoId);
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        VideoInfo video = response.data();
        if (video == null) return null;
        return video.bestAudioFormat().url();
    }
}
