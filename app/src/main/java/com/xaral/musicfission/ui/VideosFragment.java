package com.xaral.musicfission.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.SongAdapter;
import com.xaral.musicfission.adapters.VideoAdapter;
import com.xaral.musicfission.service.MusicRepository;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment {
    private RecyclerView videosView;
    private VideoAdapter videoAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_videos, container, false);

        videosView = root.findViewById(R.id.videosView);
        Bundle arguments = getArguments();
        String[] titles = arguments.getStringArray("titles");
        String[] artists = arguments.getStringArray("artists");
        String[] artistids = arguments.getStringArray("artistids");
        String[] uris = arguments.getStringArray("uris");
        String[] images = arguments.getStringArray("images");

        List<MusicRepository.Video> videosList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++)
            videosList.add(new MusicRepository.Video(titles[i], artists[i], artistids[i], uris[i], images[i]));

        videoAdapter = new VideoAdapter(root, videosList);
        videosView.setAdapter(videoAdapter);

        return root;
    }
}
