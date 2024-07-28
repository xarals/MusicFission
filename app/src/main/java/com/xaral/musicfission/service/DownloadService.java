package com.xaral.musicfission.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.ytmusicapi.YTMusicApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private static final int NOTIFICATION_ID = 405;
    private static final String CHANNEL_ID = "DownloadChannel";
    private static boolean isServiceRunning = false;

    public static List<MusicRepository.Track> downloadQueue = new ArrayList<>();
    public static final LinkedHashMap<MusicRepository.Track, Integer> queue = new LinkedHashMap<>();
    public static MusicRepository.Track currentTrack;
    public static Integer currentProgress;
    public static String stringProgress;
    public static Double size;
    public static Response<VideoInfo> response;
    public static Response<File> responseFile;

    private final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Download controls", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        startForeground(NOTIFICATION_ID, getNotification(0, "...", 0.0));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!downloadQueue.isEmpty()) {
                    MusicRepository.Track track = downloadQueue.remove(0);
                    currentTrack = track;
                    currentProgress = 0;
                    stringProgress = "0 / 0 MB";
                    String fileUrl = track.getUri();
                    String title = track.getTitle();
                    String videoId = fileUrl.split("\\?v=")[1];
                    YoutubeDownloader downloader = new YoutubeDownloader();
                    RequestVideoInfo request = new RequestVideoInfo(videoId).callback(new YoutubeCallback<VideoInfo>() {
                        @Override
                        public void onFinished(VideoInfo data) {
                            for (MusicRepository.Track track1 : FavoriteService.getDownloads()) {
                                if (track1.getUri().equals(track.getUri())) {
                                    queue.put(track, 0);
                                    refreshProgress(-1, track.getTitle(), 0.0);
                                    return;
                                }
                            }
                            startDownload(data.bestAudioFormat(), videoId, title, track);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            refreshProgress(-2, track.getTitle(), 0.0);
                            queue.put(track, -1);
                        }
                    }).async();
                    response = downloader.getVideoInfo(request);
                    try {
                        response.data();
                    } catch (Exception e) {
                        refreshProgress(-2, track.getTitle(), 0.0);
                        queue.put(track, -1);
                    }
                }
                currentTrack = null;
                currentProgress = null;
                stringProgress = null;
                isServiceRunning = false;
                stopForeground(false);
            }
        });
        thread.start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void addFileToQueue(MusicRepository.Track track) {
        for (MusicRepository.Track track1 : downloadQueue) {
            if (track1.getUri().equals(track.getUri()))
                return;
        }
        if (currentTrack != null && currentTrack.getUri().equals(track.getUri()))
            return;
        downloadQueue.add(track);
        if (!isServiceRunning) {
            startService();
        }
    }

    private static void startService() {
        Intent intent = new Intent(MainActivity.activity, DownloadService.class);
        MainActivity.activity.startService(intent);
        isServiceRunning = true;
    }

    private void startDownload(Format format, String name, String title, MusicRepository.Track track) {
        YoutubeDownloader downloader = new YoutubeDownloader();
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .callback(new YoutubeProgressCallback<File>() {
                    @Override
                    public void onDownloading(int progress) {
                        size = Math.round(format.contentLength() / 1024.0 / 1024.0 * 10) / 10.0;
                        currentProgress = progress;
                        refreshProgress(progress, title, size);
                        double sizeDownload = size * progress / 100;
                        double sizeDownloadRound = Math.round(sizeDownload * 10) / 10.0;
                        stringProgress = sizeDownloadRound + " / " + size + " MB";
                    }

                    @Override
                    public void onFinished(File data) {
                        queue.put(track, 0);
                        refreshProgress(-1, track.getTitle(), 0.0);
                        FavoriteService.saveDownloads(track);
                        if (!FavoriteService.getFolder().equals("")) {
                            try {
                                File firstFile = new File(getDataDir() + "/downloads/" + data.getName());
                                String format = "." + data.getName().split("\\.")[data.getName().split("\\.").length - 1];
                                File secondFile = new File(FavoriteService.getFolder() + "/" + track.getTitle() + " - " + track.getArtist() + format);
                                copyFile(firstFile, secondFile);
                            } catch (Exception ignored) {}
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        queue.put(track, -1);
                        refreshProgress(-2, track.getTitle(), 0.0);
                    }
                })
                .saveTo(new File(getDataDir() + "/downloads"))
                .renameTo(name)
                .overwriteIfExists(true)
                .async();
        responseFile = downloader.downloadVideoFile(request);
        responseFile.data();
    }

    private void refreshProgress(int progress, String title, Double size) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(DownloadService.this).notify(NOTIFICATION_ID, getNotification(progress, title, size));
        //stopForeground(false);
    }

    public static void cancelDownload(MusicRepository.Track track) {
        if (currentTrack != null && currentTrack.equals(track)) {
            if (response != null)
                response.cancel();
            if (responseFile != null)
                responseFile.cancel();
        }
    }

    public static void restartDownload(MusicRepository.Track track) {
        if (queue.containsKey(track)) {
            queue.remove(track);
            addFileToQueue(track);
        }
    }

    private Notification getNotification(int progress, String title, Double size) {
        builder
                .setContentTitle(getString(R.string.download_process))
                //.setLargeIcon(description.getIconBitmap())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSilent(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        if (progress == -1) {
            builder.setContentText(title);
            builder.setContentTitle(getString(R.string.downloading_is_complete));
            builder.setOngoing(false);
            //builder.setProgress(0, 0, false);
            builder.setSmallIcon(R.drawable.ic_file_download_done_white_24dp);
        }
        else if (progress == -2) {
            builder.setContentText(title);
            //builder.setProgress(0, 0, false);
            builder.setContentTitle(getString(R.string.download_error));
            builder.setOngoing(false);
            builder.setSmallIcon(R.drawable.ic_file_download_off_white_24dp);
        }
        else {
            builder.setSubText(title);
            builder.setOngoing(true);
            double sizeDownload = size * progress / 100;
            double sizeDownloadRound = Math.round(sizeDownload * 10) / 10.0;
            builder.setContentText(sizeDownloadRound + " / " + size + " MB");
            builder.setProgress(100, progress, false);
            builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
        }
        builder.setShowWhen(false);
        builder.setPriority(NotificationManager.IMPORTANCE_LOW);
        builder.setOnlyAlertOnce(true);
        return builder.build();
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        FileOutputStream fos = new FileOutputStream(destFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        fis.close();
        fos.close();
    }
}
