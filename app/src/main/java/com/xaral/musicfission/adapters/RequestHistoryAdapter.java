package com.xaral.musicfission.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.xaral.musicfission.MainActivity;
import com.xaral.musicfission.R;
import com.xaral.musicfission.service.FavoriteService;
import com.xaral.musicfission.service.MusicRepository;

import java.util.List;

public class RequestHistoryAdapter  extends RecyclerView.Adapter<RequestHistoryAdapter.ViewHolder>{

    private final View view;
    private final LayoutInflater inflater;
    private List<String> requests;

    public RequestHistoryAdapter(View view, List<String> requests) {
        this.requests = requests;
        this.view = view;
        this.inflater = LayoutInflater.from(view.getContext());
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String request = requests.get(position);
        holder.requestText.setText(request);
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                SearchView searchView = view.findViewById(R.id.searchView);
                searchView.setQuery(request, true);
            }
        });
        holder.pastIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchView searchView = view.findViewById(R.id.searchView);
                searchView.setQuery(request, false);
                searchView.setIconified(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequestList(List<String> newRequests) {
        requests = newRequests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView requestText;
        private final ImageView pastIcon;
        //private final View view;
        ViewHolder(View view){
            super(view);
            //view = view.findViewById(R.id.view3);
            requestText = view.findViewById(R.id.requestText);
            pastIcon = view.findViewById(R.id.pastIcon);
        }
    }
}

