package com.example.gazandic.blurfilter;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gazandic on 28/09/16.
 */
public class Matrix {
    private List<Integer> list;
    private int size;

    public Matrix() {
        list = new ArrayList<Integer>();
        setSize(0);
    }

    public Matrix(List<Integer> list) {
        for(int i=0;i<9;i++) {
            if (i*i == list.size()) {
                this.list = list;
                setSize(i);
                return;
            }
        }
        list = new ArrayList<Integer>();
        setSize(0);
    }

    private void setSize(int size) {
        this.size = size;
    }

    public void parse(File file) {
        //Read text from file
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] strings;
            while ((line = br.readLine()) != null) {
                strings = line.split("\\\\s+");
                for(String string : strings) {
                    list.add(Integer.valueOf(string));
                }
                setSize(strings.length);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
    }

    public int getSize() {
        return size;
    }

    public int getData(int index) {
        return list.get(index);
    }

    public void setMatrix(List<Integer> list) {
        for(int i=0;i<9;i++) {
            if (i*i == list.size()) {
                this.list = list;
                setSize(i);
                return;
            }
        }
    }

    public void swapToRight() {
        if(size == 3) {
            int temp = list.get(2);
            list.set(2, list.get(5));
            list.set(5, list.get(8));
            list.set(8, list.get(7));
            list.set(7, list.get(6));
            list.set(6, list.get(3));
            list.set(3, list.get(0));
            list.set(0, list.get(1));
            list.set(1, temp);
        }
    }

    public void swapToLeft() {
        if(size == 3) {
            int temp = list.get(2);
            list.set(2, list.get(1));
            list.set(1, list.get(0));
            list.set(0, list.get(3));
            list.set(3, list.get(6));
            list.set(6, list.get(7));
            list.set(7, list.get(8));
            list.set(8, list.get(5));
            list.set(5, temp);
        }
    }
}
