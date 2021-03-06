package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;

public final class AlbumArtUtil {

    private static final int MAX_SCREEN_SIZE = getMaxScreenLength();

    private static List<Callback> listeners = new ArrayList<>();

    private static Bitmap defaultAlbumArt;

    private static boolean isDefaultAlbumArt = true;
    private static Bitmap currentAlbumArt;
    private static int currentAccentColor;
    private static int currentBodyColor;

    public static void registerListener(Callback callback) {
        listeners.add(callback);
    }

    public static void unregisterListener(Callback callback) {
        if (listeners.contains(callback)) {
            listeners.remove(callback);
        }
    }

    public static Bitmap getCurrentAlbumArt() {
        return currentAlbumArt;
    }

    public static boolean isDefaultAlbumArt() {
        return isDefaultAlbumArt;
    }

    public static void updateAlbumArt(Context context, Song song) {
        final String albumArtUrl = song.getAlbumArtUrl();
        if (albumArtUrl != null && App.getPreferenceUtil().shouldDownloadImage(context)) {
            downloadAlbumArtBitmap(context, albumArtUrl);
            return;
        }

        updateListeners(getDefaultAlbumArt(context));
    }

    public static int getCurrentAccentColor() {
        return currentAccentColor;
    }

    public static int getCurrentBodyColor() {
        return currentBodyColor;
    }

    private static void updateListeners(Bitmap bitmap) {
        currentAlbumArt = bitmap;
        for (Callback listener : listeners) {
            listener.onAlbumArtReady(bitmap);
        }
    }

    private static void downloadAlbumArtBitmap(Context context, String url) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (context == null) {
                return;
            }

            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop())
                    .into(new SimpleTarget<Bitmap>(MAX_SCREEN_SIZE, MAX_SCREEN_SIZE) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            isDefaultAlbumArt = false;

                            Palette.Swatch swatch = Palette.from(resource).generate().getVibrantSwatch();
                            if (swatch == null) {
                                swatch = Palette.from(resource).generate().getMutedSwatch();
                            }
                            if (swatch != null) {
                                currentAccentColor = swatch.getRgb();
                                currentBodyColor = swatch.getBodyTextColor();
                            }

                            updateListeners(resource);
                        }
                    });
        });
    }

    private static Bitmap getDefaultAlbumArt(Context context) {
        if (defaultAlbumArt == null) {
            defaultAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.blank);
        }

        isDefaultAlbumArt = true;
        currentAccentColor = Color.BLACK;
        currentBodyColor = Color.WHITE;

        return defaultAlbumArt;
    }

    private static int getMaxScreenLength() {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public interface Callback {
        void onAlbumArtReady(Bitmap bitmap);
    }

}
