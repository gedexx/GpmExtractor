package com.gedexx.gpmextractor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Track;

import java.io.FileNotFoundException;
import java.util.List;

public class TrackAdapter extends ArrayAdapter<Track>{

    public TrackAdapter(Context context, List<Track> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_track, parent, false);
        }

        TrackViewHolder viewHolder = (TrackViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new TrackViewHolder();
            viewHolder.ivTrackAlbumCoverArt = (ImageView) convertView.findViewById(R.id.ivTrackAlbumCoverArt);
            viewHolder.tvTrackTitle = (TextView) convertView.findViewById(R.id.tvTrackTitle);
            viewHolder.tvTrackArtistName = (TextView) convertView.findViewById(R.id.tvTrackArtistName);
            viewHolder.tvTrackAlbumName = (TextView) convertView.findViewById(R.id.tvTrackAlbumName);
            viewHolder.tvTrackDuration = (TextView) convertView.findViewById(R.id.tvTrackDuration);

            convertView.setTag(viewHolder);
        }

        Track track = getItem(position);

        try {
            Bitmap photo = BitmapFactory.decodeStream(getContext().openFileInput(track.getAlbum().getCoverArtLocalPath()));
            viewHolder.ivTrackAlbumCoverArt.setImageDrawable(new BitmapDrawable(getContext().getResources(), photo));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        viewHolder.tvTrackTitle.setText(track.getTitle().replace("\"", ""));
        viewHolder.tvTrackArtistName.setText(track.getArtist().getName());
        viewHolder.tvTrackAlbumName.setText(track.getAlbum().getName());
        viewHolder.tvTrackDuration.setText(String.format("%1$tM:%1$tS", track.getDuration()));


        return convertView;
    }

    private class TrackViewHolder {
        public ImageView ivTrackAlbumCoverArt;
        public TextView tvTrackTitle;
        public TextView tvTrackArtistName;
        public TextView tvTrackAlbumName;
        public TextView tvTrackDuration;
    }
}
