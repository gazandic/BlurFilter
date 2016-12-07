package com.example.gazandic.blurfilter;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gazandic on 05/10/16.
 */

public class Shape {
    private List<Integer> lis;
    private int[] arah;

    public int minX;
    public int minY;
    public int maxX;
    public int maxY;


    public Shape() {
        lis = new ArrayList<>();
        arah = new int[9];
    }

    public Shape(List<Integer> _lis, int[] _arah) {
        lis = _lis;
        arah = _arah;
    }

    public void setLis(List<Integer> _lis) {
        lis = _lis;
    }

    public void setArah(int[] _arah) {
        arah = _arah;
    }

    public int[] getArah() {
        return arah;
    }

    public List<Integer> getLis() {
        return lis;
    }

    public boolean checkRound() {
        for (int i=0; i<9; i++) {
            if(i != 4 && i > 0 && arah[i] == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean check2() {
        int temp = 0;
        List<Pair<Integer, Integer>> Bentuk = new ArrayList<>();
        int kanan = 0;

        int bawah = 0;
        int kiri = 0;
        for (Integer lol:lis) {
            if ((temp == lol && temp == 5) || (temp==5 && lol == 7)) {
                kanan++;
            }
            if (temp == lol && temp == 7 || (temp==7 && lol == 3)) {
                if (kanan > 0 && bawah == 0) {
                    Pair<Integer,Integer> bb = new Pair<Integer, Integer>(5,kanan);
                    Bentuk.add(bb);
                }
                else if (bawah == 0){
                    return false;
                }
                bawah++;
            }

            if (temp == lol && temp == 3 || (temp==3 && lol == 7)) {
                if (kanan > 0 && kiri == 0) {
                    Pair<Integer,Integer> bb = new Pair<Integer, Integer>(7,bawah);
                    Bentuk.add(bb);
                    bawah = 0;
                    kanan = 0;
                }
                else if (kiri == 0){
                    return false;
                }
                kiri++;
            }
            if (temp == lol && temp == 3 || (temp==3 && lol == 7)) {
                if (kiri > 0 && bawah == 0) {
                    Pair<Integer,Integer> bb = new Pair<Integer, Integer>(3,kiri);
                    Bentuk.add(bb);
                }
                else if (bawah == 0){
                    return false;
                }
                bawah++;
            }
            if (temp == lol && temp == 5 || (temp==5 && lol == 7  )) {
                if (bawah > 0 && kanan == 0) {
                    Pair<Integer,Integer> bb = new Pair<Integer, Integer>(7,bawah);
                    Bentuk.add(bb);
                }
                else if (kanan == 0){
                    return false;
                }
                kanan++;
            }
            temp =lol;
        }

        if (kanan > 0) {
            Pair<Integer,Integer> bb = new Pair<Integer, Integer>(5,kanan);
            Bentuk.add(bb);
        }

        String s = "";
        for (Pair<Integer,Integer> p: Bentuk) {
            s.concat(String.valueOf(p.first));
        }

        if (s == "57375") return true;
        else {
            Log.d("Shape", s);
            return false;
        }
    }
}
