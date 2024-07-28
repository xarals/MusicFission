package com.xaral.musicfission.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.xaral.musicfission.R;
import com.xaral.musicfission.adapters.AlbumsAdapter;
import com.xaral.musicfission.adapters.RelatedArtistsAdapter;
import com.xaral.musicfission.service.MusicRepository;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment  extends Fragment {

    private RecyclerView albumsView;
    private AlbumsAdapter albumsAdapter;
    private ImageView line;
    private static final int[] position = {0};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_albums, container, false);

        albumsView = root.findViewById(R.id.albumsView);

        Bundle arguments = getArguments();
        String[] titles = arguments.getStringArray("titles");
        String[] artists = arguments.getStringArray("artists");
        String[] artistids = arguments.getStringArray("artistids");
        String[] browseIds = arguments.getStringArray("browseIds");
        String[] years = arguments.getStringArray("years");
        String[] images = arguments.getStringArray("images");

        List<MusicRepository.Album> albumList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++)
            albumList.add(new MusicRepository.Album(titles[i], artists[i], artistids[i], browseIds[i], years[i], images[i]));

        albumsAdapter = new AlbumsAdapter(root, albumList);
        albumsView.setAdapter(albumsAdapter);

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
        albumsView.setOnScrollChangeListener(scrollListener);

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
