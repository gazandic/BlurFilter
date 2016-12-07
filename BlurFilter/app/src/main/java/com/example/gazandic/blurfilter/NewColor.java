package com.example.gazandic.blurfilter;

/**
 * Created by gazandic on 31/08/16.
 */
public class NewColor {
    protected int r;
    protected int g;
    protected int b;
    protected int sum;
    protected int grayscale;
    protected int rgb;
    private int imaginarygrayscale = 0;

    public NewColor() {
        r = 0;
        g = 0;
        b = 0;
        sum = 1;
        rgb = 0;
        setGrayscale();
    }

    public NewColor(int _r, int _g, int _b) {
        r = _r;
        g = _g;
        b = _b;
        sum = 1;
        this.rgb = rgb;
        setGrayscale();
    }

    public NewColor(int rgb) {
        r = (rgb & 0xFF0000) >> 16;
        g = (rgb & 0xFF00) >> 8;
        b = rgb & 0xFF;
        sum = 1;
        this.rgb = rgb;
        setGrayscale();
    }

    public int getImaginarygrayscale() {
        return imaginarygrayscale;
    }

    public void setImaginarygrayscale(int imaginarygrayscale) {
        this.imaginarygrayscale = imaginarygrayscale;
    }

    public void setGrayscale(int grayscale) {
        this.grayscale = grayscale;
    }

    public void setGrayscale() {
        this.grayscale = grayScale();
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public int getRGB() { return rgb; }

    public int getGrayscale() {
        return grayscale;
    }

    public String getColor() { return "Color:" + rgb; }

    public String toString() { return "Red : " + r + " Green : " + g + " Blue : " + b ; }

    public void addSum() { sum++; }

    public int getSum() { return sum; }

    public boolean checkSame(NewColor c) {
        return getRed() == c.getRed() && getGreen() == c.getGreen() && getBlue() == c.getBlue();
    }

    public boolean checkGrayScale(NewColor c) {
        return grayscale == c.grayscale;
    }

    public int grayScale() {
        return ( r + g + b ) / 3 ;
    }



}
