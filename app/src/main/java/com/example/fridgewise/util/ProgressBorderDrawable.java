package com.example.fridgewise.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProgressBorderDrawable extends Drawable {
    private final Paint paintTrack = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintAnchor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintGlowOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintGlowMid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintGlowCore = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path rectPath = new Path();
    private final Path segmentPath = new Path();
    private final RectF rectF = new RectF();
    private final float[] anchorPos = new float[2];
    private final float[] headPos = new float[2];

    private float progress = 0f; // 0.0 to 1.0
    private float cornerRadius = 0f;
    private float strokeWidth = 0f;
    private float density = 1f;
    private int greenColor;
    private int orangeColor;
    private int redColor;

    public ProgressBorderDrawable(float cornerRadiusPx, float strokeWidthPx, float density, int trackColor, int greenColor, int orangeColor, int redColor) {
        this.cornerRadius = cornerRadiusPx;
        this.strokeWidth = strokeWidthPx;
        this.density = density;
        this.greenColor = greenColor;
        this.orangeColor = orangeColor;
        this.redColor = redColor;

        paintTrack.setStyle(Paint.Style.STROKE);
        paintTrack.setStrokeWidth(strokeWidthPx);
        paintTrack.setColor(trackColor);
        paintTrack.setStrokeCap(Paint.Cap.ROUND);
        paintTrack.setStrokeJoin(Paint.Join.ROUND);

        paintProgress.setStyle(Paint.Style.STROKE);
        paintProgress.setStrokeWidth(strokeWidthPx);
        paintProgress.setStrokeCap(Paint.Cap.ROUND);
        paintProgress.setStrokeJoin(Paint.Join.ROUND);

        paintAnchor.setStyle(Paint.Style.FILL);
        paintAnchor.setColor(Color.parseColor("#A0FFFFFF"));

        paintGlowOuter.setStyle(Paint.Style.FILL);
        paintGlowMid.setStyle(Paint.Style.FILL);
        paintGlowCore.setStyle(Paint.Style.FILL);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        int currentColor = getStageColor(this.progress);
        paintProgress.setColor(currentColor);
        invalidateSelf();
    }

    private int getStageColor(float p) {
        if (p < 0.5f) {
            return greenColor;
        } else if (p < 0.85f) {
            return orangeColor;
        } else {
            return redColor;
        }
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        float glowPadding = 3.5f * density;
        float halfStroke = strokeWidth / 2f;
        float inset = halfStroke + glowPadding;
        rectF.set(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset);
        rectPath.reset();
        float adjustedRadius = Math.max(4f * density, cornerRadius - glowPadding);
        rectPath.addRoundRect(rectF, adjustedRadius, adjustedRadius, Path.Direction.CW);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // 1. Draw background track border
        canvas.drawPath(rectPath, paintTrack);

        PathMeasure measure = new PathMeasure(rectPath, false);
        float length = measure.getLength();

        if (length > 0) {
            // 2. Draw origin anchor marker
            measure.getPosTan(0f, anchorPos, null);
            canvas.drawCircle(anchorPos[0], anchorPos[1], 2.0f * density, paintAnchor);

            // 3. Draw active progress segment
            float activePercent = Math.max(0.06f, progress);
            segmentPath.reset();
            measure.getSegment(0f, length * activePercent, segmentPath, true);
            canvas.drawPath(segmentPath, paintProgress);

            // 4. Draw Neon Particle Glowing Head Tip at current tip position
            measure.getPosTan(length * activePercent, headPos, null);
            int currentColor = getStageColor(progress);

            // Outer ambient aura
            paintGlowOuter.setColor(currentColor);
            paintGlowOuter.setAlpha(70);
            canvas.drawCircle(headPos[0], headPos[1], 4.8f * density, paintGlowOuter);

            // Mid glow ring
            paintGlowMid.setColor(currentColor);
            paintGlowMid.setAlpha(160);
            canvas.drawCircle(headPos[0], headPos[1], 3.2f * density, paintGlowMid);

            // Bright core dot
            paintGlowCore.setColor(Color.WHITE);
            canvas.drawCircle(headPos[0], headPos[1], 2.0f * density, paintGlowCore);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paintTrack.setAlpha(alpha);
        paintProgress.setAlpha(alpha);
        paintAnchor.setAlpha(alpha);
        paintGlowOuter.setAlpha(alpha);
        paintGlowMid.setAlpha(alpha);
        paintGlowCore.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paintTrack.setColorFilter(colorFilter);
        paintProgress.setColorFilter(colorFilter);
        paintAnchor.setColorFilter(colorFilter);
        paintGlowOuter.setColorFilter(colorFilter);
        paintGlowMid.setColorFilter(colorFilter);
        paintGlowCore.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
