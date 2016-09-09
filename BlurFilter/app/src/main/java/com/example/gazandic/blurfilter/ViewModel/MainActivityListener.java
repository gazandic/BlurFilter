package com.example.gazandic.blurfilter.ViewModel;

/**
 * Created by gazandic on 31/08/16.
 */
public interface MainActivityListener {

    /**
     * Called to open createFindMeActivity
     */
    void onAddFindMeButtonClicked();


    /**
     * Called to open createFindMeActivity
     */
    void onImageSaved();


    /**
     * Called to open createFindMeActivity
     */
    void onBrightnessChanged(int i);


    /**
     * Called to open createFindMeActivity
     */
    void onContrastChanged(int i);

    /**
     * Called to open createFindMeActivity
     */
    void onHistChanged(int i);

    /**
     * Called to open createFindMeActivity
     */
    void onSmooth();

}