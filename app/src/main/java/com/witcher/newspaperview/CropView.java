package com.witcher.newspaperview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CropView extends BaseView {

    public CropView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                mDownMatrix.set(mCropImageGroup.matrix);
                mMode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mDownMatrix.set(mCropImageGroup.matrix);
                mMode = ZOOM;
                mOldDistance = getDistance(event);
                mMidPoint = midPoint(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mMode == ZOOM) {
                    mMoveMatrix.set(mDownMatrix);
                    float newDist = getDistance(event);
                    float scale = newDist / mOldDistance;
                    mMoveMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);// 縮放
                    mCropImageGroup.matrix.set(mMoveMatrix);
                    invalidate();

                } else if (mMode == DRAG) {
                    mMoveMatrix.set(mDownMatrix);
                    mMoveMatrix.postTranslate(event.getX() - mDownX, event.getY() - mDownY);// 平移
                    mCropImageGroup.matrix.set(mMoveMatrix);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mCropImageGroup.bitmap != null) {
                    matrixFix();
                }
                mMode = NONE;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mMode = NONE;
                break;
        }
        return true;
    }

    private void matrixFix() {
        float[] points = getBitmapPoints(mCropImageGroup.bitmap, mMoveMatrix);
        float x1 = points[0];
        float y1 = points[1];
        float x2 = points[2];
        float y3 = points[5];

        if (mCropImageGroup.bitmap.getWidth() <= mCropImageGroup.bitmap.getHeight()) {
            if ((x2 - x1) < getWidth()) {
                mMoveMatrix.set(matrixBig);
            }

            if ((y3 - y1) < getHeight()) {
                mMoveMatrix.set(matrixSmall);
            }
        } else if (mCropImageGroup.bitmap.getWidth() > mCropImageGroup.bitmap.getHeight()) {
            if ((y3 - y1) < getHeight()) {
                mMoveMatrix.set(matrixBig);
            }

            if ((x2 - x1) < getWidth()) {
                mMoveMatrix.set(matrixSmall);
            }
        }

        if (!mMoveMatrix.equals(matrixBig) && !mMoveMatrix.equals(matrixSmall)) {
            if (x1 >= targetRect.left) {
                mMoveMatrix.postTranslate(targetRect.left - x1, 0);
            }

            if (x2 <= targetRect.left + getWidth()) {
                mMoveMatrix.postTranslate(getWidth() - x2, 0);
            }

            if (y1 >= targetRect.top) {
                mMoveMatrix.postTranslate(0, targetRect.top - y1);
            }

            if (y3 <= targetRect.top + getHeight()) {
                mMoveMatrix.postTranslate(0, targetRect.top + getHeight() - y3);
            }
        }

        mCropImageGroup.matrix.set(mMoveMatrix);
        invalidate();
    }
}