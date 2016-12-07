package com.example.gazandic.blurfilter;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void powerOf2_isCorrect() throws Exception {
        ImageFiltering ima = new ImageFiltering();
        assertFalse(ima.powerOf2(2,3,8));
    }

    @Test
    public void iscorrectDoubleOpt() throws Exception {
        Integer i = 20;
        Double d = Double.valueOf(i);
        Integer j = d.intValue();
        assertEquals(i,j);
    }

    @Test
    public void fft2d_isCorrect() throws Exception {
        ImageFiltering ima = new ImageFiltering();

        List<NewColor> li = new ArrayList<>();
        li.add(new NewColor(5,5,53));
        li.add(new NewColor(2,22,2));
        li.add(new NewColor(31,3,3));
        li.add(new NewColor(51,23,44));
        li.add(new NewColor(51,2,4));
        li.add(new NewColor(51,23,4));
        li.add(new NewColor(5,23,4));
        li.add(new NewColor(51,3,3));
        li.add(new NewColor(31,13,2));

        int val = ima.fft2d(li,1,1,3);
        assertEquals(-1, val);

    }
}