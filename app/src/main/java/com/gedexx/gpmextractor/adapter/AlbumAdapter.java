package com.gedexx.gpmextractor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Album;

import java.io.FileNotFoundException;
import java.util.List;

public class AlbumAdapter extends ArrayAdapter<Album> {

    public AlbumAdapter(Context context, List<Album> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_album, parent, false);
        }

        AlbumViewHolder viewHolder = (AlbumViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new AlbumViewHolder();
            viewHolder.ivAlbumCoverArt = (ImageView) convertView.findViewById(R.id.ivAlbumCoverArt);
            viewHolder.tvAlbumName = (TextView) convertView.findViewById(R.id.tvAlbumName);
            viewHolder.tvArtistAlbumName = (TextView) convertView.findViewById(R.id.tvArtistAlbumName);

            convertView.setTag(viewHolder);
        }

        Album album = getItem(position);

        try {
            Bitmap photo = BitmapFactory.decodeStream(getContext().openFileInput(album.getCoverArtLocalPath()));
            viewHolder.ivAlbumCoverArt.setImageDrawable(new BitmapDrawable(getContext().getResources(), photo));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        viewHolder.tvAlbumName.setText(album.getName());
        viewHolder.tvArtistAlbumName.setText(album.getArtist().getName());

        return convertView;
    }

    private class AlbumViewHolder {
        public ImageView ivAlbumCoverArt;
        public TextView tvAlbumName;
        public TextView tvArtistAlbumName;
    }
}
