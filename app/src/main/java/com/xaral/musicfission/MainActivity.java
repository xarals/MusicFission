package com.xaral.musicfission;

import static com.xaral.musicfission.service.PlayerService.exoPlayer;
import static com.xaral.musicfission.service.PlayerService.musicRepository;
import static com.xaral.musicfission.service.PlayerService.nextTrackUri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xaral.musicfission.adapters.AddToPlaylistAdapter;
import com.xaral.musicfission.adapters.CurrentPlaylistAdapter;
import com.xaral.musicfission.adapters.SongAdapter;
import com.xaral.musicfission.service.DownloadService;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;
import com.xaral.musicfission.ui.HomeFragment;
import com.xaral.musicfission.ui.SearchFragment;
import com.xaral.musicfission.ui.SettingsFragment;
import com.xaral.musicfission.ytmusicapi.YTMusicApi;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private PlayerService.PlayerServiceBinder playerServiceBinder;
    private static MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private ServiceConnection serviceConnection;

    private static boolean playing = false;
    private static boolean isCreatePlayer = false;
    private static ImageView logo, play, skipToPrevious, skipToNext, repeat, favorite, hide, more, playHide, hideCurrentPlaylist;
    private static CardView imageView;
    private static TextView titleSong, artistSong, currentTime, totalTime, hideArtist, hideTitle;
    private static SeekBar seekBar;
    private static LinearLayout titleMin;
    private static ConstraintLayout player;
    private static CoordinatorLayout blur;
    private static FrameLayout bottomSheet, bottomSheetPlaylist;
    private static View nav_host_fragment_activity_main;
    private static BottomNavigationView navigationView;
    private static RecyclerView currentPlaylistView;
    public static CurrentPlaylistAdapter currentPlaylistAdapter;
    private static int currHeight = 0;

    public static Activity activity;
    private static String currentTrackBrowseId = "";
    private static LocalTime currentTrackTime;
    public static AlertDialog alertDialog;
    private static NavController navController;

    private static int maxIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FavoriteService.mSettings = getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        //FavoriteService.mSettings.edit().clear().apply();
        if (!FavoriteService.getLocale().equals("")) {
            Locale locale = new Locale(FavoriteService.getLocale());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        } else {
            String languageCode = getResources().getConfiguration().locale.getLanguage();
            FavoriteService.setLocale(languageCode);
        }
        setContentView(R.layout.activity_main);

        activity = this;
        nav_host_fragment_activity_main = findViewById(R.id.nav_host_fragment_activity_main);

        logo = findViewById(R.id.logo);
        titleSong = findViewById(R.id.titleSong);
        hideTitle = findViewById(R.id.hideTitle);
        hideArtist = findViewById(R.id.hideArtist);
        artistSong = findViewById(R.id.artistSong);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);
        imageView = findViewById(R.id.imageView);
        play = findViewById(R.id.play);
        skipToPrevious = findViewById(R.id.skip_to_previous);
        skipToNext = findViewById(R.id.skip_to_next);
        repeat = findViewById(R.id.repeat);
        favorite = findViewById(R.id.favorite);
        hide = findViewById(R.id.hide);
        more = findViewById(R.id.more);
        hideCurrentPlaylist = findViewById(R.id.hideCurrentPlaylist);
        playHide = findViewById(R.id.playHide);
        titleMin = findViewById(R.id.titleMin);
        player = findViewById(R.id.playerLayout);
        bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetPlaylist = findViewById(R.id.bottomSheetPlaylist);
        hideCurrentPlaylist = findViewById(R.id.hideCurrentPlaylist);
        currentPlaylistView = findViewById(R.id.currentPlaylistView);
        currentPlaylistAdapter = new CurrentPlaylistAdapter(this, new ArrayList<>());
        currentPlaylistView.setAdapter(currentPlaylistAdapter);
        blur = findViewById(R.id.blurLayout);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavOptions navOptions1 = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right)
                .setPopEnterAnim(R.anim.slide_out_left)
                .setPopExitAnim(R.anim.slide_in_right)
                .build();
        NavOptions navOptions2 = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_out_left)
                .setExitAnim(R.anim.slide_in_right)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build();

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                BottomSheetBehavior<View> bottomSheetPlaylistBehavior = BottomSheetBehavior.from(bottomSheetPlaylist);
                if (bottomSheetPlaylistBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetPlaylistBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (item.getItemId() == R.id.navigation_home) {
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_home)
                        return true;
                    if (navView.getSelectedItemId() == R.id.navigation_search || navView.getSelectedItemId() == R.id.navigation_library || navView.getSelectedItemId() == R.id.navigation_downloads || navView.getSelectedItemId() == R.id.navigation_settings) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_home, null, navOptions2);
                    }
                    else {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_home, null, navOptions2);
                    }
                    return true;
                }
                else if (item.getItemId() == R.id.navigation_search) {
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_search)
                        return true;
                    if (navView.getSelectedItemId() == R.id.navigation_home) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_search, null, navOptions1);
                    }
                    else if (navView.getSelectedItemId() == R.id.navigation_library || navView.getSelectedItemId() == R.id.navigation_downloads || navView.getSelectedItemId() == R.id.navigation_settings) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_search, null, navOptions2);
                    }
                    else {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_search, null, navOptions2);
                    }
                    return true;
                }
                else if (item.getItemId() == R.id.navigation_library) {
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_library)
                        return true;
                    if (navView.getSelectedItemId() == R.id.navigation_home || navView.getSelectedItemId() == R.id.navigation_search || navView.getSelectedItemId() == R.id.navigation_downloads) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_library, null, navOptions1);
                    }
                    else if (navView.getSelectedItemId() == R.id.navigation_settings) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_library, null, navOptions2);
                    }
                    else {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_library, null, navOptions2);
                    }
                    return true;
                }
                else if (item.getItemId() == R.id.navigation_downloads) {
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_downloads)
                        return true;
                    if (navView.getSelectedItemId() == R.id.navigation_home || navView.getSelectedItemId() == R.id.navigation_search) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_downloads, null, navOptions1);
                    }
                    else if (navView.getSelectedItemId() == R.id.navigation_library || navView.getSelectedItemId() == R.id.navigation_settings) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_downloads, null, navOptions2);
                    }
                    else {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_downloads, null, navOptions2);
                    }
                    return true;
                }
                else if (item.getItemId() == R.id.navigation_settings) {
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_settings)
                        return true;
                    if (navView.getSelectedItemId() == R.id.navigation_home || navView.getSelectedItemId() == R.id.navigation_search || navView.getSelectedItemId() == R.id.navigation_library || navView.getSelectedItemId() == R.id.navigation_downloads) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_settings, null, navOptions1);
                    }
                    else {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        navController.navigate(R.id.navigation_settings, null, navOptions2);
                    }
                    return true;
                }
                return false;
            }
        });

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                if (navDestination.getId() == R.id.navigation_home || navDestination.getId() == R.id.navigation_search || navDestination.getId() == R.id.navigation_library || navDestination.getId() == R.id.navigation_downloads || navDestination.getId() == R.id.navigation_settings)
                    navView.getMenu().findItem(navDestination.getId()).setChecked(true);
            }
        });

        titleSong.setSelected(true);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        BottomSheetBehavior<View> bottomSheetPlaylistBehavior = BottomSheetBehavior.from(bottomSheetPlaylist);
        bottomSheetPlaylistBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case (BottomSheetBehavior.STATE_COLLAPSED):
                        hideCurrentPlaylist.setVisibility(View.INVISIBLE);
                        hideCurrentPlaylist.setAlpha(0f);
                        bottomSheetBehavior.setDraggable(true);
                        break;
                    case (BottomSheetBehavior.STATE_EXPANDED):
                        hideCurrentPlaylist.setVisibility(View.VISIBLE);
                        hideCurrentPlaylist.setAlpha(1f);
                        bottomSheetBehavior.setDraggable(false);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0.6) {
                    hideCurrentPlaylist.setAlpha(2.5f * slideOffset - 1.5f);
                    hideCurrentPlaylist.setVisibility(View.VISIBLE);
                } else {
                    hideCurrentPlaylist.setAlpha(0f);
                    hideCurrentPlaylist.setVisibility(View.INVISIBLE);
                }
            }
        });

        final int initialImageHeight = (int) getResources().getDimension(R.dimen.image_width);
        final boolean[] firstUp = {true};
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                //Log.i("new_stand", Integer.toString(newState));
                ViewGroup.LayoutParams layoutParams2 = player.getLayoutParams();
                ConstraintLayout.LayoutParams layoutParams1 = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
                ViewGroup.LayoutParams layoutParams = logo.getLayoutParams();
                ConstraintLayout.LayoutParams titleParams = (ConstraintLayout.LayoutParams) titleMin.getLayoutParams();
                switch (newState) {
                    case (BottomSheetBehavior.STATE_EXPANDED):
                        layoutParams2.height = currHeight;
                        layoutParams1.horizontalBias = 0.5f;
                        layoutParams1.verticalBias = 0.184f;
                        layoutParams.height = (int) getResources().getDimension(R.dimen.image_width);
                        layoutParams.width = (int) getResources().getDimension(R.dimen.image_width);
                        imageView.setRadius((int) getResources().getDimension(R.dimen.big_radius));
                        setAlpha(1);
                        changeVisibility(View.VISIBLE);
                        titleMin.setVisibility(View.INVISIBLE);
                        titleMin.setAlpha(0);
                        titleParams.setMarginStart((int) getResources().getDimension(R.dimen.big_margin));
                        playHide.setAlpha(0.0f);
                        playHide.setVisibility(View.INVISIBLE);
                        BottomSheetBehavior.from(bottomSheetPlaylist).setState(BottomSheetBehavior.STATE_COLLAPSED);
                        firstUp[0] = false;
                        break;
                    case (BottomSheetBehavior.STATE_COLLAPSED):
                        layoutParams2.height = (int) getResources().getDimension(R.dimen.playerHiddenHeight);
                        layoutParams1.horizontalBias = 0.05f;
                        layoutParams1.verticalBias = 0.5f;
                        layoutParams.height = (int) getResources().getDimension(R.dimen.min_height);
                        layoutParams.width = (int) getResources().getDimension(R.dimen.min_height);
                        imageView.setRadius((int) getResources().getDimension(R.dimen.small_radius));
                        changeVisibility(View.INVISIBLE);
                        titleMin.setVisibility(View.VISIBLE);
                        titleMin.setAlpha(1);
                        titleParams.setMarginStart((int) getResources().getDimension(R.dimen.small_margin));
                        playHide.setAlpha(1.0f);
                        playHide.setVisibility(View.VISIBLE);
                        BottomSheetBehavior.from(bottomSheetPlaylist).setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    default:
                        return;
                }
                player.setLayoutParams(layoutParams2);
                logo.setLayoutParams(layoutParams);
                imageView.setLayoutParams(layoutParams1);
                titleMin.setLayoutParams(titleParams);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                int newImageHeight = (int) (initialImageHeight * slideOffset + getResources().getDimension(R.dimen.min_height) * (1 - slideOffset));
                ViewGroup.LayoutParams layoutParams = logo.getLayoutParams();
                if (!firstUp[0]) {
                    int color = interpolateColor(getResources().getColor(R.color.grey_1000), getResources().getColor(R.color.black), slideOffset);
                    bottomSheet.setBackgroundColor(color);
                    getWindow().setNavigationBarColor(color);
                    navView.setBackgroundColor(color);
                }
                if (slideOffset > 0.8) {
                    setAlpha(5 * slideOffset - 4);
                    changeVisibility(View.VISIBLE);
                }
                else {
                    setAlpha(0);
                    changeVisibility(View.INVISIBLE);
                }
                if (slideOffset < 0.4) {
                    titleMin.setAlpha(-2.5f * slideOffset + 1);
                    playHide.setAlpha(-2.5f * slideOffset + 1);
                    titleMin.setVisibility(View.VISIBLE);
                    playHide.setVisibility(View.VISIBLE);
                }
                else {
                    titleMin.setAlpha(0);
                    playHide.setAlpha(0.0f);
                    playHide.setVisibility(View.INVISIBLE);
                    titleMin.setVisibility(View.INVISIBLE);
                }
                ConstraintLayout.LayoutParams titleParams = (ConstraintLayout.LayoutParams) titleMin.getLayoutParams();
                titleParams.setMarginStart((int) ((getResources().getDimension(R.dimen.big_margin) - getResources().getDimension(R.dimen.small_margin)) * slideOffset + getResources().getDimension(R.dimen.small_margin)));
                if (newImageHeight > (int) getResources().getDimension(R.dimen.min_height)) {
                    layoutParams.height = newImageHeight;
                    layoutParams.width = newImageHeight;
                }
                ViewGroup.LayoutParams layoutParams2 = player.getLayoutParams();
                int layoutHeight = (int) (currHeight * slideOffset + ((int) getResources().getDimension(R.dimen.playerHiddenHeight)) * (1 - slideOffset));
                if (layoutHeight > getResources().getDimension(R.dimen.playerHiddenHeight))
                    layoutParams2.height = layoutHeight;
                else
                    layoutParams2.height = (int) getResources().getDimension(R.dimen.playerHiddenHeight);
                ConstraintLayout.LayoutParams layoutParams1 = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams1.horizontalBias = 0.05f + 0.45f * slideOffset;
                layoutParams1.verticalBias = - 0.316f * slideOffset + 0.5f;
                titleMin.setLayoutParams(titleParams);
                player.setLayoutParams(layoutParams2);
                logo.setLayoutParams(layoutParams);
                imageView.setLayoutParams(layoutParams1);
                imageView.setRadius(slideOffset * ((int) getResources().getDimension(R.dimen.big_radius) - (int) getResources().getDimension(R.dimen.small_radius)) + (int) getResources().getDimension(R.dimen.small_radius));
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(MainActivity.mediaController != null && fromUser){
                    MainActivity.mediaController.getTransportControls().seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playing && mediaController != null && musicRepository.getCurrentItemIndex() > -1 && exoPlayer.getDuration() - exoPlayer.getCurrentPosition() < 10000) {
                    PlayerService.threadAddItemNext(musicRepository.willBeNext());
                    //Log.i("-", "-");
                }
                new Handler().postDelayed(this,3000);
            }
        });

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaController != null) {
                    try {
                        long position = exoPlayer.getCurrentPosition();
                        if (exoPlayer.getDuration() > 0) {
                            seekBar.setMax((int) exoPlayer.getDuration());
                            totalTime.setText(convertToMMSS(exoPlayer.getDuration() + ""));
                            seekBar.setProgress((int) position);
                            currentTime.setText(convertToMMSS(position + ""));
                        }
                        if (nextTrackUri != null && !nextTrackUri.equals("-1") && musicRepository.willBeNext() != null && !nextTrackUri.equals(musicRepository.willBeNext().getUri())) {
                            if (exoPlayer.getMediaItemCount() >= exoPlayer.getCurrentMediaItemIndex() + 1 ) {
                                exoPlayer.removeMediaItem(exoPlayer.getCurrentMediaItemIndex() + 1);
                                nextTrackUri = "-1";
                            }
                        }
                        if (musicRepository.getCurrentItemIndex() >= 0 && !currentTrackBrowseId.equals(musicRepository.data.get(musicRepository.getCurrentItemIndex()).getUri())) {
                            String title = musicRepository.data.get(musicRepository.getCurrentItemIndex()).getTitle();
                            String artist = musicRepository.data.get(musicRepository.getCurrentItemIndex()).getArtist();
                            titleSong.setText(title);
                            hideTitle.setText(title);
                            artistSong.setText(artist);
                            hideArtist.setText(artist);
                            bottomSheet.setVisibility(View.VISIBLE);
                            CoordinatorLayout.LayoutParams navParams = (CoordinatorLayout.LayoutParams) nav_host_fragment_activity_main.getLayoutParams();
                            navParams.bottomMargin = (int) getResources().getDimension(R.dimen.playerHiddenHeight);
                            nav_host_fragment_activity_main.setLayoutParams(navParams);
                            currentTrackBrowseId = musicRepository.getCurrent().getUri();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                currentTrackTime = LocalTime.now();
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && musicRepository.getCurrentItemIndex() >= 0 && currentTrackBrowseId.length() > 3 && Math.abs(Duration.between(currentTrackTime, LocalTime.now()).toMinutes()) >= 30) {
                            PlayerService.updateTrack(currentTrackBrowseId);
                            currentTrackTime = LocalTime.now();
                        }
                        if (musicRepository.getMaxIndex() != maxIndex) {
                            currentPlaylistAdapter.updateRequestList(musicRepository.getData());
                            maxIndex = musicRepository.getMaxIndex();
                        }
                        if (musicRepository.getCurrentItemIndex() >= 0 && !musicRepository.getData().get(musicRepository.getCurrentItemIndex()).getBigImage().toString().equals("") && !currentTrackBrowseId.equals("")) {
                            Picasso.get().load(musicRepository.getCurrent().getBigImage()).into(logo);
                            /*Picasso.get().load(PlayerService.musicRepository.getCurrent().getBigImage()).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    float radius = 25;

                                    Bitmap blurredBitmap = Bitmap.createBitmap(bitmap);
                                    RenderScript rs = RenderScript.create(MainActivity.this);
                                    ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                                    Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SHARED);
                                    Allocation output = Allocation.createTyped(rs, input.getType());
                                    blurScript.setRadius(radius);
                                    blurScript.setInput(input);
                                    blurScript.forEach(output);
                                    output.copyTo(blurredBitmap);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            blur.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                                        }
                                    });
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });*/
                        } else if (!currentTrackBrowseId.equals("")) {
                            Picasso.get().load(musicRepository.getCurrent().getImage()).into(logo);
                        }
                        if (playing) {
                            play.setImageResource(R.drawable.button_pause_white_24dp);
                            playHide.setImageResource(R.drawable.ic_pause_white_24dp);
                        } else {
                            play.setImageResource(R.drawable.button_play_white_24dp);
                            playHide.setImageResource(R.drawable.ic_play_white_24dp);
                        }
                        if (musicRepository.getCurrentItemIndex() > -1) {
                            if (FavoriteService.isLiked(musicRepository.getCurrent().getUri().toString())){
                                favorite.setImageResource(R.drawable.button_favorite_white_24dp);
                            } else {
                                favorite.setImageResource(R.drawable.button_unfavorite_white_24dp);
                            }
                        }
                        switch (musicRepository.repeat){
                            case 0:
                                repeat.setImageResource(R.drawable.ic_repeat_grey_24dp);
                                break;
                            case 1:
                                repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                                break;
                            case 2:
                                repeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                                break;
                        }
                    } catch (NullPointerException e) { }
                }
                new Handler().postDelayed(this,100);
            }
        });

        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state == null)
                    return;
                playing = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                isCreatePlayer = true;
            }
        };

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (PlayerService.PlayerServiceBinder) service;
                mediaController = new MediaControllerCompat(MainActivity.this, playerServiceBinder.getMediaSessionToken());
                mediaController.registerCallback(callback);
                callback.onPlaybackStateChanged(mediaController.getPlaybackState());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                if (mediaController != null) {
                    mediaController.unregisterCallback(callback);
                    mediaController = null;
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
        }
        if (!FavoriteService.getLocale().equals("")) {
            Locale locale = new Locale(FavoriteService.getLocale());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        } else {
            String languageCode = getResources().getConfiguration().locale.getLanguage();
            FavoriteService.setLocale(languageCode);
        }
        if (isCreatePlayer) {
            String title = mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            String artist = mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            titleSong.setText(title);
            hideTitle.setText(title);
            artistSong.setText(artist);
            hideArtist.setText(artist);
            bottomSheet.setVisibility(View.VISIBLE);
            firstUp[0] = false;
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            playlistUpdate();
            CoordinatorLayout.LayoutParams navParams = (CoordinatorLayout.LayoutParams) nav_host_fragment_activity_main.getLayoutParams();
            navParams.bottomMargin = (int) getResources().getDimension(R.dimen.playerHiddenHeight);
            nav_host_fragment_activity_main.setLayoutParams(navParams);
        } else {
            clearDownloads();
            bindService(new Intent(this, PlayerService.class), serviceConnection, BIND_AUTO_CREATE);
        }
    }

    public static void clearDownloads() {
        File dir = new File(MainActivity.activity.getDataDir() + "/downloads");
        if (!dir.exists() || dir.listFiles() == null) return;
        List<String> videoId = new ArrayList<>();
        for (MusicRepository.Track track : FavoriteService.getDownloads())
            videoId.add(track.getUri().split("\\?v=")[1] + ".m4a");
        for (File file : dir.listFiles()) {
            if (!videoId.contains(file.getName()))
                file.delete();
        }
    }

    public static void playlistUpdate() {
        currentPlaylistAdapter.updateRequestList(musicRepository.getData());
    }

    private static String convertToMMSS(String duration){
        Long millis = Long.parseLong(duration);
        if (millis >= 60 * 60 * 1000)
            return String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis) % TimeUnit.DAYS.toHours(1),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private int interpolateColor(int colorStart, int colorEnd, float slideOffset) {
        float[] hsvStart = new float[3];
        float[] hsvEnd = new float[3];
        float[] hsvResult = new float[3];

        Color.colorToHSV(colorStart, hsvStart);
        Color.colorToHSV(colorEnd, hsvEnd);

        for (int i = 0; i < 3; i++) {
            hsvResult[i] = (1 - slideOffset) * hsvStart[i] + slideOffset * hsvEnd[i];
        }

        return Color.HSVToColor(hsvResult);
    }

    private void setAlpha(float alpha) {
        currentTime.setAlpha(alpha);
        totalTime.setAlpha(alpha);
        titleSong.setAlpha(alpha);
        artistSong.setAlpha(alpha);
        //currentPlaylist.setAlpha(alpha);
        repeat.setAlpha(alpha);
        favorite.setAlpha(alpha);
        skipToNext.setAlpha(alpha);
        skipToPrevious.setAlpha(alpha);
        play.setAlpha(alpha);
        hide.setAlpha(alpha);
        //more.setAlpha(alpha);
        seekBar.setAlpha(alpha);
        bottomSheetPlaylist.setAlpha(alpha);
    }

    private void changeVisibility(int visible) {
        currentTime.setVisibility(visible);
        totalTime.setVisibility(visible);
        titleSong.setVisibility(visible);
        artistSong.setVisibility(visible);
        //currentPlaylist.setVisibility(visible);
        repeat.setVisibility(visible);
        favorite.setVisibility(visible);
        skipToNext.setVisibility(visible);
        skipToPrevious.setVisibility(visible);
        play.setVisibility(visible);
        hide.setVisibility(visible);
        //more.setVisibility(visible);
        seekBar.setVisibility(visible);
        bottomSheetPlaylist.setVisibility(visible);
    }

    public static void addToPlaylistDialog(Context context, MusicRepository.Track track) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.list_playlists, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        final RecyclerView playlistView = promptView.findViewById(R.id.playlistsView);
        List<String> playlistsBrowseId = FavoriteService.getPlaylists();
        List<MusicRepository.Playlist> playlistList = new ArrayList<>();
        playlistList.add(new MusicRepository.Playlist(context.getString(R.string.liked), context.getString(R.string.you), "", Integer.toString(FavoriteService.getLiked().size()), ""));
        for (String browseId : playlistsBrowseId) {
            String title = FavoriteService.getPlaylistTitle(browseId);
            String author = FavoriteService.getPlaylistAuthor(browseId);
            String image = FavoriteService.getPlaylistUriImage(browseId);
            int itemCount = FavoriteService.getPlaylistSongs(browseId).size();
            playlistList.add(new MusicRepository.Playlist(title, author, browseId, Integer.toString(itemCount), image));
        }
        AddToPlaylistAdapter adapter = new AddToPlaylistAdapter(context, playlistList, track);
        playlistView.setAdapter(adapter);

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currHeight == 0) {
            currHeight = player.getHeight();
        }
    }

    @Override
    public void onBackPressed() {
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        BottomSheetBehavior<View> bottomSheetPlaylistBehavior = BottomSheetBehavior.from(bottomSheetPlaylist);
        if (bottomSheetPlaylistBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetPlaylistBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    public void onSearchClick(View view) {
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right)
                .setPopEnterAnim(R.anim.slide_out_left)
                .setPopExitAnim(R.anim.slide_in_right)
                .build();
        Navigation.findNavController(view).navigate(R.id.navigation_search, null, navOptions);
    }

    public void onHomeClick(View view) {
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_out_left)
                .setExitAnim(R.anim.slide_in_right)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build();
        navController.popBackStack(navController.getGraph().getStartDestination(), false);
        Navigation.findNavController(view).navigate(R.id.navigation_home, null, navOptions);
    }

    public static void startMusic(int index) {
        musicRepository.setCurrentItemIndex(index - 1);
        mediaController.getTransportControls().skipToNext();
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        musicRepository.newRelatedData();
    }

    public static void startMusic(MusicRepository.Track song) {
        musicRepository.playLast();
        musicRepository.addEnd(song);
        mediaController.getTransportControls().skipToNext();
        musicRepository.newRelatedData();
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public static void startMusic(MusicRepository.Video video) {
        MusicRepository.Track song = new MusicRepository.Track(video.getTitle(), video.getArtist(), video.getArtistId(), video.getUrl(), video.getImage(), -1);
        musicRepository.playLast();
        musicRepository.addEnd(song);
        mediaController.getTransportControls().skipToNext();
        musicRepository.newRelatedData();
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void setLogoHeight(boolean isTrack) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) logo.getLayoutParams();
        if (!isTrack)
            layoutParams.height = (int) getResources().getDimension(R.dimen.video_height);
        else
            layoutParams.height = (int) getResources().getDimension(R.dimen.song_height);
        logo.setLayoutParams(layoutParams);
    }

    public static void setTrack(int index) {
        musicRepository.setCurrentItemIndex(index - 1);
        mediaController.getTransportControls().skipToNext();
    }

    public void onLibraryClick(View view) {
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right)
                .setPopEnterAnim(R.anim.slide_out_left)
                .setPopExitAnim(R.anim.slide_in_right)
                .build();
        Navigation.findNavController(view).navigate(R.id.navigation_library, null, navOptions);
    }

    public void onClickMore(View view) {
    }

    public void onClickHide(View view) {
        if (BottomSheetBehavior.from(bottomSheet).getState() == BottomSheetBehavior.STATE_COLLAPSED)
            return;
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void favoriteClick(View view) {
        if (musicRepository.getCurrentItemIndex() < 0) return;
        MusicRepository.Track track = musicRepository.getCurrent();
        if (FavoriteService.isLiked(track.getUri().toString())){
            FavoriteService.dislike(track);
        } else {
            FavoriteService.like(track);
        }
    }

    public void playClick(View view) {
        //Log.i("play", Boolean.toString(playing));
        if (!playing) {
            mediaController.getTransportControls().play();
        } else {
            mediaController.getTransportControls().pause();
        }
    }

    public void nextClick(View view) {
        if (mediaController != null) {
            if (musicRepository.getCurrentItemIndex() != -1)
                FavoriteService.saveToHistory(musicRepository.getCurrent(), "skip");
            mediaController.getTransportControls().skipToNext();
        }
    }

    public void previousClick(View view) {
        if (mediaController != null)
            mediaController.getTransportControls().skipToPrevious();
    }

    public void repeatClick(View view) {
        musicRepository.repeat++;
        if (musicRepository.repeat > 2) musicRepository.repeat = 0;
    }

    public void onBackClick(View view) {
        onBackPressed();
    }

    public void loadViewClick(View view) {
    }

    public void onSheetBehaviorClick(View view) {
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onPlaylistBehaviorClick(View view) {
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetPlaylist);
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onClickHidePlaylist(View view) {
        if (BottomSheetBehavior.from(bottomSheetPlaylist).getState() == BottomSheetBehavior.STATE_COLLAPSED)
            return;
        BottomSheetBehavior.from(bottomSheetPlaylist).setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}