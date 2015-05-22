package com.kamcord.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by donliang1 on 5/22/15.
 */
public class RoundedImageView extends ImageView {

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Path roundedPath = new Path();
        int width = this.getWidth();
        roundedPath.addRoundRect(new RectF(0, 0, width, width), 30.0f, 30.0f, Path.Direction.CW);
        canvas.clipPath(roundedPath);
        super.onDraw(canvas);
    }

}
