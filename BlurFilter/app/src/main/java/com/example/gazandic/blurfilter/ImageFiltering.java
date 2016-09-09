package com.example.gazandic.blurfilter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gazandic on 31/08/16.
 */
public class ImageFiltering implements Parcelable {

    private Bitmap image1;
    private List<Integer> grayscales;
    private List<Integer> afterTransform;
    private HashMap<Integer, Integer> map;
    private int height;
    private int width;
    private int brightness = 0;
    private int histogram = 0;
    private double contrast = 1;

    public ImageFiltering() {
        image1 = null;
    }

    protected ImageFiltering(Parcel in) {
        image1 = in.readParcelable(Bitmap.class.getClassLoader());
        height = in.readInt();
        width = in.readInt();
        brightness = in.readInt();
        histogram = in.readInt();
        contrast = in.readDouble();
    }

    public static final Creator<ImageFiltering> CREATOR = new Creator<ImageFiltering>() {
        @Override
        public ImageFiltering createFromParcel(Parcel in) {
            return new ImageFiltering(in);
        }

        @Override
        public ImageFiltering[] newArray(int size) {
            return new ImageFiltering[size];
        }
    };

    public List<Integer> getList(){
        return grayscales;
    }

    public void filter(String selectedImagePath) {
        long start = System.currentTimeMillis();
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            image1 = BitmapFactory.decodeFile(selectedImagePath, options);
            initData();
            height = options.outHeight;
            width = options.outWidth;
            fillMatrix();
            filterBuram();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    private void initData() {
        grayscales = new ArrayList<>();
        for (int i=0;i<257;i++) {
            grayscales.add(0);
        }
        afterTransform = new ArrayList<>();
        for (int i=0;i<257;i++) {
            afterTransform.add(0);
        }
        map = new HashMap<>(257);

    }

    public void setBrightness(int i) {
        brightness = i - 50;
    }

    public void setContrast(double value) {
        contrast = Math.pow((100 + value) / 100, 2) / 3;
    }

    public void setHistogram(int value) {
        histogram = value - 50;
    }

    private void fillMatrix() {
        try {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    NewColor c = new NewColor(image1.getPixel(x, y));
                    grayscales.set(c.grayScale(), grayscales.get(c.grayScale()) + 1);
                }
            }
        }
        catch (Exception e) {
            Log.d("ImageFiltering", e.getMessage());
        }
    }


    private void filterBuram() {
        int it = 0;
        while (grayscales.get(it) < 1) {
            it++;
        }
        int cdfmin = grayscales.get(it);
        int Area = height * width;
        int cdf = cdfmin ;
        transformGrayscale(it, cdf, cdfmin, Area);
        for (int i=it+1;i<grayscales.size();i++) {
            if (grayscales.get(i) > 0) {
                cdf = cdf + grayscales.get(i);
                transformGrayscale(i, cdf, cdfmin, Area);
            }
        }
    }

    public void transformGrayscale(int index, int cdf, int cdfmin, int Area) {
        int afterindex = 0;
        try {
            afterindex = Math.round(255 * (cdf - cdfmin) / (Area - cdfmin));
            afterTransform.set(afterindex, afterTransform.get(afterindex) + 1);
            if (!map.containsKey(index)) map.put(index, afterindex);
        }
        catch (Exception ex) {
            Log.d("ImageFiltering", ex.getMessage());
        }
    }

    public Bitmap retBitmapAfterFilterGrayscale(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                Integer idx = map.get(grayscale) + (brightness );
                setPixel(pixels, index, idx, mode, c, grayscale);
            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap smoothing(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int x1 = -1, y1 = -1, avg = 0;
                for (int i=0;i<3;i++) {
                    x1 = -1;
                    for (int j=0;j<3;j++) {
                        NewColor c1 = new NewColor(image1.getPixel(x+x1,y+y1));
                        Integer idx = map.get(c1.getGrayscale()) + (brightness ) ;
                        avg += idx;
                        x1++;
                    }
                    y1++;
                }
                avg /= 9;
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                setPixel(pixels, index, avg, mode, c, grayscale);

            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    private int setInt(int color){
        if (color > 255) color = 255;
        if (color < 0) color = 0;
        return color;
    }

    private void setPixel(int[] pixels, int index, int val, int mode, NewColor c, int grayscale){
        if (mode == 1) {
            Integer diff = val - grayscale;
            int red = c.getRed() + diff;
            red = setContrastInt(red);
            red = setInt(red);

            int green = c.getGreen() + diff;
            green = setContrastInt(green);
            green = setInt(green);

            int blue = c.getBlue() + diff;
            blue = setContrastInt(blue);
            blue = setInt(blue);
            pixels[index] = Color.rgb(red, green, blue);
        }
        else {
            val = setInt(val);
            pixels[index] = Color.rgb(val, val, val);
        }
    }

    private int getGrayscaleHisto(int gray) {
        int grayscale = gray;
        int iterasi = 0;
        if (histogram >= 0) {
            while (iterasi < histogram) {
                grayscale++;
                if (grayscale > 255) {
                    grayscale = 0;
                }
                if (map.containsKey(grayscale)) {
                    iterasi++;
                }
            }
            return grayscale;
        }
        else {
            while (iterasi > histogram) {
                grayscale--;
                if (grayscale < 0) {
                    grayscale = 255;
                }
                if (map.containsKey(grayscale)) {
                    iterasi--;
                }
            }
            return grayscale;

        }
    }

    private int setContrastInt(int color) {
        return (int)(((((color / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
