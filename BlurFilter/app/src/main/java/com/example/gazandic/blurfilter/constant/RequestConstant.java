package com.example.gazandic.blurfilter.constant;

import android.content.Intent;

/**
 * Created by gazandic on 31/08/16.
 */
public final class RequestConstant {
    /**
     * Activity request code.
     * Used in Activity request result.
     *
     * {@link android.app.Activity#startActivityForResult(Intent, int)}
     * {@link android.support.v4.app.FragmentActivity#startActivityForResult(Intent, int)}
     */
    public static final int
            CAMERA_REQUEST                 = 0,
            REQUEST_GALLERY                 = 1;
}
