package com.xaral.musicfission.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.SongAdapter;
import com.xaral.musicfission.service.MusicRepository;

import java.util.ArrayList;
import java.util.List;

public class SongsFragment extends Fragment {
    private RecyclerView songsView;
    private SongAdapter songAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_songs, container, false);

        songsView = root.findViewById(R.id.songsView);
        Bundle arguments = getArguments();
        String[] titles = arguments.getStringArray("titles");
        String[] artists = arguments.getStringArray("artists");
        String[] artistids = arguments.getStringArray("artistids");
        String[] uris = arguments.getStringArray("uris");
        String[] images = arguments.getStringArray("images");
        long[] durations = arguments.getLongArray("durations");

        List<MusicRepository.Track> songsList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++)
            songsList.add(new MusicRepository.Track(titles[i], artists[i], artistids[i], uris[i], images[i], durations[i]));

        songAdapter = new SongAdapter(root, songsList);
        songsView.setAdapter(songAdapter);

        return root;
    }
}
