package com.gedexx.gpmextractor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Artist;

import java.util.List;

public class ArtistAdapter extends ArrayAdapter<Artist> {


    public ArtistAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_artist, parent, false);
        }

        ArtistViewHolder viewHolder = (ArtistViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new ArtistViewHolder();
            viewHolder.tvArtistName = (TextView) convertView.findViewById(R.id.tvArtistName);

            convertView.setTag(viewHolder);
        }

        Artist artist = getItem(position);

        viewHolder.tvArtistName.setText(artist.getName());

        return convertView;
    }

    private class ArtistViewHolder {
        public TextView tvArtistName;
    }
}
