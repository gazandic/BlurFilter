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

    public Bitmap getImage1() {
        return image1;
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


    public Bitmap sharpen(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int val = 0;
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                NewColor c1 = new NewColor(image1.getPixel(x,y+1));
                Integer idx1 = map.get(c1.getGrayscale()) + (brightness ) ;
                NewColor c2 = new NewColor(image1.getPixel(x,y-1));
                Integer idx2 = map.get(c2.getGrayscale()) + (brightness ) ;
                NewColor c3 = new NewColor(image1.getPixel(x+1,y));
                Integer idx3 = map.get(c3.getGrayscale()) + (brightness ) ;
                NewColor c4 = new NewColor(image1.getPixel(x-1,y));
                Integer idx4 = map.get(c4.getGrayscale()) + (brightness ) ;
                val = 5 * grayscale - (idx1 + idx2 + idx3 + idx4);
                setPixel(pixels, index, val, mode, c, grayscale);

            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap blur(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int val = 0;
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                NewColor c1 = new NewColor(image1.getPixel(x,y+1));
                Integer idx1 = map.get(c1.getGrayscale()) + (brightness ) ;
                NewColor c2 = new NewColor(image1.getPixel(x,y-1));
                Integer idx2 = map.get(c2.getGrayscale()) + (brightness ) ;
                NewColor c3 = new NewColor(image1.getPixel(x+1,y));
                Integer idx3 = map.get(c3.getGrayscale()) + (brightness ) ;
                NewColor c4 = new NewColor(image1.getPixel(x-1,y));
                Integer idx4 = map.get(c4.getGrayscale()) + (brightness ) ;
                val = (int) grayscale / 5 + (idx1 / 5  + idx2 / 5 + idx3 / 5 + idx4 / 5) ;
                setPixel(pixels, index, val, mode, c, grayscale);
            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap robertscross (int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 0; y < height-1; ++y) {
            for (int x = 0; x < width-1; ++x) {
                int val = 0;
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                NewColor c1 = new NewColor(pixels[index+(width+1)]);
                Integer idx1 = map.get(c1.getGrayscale()) + (brightness ) ;
                NewColor c2 = new NewColor(pixels[index+1]);
                Integer idx2 = map.get(c2.getGrayscale()) + (brightness ) ;
                NewColor c3 = new NewColor(pixels[index+width]);
                Integer idx3 = map.get(c3.getGrayscale()) + (brightness ) ;
                int Gx = grayscale-idx1;
                int Gy = idx2-idx3;
//                val = (int) Math.sqrt((Gx * Gx)  + (Gy * Gy)) ;
                if ( Math.abs(Gx) > Math.abs(Gy) ) setPixel(pixels, index, Math.abs(Gx), 2, c, grayscale);
                else setPixel(pixels, index, Math.abs(Gy), 2, c, grayscale);
            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap freioperator(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int val = 0;
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                NewColor c1 = new NewColor(image1.getPixel(x,y+1));
                Integer idx1 = map.get(c1.getGrayscale()) + (brightness ) ;
                NewColor c2 = new NewColor(image1.getPixel(x,y-1));
                Integer idx2 = map.get(c2.getGrayscale()) + (brightness ) ;
                NewColor c3 = new NewColor(image1.getPixel(x+1,y));
                Integer idx3 = map.get(c3.getGrayscale()) + (brightness ) ;
                NewColor c4 = new NewColor(image1.getPixel(x-1,y));
                Integer idx4 = map.get(c4.getGrayscale()) + (brightness ) ;
                NewColor c5 = new NewColor(image1.getPixel(x+1,y+1));
                Integer idx5 = map.get(c5.getGrayscale()) + (brightness ) ;
                NewColor c6 = new NewColor(image1.getPixel(x+1,y-1));
                Integer idx6 = map.get(c6.getGrayscale()) + (brightness ) ;
                NewColor c7 = new NewColor(image1.getPixel(x-1,y-1));
                Integer idx7 = map.get(c7.getGrayscale()) + (brightness ) ;
                NewColor c8 = new NewColor(image1.getPixel(x-1,y+1));
                Integer idx8 = map.get(c8.getGrayscale()) + (brightness ) ;
                int Gx = (int) ((Math.sqrt(2) * idx1 ) - ( (Math.sqrt(2) * idx2 ) + idx5 + idx6 - idx8 - idx7));
                int Gy = (int) ((Math.sqrt(2) * idx3 ) - ( (Math.sqrt(2) * idx4 ) + idx5 - idx6 + idx8 - idx7 ));

                if ( Math.abs(Gx) > Math.abs(Gy) ) setPixel(pixels, index, Math.abs(Gx), 2, c, grayscale);
                else setPixel(pixels, index, Math.abs(Gy), 2, c, grayscale);
            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap matrixLoader(int mode, Matrix matrix, int level, int amount) {
        int[] pixels = new int[width * height];
        int[] pixelsface = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        image1.getPixels(pixelsface, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        int mark = 0;
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int val = 0;
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = c.getGrayscale();
                int x1 = -1, y1 = -1;
                List<Integer> idxArr = new ArrayList<>();
                for (int i=0;i<3;i++) {
                    x1 = -1;
                    for (int j=0;j<3;j++) {
                        NewColor c1 = new NewColor(image1.getPixel(x + x1, y + y1));
                        Integer idx = c1.getGrayscale() + (brightness);
                        idxArr.add(idx);
                        x1++;
                    }
                    y1++;
                }

                int result = 0;
                for (int i=0;i<amount;i++) {
                    int sum = 0;
                    for (int j=0;j<9;j++) {
                        if (matrix.getData(j) != 0) {
                            sum += matrix.getData(j) * idxArr.get(j);
                        }
                    }
                    matrix.swapToLeft();
                    if(result < sum * sum) {
                        result = sum * sum;
                    }
                    if(level == 1) matrix.swapToLeft();
                }
                result = (int) Math.sqrt(result);
                if (mark == 0 && result > 5) {
                    mark = index;
                }
                setPixel(pixels, index, result, 2, c, grayscale);
            }
        }
        //new
        int[] arr = new int[width * height];
        for (int i = 0; i < width * height; ++i) {
            arr[i] = 0;
        }
        for (int y = mark / width; y < height-1; ++y) {
            for (int x = mark % width; x < width - 1; ++x) {
                int index = y * width + x;
                if (arr[index] < 1) {
                    NewColor c = new NewColor(pixels[index]);
                    int grayscale = c.getGrayscale();
                    if (grayscale > 70) {
                        int x1 = -1, y1 = -1;
                        List<Integer> idxArr = new ArrayList<>();
                        for (int i = 0; i < 3; i++) {
                            x1 = -1;
                            for (int j = 0; j < 3; j++) {
                                NewColor c1 = new NewColor(pixels[index + x1 + (y1*width)]);
                                Integer idx = c1.getGrayscale() + (brightness);
                                idxArr.add(idx);
                                x1++;
                            }
                            y1++;
                        }
                        int next = 4;
                        for (int j = 0; j < 9; j++) {
                            if (idxArr.get(j) > 70) {
                                next = j;
                            }
                        }
                        arr[index]++;
                        int[] arah = new int[9];
                        List<Integer> urutan = new ArrayList<>();

                        for (int i = 0 ; i< 9 ; i++) {
                            arah[i] = 0;
                        }

                        if (next != 4) {
                            arah[next]++;
                            urutan.add(next);
//                            pixels[index] = Color.argb(255, 0, 255, 255);
                        }
                        int xx =x, yy =y;
                        int indexs= 0;
                        while (next != 4 && indexs != index) {

                            int x2 = -1;
                            int y2 = -1;
                            xx += (next % 3) - 1;
                            yy +=  (next-(next % 3)) / 3 - 1;
                            indexs = yy * width + xx;
                            idxArr = new ArrayList<>();
                            for (int i = 0; i < 3; i++) {
                                x2 = -1;
                                for (int j = 0; j < 3; j++) {
                                    if (indexs + x2 + (y2 * width) <= (width-2) * (height-2) && (indexs + x2 + (y2 * width)) > 0) {
                                        NewColor c1 = new NewColor(pixels[(indexs + x2 + (y2 * width))]);
                                        Integer idx = c1.getGrayscale() + (brightness);
                                        idxArr.add(idx);
                                    }
                                    else {
                                        idxArr.add(0);
                                    }
                                    x2++;
                                }
                                y2++;
                            }
                            int prevnext = next;

                            for (int j = 0; j < 9; j++) {
                                if (j != 4 && idxArr.get(j) > 70 && !reverse(3, j, prevnext) && prior(j,prevnext) > prior(next,prevnext)) {
                                    next = j;
                                } else if (j == 4) {
                                    next = j;
                                }
                            }

                            if (next != 4) {
                                arah[next]++;
                                urutan.add(next);
                                if (indexs <= arr.length && indexs > 0) {
                                    arr[ indexs]++;
                                    pixels[indexs] = Color.argb(255, 0, 255, 255);

                                }
                            }

                        }
                        Shape shape = new Shape(urutan, arah);
                        if (shape.checkRound()) {
                            Log.d("bentuknyaa","bundar");
                        }
                    }
                }

            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    private boolean reverse(int size, int a, int b) {
        int x1 = a % size;
        int y1 = (a-x1) / size;
        x1--; y1--;
        int x2 = b % size;
        int y2 = (b-x2) / size;
        x2--; y2--;
//        Log.d("dekat","x1:" + x1 + " y1:" + y1 +"x2:" + x2 + " y2:" + y2 );
        return (-x1 == x2 && -y1 == y2);
    }

    private int prior(int a, int b) {
        int point = 0;
        int x1 = a % 3;
        int y1 = (a-x1) / 3;
        x1--; y1--;
        int x2 = b % 3;
        int y2 = (b-x2) / 3;
        x2--; y2--;
//        Log.d("dekat","x1:" + x1 + " y1:" + y1 +"x2:" + x2 + " y2:" + y2 );
        if (a != b ) {
            point++;
        }

        if (-x1 == x2) {
            point++;
        }

        if (-y1 == y2) {
            point++;
        }
        return point;
    }
    private int getNewIndex(int size, int a, int index) {
        int x1 = a % size;
        int y1 = (a-x1) / size;
        x1--; y1--;
        return index + x1 + (y1 * width);
    }

    public Bitmap homogenBoundary(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int x1 = -1, y1 = -1, max = 0;

                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                for (int i=0;i<3;i++) {
                    x1 = -1;
                    for (int j=0;j<3;j++) {
                        NewColor c1 = new NewColor(image1.getPixel(x+x1,y+y1));
                        Integer idx = map.get(c1.getGrayscale()) + (brightness ) ;
                        Integer diff = Math.abs(idx-grayscale);
                        if (max < diff) {
                            max = diff;
                        }
                        x1++;
                    }
                    y1++;
                }
                setPixel(pixels, index, max, 2, c, grayscale);

            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap boundary(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int x1 = -1, y1 = -1, avg = 0;
                int max = 0;
                for (int i=0;i<4;i++) {
                    NewColor c1 = new NewColor(image1.getPixel(x+x1,y+y1));
                    Integer idx1 = map.get(c1.getGrayscale()) + (brightness ) ;
                    NewColor c2 = new NewColor(image1.getPixel(x-x1,y-y1));
                    Integer idx2 = map.get(c2.getGrayscale()) + (brightness ) ;
                    Integer newidx = Math.abs(idx1-idx2);
                    if (max < newidx) {
                        max = newidx;
                    }
                    x1++;
                    if (x1 > 1) {
                        x1 = -1;
                        y1++;
                    }
                }
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                setPixel(pixels, index, max, 2, c, grayscale);

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
