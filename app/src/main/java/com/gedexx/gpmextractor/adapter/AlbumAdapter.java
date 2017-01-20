package com.gedexx.gpmextractor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gedexx.gpmextractor.domain.Album;
import com.gedexx.gpmextractor.itemview.AlbumItemView;
import com.gedexx.gpmextractor.itemview.AlbumItemView_;

import java.util.List;

public class AlbumAdapter extends ArrayAdapter<Album> {

    public AlbumAdapter(Context context, List<Album> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AlbumItemView albumItemView;
        if (convertView == null) {
            albumItemView = AlbumItemView_.build(getContext());
        } else {
            albumItemView = (AlbumItemView) convertView;
        }

        albumItemView.bind(getItem(position));

        return albumItemView;
    }
}
