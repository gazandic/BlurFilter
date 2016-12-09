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
import android.graphics.Point;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.style.LineHeightSpan;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.widget.Button;

import org.antlr.runtime.tree.Tree;

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
    private List<NewColor> lc;
    private List<Double> real;
    private List<Double> imag;

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


    private void processTree(List<List<Integer>> lisTetangga) {
        int iterator = 0;
        for (List<Integer> liz:lisTetangga) {
//            processTree2(liz,lisTetangga,iterator);
            iterator++;
        }
    }

//    private void processTree2(List<Integer> liz, List<List<Integer>> lisTetangga, int iterator) {
//        for (Integer is : liz) {
//            if (lisTetangga.get(is) != null && lisTetangga.get(is).size() > 0) {
//                processTree2(lisTetangga.get(is), lisTetangga, is);
//                for (Integer iz:lisTetangga.get(is)) {
//                    liz.add(iz);
//                }
//                lisTetangga.set(iterator,liz);
//                lisTetangga.remove(is);
//            }
//        }
//    }

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

    public Bitmap retBitmap(int mode) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                Integer idx = grayscale + (brightness );
                setPixel(pixels, index, idx, mode, c, grayscale);
            }
        }

        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap retFFTBitmap(int mode) {
        int[] pixels = new int[width * height] ;
        lc = new ArrayList<>();

        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                lc.add(c);
            }
        }

        int wid = powerOf2Pm(width, 0, 0);
        int hei = powerOf2Pm(height, 0, 0);
        fft2d(wid, hei, 1);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int index = y * width + x;
                NewColor c = lc.get(index);
                int grayscale = c.getGrayscale();
                int imaggrayscale = c.getImaginarygrayscale();
                Double d = Math.sqrt((grayscale * grayscale) + (imaggrayscale * imaggrayscale));
                Integer idx = d.intValue();

                idx = 255 - idx;

                setPixel(pixels, index, idx, 2, c, grayscale);
            }
        }

        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public int fft2d(int nx, int ny, int dir) {
        int i,j;
        Integer m = 0, twopm = 0;
        real = new ArrayList<>();
        imag = new ArrayList<>();
        for (i = 0; i < nx; i++) {
            real.add(0.0);
            imag.add(0.0);
        }
        m = powerOf2(nx, m, twopm);
        if (m == 0)
            return 0;
        for (j = 0; j < ny; j++) {
            for (i = 0; i < nx; i++) {
                int index = j * width + i;
                int value = lc.get(index).getGrayscale();
                Double d = Double.valueOf(value);
                int imagvalue = lc.get(index).getImaginarygrayscale();
                Double d2 = Double.valueOf(imagvalue);
                real.set(i, d);
                imag.set(i, d2);
            }
            FFT(dir,m);
            for (i=0;i<nx;i++) {
                int index = j * width + i;
                int rea = real.get(i).intValue();
                int ima = imag.get(i).intValue();
                NewColor c = lc.get(index);
                c.setImaginarygrayscale(ima);
                c.setGrayscale(rea);
                lc.set(index, c);
            }
        }

        real = new ArrayList<>();
        imag = new ArrayList<>();

        for (i = 0; i < ny; i++) {
            real.add(0.0);
            imag.add(0.0);
        }
        m = powerOf2(ny, m, twopm);
        if (m == 0)
            return 0;
        for (i = 0; i < nx; i++) {
            for (j = 0; j < ny; j++) {
                int index = j * width + i;
                int value = lc.get(index).getGrayscale();
                Double d = Double.valueOf(value);
                int imagvalue = lc.get(index).getImaginarygrayscale();
                Double d2 = Double.valueOf(imagvalue);
                real.set(j, d);
                imag.set(j, d2);
            }
            FFT(dir, m);
            for (j = 0; j < ny; j++) {
                int index = j * width + i;
                int rea = real.get(j).intValue();
                int ima = imag.get(j).intValue();
                NewColor c = lc.get(index);
                c.setImaginarygrayscale(ima);
                c.setGrayscale(rea);

                lc.set(index, c );
            }
        }
        return 1;
    }


    public int FFT(int dir, int m) {
        int nn,i,i1,j,k,i2,l,l1,l2;
        double c1,c2,tx,ty,t1,t2,u1,u2,z;
        nn = 1;
        for (i=0;i<m;i++)
            nn *= 2;

        /* Do the bit reversal */
        i2 = nn >> 1;
        j = 0;
        for (i = 0;i < nn-1;i++) {
            if (i < j) {
                tx = real.get(i);
                ty = imag.get(i);
                real.set(i, real.get(j));
                real.set(i, imag.get(j));
                real.set(j, tx);
                imag.set(j, ty);
            }
            k = i2;
            while (k <= j) {
                j -= k;
                k >>= 1;
            }
            j += k;
        }
//
        /* Compute the FFT */
        c1 = -1.0;
        c2 = 0.0;
        l2 = 1;
        for (l = 0;l < m;l++) {
            l1 = l2;
            l2 <<= 1;
            u1 = 1.0;
            u2 = 0.0;
            for (j = 0;j < l1;j++) {
                for (i = j;i < nn;i+=l2) {
                    i1 = i + l1;
                    t1 = (u1 * real.get(i1)) - (u2 * imag.get(i1));
                    t2 = (u1 * imag.get(i1)) + (u2 * real.get(i1));
                    real.set(i1, real.get(i) - t1);
                    imag.set(i1, imag.get(i) - t2);
                    real.set(i, real.get(i) + t1);
                    imag.set(i, imag.get(i) + t2);
                }
                z =  (u1 * c1) - (u2 * c2);
                u2 = (u1 * c2) + (u2 * c1);
                u1 = z;
            }
            c2 = Math.sqrt((1.0 - c1) * 0.5);
            if (dir == 1)
                c2 = -c2;
            c1 = Math.sqrt((1.0 + c1) * 0.5);
        }

        return 1;
    }

    public int powerOf2(int n, int m, int twopm) {
        if (n <= 1) {
            m = 0;
            twopm = 1;
            return m;
        }
        m = 1;
        twopm = 2;
        do {
            m++;
            twopm *= 2;
        } while (2*twopm <= n);
        if (twopm !=n)
            return 0;
        return m;
    }

    public int powerOf2Pm(int n, int m, int twopm) {
        if (n <= 1) {
            m = 0;
            twopm = 1;
            return twopm;
        }
        m = 1;
        twopm = 2;
        do {
            m++;
            twopm *= 2;
        } while (2*twopm <= n);
        return twopm;
    }

    private boolean inShape(Shape s, Shape s1) {
        return (((s.maxX <= s1.maxX && s.maxX >= s1.minX ) || (s.minX <= s1.maxX && s.minX >= s1.minX ))
                && ((s.maxY <= s1.maxY && s.maxY >= s1.minY) || (s.minY <= s1.maxY && s.minY >= s1.minY)));
    }

    private Shape mergeShape(Shape s1, Shape s2) {
        Shape s = new Shape();
        if (s1.minY < s2.minY ) s.minY = s1.minY;
        else s.minY = s2.minY;
        if (s1.maxY > s2.maxY ) s.maxY = s1.maxY;
        else s.maxY = s2.maxY;
        if (s1.minX < s2.minX ) s.minX = s1.minX;
        else s.minX = s2.minX;
        if (s1.maxX > s2.maxX ) s.maxX = s1.maxX;
        else s.maxX = s2.maxX;
        return s;
    }

    private void setArrIndex(int[] pixels, int[] arrLoader, List<Shape> minMax) {
        int shapecount = 1;
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width-1; ++x) {
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = getGrayscaleHisto(c.getGrayscale());
                Integer idx = map.get(grayscale) + (brightness );
                List<Integer> idxArr = new ArrayList<>();
                double cb = (-0.169 * c.getRed() ) - ( 0.332 * c.getGreen() ) + (0.5 * c.getBlue());
                double cr = (0.5 * c.getRed() ) - ( 0.419 * c.getGreen() ) - (0.081 * c.getBlue());
                if (((cr > 15 && cr < 35) || (cr+cb < 6 && cr+cb > -6 && cb < 11 && cb > -37) ) && (cb < 11 && cb > -37 && cr > 15 && cr < 35)) {
                    arrLoader[index] = shapecount;
                    int x1, y1=-1;
                    boolean found =false;
                    boolean setted = false;
                    for (int i=0;i<2;i++) {
                        x1 = -1;
                        for (int j=0;j<3;j++) {
                            if (j==1 && i==1) break;
                            if(arrLoader[index+x1+(y1*width)] > 0) {
                                if (!setted) {
                                    arrLoader[index] = arrLoader[index + x1 + (y1 * width)];
                                    found = true;
                                    setted = true;
                                    break;
                                }
                            }
                            x1++;
                        }
                        if (found) break;
                        y1++;
                    }
                    if (!found) {
                        shapecount++;
                        Shape s = new Shape();
                        s.maxX = x;
                        s.minX = x;
                        s.maxY = y;
                        s.minY = y;
                        minMax.add(s);
                    }
                    else {
                        boolean change = false;
                        int idxx = arrLoader[index];
                        Shape s = minMax.get(idxx-1);
                        if (s.maxY < y) {
                            s.maxY = y;
                            change = true;
                        }
                        else if (s.minY > y) {
                            s.minY = y;
                            change = true;
                        }
                        if (s.maxX < x) {
                            s.maxX = x;
                            change = true;
                        }
                        else if (s.minX > x) {
                            s.minX = x;
                            change = true;
                        }
                        if (change) minMax.set(idxx-1,s);
                    }
                }
                else {
                    arrLoader[index] = 0;
                }

            }
        }
//        processTree(lisTetangga);
//        Log.d("lisTetangga",String.valueOf(lisTetangga.size()));
        Log.d("shapecount",String.valueOf(shapecount));
        Log.d("shapecount2",String.valueOf(minMax.size()));
        for (int i = 0; i < 3; i++) {
            mergeShapes(minMax,shapecount);
        }
        Log.d("shapecount3",String.valueOf(shapecount));
        Log.d("shapecount4",String.valueOf(minMax.size()));
        processShapes(arrLoader, minMax);
    }

    private void processShapes(int[] arrLoader,List<Shape> minMax) {
        List<Integer> notchoosen = new ArrayList<>();
        for (int i = 0; i < minMax.size() ; i++) {
            Shape s = minMax.get(i);
            if (s!=null) {
                int lengthY = s.maxY - s.minY;
                int lengthX = s.maxX - s.minX;
                if (lengthX > 0 && lengthY > 0) {
                    double ratio = lengthY / lengthX;
                    if ((ratio > 2.0 || ratio < 1.0) && (lengthX < width / 25 || lengthY < height / 25)) {
                        notchoosen.add(i + 1);
                        minMax.remove(i);
                    }

                } else {
                    Log.d("ga mutu gaz", lengthX + "," + lengthY);
                    notchoosen.add(i + 1);
                    minMax.remove(i);
                }
            }
        }
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width - 1; ++x) {
                int index = y * width + x;
                for (Integer i: notchoosen) {
                    if (i==arrLoader[index]) {
                        arrLoader[index] = 0;
                        break;
                    }
                }
            }
        }

    }
    private void mergeShapes(List<Shape> minMax, int shapecount) {
        int iterasi = 0;
        for (int j= 0; j <  minMax.size() - 1 ; j++) {
            Shape s = minMax.get(j);
            if (s != null) {
                for (int i = j+1; i < minMax.size(); i++) {
                    Shape s1 = minMax.get(i);
                    int indexs = minMax.indexOf(s);
                    if (s != null && (s.minY != s1.minY || s.maxY != s1.maxY || s.minX != s1.minX || s.maxX != s1.maxX) && s1 !=null && indexs > -1) {
                        int diffx = Math.abs(s.maxX - s1.maxX);
                        if (Math.abs(s.minX - s1.minX) > diffx) diffx = Math.abs(s.minX - s1.minX);
                        int diffy = Math.abs(s.maxY - s1.maxY);
                        if (Math.abs(s.minY - s1.minY) > diffy) diffy = Math.abs(s.minY - s1.minY);
                        if ((diffx < width/20 && diffx > 0) && (diffy < height/20 && diffy > 0)) {
                            minMax.set(indexs, mergeShape(s, s1));
                            minMax.remove(s1);
                            shapecount--;
                        }
                        else if (inShape(s1,s) || inShape(s,s1)) {
                            minMax.set(indexs, mergeShape(s, s1));
                            minMax.remove(s1);
                            shapecount--;
                        }
                    }
                }
            }
            iterasi++;
        }
    }

    public void matrixLoader(int[] pixels, List<Integer> histogram, int mark, int mode, Matrix matrix, int level, int amount) {
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
                if (result>255) {
                    result = 255;
                }
                else if (result <0 ) {
                    result = 0;
                }

                histogram.set(result, histogram.get(result)+1);
                setPixel(pixels, index, result, 2, c, grayscale);
            }
        }
    }

    public void otsuPixels(int[] pixels, double d) {
        for (int y = 1; y < height-1; ++y) {
            for (int x = 1; x < width - 1; ++x) {
                int val = 0;
                int index = y * width + x;
                NewColor c = new NewColor(pixels[index]);
                int grayscale = c.getGrayscale();
                if (grayscale < d ) {
                    grayscale = 0;
                }
                else if ((grayscale > d)){
                    grayscale = 255;
                }
//                if (grayscale < d && d < 200.0) {
//                    grayscale = 255;
//                }
//                else if ((grayscale > d && d < 200.0)){
//                    grayscale = 0;
//                }
//                histogram2.set(grayscale, histogram.get(grayscale)+1);
                setPixel(pixels, index, grayscale, 2, c, grayscale);
            }
        }
    }

    private void traceShapes(int[] pixels, int mark, int[] arr, int[] arrLoader, List<Shape> shapes) {
        if (mark/width < 1.0) {
            mark = width;
        }
        for (int y = mark / width; y < height-1; ++y) {
            for (int x = mark % width; x < width - 1; ++x) {
                if (x==0) x=1;
                int index = y * width + x;
                if (arr[index] < 1
                        && arrLoader[index] > 0
                    ) {
                    NewColor c = new NewColor(pixels[index]);
                    int grayscale = c.getGrayscale();
                    if (grayscale > 70) {
                        int minX = x, maxX = x;
                        int minY = y, maxY = y;
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
                            if (j != 4 && idxArr.get(j) > 70) {
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
                        xx += (next % 3) - 1;
                        yy +=  (next-(next % 3)) / 3 - 1;
//                            Log.d("TAG",next+ ","+xx+ ","+yy);
                        if (xx > width -2 ) xx =(width - 2);
                        if (yy > (height - 2)) yy = (height - 2);
                        if (xx < 2) xx =1;
                        if (yy < 2) xx=1;
                        if (xx < minX) {
                            minX = xx;
                        }
                        if (xx > minX) {
                            maxX = xx;
                        }
                        if (yy < minY) {
                            minY = yy;
                        }
                        if (xx > minX) {
                            maxY = yy;
                        }
                        indexs = yy * width + xx;
//                        Log.d("TAG1",next+ ","+xx+ ","+yy);
                        while (next != 4 && indexs != index && arr[indexs] < 1) {
                            int y2 = -1;
                            idxArr = new ArrayList<>();
                            for (int i = 0; i < 3; i++) {
                                int x2 = -1;
                                for (int j = 0; j < 3; j++) {
                                    NewColor c1 = new NewColor(pixels[(indexs + x2 + (y2 * width))]);
                                    Integer idx = c1.getGrayscale() + (brightness);
                                    idxArr.add(idx);
                                    x2++;
                                }
                                y2++;
                            }
                            int prevnext = next;
                            next = 4 ;
                            for (int j = 0; j < 9; j++) {
//                                && prior(j,prevnext) > prior(next,prevnext)
                                if (j != 4 && idxArr.get(j) > 70 && !reverse(3, j, prevnext) )
                                    next = j;
                            }

                            arr[indexs]++;

                            if (next != 4) {
//                                if ((prevnext == 5 && next==1) || (prevnext == 1 && next==5)) next = 2;
//                                else if ((prevnext == 5 && next==7) || (prevnext == 7 && next==5))next = 8;
//                                else if ((prevnext == 5 && next==7) || (prevnext == 1 && next==5))) next = 8;
//                                else if ((prevnext == 1 && next==5) next = 8;
                                arah[next]++;

                                urutan.add(next);
                                pixels[indexs] = Color.argb(255, 0, 255, 255);
                            }

                            xx += (next % 3) - 1;
                            yy +=  (next-(next % 3)) / 3 - 1;
//                            Log.d("TAG",next+ ","+xx+ ","+yy);
                            if (xx > width -2 ) xx =(width - 2);
                            if (yy > (height - 2)) yy = (height - 2);
                            if (xx < 2) xx =1;
                            if (yy < 2) xx=1;
                            if (xx < minX) {
                                minX = xx;
                            }
                            if (xx > minX) {
                                maxX = xx;
                            }
                            if (yy < minY) {
                                minY = yy;
                            }
                            if (xx > minX) {
                                maxY = yy;
                            }
                            indexs = yy * width + xx;

                        }
                        Shape shape = new Shape(urutan, arah);
                        shape.minX = minX;
                        shape.minY = minY;
                        shape.maxX = maxX;
                        shape.maxY = maxY;
                        shapes.add(shape);
                    }
                }

            }
        }
    }

    private void setFacesRect(int[] pixels, List<Shape> MaxMinShape,List<Shape> shapes) {
        int iterasi = 0;

        int value = MaxMinShape.size();
        for (int i = 0; i < 2; i++) {
            mergeShapes(MaxMinShape,value);
        }
        int[] sum = new int[MaxMinShape.size()];
        for (Shape shape : MaxMinShape) {
            int sums = 0;
            for (Shape shape1 : shapes) {
                if (inShape(shape1,shape) || inShape(shape,shape1) )  {
                    sums++;
                }
            }
            sum[iterasi] = sums;
            iterasi ++;
        }

        for (int i = 0; i < MaxMinShape.size() ; i++) {
            Shape s = MaxMinShape.get(i);
            if (s!=null) {
                if (sum[i] < 5) {
                    MaxMinShape.remove(s);
                    i--;
                }
                else {

                    int lengthX = s.maxX - s.minX;
                    int lengthY = s.maxY - s.minY;
                    lengthY = (int) Math.floor(1.618 * lengthX);
                    s.maxY = s.minY + lengthY;
                    MaxMinShape.set(i,s);
                }

            }
        }
        value = MaxMinShape.size();
        mergeShapes(MaxMinShape,value);
        for (int i = 0; i < MaxMinShape.size() ; i++) {
            Shape s = MaxMinShape.get(i);
            if (s!=null) {
                int lengthX = s.maxX - s.minX;
                int lengthY = s.maxY - s.minY;
                if (lengthY / lengthX > 2.0 || lengthY/lengthX < 1.0) {
                    MaxMinShape.remove(s);
                    i--;
                }
                else {
                    lengthY = (int) Math.floor(1.618 * lengthX);
                    s.maxY = s.minY + lengthY;
                    MaxMinShape.set(i,s);
                }

            }
        }
        List<Shape> MaxMinShape2 = new ArrayList<>();
        for(Shape s : MaxMinShape) {
            MaxMinShape2.add(s);
        }
        for (int i = 0; i < MaxMinShape2.size() ; i++) {
            Shape shape = MaxMinShape2.get(i);
            int sums = 0;
            int shapewidth = (shape.maxX - shape.minX)/7 ;
            int shapeHeight = (shape.maxY - shape.minY)/7;
            if(shape != null) {
                for (Shape shape1 : shapes) {
                    if (inShape(shape1, shape) && Math.abs(shape1.minY - ((shape.maxY - shape.minY) / 3) -
                            shape.minY) < shapeHeight  && Math.abs(shape1.minX - ((shape.maxX - shape.minX) / 5) -
                            shape.minX) < shapewidth ) {
                        sums++;
                    } else if (inShape(shape1, shape) && Math.abs(shape1.minY - ((shape.maxY - shape.minY) / 3) -
                            shape.minY) < shapeHeight  && Math.abs(shape.maxX - ((shape.maxX - shape.minX) / 5) - shape1.maxX) < shapewidth) {
                        sums++;
                    } else if (inShape(shape1, shape) && Math.abs(shape1.minY - ((shape.maxY - shape.minY) / 2) -
                            shape.minY) < shapeHeight && Math.abs(shape.maxX - ((shape.maxX - shape.minX) / 2) - shape1.maxX) < shapewidth ) {
                        sums++;
                    } else if (inShape(shape1, shape) && Math.abs(shape1.minY - ((shape.maxY - shape.minY) / 3) -
                            shape.minY) < shapeHeight && Math.abs(shape.maxX - ((shape.maxX - shape.minX) / 2) - shape1.maxX) < shapewidth ) {
                        sums++;
                    }
                }
                if (sums < 6) {
                    MaxMinShape2.remove(i);
                }
            }
        }
        if (MaxMinShape2.size() > 0) {
            MaxMinShape = new ArrayList<>();
            for (Shape s: MaxMinShape2) {
                MaxMinShape.add(s);
            }
        }
        for (Shape shape : MaxMinShape) {
            for (int y = shape.minY; y <= shape.maxY; ++y) {
                for (int x = shape.minX; x <= shape.maxX; ++x) {
                    if (y == shape.minY || y == shape.maxY || x == shape.minX || x == shape.maxX) {
                        if (x > width-2 ) x = width-2;
                        if (y > height-2 ) y = height-2;
                        pixels[y * width + x] = Color.argb(255, 255, 0, 0);
                    }
                }
            }
        }
    }

    public Bitmap matrixLoader(int mode, Matrix matrix, int level, int amount) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pixelsret = new int[width * height];
        image1.getPixels(pixelsret, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());

        List<Shape> MaxMinShape = new ArrayList<>();

        List<Integer> histogram;
        histogram = new ArrayList<>();

        for (int i=0;i<256;i++) {
            histogram.add(0);
        }

        int[] arrLoader = new int[width * height];
        for (int i = 0; i < width * height; ++i) {
            arrLoader[i] = 1;
        }
//        setArrIndex(pixels,arrLoader, MaxMinShape);
        int mark = 0;
        matrixLoader(pixels, histogram, mark, mode, matrix, level, amount);
//
        double d = otsu(histogram, width*height);
        Log.d("TAG",String.valueOf(d));
        otsuPixels(pixels, d);

        pixels = thinImage(pixels);
        //new
        int[] arr = new int[width * height];
        for (int i = 0; i < width * height; ++i) {
            arr[i] = 0;
        }
        List<Shape> shapes = new ArrayList<Shape>();
        traceShapes(pixels, mark, arr, arrLoader, shapes);
        Log.d("shape",String.valueOf(shapes.size()));
//        setFacesRect(pixels,shapes);
//        setFacesRect(pixelsret,MaxMinShape,shapes);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap faceDetect(int mode, Matrix matrix, int level, int amount) {
        int[] pixels = new int[width * height];
        image1.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pixelsret = new int[width * height];
        image1.getPixels(pixelsret, 0, width, 0, 0, width, height);
        Bitmap bmOut = Bitmap.createBitmap(width, height, image1.getConfig());

        List<Shape> MaxMinShape = new ArrayList<>();

        List<Integer> histogram;
        histogram = new ArrayList<>();

        for (int i=0;i<256;i++) {
            histogram.add(0);
        }

        int[] arrLoader = new int[width * height];
        for (int i = 0; i < width * height; ++i) {
            arrLoader[i] = 0;
        }
        setArrIndex(pixels,arrLoader, MaxMinShape);
        int mark = 0;
        matrixLoader(pixels, histogram, mark, mode, matrix, level, amount);

        double d = otsu(histogram, width*height);
        otsuPixels(pixels, d);

        pixels = thinImage(pixels);
        //new
        int[] arr = new int[width * height];
        for (int i = 0; i < width * height; ++i) {
            arr[i] = 0;
        }
        List<Shape> shapes = new ArrayList<Shape>();
        traceShapes(pixels, mark, arr, arrLoader, shapes);
//        setFacesRect(pixels,shapes);
        setFacesRect(pixelsret,MaxMinShape,shapes);
        bmOut.setPixels(pixelsret, 0, width, 0, 0, width, height);
        return bmOut;
    }

    private int[] thinImage(int[] pixels) {
        boolean firstStep = false;
        boolean hasChanged;
        int[][] nbrs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1},
                {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}};
        int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}}, {{0, 2, 6},
                {0, 4, 6}}};
        List<Point> toWhite = new ArrayList<>();
        do {
            hasChanged = false;
            firstStep = !firstStep;

            for (int r = 1; r < height - 1; r++) {
                for (int c = 1; c < width - 1; c++) {
                    int index = r * width + c;

                    NewColor color = new NewColor(pixels[index]);
                    Integer gray = color.getGrayscale();
                    if (gray == 0)
                        continue;

                    int nn = numNeighbors(pixels, r, c, nbrs);
                    if (nn < 2 || nn > 6)
                        continue;

                    if (numTransitions(pixels, r, c, nbrs) != 1)
                        continue;

                    if (!atLeastOneIsWhite(pixels, r, c, firstStep ? 0 : 1, nbrs, nbrGroups))
                        continue;

                    toWhite.add(new Point(c, r));
                    hasChanged = true;
                }
            }

            for (Point p : toWhite) {
                pixels[p.y * width + p.x] = Color.rgb(0,0,0);
            }
            toWhite.clear();

        } while (firstStep || hasChanged);
        return pixels;
    }

    int numNeighbors(int[] histogram, int r, int c, int[][] nbrs) {
        int count = 0;
        for (int i = 0; i < nbrs.length - 1; i++) {
            int x1 = c + nbrs[i][0];
            int y1 = r + nbrs[i][1];
            int index = y1 * width + x1;
            NewColor color = new NewColor(histogram[index]);
            Integer gray = color.getGrayscale();
            if (gray > 1)
                count++;
        }
        return count;
    }

    int numTransitions(int[] histogram, int r, int c, int[][] nbrs) {
        int count = 0;
        for (int i = 0; i < nbrs.length - 1; i++) {
            int x1 = c + nbrs[i][0];
            int y1 = r + nbrs[i][1];

            int x2 = c + nbrs[i+1][0];
            int y2 = r + nbrs[i+1][1];
            int index = y1 * width + x1;
            NewColor color = new NewColor(histogram[index]);
            Integer gray = color.getGrayscale();
            if (gray < 1) {
                NewColor color2 = new NewColor(histogram[y2 * width + x2]);
                Integer gray2 = color2.getGrayscale();
                if (gray2 > 1)
                    count++;
            }
        }
        return count;
    }

    boolean atLeastOneIsWhite(int[] histogram, int r, int c, int step, int[][] nbrs, int[][][] nbrGroups) {
        int count = 0;
        int[][] group = nbrGroups[step];
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < group[i].length; j++) {
                int[] nbr = nbrs[group[i][j]];
                int x1 = c + nbr[0];
                int y1 = r + nbr[1];
                int index = y1 * width + x1;
                NewColor color = new NewColor(histogram[index]);
                Integer gray = color.getGrayscale();
                if (gray < 1) {
                    count++;
                    break;
                }
            }
        return count > 1;
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

        if (point == 3) {
            point = 0;
        }
        return point;
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

    private double otsu(List<Integer> histogram, int total) {
        int sum = 0;
        for (int i = 1; i < 256; ++i)
            sum += i * histogram.get(i);
        int sumB = 0;
        int wB = 0;
        int wF = 0;
        int mB;
        int mF;
        double max = 0.0;
        double between = 0.0;
        double threshold1 = 0.0;
        double threshold2 = 0.0;
        for (int i = 0; i < 256; ++i) {
            wB += histogram.get(i);
            if (wB == 0)
                continue;
            wF = total - wB;
            if (wF == 0)
                break;
            sumB += i * histogram.get(i);
            mB = sumB / wB;
            mF = (sum - sumB) / wF;
            between = wB * wF * (mB - mF) * (mB - mF);
            if (between >= max) {
                threshold1 = i;
                if (between > max) {
                    threshold2 = i;
                }
                max = between;
            }
        }
        return (threshold1 + threshold2) / 2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
