package com.witcher.newspaperview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * done
 * 1.翻转
 * 2.缩放上下限
 * 3.移动边界检测 中心点不能超过屏幕
 * 4.图层修改
 * 5.手势落点认定
 * 6.贴纸入场到指定位置
 * 7.拖拽按钮实现
 * undone
 * 8.双指移动
 * 9.UI绘制变化
 */


public class NewsPaperView extends BaseView {

    //男女头部距离顶部距离 控件整体高度的千分比
    public static final int MAN_TOP = 75;
    public static final int WOMAN_TOP = 120;
    //人物脚部距离底部距离 控件整体高度的千分比
    public static final int PEOPLE_BOTTOM = 130;

    //缩放边界
    public static final float PAPER_SCALE_MAX = 5f;
    public static final float PAPER_SCALE_MIN = 0.2f;
    public static final float PEOPLE_SCALE_MAX = 2f;
    public static final float PEOPLE_SCALE_MIN = 0.5f;
    //数量边界
    public static final int PAPER_MAX = 20;
    public static final int PEOPLE_MAX = 5;

    private final Paint mPaintForLineAndCircle;
    private final Paint mPaintFlip;
    private final Paint mPaintMove;

    private int mMoveTag, mTransformTag, mDeleteTag, mFlipTag, mActionTag;
    private float mCurrentScale;//当前双指触控贴纸的本次move缩放数值

    private float mMaxLeft, mMaxRight, mMaxTop, mMaxBottom;//控制本次移动边缘的数值

    private Bitmap mDeleteIcon;
    private Bitmap mFlipIcon;
    private Bitmap mMoveIcon;

    private int mIconWidth;

    private int test = 0;

    private boolean mShowUI = true;

    private List<ImageGroup> mDecalImageGroupList = new ArrayList<>();

    public NewsPaperView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mDeleteIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_close);
        mFlipIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_close);
        mMoveIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_close);
        mIconWidth = 40;
        mPaintForLineAndCircle = new Paint();
        mPaintForLineAndCircle.setAntiAlias(true);
        mPaintForLineAndCircle.setColor(Color.RED);
        mPaintForLineAndCircle.setAlpha(170);

        mPaintFlip = new Paint();
        mPaintFlip.setAntiAlias(true);
        mPaintFlip.setColor(Color.BLUE);
        mPaintFlip.setAlpha(170);

        mPaintMove = new Paint();
        mPaintMove.setAntiAlias(true);
        mPaintMove.setColor(Color.BLACK);
        mPaintMove.setAlpha(170);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //先画人物
        for (int i = 0; i < mDecalImageGroupList.size(); ++i) {
            ImageGroup imageGroup = mDecalImageGroupList.get(i);
            if (!imageGroup.isPaper) {
                drawPaper(canvas, imageGroup);
            }
        }

        //再画贴纸
        for (int i = 0; i < mDecalImageGroupList.size(); ++i) {
            ImageGroup imageGroup = mDecalImageGroupList.get(i);
            if (imageGroup.isPaper) {
                drawPaper(canvas, imageGroup);
            }
        }

        //最后画控制UI
        if (mDecalImageGroupList.size() > 0 && mShowUI) {
            drawPaperUI(canvas, mDecalImageGroupList.get(mDecalImageGroupList.size() - 1));
        }
    }

    private void drawPaperUI(Canvas canvas, ImageGroup imageGroup) {
        float[] points = getBitmapPoints(imageGroup);
        float x1 = points[0];
        float y1 = points[1];
        float x2 = points[2];
        float y2 = points[3];
        float x3 = points[4];
        float y3 = points[5];
        float x4 = points[6];
        float y4 = points[7];

        if (imageGroup.isPaper) {
            mPaintForLineAndCircle.setColor(Color.RED);
        } else {
            mPaintForLineAndCircle.setColor(Color.BLUE);
        }
        canvas.drawLine(x1, y1, x2, y2, mPaintForLineAndCircle);
        canvas.drawLine(x2, y2, x4, y4, mPaintForLineAndCircle);
        canvas.drawLine(x4, y4, x3, y3, mPaintForLineAndCircle);
        canvas.drawLine(x3, y3, x1, y1, mPaintForLineAndCircle);

        //删除
        canvas.drawCircle(x2, y2, mIconWidth, mPaintForLineAndCircle);
        canvas.drawBitmap(mDeleteIcon, x2 - mDeleteIcon.getWidth() / 2, y2 - mDeleteIcon.getHeight() / 2, mPaintForBitmap);

        //翻转
        canvas.drawCircle(x1, y1, mIconWidth, mPaintFlip);
        canvas.drawBitmap(mFlipIcon, x1 - mFlipIcon.getWidth() / 2, y1 - mFlipIcon.getHeight() / 2, mPaintForBitmap);
        //拖动
        canvas.drawCircle(x4, y4, mIconWidth, mPaintMove);
        canvas.drawBitmap(mMoveIcon, x4 - mMoveIcon.getWidth() / 2, y4 - mMoveIcon.getHeight() / 2, mPaintForBitmap);
    }

    private void drawPaper(Canvas canvas, ImageGroup imageGroup) {
        //贴图
        canvas.drawBitmap(imageGroup.bitmap, imageGroup.matrix, mPaintForBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = event.getX();
                mDownY = event.getY();
                mMoveTag = decalCheck(mDownX, mDownY);
                mDeleteTag = deleteCheck(mDownX, mDownY);
                mFlipTag = flipCheck(mDownX, mDownY);
                mActionTag = actionCheck(mDownX, mDownY);
                L.i("actionTag:" + mActionTag);
                if (mMoveTag != -1 && mDeleteTag == -1 && mFlipTag == -1 && mActionTag == -1) {
                    mShowUI = true;
                    mDownMatrix.set(mDecalImageGroupList.get(mMoveTag).matrix);
                    mMode = DRAG;
                    //记录4个方向最大移动距离
                    ImageGroup imageGroup = mDecalImageGroupList.get(mMoveTag);
                    float centerX = getCenterX(imageGroup);
                    float centerY = getCenterY(imageGroup);

                    mMaxLeft = centerX;
                    mMaxRight = getWidth() - centerX;
                    mMaxTop = centerY;
                    mMaxBottom = getHeight() - centerY;
                }
                if (mActionTag != -1) {
                    mShowUI = true;
                    mDownMatrix.set(mDecalImageGroupList.get(mActionTag).matrix);
                    mMode = SINGLE_ZOOM;

                    ImageGroup imageGroup = mDecalImageGroupList.get(mActionTag);
                    float centerX = getCenterX(imageGroup);
                    float centerY = getCenterY(imageGroup);

                    mMoveTag = decalCheck(mDownX, mDownY);
                    mTransformTag = decalCheck(centerX, centerY);
                    mOldDistance = getDistanceOld(event, centerX, centerY);
                    mOldRotation = getRotationOld(event, centerX, centerY);

                    mMidPoint = midPointSingle(centerX, centerY);
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                if (mMode != SINGLE_ZOOM) {
                    mMoveTag = decalCheck(event.getX(0), event.getY(0));
                    mTransformTag = decalCheck(event.getX(1), event.getY(1));
                    if (mMoveTag != -1 && mTransformTag == mMoveTag && mDeleteTag == -1 && mFlipTag == -1 && mActionTag == -1) {
                        mDownMatrix.set(mDecalImageGroupList.get(mMoveTag).matrix);
                        mMode = ZOOM;
                    }
                    mOldDistance = getDistance(event);
                    mOldRotation = getRotation(event);

                    mMidPoint = midPoint(event);
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                if (mMode == ZOOM) {
                    mMoveMatrix.set(mDownMatrix);
                    float newRotation = getRotation(event) - mOldRotation;
                    float newDistance = getDistance(event);
                    mCurrentScale = newDistance / mOldDistance;
                    ImageGroup imageGroup = mDecalImageGroupList.get(mMoveTag);

                    if (mMoveTag != -1) {
                        mMoveMatrix.postRotate(newRotation, mMidPoint.x, mMidPoint.y);// 旋轉
                        //缩放界限检测
                        float downScale = MatrixUtil.getMatrixScale(mDownMatrix);
                        float newScale = mCurrentScale * downScale;
                        if (newScale >= getScaleMax(imageGroup.isPaper)) {
                            mCurrentScale = getScaleMax(imageGroup.isPaper) / downScale;
                        }
                        if (newScale <= getScaleMin(imageGroup.isPaper)) {
                            mCurrentScale = getScaleMin(imageGroup.isPaper) / downScale;
                        }
                        L.i("mCurrentScale:" + mCurrentScale);
                        mMoveMatrix.postScale(mCurrentScale, mCurrentScale, mMidPoint.x, mMidPoint.y);// 縮放
                        imageGroup.matrix.set(mMoveMatrix);


                        imageGroup.haveMove = true;
                        imageGroup.rotation = newRotation;
                    }
                    invalidate();
                } else if (mMode == DRAG) {
                    mMoveMatrix.set(mDownMatrix);

                    if (mMoveTag != -1) {
                        //拖拽边界检测
                        float newX = event.getX() - mDownX;
                        float newY = event.getY() - mDownY;

                        newX = Math.max(-mMaxLeft, newX);
                        newX = Math.min(mMaxRight, newX);
                        newY = Math.max(-mMaxTop, newY);
                        newY = Math.min(mMaxBottom, newY);

                        mMoveMatrix.postTranslate(newX, newY);// 平移
                        ImageGroup imageGroup = mDecalImageGroupList.get(mMoveTag);
                        imageGroup.matrix.set(mMoveMatrix);
                        imageGroup.haveMove = true;
                    }
                    invalidate();
                } else if (mMode == SINGLE_ZOOM) {
                    ImageGroup imageGroup = mDecalImageGroupList.get(mActionTag);
                    float centerX = getCenterX(imageGroup);
                    float centerY = getCenterY(imageGroup);

                    mMoveMatrix.set(mDownMatrix);
                    float newRotation = getRotationOld(event, centerX, centerY) - mOldRotation;
                    float newDistance = getDistanceOld(event, centerX, centerY);
                    mCurrentScale = newDistance / mOldDistance;
                    if (mActionTag != -1) {
                        mMoveMatrix.postRotate(newRotation, mMidPoint.x, mMidPoint.y);// 旋轉
                        //缩放界限检测
                        L.i("mCurrentScale:" + mCurrentScale);
                        float downScale = MatrixUtil.getMatrixScale(mDownMatrix);
                        float newScale = mCurrentScale * downScale;
                        if (newScale >= getScaleMax(imageGroup.isPaper)) {
                            mCurrentScale = getScaleMax(imageGroup.isPaper) / downScale;
                        }
                        if (newScale <= getScaleMin(imageGroup.isPaper)) {
                            mCurrentScale = getScaleMin(imageGroup.isPaper) / downScale;
                        }

                        mMoveMatrix.postScale(mCurrentScale, mCurrentScale, mMidPoint.x, mMidPoint.y);// 縮放
                        imageGroup.matrix.set(mMoveMatrix);


                        imageGroup.rotation = newRotation;
                        imageGroup.haveMove = true;
                    }
                    invalidate();
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                if (mDeleteTag != -1) {
                    mDecalImageGroupList.remove(mDeleteTag).release();
                    invalidate();
                }
                if (mFlipTag != -1) {
                    flip(mFlipTag);
                    invalidate();
                }
                if (mFlipTag == -1 && mMoveTag == -1 && mDeleteTag == -1 && mActionTag == -1) {
                    L.i("什么都没点中");
                    mShowUI = false;
                } else {
                    mShowUI = true;
                }
                invalidate();
                mMode = NONE;
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                if (mMode == ZOOM) {
                    //双指缩放结束后 记录最新的缩放大小
                    ImageGroup imageGroup = mDecalImageGroupList.get(mMoveTag);
                    imageGroup.scale = imageGroup.scale * mCurrentScale;
                }
                mMode = NONE;
            }
            break;
        }
        return true;
    }

    private void flip(int index) {
//        L.i("翻转:" + index);
        ImageGroup imageGroup = mDecalImageGroupList.get(index);
//
        float[] points = getBitmapPoints(imageGroup);
        float x1 = points[0];
        float y1 = points[1];
        float x4 = points[6];
        float y4 = points[7];

        Matrix matrix = new Matrix();
        matrix.postScale(-1f, 1f, x1 + ((x4 - x1) / 2), y1 + ((y4 - y1) / 2));
        if (imageGroup.bitmap == null || imageGroup.bitmap.getWidth() == 0 || imageGroup.bitmap.getHeight() == 0) {
            return;
        }
        imageGroup.bitmap = Bitmap.createBitmap(imageGroup.bitmap, 0, 0,
                imageGroup.bitmap.getWidth(), imageGroup.bitmap.getHeight(), matrix, true);
        invalidate();
    }

    private boolean pointCheck(ImageGroup imageGroup, float x, float y) {
//        float edge = (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
//        if ((2 + Math.sqrt(2)) * edge >= Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2))
//                + Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2))
//                + Math.sqrt(Math.pow(x - x3, 2) + Math.pow(y - y3, 2))
//                + Math.sqrt(Math.pow(x - x4, 2) + Math.pow(y - y4, 2))) {
//            return true;
//        }
//        return false;
        return imageGroup.contains(x, y);
    }

    private boolean deleteCircleCheck(ImageGroup imageGroup, float x, float y) {
        float[] points = getBitmapPoints(imageGroup);
        float x2 = points[2];
        float y2 = points[3];

        int checkDis = (int) Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2));

        if (checkDis < mIconWidth) {
            return true;
        }
        return false;
    }

    private boolean actionCircleCheck(ImageGroup imageGroup, float x, float y) {
        float[] points = getBitmapPoints(imageGroup);
        float x4 = points[6];
        float y4 = points[7];

        int checkDis = (int) Math.sqrt(Math.pow(x - x4, 2) + Math.pow(y - y4, 2));

        if (checkDis < mIconWidth) {
            return true;
        }
        return false;
    }

    private boolean flipCircleCheck(ImageGroup imageGroup, float x, float y) {
        float[] points = getBitmapPoints(imageGroup);
        float x1 = points[0];
        float y1 = points[1];

        int checkDis = (int) Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2));

        if (checkDis < mIconWidth) {
            return true;
        }
        return false;
    }

    private int deleteCheck(float x, float y) {
        if (!mShowUI) {
            return -1;
        }
        L.i("deleteCheck 当前数组 :" + mDecalImageGroupList.toString());
        int index = -1;
        for (int i = mDecalImageGroupList.size() - 1; i > -1; i--) {
            if (deleteCircleCheck(mDecalImageGroupList.get(i), x, y)) {
                L.i("删除 判定 ：" + i);
                index = i;
                break;
            }
        }
        //层级拦截
        if (index != -1) {
            for (int i = mDecalImageGroupList.size() - 1; i > index; i--) {
                if (pointCheck(mDecalImageGroupList.get(i), x, y)) {
                    L.i("删除 被" + i + "拦截");
                    index = -1;
                    break;
                }
            }
        }
        //调整层级
        if (index != -1 && index != mDecalImageGroupList.size() - 1) {
            ImageGroup imageGroup = mDecalImageGroupList.remove(index);
            mDecalImageGroupList.add(imageGroup);
            invalidate();
            return mDecalImageGroupList.size() - 1;
        }
        return index;
    }

    private int actionCheck(float x, float y) {
        if (!mShowUI) {
            return -1;
        }
        L.i("actionCheck 当前数组 :" + mDecalImageGroupList.toString());
        int index = -1;
        for (int i = mDecalImageGroupList.size() - 1; i > -1; i--) {
            if (actionCircleCheck(mDecalImageGroupList.get(i), x, y)) {
                index = i;
                break;
            }
        }
        //层级拦截
        if (index != -1) {
            for (int i = mDecalImageGroupList.size() - 1; i > index; i--) {
                if (pointCheck(mDecalImageGroupList.get(i), x, y)) {
                    index = -1;
                    break;
                }
            }
        }
        //调整层级
        if (index != -1 && index != mDecalImageGroupList.size() - 1) {
            ImageGroup imageGroup = mDecalImageGroupList.remove(index);
            mDecalImageGroupList.add(imageGroup);
            L.i("actionCheck 重排序 当前数组 :" + mDecalImageGroupList.toString());
            invalidate();
            return mDecalImageGroupList.size() - 1;
        }
        return index;
    }

    private int flipCheck(float x, float y) {
        if (!mShowUI) {
            return -1;
        }
        L.i("flipCheck 当前数组 :" + mDecalImageGroupList.toString());
        int index = -1;
        for (int i = mDecalImageGroupList.size() - 1; i > -1; i--) {
            if (flipCircleCheck(mDecalImageGroupList.get(i), x, y)) {
                index = i;
                break;
            }
        }
        //层级拦截
        if (index != -1) {
            for (int i = mDecalImageGroupList.size() - 1; i > index; i--) {
                if (pointCheck(mDecalImageGroupList.get(i), x, y)) {
                    index = -1;
                    break;
                }
            }
        }
        //调整层级
        if (index != -1 && index != mDecalImageGroupList.size() - 1) {
            ImageGroup imageGroup = mDecalImageGroupList.remove(index);
            mDecalImageGroupList.add(imageGroup);
            L.i("flipCheck 重排序 当前数组 :" + mDecalImageGroupList.toString());
            invalidate();
            return mDecalImageGroupList.size() - 1;
        }
        return index;
    }

    private int decalCheck(float x, float y) {
        L.i("decalCheck 当前数组 :" + mDecalImageGroupList.toString());
        int index = -1;
        for (int i = mDecalImageGroupList.size() - 1; i > -1; i--) {
            if (pointCheck(mDecalImageGroupList.get(i), x, y)) {
                index = i;
                break;
            }
        }
        //如果落点在某个图中  并且 在更高层级的按钮中 拦截
        if (index != -1) {
            for (int i = mDecalImageGroupList.size() - 1; i > index; i--) {
                if (deleteCircleCheck(mDecalImageGroupList.get(i), x, y)
                        || flipCircleCheck(mDecalImageGroupList.get(i), x, y)) {
                    index = -1;
                    break;
                }
            }
        }
        //调整层级
        if (index != -1 && index != mDecalImageGroupList.size() - 1) {
            ImageGroup imageGroup = mDecalImageGroupList.remove(index);
            mDecalImageGroupList.add(imageGroup);
            L.i("decalCheck 重排序 当前数组 :" + mDecalImageGroupList.toString());
            invalidate();
            return mDecalImageGroupList.size() - 1;
        }
        return index;
    }

    public int addPeople(Bitmap bitmap, boolean man) {
        if (bitmap == null) {
            return -2;
        }
        int peopleCount = 0;

        for (ImageGroup imageGroup : mDecalImageGroupList) {
            if (!imageGroup.isPaper) {
                peopleCount++;
            }
        }

        //数量控制
        if (peopleCount >= PEOPLE_MAX) {
            return -1;
        }

        ImageGroup imageGroupTemp = new ImageGroup();
        imageGroupTemp.test = test;
        test++;
//        Bitmap newB;
//        //根据规则控制人物入场尺寸
//        if (man) {
//            int targetHeight = getHeight() * (MAN_TOP + PEOPLE_BOTTOM) / 1000;
//            float scale = (float) targetHeight / bitmap.getHeight();
//            int newWidth = (int) (bitmap.getWidth() * scale);
//            if (newWidth == 0 || targetHeight == 0) {
//                return -3;
//            }
//            newB = Bitmap.createScaledBitmap(bitmap, newWidth, targetHeight, true);
//        } else {
//            int targetHeight = getHeight() * (WOMAN_TOP + PEOPLE_BOTTOM) / 1000;
//            float scale = (float) targetHeight / bitmap.getHeight();
//            int newWidth = (int) (bitmap.getWidth() * scale);
//            if (newWidth == 0 || targetHeight == 0) {
//                return -3;
//            }
//            newB = Bitmap.createScaledBitmap(bitmap, newWidth, targetHeight, true);
//        }
//        imageGroupTemp.bitmap = newB;
        imageGroupTemp.bitmap = bitmap;
        imageGroupTemp.isPaper = false;
        if (imageGroupTemp.matrix == null) {
            imageGroupTemp.matrix = new Matrix();
        }
        //落点控制
//        float transX;
//        float transY = (getHeight() - imageGroupTemp.bitmap.getHeight()) / 2;
//        if (man) {
//            transX = getHeight() * MAN_TOP / 1000;
//        } else {
//            transX = getHeight() * WOMAN_TOP / 1000;
//        }
//        //等产品出需求
//        if (peopleCount == 0) {
//
//        } else if (peopleCount == 1) {
//
//        } else if (peopleCount == 2) {
//
//        } else if (peopleCount == 3) {
//
//        } else if (peopleCount == 4) {
//
//        }
        float transX = (getWidth() - imageGroupTemp.bitmap.getWidth()) / 2;
        float transY = (getHeight() - imageGroupTemp.bitmap.getHeight()) / 2;
        imageGroupTemp.matrix.postTranslate(transX, transY);

        mDecalImageGroupList.add(imageGroupTemp);
        mShowUI = true;
        invalidate();
        return 0;
    }

    public void reLayoutPeople() {
        //等产品出需求
    }

    public int addPaper(Bitmap bitmap) {
        if (bitmap == null) {
            return -2;
        }
        int paperCount = 0;

        for (ImageGroup imageGroup : mDecalImageGroupList) {
            if (imageGroup.isPaper) {
                paperCount++;
            }
        }

        //数量控制
        if (paperCount >= PAPER_MAX) {
            return -1;
        }

        ImageGroup imageGroupTemp = new ImageGroup();
        imageGroupTemp.test = test;
        test++;
        imageGroupTemp.bitmap = bitmap;
        imageGroupTemp.isPaper = true;
        if (imageGroupTemp.matrix == null) {
            imageGroupTemp.matrix = new Matrix();
        }
        //落点控制
        if (paperCount == 0) {
            //中间落点
            float transX = (getWidth() - imageGroupTemp.bitmap.getWidth()) / 2;
            float transY = (getHeight() - imageGroupTemp.bitmap.getHeight()) / 2;
            imageGroupTemp.matrix.postTranslate(transX, transY);
        } else {
            //随机落点
            float transX = new Random().nextInt(getWidth() - imageGroupTemp.bitmap.getWidth());
            float transY = new Random().nextInt(getHeight() - imageGroupTemp.bitmap.getHeight());
            imageGroupTemp.matrix.postTranslate(transX, transY);
        }

        mDecalImageGroupList.add(imageGroupTemp);
        mShowUI = true;
        invalidate();
        return 0;
    }

    public void hideUI() {
        mShowUI = false;
        invalidate();
    }

    private static float getScaleMax(boolean isPaper) {
        if (isPaper) {
            return PAPER_SCALE_MAX;
        } else {
            return PEOPLE_SCALE_MAX;
        }
    }

    private static float getScaleMin(boolean isPaper) {
        if (isPaper) {
            return PAPER_SCALE_MIN;
        } else {
            return PEOPLE_SCALE_MIN;
        }
    }
}