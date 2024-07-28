package com.xaral.musicfission.ui;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.ArtistsAdapter;
import com.xaral.musicfission.adapters.HistoryAdapter;
import com.xaral.musicfission.adapters.RelatedSongsAdapter;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;

import java.util.List;

public class HomeFragment extends Fragment {

    private ScrollView scrollStatusBar, scrollContent;
    private ImageView line;
    private ConstraintLayout nothingHistory, nothingArtists, nothingSimilar, firstStartView;
    private RecyclerView historyView, artistsView, similarView;
    private HistoryAdapter historyAdapter;
    private ArtistsAdapter artistsAdapter;
    private RelatedSongsAdapter relatedSongsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        /*try {
            AdView adView = root.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } catch (Exception ignored) {}*/

        line = root.findViewById(R.id.line);

        firstStartView = root.findViewById(R.id.firstStartView);

        if (FavoriteService.isFirstStart())
            firstStartView.setVisibility(View.VISIBLE);
        else
            firstStartView.setVisibility(View.INVISIBLE);

        nothingHistory = root.findViewById(R.id.nothingHistory);
        nothingArtists = root.findViewById(R.id.nothingArtists);
        nothingSimilar = root.findViewById(R.id.nothingSimilar);

        scrollStatusBar = root.findViewById(R.id.scrollStatusBar);
        scrollContent = root.findViewById(R.id.scrollContent);

        historyView = root.findViewById(R.id.historyView);
        artistsView = root.findViewById(R.id.artistsView);
        similarView = root.findViewById(R.id.similarView);

        List<MusicRepository.Track> similarList = FavoriteService.getRelatedSongs();
        if (similarList.size() > 15)
            relatedSongsAdapter = new RelatedSongsAdapter(root, similarList.subList(0, 15));
        else
            relatedSongsAdapter = new RelatedSongsAdapter(root, similarList);
        if (similarList.size() > 0)
            nothingSimilar.setVisibility(View.INVISIBLE);
        similarView.setAdapter(relatedSongsAdapter);

        List<MusicRepository.Track> trackList = FavoriteService.getFromHistory("end");
        if (trackList.size() > 10)
            historyAdapter = new HistoryAdapter(root, trackList.subList(0, 10));
        else
            historyAdapter = new HistoryAdapter(root, trackList);
        if (trackList.size() > 0)
            nothingHistory.setVisibility(View.INVISIBLE);
        historyView.setAdapter(historyAdapter);

        List<MusicRepository.Artist> artistList = FavoriteService.getFavoriteArtist();
        artistsAdapter = new ArtistsAdapter(root, artistList);
        if (artistList.size() > 0)
            nothingArtists.setVisibility(View.INVISIBLE);
        artistsView.setAdapter(artistsAdapter);

        scrollContent.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            int lastScrollY = 0;
            @Override
            public void onScrollChanged() {
                int scrollY = scrollContent.getScrollY();
                if (scrollY > lastScrollY) {
                    scrollStatusBar.scrollBy(0, scrollY - lastScrollY);
                } else if (scrollY < lastScrollY) {
                    scrollStatusBar.scrollBy(0, scrollY - lastScrollY);
                }
                if (scrollY == 0) {
                    scrollStatusBar.fullScroll(View.FOCUS_UP);
                    line.setVisibility(View.INVISIBLE);
                }
                else
                    line.setVisibility(View.VISIBLE);
                lastScrollY = scrollY;
            }
        });

        scrollStatusBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollContent.onTouchEvent(event);
                return true;
            }
        });

        return root;
    }
}