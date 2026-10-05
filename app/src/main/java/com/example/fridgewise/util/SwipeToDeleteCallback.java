package com.example.fridgewise.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fridgewise.R;

public abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final Context context;
    private final Drawable deleteIcon;
    
    private final Paint backgroundPaint;
    private final Paint badgePaint;
    private final Paint haloPaint;
    private final TextPaint textPaint;
    
    private final float defaultCornerRadius;
    private final float badgeRadiusPx;
    private final float badgeMarginRightPx;
    
    // State tracking for haptic and pop animation
    private RecyclerView.ViewHolder activeViewHolder = null;
    private boolean isGuardThresholdReached = false;
    private long guardPopStartTime = 0;

    public SwipeToDeleteCallback(Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.context = context.getApplicationContext();
        
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.outline_delete_24);
        
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        textPaint.setTextSize(dpToPx(12f));
        textPaint.setColor(Color.WHITE);
        
        defaultCornerRadius = dpToPx(16f);
        badgeRadiusPx = dpToPx(20f);
        badgeMarginRightPx = dpToPx(20f);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.35f; // Guard threshold at 35% displacement (Gmail style)
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 1.2f;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        
        View itemView = viewHolder.itemView;
        float itemWidth = itemView.getWidth();
        
        if (dX < 0 && itemWidth > 0) { // Swiping left
            float swipeDistance = Math.abs(dX);
            float progress = Math.min(1.0f, swipeDistance / itemWidth);
            float threshold = getSwipeThreshold(viewHolder);
            boolean isThresholdReached = progress >= threshold;

            // Track state transitions for haptic feedback and pop animation
            if (activeViewHolder != viewHolder) {
                activeViewHolder = viewHolder;
                isGuardThresholdReached = false;
                guardPopStartTime = 0;
            }

            if (isThresholdReached && !isGuardThresholdReached) {
                isGuardThresholdReached = true;
                guardPopStartTime = System.currentTimeMillis();
                try {
                    itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                } catch (Exception ignored) { }
            } else if (!isThresholdReached && isGuardThresholdReached) {
                isGuardThresholdReached = false;
                guardPopStartTime = 0;
            }

            // Calculate pop bounce scale
            float popScale;
            if (guardPopStartTime > 0) {
                long elapsed = System.currentTimeMillis() - guardPopStartTime;
                if (elapsed < 250) {
                    float t = elapsed / 250f;
                    popScale = (float) Math.sin(t * Math.PI) * 0.25f; // Pop curve
                } else {
                    guardPopStartTime = 0;
                    popScale = 0f;
                }
            } else {
                popScale = 0f;
            }

            // Base scale of icon/badge
            float badgeScale;
            if (!isThresholdReached) {
                badgeScale = 0.5f + 0.5f * (progress / threshold);
            } else {
                badgeScale = 1.0f + popScale;
            }

            // Item bounds
            float top = itemView.getTop();
            float bottom = itemView.getBottom();
            float right = itemView.getRight();
            float left = itemView.getLeft();
            float height = bottom - top;
            float centerY = top + height / 2f;

            // Revealed area bounds
            float leftBound = right + dX;

            c.save();
            c.clipRect(leftBound, top, right, bottom);

            // 1. Draw Red Background with Gradient
            int startColor = isThresholdReached ? Color.parseColor("#EF4444") : Color.parseColor("#E53935");
            int endColor = isThresholdReached ? Color.parseColor("#B91C1C") : Color.parseColor("#C62828");

            Shader shader = new LinearGradient(
                    leftBound, top, right, bottom,
                    startColor, endColor,
                    Shader.TileMode.CLAMP
            );
            backgroundPaint.setShader(shader);

            RectF fullRect = new RectF(left, top, right, bottom);
            c.drawRoundRect(fullRect, defaultCornerRadius, defaultCornerRadius, backgroundPaint);

            // 2. Calculate badge center position
            float targetBadgeX = right - badgeMarginRightPx - badgeRadiusPx;
            // Prevent badge from overflowing left of revealed area
            float minAllowedX = leftBound + badgeMarginRightPx + badgeRadiusPx;
            float badgeX = Math.max(targetBadgeX, minAllowedX);

            // 3. Draw Badge & Guard Effect
            float currentBadgeRadius = badgeRadiusPx * badgeScale;

            if (isThresholdReached) {
                // Outer Guard Halo Ring
                haloPaint.setColor(Color.parseColor("#60FFFFFF"));
                haloPaint.setStyle(Paint.Style.STROKE);
                haloPaint.setStrokeWidth(dpToPx(2.5f));
                c.drawCircle(badgeX, centerY, currentBadgeRadius + dpToPx(4f), haloPaint);

                // Solid White Badge
                badgePaint.setColor(Color.WHITE);
                badgePaint.setStyle(Paint.Style.FILL);
            } else {
                // Translucent Soft Disk
                badgePaint.setColor(Color.parseColor("#33FFFFFF"));
                badgePaint.setStyle(Paint.Style.FILL);
            }
            c.drawCircle(badgeX, centerY, currentBadgeRadius, badgePaint);

            // 4. Draw Trash Icon
            if (deleteIcon != null) {
                float iconSize = dpToPx(20f) * badgeScale;
                int iLeft = (int) (badgeX - iconSize / 2f);
                int iTop = (int) (centerY - iconSize / 2f);
                int iRight = (int) (badgeX + iconSize / 2f);
                int iBottom = (int) (centerY + iconSize / 2f);

                int iconTint = isThresholdReached ? Color.parseColor("#DC2626") : Color.WHITE;
                deleteIcon.setTint(iconTint);

                int iconAlpha = (int) (Math.min(1.0f, progress / 0.12f) * 255);
                deleteIcon.setAlpha(iconAlpha);
                deleteIcon.setBounds(iLeft, iTop, iRight, iBottom);
                deleteIcon.draw(c);
            }

            // 5. Draw "DELETE" Text Label
            float textAlphaProgress = Math.max(0f, (progress - 0.08f) / (threshold - 0.08f));
            int textAlpha = (int) (Math.min(1.0f, textAlphaProgress) * 255);
            
            if (textAlpha > 0) {
                textPaint.setAlpha(textAlpha);
                String label = "DELETE";
                float textWidth = textPaint.measureText(label);
                Paint.FontMetrics fm = textPaint.getFontMetrics();
                float textY = centerY - (fm.ascent + fm.descent) / 2f;
                float textX = badgeX - currentBadgeRadius - dpToPx(8f) - textWidth;

                if (textX > leftBound + dpToPx(12f)) {
                    c.drawText(label, textX, textY, textPaint);
                }
            }

            c.restore();

            if (guardPopStartTime > 0) {
                recyclerView.postInvalidateOnAnimation();
            }
        } else if (dX == 0) {
            activeViewHolder = null;
            isGuardThresholdReached = false;
            guardPopStartTime = 0;
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
