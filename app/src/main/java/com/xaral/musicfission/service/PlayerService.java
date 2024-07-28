package com.xaral.musicfission.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.ExtractorsFactory;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.ytmusicapi.YTMusicApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final public class PlayerService extends Service {

    private final int NOTIFICATION_ID = 404;
    private final String NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel";

    private static final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    | PlaybackStateCompat.ACTION_SEEK_TO
    );

    private static MediaSessionCompat mediaSession;

    private static AudioManager audioManager;
    private static AudioFocusRequest audioFocusRequest;
    private boolean audioFocusRequested = false;

    public static ExoPlayer exoPlayer;
    public static ExtractorsFactory extractorsFactory;

    public static final MusicRepository musicRepository = new MusicRepository();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static String nextTrackUri = "-1";

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID, "Player controls", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        mediaSession = new MediaSessionCompat(this, "PlayerService", null, pendingIntent);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);

        Context appContext = getApplicationContext();

        mediaSession.setSessionActivity(PendingIntent.getActivity(appContext, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE));

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE));

        //exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer = new ExoPlayer.Builder(this).build();
        exoPlayer.addListener(exoPlayerListener);
        //DataSource.Factory httpDataSourceFactory = new OkHttpDataSourceFactory((Call.Factory) new OkHttpClient(), Util.getUserAgent(this, getString(R.string.app_name)));
        //dataSourceFactory = new CacheDataSourceFactory(MediaCache.getCache(this), httpDataSourceFactory, CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        //extractorsFactory = new DefaultExtractorsFactory();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        refreshNotificationAndForegroundStatus(exoPlayer.getPlaybackState());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        exoPlayer.release();
    }

    private Uri currentUri = Uri.parse("");
    private static int currentState = PlaybackStateCompat.STATE_STOPPED;
    private final MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            if (!exoPlayer.getPlayWhenReady()) {
                startService(new Intent(getApplicationContext(), PlayerService.class));
                if (musicRepository.getMaxIndex() < 0 || musicRepository.getCurrentItemIndex() < 0) {
                    return;
                }
                MusicRepository.Track track = musicRepository.getCurrent();
                updateMetadataFromTrack(track);
                if (!track.getUri().equals(currentUri.toString()) && track.getUri().toString().startsWith("https://www.youtube.com/watch?v=")) {
                    prepareToPlay(Uri.parse(track.getUri()));
                    return;
                }
                if (!audioFocusRequested) {
                    audioFocusRequested = true;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int audioFocusResult;
                        audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
                        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                            return;
                    }
                }

                mediaSession.setActive(true);

                registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

                exoPlayer.setPlayWhenReady(true);
            }

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, exoPlayer.getCurrentPosition(), 1).build());
            currentState = PlaybackStateCompat.STATE_PLAYING;

            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onPause() {
            if (exoPlayer.getPlayWhenReady()) {
                exoPlayer.setPlayWhenReady(false);
                try {
                    unregisterReceiver(becomingNoisyReceiver);
                } catch (Exception ignored) {
                }
            }

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, exoPlayer.getCurrentPosition(), 1).build());
            currentState = PlaybackStateCompat.STATE_PAUSED;

            refreshNotificationAndForegroundStatus(currentState);

        }

        @Override
        public void onStop() {
            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, exoPlayer.getCurrentPosition(), 1).build());
            currentState = PlaybackStateCompat.STATE_STOPPED;

            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onSkipToNext() {
            MusicRepository.Track track = musicRepository.getNext();
            updateMetadataFromTrack(track);
            refreshNotificationAndForegroundStatus(currentState);
            if (track.getUri().equals(currentUri.toString())) {
                prepareToPlay(Uri.parse(track.getUri()));
                mediaSessionCallback.onSeekTo(0);
            } else {
                prepareToPlay(Uri.parse(track.getUri()));
                //MainActivity.mediaController.getTransportControls().pause();
            }
        }

        @Override
        public void onSkipToPrevious() {
            MusicRepository.Track track = musicRepository.getPrevious();
            updateMetadataFromTrack(track);
            refreshNotificationAndForegroundStatus(currentState);
            if (track.getUri().equals(currentUri.toString())) {
                prepareToPlay(Uri.parse(track.getUri()));
                //mediaSessionCallback.onSeekTo(0);
            } else {
                prepareToPlay(Uri.parse(track.getUri()));
            }
        }

        @Override
        public void onSeekTo(long pos) {
            exoPlayer.seekTo(pos);
            mediaSession.setPlaybackState(stateBuilder.setState(mediaSession.getController().getPlaybackState().getState(), exoPlayer.getCurrentPosition(), 1).build());
            refreshNotificationAndForegroundStatus(currentState);
        }

        private void prepareToPlay(Uri uri) {
            startService(new Intent(getApplicationContext(), PlayerService.class));
            MainActivity.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.currentPlaylistAdapter.updateRequestList(musicRepository.getData());
                }
            });
            if (!uri.equals(currentUri)) {
                if (uri.toString().startsWith("https://www.youtube.com")) {
                    currentUri = uri;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            exoPlayer.clearMediaItems();
                        }
                    });

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                String responseBody = null;
                                File dir = new File(MainActivity.activity.getDataDir() + "/downloads");
                                if (dir.exists() && dir.listFiles() != null) {
                                    for (MusicRepository.Track track : FavoriteService.getDownloads()) {
                                        if (uri.toString().equals(track.getUri())) {
                                            responseBody = MainActivity.activity.getDataDir() + "/downloads/" + track.getUri().split("\\?v=")[1] + ".m4a";
                                            break;
                                        }
                                    }
                                }
                                if (responseBody == null)
                                    responseBody = YTMusicApi.getSourceUrl(uri.toString());
                                if (responseBody == null) {
                                    onSkipToNext();
                                    return;
                                }
                                if (!currentUri.equals(uri)) {
                                    return;
                                }
                                String finalResponseBody = responseBody;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MediaItem mediaItem = MediaItem.fromUri(finalResponseBody);
                                        exoPlayer.addMediaItem(mediaItem);
                                        exoPlayer.prepare();
                                        exoPlayer.seekToNextMediaItem();
                                        if (!audioFocusRequested) {
                                            audioFocusRequested = true;

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                int audioFocusResult;
                                                audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
                                                if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                                    return;
                                                }
                                            }
                                        }

                                        mediaSession.setActive(true);

                                        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
                                        FavoriteService.saveToHistory(PlayerService.musicRepository.getData().get(PlayerService.musicRepository.getCurrentItemIndex()), "unknown");
                                        exoPlayer.setPlayWhenReady(true);
                                        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, exoPlayer.getCurrentPosition(), 1).build());
                                        currentState = PlaybackStateCompat.STATE_PLAYING;
                                        refreshNotificationAndForegroundStatus(currentState);
                                    }
                                });
                                updateMetadataFromTrack(musicRepository.getCurrent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                }
            }
        }
    };

    private void updateMetadataFromTrack(MusicRepository.Track track) {
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getArtist());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist());
        mediaSession.setMetadata(metadataBuilder.build());
        refreshNotificationAndForegroundStatus(currentState);
        Thread thread2;
        if (!track.getBigImage().equals("")) {
            thread2 = new Thread() {
                @Override
                public void run() {
                    try {
                        updateMetadataFromTrack(track, Picasso.get().load(track.getBigImage()).get());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshNotificationAndForegroundStatus(currentState);
                            }
                        });
                    } catch (Exception ignored) {
                    }
                }
            };
        } else {
            thread2 = new Thread() {
                @Override
                public void run() {
                    try {
                        String urlBigImage = null;
                        try {
                            urlBigImage = YTMusicApi.getSongImage(track.getUri().split("\\?v=")[1]);
                            musicRepository.setCurrentBigImage(Uri.parse(urlBigImage));
                        } catch (Exception e) {
                            urlBigImage = track.getImage();
                        }
                        Bitmap bitmap = null;
                        try {
                            bitmap = Picasso.get().load(urlBigImage).get();
                            updateMetadataFromTrack(track, bitmap);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    refreshNotificationAndForegroundStatus(currentState);
                                }
                            });
                        } catch (Exception ignored) {
                        }
                        if (bitmap == null) {
                            try {
                                bitmap = Picasso.get().load(urlBigImage).get();
                                updateMetadataFromTrack(track, bitmap);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshNotificationAndForegroundStatus(currentState);
                                    }
                                });
                            } catch (Exception ignored) {
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
        }
        thread2.start();
    }

    public void updateMetadataFromTrack(MusicRepository.Track track, Bitmap image) {
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, image);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getArtist());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist());
        mediaSession.setMetadata(metadataBuilder.build());
    }

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    exoPlayer.setVolume(1);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaSessionCallback.onPause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mediaSessionCallback.onPause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    exoPlayer.setVolume(0.3f);
                    break;
                default:
                    mediaSessionCallback.onPause();
                    break;
            }
        }
    };

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Disconnecting headphones - stop playback
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };

    public static void threadAddItemNext(MusicRepository.Track track) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (track == null) return;
                String uri = track.getUri();
                String url = null;
                File dir = new File(MainActivity.activity.getDataDir() + "/downloads");
                if (dir.exists() && dir.listFiles() != null) {
                    for (MusicRepository.Track track : FavoriteService.getDownloads()) {
                        if (uri.toString().equals(track.getUri())) {
                            url = MainActivity.activity.getDataDir() + "/downloads/" + track.getUri().split("\\?v=")[1] + ".m4a";
                            break;
                        }
                    }
                }
                if (url == null)
                    url = YTMusicApi.getSourceUrl(uri.toString());
                if (url == null) return;
                if (nextTrackUri.equals(track.getUri())) return;
                String finalUrl = url;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        exoPlayer.addMediaItem(exoPlayer.getCurrentMediaItemIndex() + 1, MediaItem.fromUri(finalUrl));
                    }
                });
                nextTrackUri = track.getUri();
            }
        };
        thread.start();
    }

    public static void updateTrack(String track) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (track == null) return;
                String uri = track;
                String url = null;
                File dir = new File(MainActivity.activity.getDataDir() + "/downloads");
                if (dir.exists() && dir.listFiles() != null) {
                    for (MusicRepository.Track track : FavoriteService.getDownloads()) {
                        if (uri.toString().equals(track.getUri())) {
                            url = MainActivity.activity.getDataDir() + "/downloads/" + track.getUri().split("\\?v=")[1] + ".m4a";
                            break;
                        }
                    }
                }
                if (url == null)
                    url = YTMusicApi.getSourceUrl(uri.toString());
                if (url == null) return;
                String finalUrl = url;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long currentPosition = exoPlayer.getCurrentPosition();
                        exoPlayer.clearMediaItems();
                        exoPlayer.addMediaItem(MediaItem.fromUri(finalUrl));
                        exoPlayer.seekTo(currentPosition);
                    }
                });
            }
        };
        thread.start();
    }

    private final ExoPlayer.Listener exoPlayerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == ExoPlayer.STATE_ENDED) {
                if (exoPlayer.getCurrentTracks().isEmpty())
                    return;
                //Log.i("end", "end");
                mediaSessionCallback.onSkipToNext();
                MainActivity.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.currentPlaylistAdapter.updateRequestList(musicRepository.getData());
                    }
                });
            }
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            if (reason != Player.EVENT_MEDIA_ITEM_TRANSITION) return;
            if (musicRepository.repeat == 2 || (musicRepository.repeat == 1 && musicRepository.getData().size() == 1))
                mediaSessionCallback.onSeekTo(0);
            else {
                if (musicRepository.getCurrentItemIndex() != -1)
                    FavoriteService.saveToHistory(musicRepository.getCurrent(), "end");
                else
                    return;
                if (musicRepository.getMaxIndex() == musicRepository.getCurrentItemIndex() && musicRepository.relatedData.size() > 0 && musicRepository.relatedData.get(0).getUri().equals(nextTrackUri)) {
                    currentUri = Uri.parse(musicRepository.getNext().getUri());
                    updateMetadataFromTrack(musicRepository.getCurrent());
                } else if (musicRepository.getMaxIndex() != musicRepository.getCurrentItemIndex() && musicRepository.data.get(musicRepository.getCurrentItemIndex() + 1).getUri().equals(nextTrackUri)) {
                    currentUri = Uri.parse(musicRepository.getNext().getUri());
                    updateMetadataFromTrack(musicRepository.getCurrent());
                } else if (musicRepository.getMaxIndex() == musicRepository.getCurrentItemIndex() && musicRepository.repeat == 1 && musicRepository.data.get(0).getUri().equals(nextTrackUri)) {
                    currentUri = Uri.parse(musicRepository.getNext().getUri());
                    updateMetadataFromTrack(musicRepository.getCurrent());
                } else {
                    mediaSessionCallback.onSkipToNext();
                }
            }
            nextTrackUri = "-1";
            MainActivity.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.currentPlaylistAdapter.updateRequestList(musicRepository.getData());
                }
            });
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("state", Integer.toString(exoPlayer.getPlaybackState()));
        refreshNotificationAndForegroundStatus(exoPlayer.getPlaybackState());
        return new PlayerServiceBinder();
    }

    public static class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }

    private Handler updateHandler = new Handler();
    private boolean update = false;
    private Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                long duration = exoPlayer.getDuration();
                long currentPosition = exoPlayer.getCurrentPosition();
                metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
                mediaSession.setMetadata(metadataBuilder.build());
                mediaSession.setPlaybackState(stateBuilder.setState(currentState, currentPosition, 1).build());
                Log.i("pos", Long.toString(currentPosition));
                updateHandler.postDelayed(this, 500);
                refreshNotificationAndForegroundStatus(PlaybackStateCompat.STATE_PLAYING);
            }
        }
    };

    private void refreshNotificationAndForegroundStatus(int playbackState) {
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                if (!update) {
                    updateHandler.post(updateNotificationRunnable);
                    update = true;
                }
                startForeground(NOTIFICATION_ID, getNotification(playbackState));
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                updateHandler.removeCallbacks(updateNotificationRunnable);
                update = false;
                NotificationManagerCompat.from(PlayerService.this).notify(NOTIFICATION_ID, getNotification(playbackState));
                stopForeground(false);
                break;
            }
            default: {
                updateHandler.removeCallbacks(updateNotificationRunnable);
                update = false;
                stopForeground(true);
                break;
            }
        }
    }

    private Notification getNotification(int playbackState) {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSession, NOTIFICATION_DEFAULT_CHANNEL_ID);
        builder.addAction(new NotificationCompat.Action(R.drawable.button_previous_white_24dp, getString(R.string.previous), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));

        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause_white_24dp, getString(R.string.pause), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        else
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_white_24dp, getString(R.string.play), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));

        builder.addAction(new NotificationCompat.Action(R.drawable.button_next_white_24dp, getString(R.string.next), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                .setMediaSession(mediaSession.getSessionToken()));
        builder.setSmallIcon(R.drawable.ic_musicfission_white_24dp);
        /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            builder.setColor(ContextCompat.getColor(this, R.color.white));*/
        builder.setShowWhen(false);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOnlyAlertOnce(true);
        builder.setSilent(true);

        return builder.build();
    }
}
