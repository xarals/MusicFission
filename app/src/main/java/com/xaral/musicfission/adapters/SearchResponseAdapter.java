package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.MusicRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchResponseAdapter  extends RecyclerView.Adapter<SearchResponseAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private Map<String, RecyclerView.Adapter> adapters;
    private List<String> titles = new ArrayList<>();

    public SearchResponseAdapter(View view, Map<String, RecyclerView.Adapter> adapters) {
        this.adapters = adapters;
        for (Map.Entry<String, RecyclerView.Adapter> key : adapters.entrySet())
            titles.add(key.getKey());
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_response, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String title = titles.get(position);
        if (title.equals(view.getContext().getString(R.string.artists)) && position == 0)
            holder.title.setText(view.getContext().getString(R.string.artist));
        else
            holder.title.setText(title);
        LinearLayoutManager layoutManager;
        if (title.equals(view.getContext().getString(R.string.songs)) || title.equals(view.getContext().getString(R.string.videos)))
            layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        else
            layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
        holder.responseItem.setLayoutManager(layoutManager);
        holder.responseItem.setAdapter(adapters.get(title));
    }

    @Override
    public int getItemCount() {
        return adapters.size();
    }

    public void updateRequestList(Map<String, RecyclerView.Adapter> newRequests) {
        adapters = newRequests;
        titles = new ArrayList<>();
        for (Map.Entry<String, RecyclerView.Adapter> key : adapters.entrySet())
            titles.add(key.getKey());
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final RecyclerView responseItem;
        //private final View view;
        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            title = view.findViewById(R.id.title);
            responseItem = view.findViewById(R.id.responseItem);
        }
    }
}