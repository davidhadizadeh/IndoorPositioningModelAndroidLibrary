package de.hadizadeh.positioning.roommodel.android;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import de.hadizadeh.positioning.roommodel.model.MapSegment;

import java.util.Calendar;

/**
 * Class for handling touch events on the room model map
 */
public class TouchView extends View {
    private static final int MAX_CLICK_DURATION = 200;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector scaleDetector;
    private float scale = 10.f;

    private float x;
    private float y;

    private float lastTouchX;
    private float lastTouchY;

    private OnTouchViewChangeListener listener;
    private float maxX;
    private float maxY;

    private long startClickTime;

    /**
     * Creates a touch view
     *
     * @param context activity context
     */
    public TouchView(Context context) {
        this(context, null, 0);
    }

    /**
     * Creates a touch view
     *
     * @param context activity context
     * @param attrs   attribute set
     */
    public TouchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a touch view
     *
     * @param context  activity context
     * @param attrs    attribute set
     * @param defStyle style id
     */
    public TouchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    /**
     * Called if a touch event happens
     *
     * @param ev event
     * @return touch event handled or not
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        scaleDetector.onTouchEvent(ev);

        boolean clicked = false;

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                lastTouchX = x;
                lastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                startClickTime = Calendar.getInstance().getTimeInMillis();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                if (!scaleDetector.isInProgress()) {
                    final float dx = x - lastTouchX;
                    final float dy = y - lastTouchY;
                    this.x -= dx / 100;
                    this.y -= dy / 100;

                    this.x = Math.max(0.0f, Math.min(this.x, maxX));
                    this.y = Math.max(0.0f, Math.min(this.y, maxY));

                    invalidate();
                }

                lastTouchX = x;
                lastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {
                    listener.onTouch(lastTouchX, lastTouchY);
                    clicked = true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = ev.getX(newPointerIndex);
                    lastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        if (!clicked) {
            listener.onTouchViewChange(this.x, this.y, scale);
        }
        return true;
    }

    /**
     * Sets the listener
     *
     * @param listener
     */
    public void setListener(OnTouchViewChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the maximum x value
     *
     * @param maxX maximum x value
     */
    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    /**
     * Sets the maximum y value
     *
     * @param maxY maximum y value
     */
    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    /**
     * Returns the current x value
     *
     * @return x value
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the current y value
     *
     * @return y value
     */
    public float getY() {
        return y;
    }

    /**
     * Sets the x value
     *
     * @param x x value
     */
    @Override
    public void setX(float x) {
        this.x = x;
        lastTouchX = x;
    }

    /**
     * Sets the y value
     *
     * @param y y value
     */
    @Override
    public void setY(float y) {
        this.y = y;
        lastTouchY = y;
    }

    /**
     * Returns the current scale
     *
     * @return scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets the current scale
     *
     * @param scale scale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max((float) MapSegment.getMinSize(), Math.min(scale, (float) MapSegment.getMaxSize()));
            invalidate();
            listener.onTouchViewChange(x, y, scale);
            return true;
        }
    }

    /**
     * Interface for handling touch event listeners
     */
    public interface OnTouchViewChangeListener {
        void onTouchViewChange(float x, float y, float scale);

        void onTouch(float x, float y);
    }
}
