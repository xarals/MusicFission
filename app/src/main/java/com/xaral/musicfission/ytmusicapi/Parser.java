package com.xaral.musicfission.ytmusicapi;

import android.util.Log;

import com.xaral.musicfission.ytmusicapi.JSONObjectPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static final String artistRequestUrl = "https://music.youtube.com/youtubei/v1/browse?alt=json";
    public static final String songRequestUrl = "https://music.youtube.com/youtubei/v1/player?alt=json";
    public static final String relatedSongsRequestUrl = "https://music.youtube.com/youtubei/v1/next?alt=json";
    public static final String searchRequestUrl = "https://music.youtube.com/youtubei/v1/search?alt=json";
    public static final Object[] namePath = new Object[]{"header", "musicImmersiveHeaderRenderer", "title", "runs", 0, "text"};
    public static final Object[] subscribersPath = new Object[]{"header", "musicImmersiveHeaderRenderer", "subscriptionButton", "subscribeButtonRenderer", "subscriberCountText", "runs", 0, "text"};
    public static final Object[] sectionListContents = new Object[]{"contents", "singleColumnBrowseResultsRenderer", "tabs", 0, "tabRenderer", "content", "sectionListRenderer", "contents"};
    public static final Object[] viewsPath = new Object[]{"musicDescriptionShelfRenderer", "subheader", "runs", 0, "text"};
    public static final Object[] menuItems = new Object[]{"menu", "menuRenderer", "items"};
    public static final Object[] menuService = new Object[]{"menuServiceItemRenderer", "serviceEndpoint"};
    public static final Object[] videoIdPath = new Object[]{"playlistEditEndpoint", "actions", 0, "removedVideoId"};
    public static final Object[] playButton = new Object[]{"overlay", "musicItemThumbnailOverlayRenderer", "content", "musicPlayButtonRenderer"};
    public static final Object[] playButtonVideoId = new Object[]{"playNavigationEndpoint", "watchEndpoint", "videoId"};
    public static final Object[] titlePath = new Object[]{"flexColumns", 0, "musicResponsiveListItemFlexColumnRenderer", "text", "runs", 0, "text"};
    public static final Object[] artistsPath = new Object[]{"flexColumns", 1, "musicResponsiveListItemFlexColumnRenderer", "text", "runs"};
    public static final Object[] musicShelfContent = new Object[]{"musicShelfRenderer", "contents"};
    public static final Object[] navigationBrowseId = new Object[]{"navigationEndpoint", "browseEndpoint", "browseId"};
    public static final Object[] thumbnail = new Object[]{"thumbnail", "musicThumbnailRenderer", "thumbnail", "thumbnails"};
    public static final Object[] durationPath1 = new Object[]{"fixedColumns", 0, "musicResponsiveListItemFixedColumnRenderer", "text", "simpleText"};
    public static final Object[] durationPath2 = new Object[]{"fixedColumns", 0, "musicResponsiveListItemFixedColumnRenderer", "text", "runs", 0, "text"};
    public static final Object[] carouselTitle = new Object[]{"musicCarouselShelfRenderer", "header", "musicCarouselShelfBasicHeaderRenderer", "title", "runs", 0, "text"};
    public static final Object[] subtitleRuns = new Object[]{"musicTwoRowItemRenderer", "subtitle", "runs"};
    public static final Object[] navigationVideoId = new Object[]{"musicTwoRowItemRenderer", "navigationEndpoint", "watchEndpoint", "videoId"};
    public static final Object[] queueVideoId = new Object[]{"menuServiceItemRenderer", "serviceEndpoint", "queueAddEndpoint", "queueTarget", "videoId"};
    public static final Object[] menuItemsVideo = new Object[]{"menuServiceItemRenderer", "menu", "menuRenderer", "items"};
    public static final Object[] titleVideoPath = new Object[]{"musicTwoRowItemRenderer", "title", "runs", 0, "text"};
    public static final Object[] thumbnailVideo = new Object[]{"musicTwoRowItemRenderer", "thumbnailRenderer", "musicThumbnailRenderer", "thumbnail", "thumbnails"};
    public static final Object[] titleAlbumPath = new Object[]{"musicTwoRowItemRenderer", "title", "runs", 0, "text"};
    public static final Object[] yearAlbumPath = new Object[]{"musicTwoRowItemRenderer", "subtitle", "runs", 2, "text"};
    public static final Object[] browseIdAlbumPath = new Object[]{"musicTwoRowItemRenderer", "title", "runs", 0, "navigationEndpoint", "browseEndpoint", "browseId"};
    public static final Object[] thumbnailsAlbumPath = new Object[]{"musicTwoRowItemRenderer", "thumbnailRenderer", "musicThumbnailRenderer", "thumbnail", "thumbnails"};
    public static final Object[] artistsAlbumPath = new Object[]{"musicTwoRowItemRenderer", "subtitle", "runs"};
    public static final Object[] relatedSubscribersPath = new Object[]{"musicTwoRowItemRenderer", "subtitle", "runs", 0, "text"};
    public static final Object[] headerDetail = new Object[]{"contents", "twoColumnBrowseResultsRenderer", "secondaryContents", "sectionListRenderer", "contents", 1, "musicCarouselShelfRenderer", "contents", 0, "musicTwoRowItemRenderer"};
    public static final Object[] titleAlbumsPath = new Object[]{"title", "runs", 0, "text"};
    public static final Object[] thumbnailCropped = new Object[]{"thumbnail", "croppedSquareThumbnailRenderer", "thumbnail", "thumbnails"};
    public static final Object[] albumResultPath = new Object[]{"contents", "twoColumnBrowseResultsRenderer", "secondaryContents", "sectionListRenderer", "contents", 0, "musicShelfRenderer", "contents"};
    public static final Object[] playlistResultPath = new Object[]{"contents", "twoColumnBrowseResultsRenderer", "secondaryContents", "sectionListRenderer", "contents", 0, "musicPlaylistShelfRenderer"};
    public static final Object[] playlistHeaderDetail = new Object[]{"contents", "twoColumnBrowseResultsRenderer", "tabs", 0, "tabRenderer", "content", "sectionListRenderer", "contents", 0, "musicResponsiveHeaderRenderer"};
    public static final Object[] subtitleRunsPlaylist = new Object[]{"subtitle", "runs"};
    public static final Object[] authorPlaylist = new Object[]{"straplineTextOne", "runs", 0, "text"};
    public static final Object[] subtitle1Playlist = new Object[]{"subtitle", "runs", 0, "text"};
    public static final Object[] subtitle2Playlist = new Object[]{"subtitle", "runs", 2, "text"};
    public static final Object[] subtitle3Playlist = new Object[]{"subtitle", "runs", 4, "text"};
    public static final Object[] thumbnailsSongPath = new Object[]{"videoDetails", "thumbnail", "thumbnails"};
    public static final Object[] searchResultsPath = new Object[]{"contents", "tabbedSearchResultsRenderer", "tabs", 0, "tabRenderer", "content"};
    public static final Object[] sectionList = new Object[]{"sectionListRenderer", "contents"};
    public static final Object[] cardShelfTitle = new Object[]{"header", "musicCardShelfHeaderBasicRenderer", "title", "runs", 0, "text"};
    public static final Object[] watchVideoId = new Object[]{"onTap", "watchEndpoint", "videoId"};
    public static final Object[] albumBrowseId = new Object[]{"title", "runs", 0, "navigationEndpoint", "browseEndpoint", "browseId"};
    public static final Object[] menuPlaylistId = new Object[]{"menu", "menuRenderer", "items", 0, "menuNavigationItemRenderer", "navigationEndpoint", "watchPlaylistEndpoint", "playlistId"};
    public static final Object[] musicCardShelfRenderer = new Object[]{"musicCardShelfRenderer", "contents"};
    public static final Object[] videoTypePath = new Object[]{"overlay", "musicItemThumbnailOverlayRenderer", "content", "musicPlayButtonRenderer", "playNavigationEndpoint", "watchEndpoint", "watchEndpointMusicSupportedConfigs", "watchEndpointMusicConfig", "musicVideoType"};
    public static final Object[] relatedSongsResults = new Object[]{"contents", "singleColumnMusicWatchNextResultsRenderer", "tabbedRenderer", "watchNextTabbedResultsRenderer", "tabs", 0, "tabRenderer", "content", "musicQueueRenderer", "content", "playlistPanelRenderer", "contents"};
    public static final Object[] thumbnailsRelatedSongs = new Object[]{"thumbnail", "thumbnails"};
    public static final Object[] runsRelatedSongs = new Object[]{"longBylineText", "runs"};
    public static JSONArray parseAlbums(JSONArray results) throws JSONException {
        JSONArray albums = new JSONArray();
        JSONArray data = new JSONArray();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String category = new JSONObjectPath(result).getString(carouselTitle);
            if (category == null) continue;
            if (!category.equalsIgnoreCase("albums")) continue;
            data.put(result.getJSONObject(carouselTitle[0].toString()));
        }
        if (data.length() == 0) return data;
        JSONArray albumsResults = new JSONObjectPath(data.getJSONObject(0)).getJSONArray(new Object[]{sectionListContents[0]});
        for (int i = 0; i < albumsResults.length(); i++) {
            String title = new JSONObjectPath(albumsResults.getJSONObject(i)).getString(titleAlbumPath);
            String year = new JSONObjectPath(albumsResults.getJSONObject(i)).getString(yearAlbumPath);
            if (year == null)
                year = "";
            String browseId = new JSONObjectPath(albumsResults.getJSONObject(i)).getString(browseIdAlbumPath);
            JSONArray thumbnails = new JSONObjectPath(albumsResults.getJSONObject(i)).getJSONArray(thumbnailsAlbumPath);
            String image = "";
            JSONObject thumbnailMaxSize = null;
            for (int j = 0; j < thumbnails.length(); j++) {
                JSONObject thumbnail = thumbnails.getJSONObject(j);
                if (thumbnailMaxSize == null)
                    thumbnailMaxSize = thumbnail;
                else if (thumbnail.getInt("height") > thumbnailMaxSize.getInt("height"))
                    thumbnailMaxSize = thumbnail;
            }
            if (thumbnailMaxSize != null)
                image = thumbnailMaxSize.getString("url");

            String artistName = "";
            String artistId = "";
            JSONArray path = new JSONObjectPath(albumsResults.getJSONObject(i)).getJSONArray(artistsAlbumPath);
            for (int j = 0; j < path.length(); j++) {
                JSONObject artist = path.getJSONObject(j);
                if (!artist.has(navigationBrowseId[0].toString())) continue;
                artistId += new JSONObjectPath(artist).getString(navigationBrowseId) + "&";
                artistName += artist.getString("text") + "&";
            }
            if (artistName.length() > 0) {
                artistName = artistName.substring(0, artistName.length() - 1);
                artistId = artistId.substring(0, artistId.length() - 1);
            }

            JSONObject album = new JSONObject();
            album.put("title", title);
            album.put("artist", artistName);
            album.put("artistId", artistId);
            album.put("browseId", browseId);
            album.put("year", year);
            album.put("image", image);
            albums.put(album);
        }
        return albums;
    }

    public static JSONArray parsePlaylistItems(JSONArray results) throws JSONException {
        JSONArray songs = new JSONArray();
        if (results == null)
            return songs;
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            if (!result.has("musicResponsiveListItemRenderer")) continue;

            String title = null;
            JSONArray artists = new JSONArray();
            String videoId = null;
            JSONArray thumbnails = new JSONArray();

            JSONObject data = result.getJSONObject("musicResponsiveListItemRenderer");

            title = new JSONObjectPath(data).getString(titlePath);
            if (title == null)
                continue;
            else if (title.equals("Song deleted"))
                continue;

            artists = parseSongArtists(new JSONObjectPath(data).getJSONArray(artistsPath));

            thumbnails = new JSONObjectPath(data).getJSONArray(thumbnail);
            if (thumbnails == null)
                thumbnails = new JSONArray();

            if (data.has("menu")) {
                JSONArray path = new JSONObjectPath(data).getJSONArray(menuItems);
                for (int j = 0; j < path.length(); j++) {
                    JSONObject item = path.getJSONObject(j);
                    if (item.has("menuServiceItemRenderer")){
                        JSONObject service = (new JSONObjectPath(item)).getJSONObject(menuService);
                        if (service.has("playlistEditEndpoint")) {
                            videoId = (new JSONObjectPath(service)).getString(videoIdPath);
                        }
                    }
                }
            }

            JSONObject videoIdObject = new JSONObjectPath(data).getJSONObject(playButton);
            if (videoIdObject != null) {
                if (videoIdObject.has("playNavigationEndpoint")) {
                    videoId = new JSONObjectPath(videoIdObject).getString(playButtonVideoId);
                }
            }

            if (videoId == null)
                continue;

            String duration = null;
            int durationMillSec = 0;

            duration = new JSONObjectPath(data).getString(durationPath1);
            if (duration == null)
                duration = new JSONObjectPath(data).getString(durationPath2);

            if (duration != null) {
                String[] time = duration.split(":");
                for (int l = time.length - 1; l >= 0; l--) {
                    durationMillSec += Integer.parseInt(time[l]) * Math.pow(60, time.length - l - 1);
                }
                durationMillSec *= 1000;
            }

            String finalArtistName = "";
            String finalArtistId = "";
            for (int j = 0; j < artists.length(); j++) {
                JSONObject artist = artists.getJSONObject(j);
                if (artist.has("name"))
                    finalArtistName += artist.getString("name") + "&";
                else
                    continue;
                if (artist.has("id"))
                    finalArtistId += artist.getString("id");
                finalArtistId += "&";
            }
            if (finalArtistId.length() > 0) {
                finalArtistName = finalArtistName.substring(0, finalArtistName.length() - 1);
                finalArtistId = finalArtistId.substring(0, finalArtistId.length() - 1);
            }
            String image = "";
            JSONObject thumbnailMaxSize = null;
            for (int j = 0; j < thumbnails.length(); j++) {
                JSONObject thumbnail = thumbnails.getJSONObject(j);
                if (thumbnailMaxSize == null)
                    thumbnailMaxSize = thumbnail;
                else if (thumbnail.getInt("height") > thumbnailMaxSize.getInt("height"))
                    thumbnailMaxSize = thumbnail;
            }
            if (thumbnailMaxSize != null)
                image = thumbnailMaxSize.getString("url");
            JSONObject song = new JSONObject();
            song.put("title", title);
            song.put("artist", finalArtistName);
            song.put("artistId", finalArtistId);
            song.put("url", "https://www.youtube.com/watch?v=" + videoId);
            song.put("image", image);
            song.put("duration_seconds", Integer.toString(durationMillSec));
            songs.put(song);
        }
        return songs;
    }

    public static JSONArray parseVideos(JSONArray results) throws JSONException {
        JSONArray videos = new JSONArray();
        JSONArray data = new JSONArray();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String category = new JSONObjectPath(result).getString(carouselTitle);
            if (category == null) continue;
            if (!category.equalsIgnoreCase("videos")) continue;
            data.put(result.getJSONObject(carouselTitle[0].toString()));
        }
        if (data.length() == 0) return data;
        JSONArray videosResult = new JSONObjectPath(data.getJSONObject(0)).getJSONArray(new Object[]{sectionListContents[0]});
        for (int i = 0; i < videosResult.length(); i++) {
            JSONArray resultVideo = new JSONObjectPath(videosResult.getJSONObject(i)).getJSONArray(subtitleRuns);
            Integer index = null;
            for (int j = 0; j < resultVideo.length(); j++) {
                JSONObject resultVideos = resultVideo.getJSONObject(j);
                if (!resultVideos.equals(new JSONObject("{\"text\": \" • \"}"))) continue;
                index = j;
            }
            if (index == null)
                index = resultVideo.length();
            String videoId = new JSONObjectPath(videosResult.getJSONObject(i)).getString(navigationVideoId);
            if (videoId == null) {
                JSONArray path = new JSONObjectPath(videosResult.getJSONObject(i)).getJSONArray(menuItems);
                for (int j = 0; j < path.length(); j++) {
                    if (new JSONObjectPath(path.getJSONObject(j)).get(queueVideoId) == null) continue;
                    videoId = new JSONObjectPath(path.getJSONObject(j)).getString(menuItems);
                }
            }
            if (videoId == null) continue;

            String title = new JSONObjectPath(videosResult.getJSONObject(i)).getString(titleVideoPath);

            JSONArray thumbnails = new JSONObjectPath(videosResult.getJSONObject(i)).getJSONArray(thumbnailVideo);

            String image = "";
            JSONObject thumbnailMaxSize = null;
            for (int j = 0; j < thumbnails.length(); j++) {
                JSONObject thumbnail = thumbnails.getJSONObject(j);
                if (thumbnailMaxSize == null)
                    thumbnailMaxSize = thumbnail;
                else if (thumbnail.getInt("height") > thumbnailMaxSize.getInt("height"))
                    thumbnailMaxSize = thumbnail;
            }
            if (thumbnailMaxSize != null)
                image = thumbnailMaxSize.getString("url");
            JSONArray artistsData = new JSONArray();
            for (int j = 0; j < 6 && j < resultVideo.length(); j++) {
                artistsData.put(resultVideo.getJSONObject(j));
            }
            JSONArray artistsArray = parseSongArtists(artistsData);
            String artist = artistsArray.getJSONObject(0).getString("name");
            String artistId;
            try {
                artistId = artistsArray.getJSONObject(0).getString("id");
            } catch (Exception ex) {
                artistId = "";
            }

            JSONObject video = new JSONObject();
            video.put("title", title);
            video.put("artist", artist);
            video.put("artistId", artistId);
            video.put("url", "https://www.youtube.com/watch?v=" + videoId);
            video.put("image", image);
            videos.put(video);
        }
        return videos;
    }

    public static JSONArray parseRelatedArtist(JSONArray results) throws JSONException {
        JSONArray related = new JSONArray();
        JSONArray data = new JSONArray();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String category = new JSONObjectPath(result).getString(carouselTitle);
            if (category == null) continue;
            if (!category.equalsIgnoreCase("Fans might also like")) continue;
            data.put(result.getJSONObject(carouselTitle[0].toString()));
        }
        if (data.length() == 0) return data;
        JSONArray relatedResult = new JSONObjectPath(data.getJSONObject(0)).getJSONArray(new Object[]{sectionListContents[0]});
        for (int i = 0; i < relatedResult.length(); i++) {
            JSONObject result = relatedResult.getJSONObject(i);
            String name = new JSONObjectPath(result).getString(titleVideoPath);
            String artistId = new JSONObjectPath(result).getString(browseIdAlbumPath);

            JSONArray thumbnails = new JSONObjectPath(result).getJSONArray(thumbnailsAlbumPath);
            String image = "";
            JSONObject thumbnailMaxSize = null;
            for (int j = 0; j < thumbnails.length(); j++) {
                JSONObject thumbnail = thumbnails.getJSONObject(j);
                if (thumbnailMaxSize == null)
                    thumbnailMaxSize = thumbnail;
                else if (thumbnail.getInt("height") > thumbnailMaxSize.getInt("height"))
                    thumbnailMaxSize = thumbnail;
            }
            if (thumbnailMaxSize != null)
                image = thumbnailMaxSize.getString("url");

            String subscribers = new JSONObjectPath(result).getString(relatedSubscribersPath);
            if (subscribers != null)
                subscribers = subscribers.split(" ")[0];
            else
                subscribers = "";

            JSONObject relate = new JSONObject();
            relate.put("name", name);
            relate.put("artistId", artistId);
            relate.put("image", image);
            relate.put("subscribers", subscribers);
            related.put(relate);
        }
        return related;
    }

    public static JSONArray parseSongArtists(JSONArray runs) throws JSONException {
        JSONArray artists = new JSONArray();
        if (runs == null)
            return artists;
        for (int i = 0; i < runs.length(); i += 2) {
            JSONObject artist = new JSONObject();
            artist.put("name", runs.getJSONObject(i).getString("text"));
            artist.put("id", new JSONObjectPath(runs.getJSONObject(i)).getString(navigationBrowseId));
            artists.put(artist);
        }
        return artists;
    }

    public static JSONObject parseAlbumHeader(JSONObject response) throws JSONException {
        JSONObject header = new JSONObjectPath(response).getJSONObject(playlistHeaderDetail);
        String title = new JSONObjectPath(header).getString(titleAlbumsPath);

        JSONObject album = new JSONObject();
        album.put("title", title);
        JSONArray runs = header.getJSONObject("subtitle").getJSONArray("runs");
        JSONArray finalRuns = new JSONArray();
        for (int i = 2; i < runs.length(); i++)
            finalRuns.put(runs.get(i));
        JSONObject parsed = parseSongRuns(finalRuns);
        album.put("year", parsed.getString("year"));
        parsed = parseSongRuns(header.getJSONObject("straplineTextOne").getJSONArray("runs"));
        String artistName = "";
        String artistId = "";
        JSONArray path = parsed.getJSONArray("artists");
        for (int i = 0; i < path.length(); i++) {
            JSONObject artist = (JSONObject) path.getJSONObject(i);
            artistName += artist.getString("name") + "&";
            if (artist.has("id"))
                artistId += artist.getString("id");
            artistId += "&";
        }
        if (artistName.length() > 0) {
            artistName = artistName.substring(0, artistName.length() - 1);
            artistId = artistId.substring(0, artistId.length() - 1);
        }
        album.put("artist", artistName);
        album.put("artistId", artistId);
        return album;
    }

    public static JSONObject parseSongRuns(JSONArray runs) throws JSONException {
        JSONArray artists = new JSONArray();
        String year = "";
        for (int i = 0; i < runs.length(); i++) {
            if (i % 2 == 1) continue;
            String text = runs.getJSONObject(i).getString("text");
            if (runs.getJSONObject(i).has(navigationBrowseId[0].toString())) {
                JSONObject item = new JSONObject();
                item.put("name", text);
                item.put("id", new JSONObjectPath(runs.getJSONObject(i)).getString(navigationBrowseId));
                if (!item.has("id"))
                    artists.put(item);
                else if (!item.getString("id").startsWith("MPRE"))
                    artists.put(item);
                else if (!item.getString("id").startsWith("{"))
                    artists.put(item);
                else if (!new JSONObject(item.getString("id")).has("release_detail"))
                    artists.put(item);
            } else {
                if (Pattern.compile("^\\d([^ ])* [^ ]*$").matcher(text).find() || Pattern.compile("^(\\d+:)*\\d+:\\d+$").matcher(text).find()) {}
                else if (Pattern.compile("^\\d{4}$").matcher(text).find()) {
                    year = text;
                } else {
                    JSONObject artist = new JSONObject();
                    artist.put("name", text);
                    artists.put(artist);
                }
            }
        }
        JSONObject parsed = new JSONObject();
        parsed.put("artists", artists);
        parsed.put("year", year);
        return parsed;
    }

    public static JSONObject parseTopResult(JSONObject data) throws JSONException {
        String resultType = getSearchResultType(new JSONObjectPath(data).getString(subtitle1Playlist));
        if (resultType == null)
            return null;
        JSONObject searchResult = new JSONObject();
        searchResult.put("category", new JSONObjectPath(data).getString(cardShelfTitle));
        searchResult.put("resultType", resultType);

        if (resultType.equalsIgnoreCase("artist")) {
            String subscribers = new JSONObjectPath(data).getString(subtitle2Playlist);
            if (subscribers != null)
                searchResult.put("subscribers", subscribers.split(" ")[0]);
            JSONObject artistInfo = parseSongRuns(data.getJSONObject("title").getJSONArray("runs"));
            JSONArray keys = artistInfo.names();
            for (int i = 0; i < keys.length(); i++)
                searchResult.put(keys.getString(i), artistInfo.get(keys.getString(i)));
        }

        if (resultType.equalsIgnoreCase("song") || resultType.equalsIgnoreCase("video")) {
            String videoId = new JSONObjectPath(data).getString(watchVideoId);
            if (videoId != null)
                searchResult.put("url", "https://www.youtube.com/watch?v=" + videoId);
            else
                return null;
        }

        if (resultType.equalsIgnoreCase("song") || resultType.equalsIgnoreCase("video") || resultType.equalsIgnoreCase("album")) {
            searchResult.put("title", new JSONObjectPath(data).getString(titleAlbumsPath));
            JSONArray runs = data.getJSONObject("subtitle").getJSONArray("runs");
            JSONObject songInfo = parseSongRuns(runs);
            JSONArray keys = songInfo.names();
            for (int i = 0; i < keys.length(); i++)
                searchResult.put(keys.getString(i), songInfo.get(keys.getString(i)));
            String finalArtistName = "";
            String finalArtistId = "";
            JSONArray path = searchResult.getJSONArray("artists");
            for (int i = 0; i < path.length(); i++) {
                JSONObject artist = path.getJSONObject(i);
                if (!artist.has("id")) continue;
                finalArtistName += artist.getString("name") + "&";
                finalArtistId += artist.getString("id") + "&";
            }
            if (finalArtistId.length() > 0) {
                finalArtistName = finalArtistName.substring(0, finalArtistName.length() - 1);
                finalArtistId = finalArtistId.substring(0, finalArtistId.length() - 1);
            }
            searchResult.put("artist", finalArtistName);
            searchResult.put("artistId", finalArtistId);
            searchResult.remove("artists");
            if (!resultType.equalsIgnoreCase("album"))
                searchResult.remove("year");
        }

        if (resultType.equalsIgnoreCase("album"))
            searchResult.put("browseId", new JSONObjectPath(data).getString(albumBrowseId));

        if (resultType.equalsIgnoreCase("playlist")) {
            searchResult.put("browseId", new JSONObjectPath(data).getString(menuPlaylistId));
            searchResult.put("title", new JSONObjectPath(data).getString(titleAlbumsPath));
            JSONArray authors = new JSONObjectPath(data).getJSONArray(subtitleRunsPlaylist);
            searchResult.put("author", authors.getJSONObject(authors.length() - 1).getString("text"));
        }

        JSONArray thumbnails = new JSONObjectPath(data).getJSONArray(thumbnail);
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
        searchResult.put("image", image);

        if (resultType.equalsIgnoreCase("artist")) {
            searchResult.put("name", searchResult.getJSONArray("artists").getJSONObject(searchResult.getJSONArray("artists").length() - 1).getString("name"));
            searchResult.put("artistId", searchResult.getJSONArray("artists").getJSONObject(searchResult.getJSONArray("artists").length() - 1).getString("id"));
            searchResult.remove("artists");
            searchResult.remove("year");
        }

        return searchResult;
    }

    public static String getSearchResultType(String resultTypeLocal) {
        if (resultTypeLocal == null)
            return null;
        String[] resultTypes = new String[]{"artist", "album", "playlist", "song", "video"};
        for (String type : resultTypes) {
            if (resultTypeLocal.equalsIgnoreCase(type))
                return type;
        }
        return null;
    }

    public static JSONArray parseSearchResult(JSONArray results) throws JSONException {
        JSONArray searchResults = new JSONArray();
        if (results == null) return searchResults;
        for (int i = 0; i < results.length(); i++) {
            JSONObject searchResult = new JSONObject();
            JSONObject result = results.getJSONObject(i);
            JSONObject data;
            try {
                data = result.getJSONObject("musicResponsiveListItemRenderer");
            } catch (Exception e) {
                continue;
            }
            String videoType = new JSONObjectPath(data).getString(videoTypePath);
            String resultType = null;
            if (videoType != null && videoType.equals("MUSIC_VIDEO_TYPE_ATV"))
                resultType = "song";
            else if (videoType != null)
                resultType = "video";
            if (resultType == null) {
                resultType = new JSONObjectPath(data).getString(new Object[]{"flexColumns", 1, "musicResponsiveListItemFlexColumnRenderer", "text", "runs", 0, "text"});
            }
            if (resultType == null)
                continue;
            searchResult.put("resultType", resultType);

            if (!resultType.equalsIgnoreCase("artist") && !resultType.equalsIgnoreCase("album") && !resultType.equalsIgnoreCase("playlist") && !resultType.equalsIgnoreCase("song") && !resultType.equalsIgnoreCase("video"))
                continue;

            if (!resultType.equalsIgnoreCase("artist"))
                searchResult.put("title", new JSONObjectPath(data).getString(new Object[]{"flexColumns", 0, "musicResponsiveListItemFlexColumnRenderer", "text", "runs", 0, "text"}));

            if (resultType.equalsIgnoreCase("artist"))
                searchResult.put("name", new JSONObjectPath(data).getString(new Object[]{"flexColumns", 0, "musicResponsiveListItemFlexColumnRenderer", "text", "runs", 0, "text"}));
            else if (resultType.equalsIgnoreCase("playlist")) {
                JSONArray flexItem = new JSONObjectPath(data).getJSONArray(new Object[]{"flexColumns", 1, "musicResponsiveListItemFlexColumnRenderer", "text", "runs"});
                if (flexItem.length() == 5)
                    searchResult.put("author", new JSONObjectPath(data).getJSONArray(new Object[]{"flexColumns", 1, "musicResponsiveListItemFlexColumnRenderer", "text", "runs", 2, "text"}));
            }

            if (resultType.equalsIgnoreCase("song") || resultType.equalsIgnoreCase("video")) {
                String videoId = new JSONObjectPath(data).getString(new Object[]{"overlay", "musicItemThumbnailOverlayRenderer", "content", "musicPlayButtonRenderer", "playNavigationEndpoint", "watchEndpoint", "videoId"});
                if (videoId == null) continue;
                searchResult.put("url", "https://www.youtube.com/watch?v=" + videoId);
                JSONArray runs = new JSONObjectPath(data).getJSONArray(new Object[]{"flexColumns", 1, "musicResponsiveListItemFlexColumnRenderer", "text", "runs"});
                JSONObject songInfo = parseSongRuns(runs);
                JSONArray keys = songInfo.names();
                for (int j = 0; j < keys.length(); j++)
                    searchResult.put(keys.getString(j), songInfo.get(keys.getString(j)));
                String finalArtistName = "";
                String finalArtistId = "";
                if (resultType.equalsIgnoreCase("song")) {
                    for (int p = 0; p < searchResult.getJSONArray("artists").length() - 1; p++) {
                        JSONObject author = searchResult.getJSONArray("artists").getJSONObject(p);
                        if (!author.has("id")) continue;
                        finalArtistName += author.getString("name") + "&";
                        finalArtistId += author.getString("id") + "&";
                    }
                } else {
                    for (int p = 0; p < searchResult.getJSONArray("artists").length(); p++) {
                        JSONObject author = searchResult.getJSONArray("artists").getJSONObject(p);
                        if (!author.has("id")) continue;
                        finalArtistName += author.getString("name") + "&";
                        finalArtistId += author.getString("id") + "&";
                    }
                }
                if (finalArtistName.length() > 0) {
                    finalArtistName = finalArtistName.substring(0, finalArtistName.length() - 1);
                    finalArtistId = finalArtistId.substring(0, finalArtistId.length() - 1);
                }
                searchResult.remove("artists");
                searchResult.remove("year");
                searchResult.put("artist", finalArtistName);
                searchResult.put("artistId", finalArtistId);
            }

            if (resultType.equalsIgnoreCase("album") || resultType.equalsIgnoreCase("playlist")) {
                searchResult.put("browseId", new JSONObjectPath(data).getString(navigationBrowseId));
            }
            else if (resultType.equalsIgnoreCase("artist")) {
                searchResult.put("artistId", new JSONObjectPath(data).getString(navigationBrowseId));
            }

            JSONArray thumbnails = new JSONObjectPath(data).getJSONArray(thumbnail);
            String image = "";
            JSONObject thumbnailMaxSize = null;
            for (int j = 0; j < thumbnails.length(); j++) {
                JSONObject thumbnail = thumbnails.getJSONObject(j);
                if (thumbnailMaxSize == null)
                    thumbnailMaxSize = thumbnail;
                else if (thumbnail.getInt("height") > thumbnailMaxSize.getInt("height"))
                    thumbnailMaxSize = thumbnail;
            }
            if (thumbnailMaxSize != null)
                image = thumbnailMaxSize.getString("url");
            searchResult.put("image", image);

            searchResults.put(searchResult);
        }
        return searchResults;
    }

    public static JSONArray parseWatchPlaylist(JSONArray results) throws JSONException {
        JSONArray tracks = new JSONArray();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String PPVWR = "playlistPanelVideoWrapperRenderer";
            String PPVR = "playlistPanelVideoRenderer";
            if (result.has(PPVWR))
                result = result.getJSONObject(PPVWR).getJSONObject("primaryRenderer");
            if (!result.has(PPVR))
                continue;
            JSONObject data = result.getJSONObject(PPVR);
            if (data.has("unplayableText")) continue;
            JSONObject track = parseWatchTrack(data);
            tracks.put(track);
        }
        return tracks;
    }

    public static JSONObject parseWatchTrack(JSONObject data) throws JSONException {
        String url = "https://www.youtube.com/watch?v=" + data.getString("videoId");

        String title = new JSONObjectPath(data).getString(titleAlbumsPath);

        JSONArray thumbnails = new JSONObjectPath(data).getJSONArray(thumbnailsRelatedSongs);
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

        JSONObject songInfo = parseSongRuns(new JSONObjectPath(data).getJSONArray(runsRelatedSongs));
        if (!songInfo.has("artists")) return new JSONObject();
        String finalArtistName = "";
        String finalArtistId = "";
        if (songInfo.getJSONArray("artists").length() > 1) {
            for (int i = 0; i < songInfo.getJSONArray("artists").length() - 1; i++) {
                JSONObject artist = songInfo.getJSONArray("artists").getJSONObject(i);
                if (!artist.has("id")) continue;
                finalArtistName += artist.getString("name") + "&";
                finalArtistId += artist.getString("id") + "&";
            }
            if (finalArtistName.length() > 0) {
                finalArtistName = finalArtistName.substring(0, finalArtistName.length() - 1);
                finalArtistId = finalArtistId.substring(0, finalArtistId.length() - 1);
            }
        } else if (songInfo.getJSONArray("artists").length() > 0) {
            finalArtistName = songInfo.getJSONArray("artists").getJSONObject(0).getString("name");
            if (songInfo.getJSONArray("artists").getJSONObject(0).has("id"))
                finalArtistId = songInfo.getJSONArray("artists").getJSONObject(0).getString("id");
        }
        JSONObject track = new JSONObject();
        track.put("title", title);
        track.put("artist", finalArtistName);
        track.put("artistId", finalArtistId);
        track.put("url", url);
        track.put("image", image);
        return track;
    }
}
