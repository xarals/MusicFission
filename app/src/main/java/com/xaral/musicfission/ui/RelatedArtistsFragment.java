package com.xaral.musicfission.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.RelatedArtistsAdapter;
import com.xaral.musicfission.service.MusicRepository;

import java.util.ArrayList;
import java.util.List;

public class RelatedArtistsFragment extends Fragment {

    private RecyclerView relatedView;
    private RelatedArtistsAdapter relatedArtistsAdapter;
    private ImageView line;
    private static final int[] position = {0};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_related, container, false);

        relatedView = root.findViewById(R.id.relatedView);

        Bundle arguments = getArguments();
        String[] artists = arguments.getStringArray("artists");
        String[] ids = arguments.getStringArray("ids");
        String[] subscribers = arguments.getStringArray("subscribers");
        String[] images = arguments.getStringArray("images");

        List<MusicRepository.Artist> relatedList = new ArrayList<>();
        for (int i = 0; i < artists.length; i++)
            relatedList.add(new MusicRepository.Artist(artists[i], ids[i], subscribers[i], images[i]));

        relatedArtistsAdapter = new RelatedArtistsAdapter(root, relatedList);
        relatedView.setAdapter(relatedArtistsAdapter);

        line = root.findViewById(R.id.line);

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
        relatedView.setOnScrollChangeListener(scrollListener);

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
