package com.witcher.newspaperview;

import android.graphics.Matrix;

import androidx.annotation.NonNull;

public class MatrixUtil {


    public static float getMatrixScale(@NonNull Matrix matrix) {
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        return (float) Math.sqrt(Math.pow(matrixValues[Matrix.MSCALE_X], 2) + Math.pow(
                matrixValues[Matrix.MSKEW_Y], 2));
    }

    public static float getMatrixAngle(@NonNull Matrix matrix) {
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        return (float) -(Math.atan2(matrixValues[Matrix.MSKEW_X],
                matrixValues[Matrix.MSCALE_X]) * (180 / Math.PI));
    }

}
