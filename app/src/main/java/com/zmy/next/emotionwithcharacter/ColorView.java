package com.zmy.next.emotionwithcharacter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zmy on 2017/7/5.
 */

public class ColorView extends View {

    private int mChooseColor;
    private int mBorderColor;
    private float mBorderWidth;
    private Paint mBorderPaint, mPaint;
    private int mCX, mCY, mRadius;

    public ColorView(Context context) {
        this(context, null);
    }

    public ColorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.ColorView);

        mChooseColor = t.getColor(R.styleable.ColorView_chooseColor, Color.BLACK);
        mBorderColor = t.getColor(R.styleable.ColorView_borderColor, Color.WHITE);
        mBorderWidth = t.getDimension(R.styleable.ColorView_borderWidth, 10);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mChooseColor);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCX == 0 || mCY == 0) {
            mCX = getWidth() / 2;
            mCY = getHeight() / 2;
            mRadius = mCX;
        }

        canvas.drawCircle(mCX, mCY, mRadius, mPaint);
        canvas.drawCircle(mCX, mCY, mRadius, mBorderPaint);
    }
}
