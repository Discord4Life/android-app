package me.echeung.listenmoeapi.interfaces;

public interface FavoriteSongListener {
    void onFailure(final String result);
    void onSuccess(final boolean favorited);
}