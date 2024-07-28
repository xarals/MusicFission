package com.xaral.musicfission.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.DownloadsAdapter;
import com.xaral.musicfission.service.DownloadService;
import com.xaral.musicfission.service.MusicRepository;

import java.util.LinkedHashMap;

public class DownloadsFragment extends Fragment {
    private ConstraintLayout downloadPage;
    private RecyclerView downloadsView;
    private DownloadsAdapter downloadsAdapter;
    private ImageView line;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int[] position = {0};
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_downloads, container, false);

        /*try {
            AdView adView = root.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } catch (Exception ignored) {}*/

        downloadsView = root.findViewById(R.id.downloadsView);
        downloadPage = root.findViewById(R.id.downloadPage);
        line = root.findViewById(R.id.line);
        LinkedHashMap<MusicRepository.Track, Integer> map = DownloadService.queue;
        for (MusicRepository.Track track : DownloadService.downloadQueue)
            map.put(track, 1);
        if (map.keySet().isEmpty() && DownloadService.currentTrack == null)
            downloadPage.setVisibility(View.VISIBLE);
        downloadsAdapter = new DownloadsAdapter(root, map, DownloadService.currentTrack);
        downloadsView.setAdapter(downloadsAdapter);

        RecyclerView.OnScrollChangeListener scrollListener = new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                position[0] -= i3;
                if (position[0] == 0)
                    line.setAlpha(0f);
                else
                    line.setAlpha(1f);
            }
        };
        downloadsView.setOnScrollChangeListener(scrollListener);

        handler.post(new Runnable() {
            @Override
            public void run() {
                LinkedHashMap<MusicRepository.Track, Integer> map = DownloadService.queue;
                for (MusicRepository.Track track : DownloadService.downloadQueue)
                    map.put(track, 1);
                if (map.keySet().isEmpty() && DownloadService.currentTrack == null)
                    downloadPage.setVisibility(View.VISIBLE);
                else
                    downloadPage.setVisibility(View.INVISIBLE);
                downloadsAdapter.updateRequestList(map, DownloadService.currentTrack);
                new Handler().postDelayed(this,100);
            }
        });
        Bundle outState = getArguments();
        if (outState != null)
            position[0] = outState.getInt("position", 0);
        else
            position[0] = 0;

        return root;
    }

    @Override
    public void onPause() {
        Bundle outState = getArguments();
        if (outState == null) outState = new Bundle();
        outState.putInt("position", position[0]);
        setArguments(outState);
        super.onPause();
    }
}
