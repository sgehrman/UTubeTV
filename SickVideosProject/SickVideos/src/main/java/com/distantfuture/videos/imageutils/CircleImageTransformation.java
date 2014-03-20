package com.distantfuture.videos.imageutils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Created by sgehrman on 3/19/14.
 */
public class CircleImageTransformation implements Transformation {
  Paint mFillPaint;
  Paint mStrokePaint;

  @Override
  public Bitmap transform(Bitmap source) {
    Bitmap result = null;

    if (mFillPaint == null) {
      mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

      mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      mStrokePaint.setStyle(Paint.Style.STROKE);
      mStrokePaint.setStrokeWidth(2.0f);
      mStrokePaint.setColor(0x99FFFFFF);
    }

    int width = source.getWidth();
    int height = source.getHeight();

    result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(result);

    // draw using circle into another bitmap, add shadows and shit
    int centerX = width / 2, centerY = height / 2, radius = (width / 2);

    BitmapShader s = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    mFillPaint.setShader(s);

    canvas.drawCircle(centerX, centerY, radius, mFillPaint);
    canvas.drawCircle(centerX, centerY, radius - 2, mStrokePaint);

    source.recycle();

    return result;
  }

  @Override
  public String key() {
    return "circleTransform";
  }
}

