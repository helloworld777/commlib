package com.lu.library.widget.wheel;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;

public class Wheel3DView extends WheelView {
    private Camera mCamera;
    private Matrix mMatrix;

    public Wheel3DView(Context context) {
        this(context, null);
    }

    public Wheel3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCamera = new Camera();
        mMatrix = new Matrix();
    }

    /**
     * @return 控件的预算宽度
     */
    public int getPrefWidth() {
        int prefWidth = super.getPrefWidth();
        int innerHeight = (int) (itemHeight * getVisibleItems() * 2 / Math.PI);
        int towardRange = (int) (Math.sin(Math.PI / 48) * innerHeight);
        // 必须增加滚轮的内边距,否则当toward不为none时文字显示不全
        prefWidth += towardRange;
        return prefWidth;
    }

    /**
     * @return 控件的预算高度
     */
    public int getPrefHeight() {
        int padding = getPaddingTop() + getPaddingBottom();
        int innerHeight = (int) (itemHeight * getVisibleItems() * 2 / Math.PI);
        return innerHeight + padding;
    }

//    /**
//     * 根据控件的测量高度，计算可见项的数量
//     */
//    public int getPrefVisibleItems() {
//        int innerHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
//        return (int) (innerHeight * Math.PI / itemHeight / 2);
//    }

    protected void drawItem(Canvas canvas, int index, int offset) {
        CharSequence text = getCharSequence(index);
        if (text == null) return;
//        // 滚轮的半径
//         int r = (int) ((getHeight() - getPaddingTop() - getPaddingBottom()) / 2);
        //半圆的周长
        int halfCircumference = (int) (itemHeight * (getVisibleItems() -0.3));
        //求出半径
        int  r = (int) (halfCircumference / Math.PI);
        // 和中间选项的距离
        final int range = (index - mScroller.getItemIndex()) * itemHeight - offset;
        // 当滑动的角度和y轴垂直时（此时文字已经显示为一条线），不绘制文字
        if (Math.abs(range) > r * Math.PI / 2) return;

        final double angle = (double) range / r;
        // 绕x轴滚动的角度
        float rotate = (float) Math.toDegrees(-angle);
        // 滚动的距离映射到x轴的长度
        float translateX = 0;//(float) (Math.cos(angle) * Math.sin(Math.PI / 36) * r * 1);
        // 滚动的距离映射到y轴的长度
        float translateY = (float) (Math.sin(angle) * r);
        // 滚动的距离映射到z轴的长度
        float translateZ = (float) ((1 - Math.cos(angle)) * r);
        // 折射偏移量x
        float refractX = getTextSize() * .05f;
        // 透明度
        int alpha = (int) (Math.cos(angle) * 255);

        int clipLeft = getPaddingLeft();
        int clipRight = getWidth() - getPaddingRight();
        int clipTop = getPaddingTop();
        int clipBottom = getHeight() - getPaddingBottom();

        // 绘制两条分界线之间的文字
        if (Math.abs(range) <= 0) {
            mPaint.setColor(getSelectedColor());
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            drawText(canvas, text, translateX, translateY, translateZ, rotate);
            canvas.restore();
        }
        // 绘制与下分界线相交的文字
        else if (range > 0 && range < itemHeight) {
            mPaint.setColor(getSelectedColor());
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            drawText(canvas, text, translateX, translateY, translateZ, rotate);
            canvas.restore();

            mPaint.setColor(getUnselectedColor());
            mPaint.setAlpha(alpha);
            canvas.save();
            canvas.clipRect(clipLeft, lowerLimit, clipRight, clipBottom);
            drawText(canvas, text, translateX, translateY, translateZ, rotate);
            canvas.restore();
        }
        // 绘制与上分界线相交的文字
        else if (range < 0 && range > -itemHeight) {
            mPaint.setColor(getSelectedColor());
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            drawText(canvas, text, translateX, translateY, translateZ, rotate);
            canvas.restore();

            mPaint.setColor(getUnselectedColor());
            mPaint.setAlpha(alpha);
            canvas.save();
            canvas.clipRect(clipLeft, clipTop, clipRight, upperLimit);
            drawText(canvas, text, translateX, translateY, translateZ, rotate);
            canvas.restore();
        } else {
            mPaint.setColor(getUnselectedColor());
            mPaint.setAlpha(alpha);
            canvas.save();
            canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
            drawText(canvas, text, translateX, translateY, translateZ, rotate);
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas, CharSequence text, float translateX, float translateY, float translateZ, float rotateX) {
        mCamera.save();
        mCamera.translate(translateX, 0, translateZ);
        mCamera.rotateX(rotateX);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        final float x = centerX;
        final float y = centerY + translateY;

        // 设置绕x轴旋转的中心点位置
        mMatrix.preTranslate(-x, -y);
        mMatrix.postTranslate(x, y);

        canvas.concat(mMatrix);
        canvas.drawText(text, 0, text.length(), x, y - baseline, mPaint);
    }
}
