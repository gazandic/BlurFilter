package com.example.gazandic.blurfilter.ViewModel;

import android.databinding.adapters.SeekBarBindingAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * FindMe list view model that used on findme fragment.
 *
 * @author Gazandi Cahyadarma <gazandi@urbanindo.com>
 * @since 2016.07.25
 */
public class ColorListViewModel {
    private MainActivityListener mainActivityListener;

    public ColorListViewModel (MainActivityListener mainActivityListener) {
        super();
        this.mainActivityListener = mainActivityListener;
    }

    public Button.OnClickListener onAddFindMeButtonClicked = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onAddFindMeButtonClicked();
        }
    };

    public Button.OnClickListener onSmooth= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onSmooth();
        }
    };


    public Button.OnClickListener onBlur= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onBlur();
        }
    };

    public Button.OnClickListener onPrewitt8= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onPrewitt8();
        }
    };


    public Button.OnClickListener onPrewitt= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onPrewitt();
        }
    };

    public Button.OnClickListener onFrei= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onFrei();
        }
    };

    public Button.OnClickListener onSobel= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onSobel();
        }
    };


    public Button.OnClickListener faceDetection= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onFaceDetect();
        }
    };

    public Button.OnClickListener onRobert= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onRobert();
        }
    };

    public Button.OnClickListener onEqualization= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onEqualization();
        }
    };
    public Button.OnClickListener onKirsch = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onKirsch();
        }
    };

    public Button.OnClickListener onSharpen= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onSharpen();
        }
    };

    public Button.OnClickListener onRobinson3= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onRobinson3();
        }
    };


    public Button.OnClickListener onRobinson5= new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onRobinson5();
        }
    };


    public ImageView.OnClickListener onImageSaved = new ImageView.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivityListener.onImageSaved();
        }
    };

    public SeekBarBindingAdapter.OnProgressChanged onBrChange = new SeekBarBindingAdapter.OnProgressChanged() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mainActivityListener.onBrightnessChanged(i);
        }
    };


    public SeekBarBindingAdapter.OnProgressChanged onCoChange = new SeekBarBindingAdapter.OnProgressChanged() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mainActivityListener.onContrastChanged(i);
        }
    };


    public SeekBarBindingAdapter.OnProgressChanged onHistChange = new SeekBarBindingAdapter.OnProgressChanged() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mainActivityListener.onHistChanged(i);
        }
    };
}
