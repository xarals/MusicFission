package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Process;
import android.os.Trace;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.DownloadService;
import com.xaral.musicfission.service.MusicRepository;
import com.xaral.musicfission.service.PlayerService;
import com.xaral.musicfission.ui.DownloadsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<MusicRepository.Track> downloadedTracks = new ArrayList<>();
    private List<MusicRepository.Track> queueTracks = new ArrayList<>();
    private List<MusicRepository.Track> errorTracks = new ArrayList<>();
    private MusicRepository.Track currentTrack;
    private ProgressBar progressBar;
    private TextView trackSize;

    public DownloadsAdapter(View view, LinkedHashMap<MusicRepository.Track, Integer> tracks, MusicRepository.Track currentTrack) {
        for (MusicRepository.Track track : tracks.keySet()) {
            if (tracks.get(track) == 0)
                downloadedTracks.add(track);
            else if (tracks.get(track) == 1)
                queueTracks.add(track);
            else if (tracks.get(track) == -1)
                errorTracks.add(track);
        }
        if (currentTrack != null) {
            queueTracks.remove(currentTrack);
            errorTracks.remove(currentTrack);
            downloadedTracks.remove(currentTrack);
        }
        Collections.reverse(downloadedTracks);
        this.currentTrack = currentTrack;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
    }
    @Override
    public DownloadsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_downloads, parent, false);
        return new DownloadsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DownloadsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position == 0 && currentTrack != null) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.trackSize.setVisibility(View.VISIBLE);
            holder.title.setText(currentTrack.getTitle());
            holder.button.setImageResource(R.drawable.ic_stop_white_24dp);
            Picasso.get().load(currentTrack.getImage()).into(holder.image);
            holder.imageStatus.setImageResource(R.drawable.ic_file_download_white_24dp);
            holder.status.setText(R.string.downloading);
            holder.status.setTextColor(MainActivity.activity.getColor(R.color.white));
            holder.progressBar.setProgress(DownloadService.currentProgress);
            progressBar = holder.progressBar;
            holder.trackSize.setText(DownloadService.stringProgress);
            trackSize = holder.trackSize;
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadService.cancelDownload(currentTrack);
                }
            });
            return;
        }
        int pos = position;
        if (currentTrack != null)
            pos--;
        if (pos < queueTracks.size()) {
            MusicRepository.Track track = queueTracks.get(pos);
            holder.title.setText(track.getTitle());
            holder.trackSize.setVisibility(View.INVISIBLE);
            Picasso.get().load(track.getImage()).into(holder.image);
            holder.button.setImageResource(R.drawable.ic_queue_white_24dp);
            holder.imageStatus.setImageResource(R.drawable.ic_queue_yellow_24dp);
            holder.status.setText(R.string.queue);
            holder.status.setTextColor(MainActivity.activity.getColor(R.color.yellow));
            holder.progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        pos -= queueTracks.size();
        if (pos < errorTracks.size()) {
            MusicRepository.Track track = errorTracks.get(pos);
            holder.title.setText(track.getTitle());
            holder.trackSize.setVisibility(View.INVISIBLE);
            Picasso.get().load(track.getImage()).into(holder.image);
            holder.button.setImageResource(R.drawable.ic_refresh_white_24dp);
            holder.imageStatus.setImageResource(R.drawable.ic_error_download_red_24dp);
            holder.status.setText(R.string.loading_error);
            holder.status.setTextColor(MainActivity.activity.getColor(R.color.red));
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadService.restartDownload(track);
                }
            });
            return;
        }
        pos -= errorTracks.size();
        if (pos < downloadedTracks.size()) {
            MusicRepository.Track track = downloadedTracks.get(pos);
            holder.title.setText(track.getTitle());
            holder.trackSize.setVisibility(View.INVISIBLE);
            Picasso.get().load(track.getImage()).into(holder.image);
            holder.button.setImageResource(R.drawable.ic_play_white_24dp);
            holder.imageStatus.setImageResource(R.drawable.ic_download_done_green_24dp);
            holder.status.setText(R.string.successful_download);
            holder.status.setTextColor(MainActivity.activity.getColor(R.color.green));
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.startMusic(track);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int size = downloadedTracks.size() + errorTracks.size() + queueTracks.size();
        if (currentTrack != null)
            size++;
        return size;
    }

    public void updateRequestList(LinkedHashMap<MusicRepository.Track, Integer> tracks, MusicRepository.Track currentTrack) {
        List<MusicRepository.Track> downloadedTracks = new ArrayList<>();
        List<MusicRepository.Track> queueTracks = new ArrayList<>();
        List<MusicRepository.Track> errorTracks = new ArrayList<>();
        for (MusicRepository.Track track : tracks.keySet()) {
            if (tracks.get(track) == 0)
                downloadedTracks.add(track);
            else if (tracks.get(track) == 1)
                queueTracks.add(track);
            else if (tracks.get(track) == -1)
                errorTracks.add(track);
        }
        if (currentTrack != null) {
            queueTracks.remove(currentTrack);
            errorTracks.remove(currentTrack);
            downloadedTracks.remove(currentTrack);
        }
        Collections.reverse(downloadedTracks);
        if (!queueTracks.equals(this.queueTracks) || !downloadedTracks.equals(this.downloadedTracks) || !errorTracks.equals(this.errorTracks) || (this.currentTrack == null && currentTrack != null) || (this.currentTrack != null && currentTrack == null) || (this.currentTrack != null && !this.currentTrack.equals(currentTrack))) {
            this.downloadedTracks = downloadedTracks;
            this.errorTracks = errorTracks;
            this.queueTracks = queueTracks;
            this.currentTrack = currentTrack;
            notifyDataSetChanged();
            return;
        }
        if (DownloadService.currentProgress != null && progressBar != null)
            progressBar.setProgress(DownloadService.currentProgress);
        if (DownloadService.stringProgress != null && trackSize != null)
            trackSize.setText(DownloadService.stringProgress);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, status, trackSize;
        private final ImageView image, imageStatus, button;
        private final ProgressBar progressBar;
        ViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.title);
            status = view.findViewById(R.id.status);
            trackSize = view.findViewById(R.id.trackSize);
            image = view.findViewById(R.id.image);
            imageStatus = view.findViewById(R.id.imageStatus);
            button = view.findViewById(R.id.button);
            progressBar = view.findViewById(R.id.progressBar);
        }
    }
}
