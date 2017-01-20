package com.gedexx.gpmextractor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gedexx.gpmextractor.domain.Track;
import com.gedexx.gpmextractor.itemview.TrackItemView;
import com.gedexx.gpmextractor.itemview.TrackItemView_;

import java.util.List;

public class TrackAdapter extends ArrayAdapter<Track> {

    public TrackAdapter(Context context, List<Track> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TrackItemView trackItemView;
        if (convertView == null) {
            trackItemView = TrackItemView_.build(getContext());
        } else {
            trackItemView = (TrackItemView) convertView;
        }

        trackItemView.bind(getItem(position));

        return trackItemView;
    }
}
