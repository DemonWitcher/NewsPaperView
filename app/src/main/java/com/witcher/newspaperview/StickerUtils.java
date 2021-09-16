package com.witcher.newspaperview;

import android.graphics.RectF;

import static java.lang.Math.round;

public class StickerUtils {

  public static RectF trapToRect(float[] array) {
    RectF r = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
        Float.NEGATIVE_INFINITY);
    for (int i = 1; i < array.length; i += 2) {
      float x = round(array[i - 1] * 10) / 10.f;
      float y = round(array[i] * 10) / 10.f;
      r.left = Math.min(x, r.left);
      r.top = Math.min(y, r.top);
      r.right = Math.max(x, r.right);
      r.bottom = Math.max(y, r.bottom);
    }
    r.sort();
    return r;
  }
}
