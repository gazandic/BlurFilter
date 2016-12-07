package com.example.gazandic.blurfilter.ViewModel;

/**
 * Created by gazandic on 31/08/16.
 */
public interface MainActivityListener {
    void onAddFindMeButtonClicked();

    void onImageSaved();

    void onBrightnessChanged(int i);

    void onContrastChanged(int i);

    void onHistChanged(int i);

    void onSmooth();

    void onPrewitt8();

    void onPrewitt();

    void onFrei();

    void onSobel();

    void onRobert();

    void onEqualization();

    void onKirsch();

    void onRobinson3();

    void onRobinson5();

    void onFaceDetect();

    void onSharpen();

    void onBlur();

}
