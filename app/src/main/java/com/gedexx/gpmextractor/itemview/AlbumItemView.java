package com.gedexx.gpmextractor.itemview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Album;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

import java.io.FileNotFoundException;

@EViewGroup(R.layout.row_album)
public class AlbumItemView extends RelativeLayout {

    @ViewById
    public ImageView ivAlbumCoverArt;

    @ViewById
    public TextView tvAlbumName;

    @ViewById
    public TextView tvArtistAlbumName;

    @ViewById
    public CheckBox cbAlbum;

    public AlbumItemView(Context context) {
        super(context);
    }

    public void bind(Album album) {

        try {
            Bitmap photo = BitmapFactory.decodeStream(getContext().openFileInput(album.getCoverArtLocalPath()));
            ivAlbumCoverArt.setImageDrawable(new BitmapDrawable(getContext().getResources(), photo));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        tvAlbumName.setText(album.getName().replace("\"", ""));
        tvArtistAlbumName.setText(album.getArtist().getName().replace("\"", ""));
        cbAlbum.setChecked(cbAlbum.isChecked());
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.download_finished", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    void onFinishedDownload() {
        ivAlbumCoverArt.invalidate();
    }
}
