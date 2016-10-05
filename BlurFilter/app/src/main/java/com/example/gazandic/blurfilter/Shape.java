package com.example.gazandic.blurfilter;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gazandic on 05/10/16.
 */

public class Shape {
    private List<Integer> lis;
    private int[] arah;

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
}
