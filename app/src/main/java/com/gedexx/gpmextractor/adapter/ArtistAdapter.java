package com.gedexx.gpmextractor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gedexx.gpmextractor.domain.Artist;
import com.gedexx.gpmextractor.itemview.ArtistItemView;
import com.gedexx.gpmextractor.itemview.ArtistItemView_;

import java.util.List;

public class ArtistAdapter extends ArrayAdapter<Artist> {


    public ArtistAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ArtistItemView artistItemView;
        if (convertView == null) {
            artistItemView = ArtistItemView_.build(getContext());
        } else {
            artistItemView = (ArtistItemView) convertView;
        }

        artistItemView.bind(getItem(position));

        return artistItemView;
    }
}
