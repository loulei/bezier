package com.dk.view.drop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.os.Build.VERSION;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class DropCover extends SurfaceView implements SurfaceHolder.Callback {

    private static final int EXPLOSION_SIZE = 200;
    private int mMaxDistance = 200;

    private ExplosionUpdateThread mThread;
    private Explosion mExplosion;

    private float mBaseX;
    private float mBaseY;

    private float mTargetX;
    private float mTargetY;

    private Bitmap mDest;
    private Paint mPaint = new Paint();

    private float targetWidth;
    private float targetHeight;
    private float mRadius = 0;
    private float mStrokeWidth = 20;
    private float mBaseRadius = 20;
    private boolean isDraw = true;
    private float mStatusBarHeight = 0;
    
    private float slope = 0;
    private OnDragCompeteListener mOnDragCompeteListener;

    public interface OnDragCompeteListener {
        void onDrag(float slope);
    }

    @SuppressLint("NewApi")
    public DropCover(Context context) {
        super(context);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);
        setFocusable(false);
        setClickable(false);
        mPaint.setAntiAlias(true);
        if (VERSION.SDK_INT > 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    /**
     * draw drop and line
     */
    private void drawDrop() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

            if (isDraw) {
                double distance = Math.sqrt(Math.pow(mBaseX - mTargetX, 2) + Math.pow(mBaseY - mTargetY, 2));
                mPaint.setColor(0xffff0000);
                if (distance < mMaxDistance) {
                    mStrokeWidth = Math.max(mBaseRadius,(float) ((1f - distance / mMaxDistance) * mRadius));
                    mPaint.setStrokeWidth(mStrokeWidth);
                    //canvas.drawCircle(mBaseX, mBaseY, mStrokeWidth / 2, mPaint);
                    // canvas.drawLine(mBaseX, mBaseY, mTargetX + targetWidth /
                    // 2, mTargetY + targetHeight / 2, mPaint);
                    drawBezier(canvas);
                }
                //canvas.drawBitmap(mDest, mTargetX, mTargetY, mPaint);
               canvas.drawCircle(mTargetX, mTargetY, mDest.getWidth()/2, mPaint);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void drawBezier(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        Point[] points = calculate(new Point(mBaseX, mBaseY), new Point(mTargetX, mTargetY));

        float centerX = (points[0].x + points[1].x + points[2].x + points[3].x) / 4f;
        float centerY = (points[0].y + points[1].y + points[2].y + points[3].y) / 4f;
        mPaint.setStrokeWidth(1);
        
        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);
        path.quadTo(centerX, centerY, points[1].x, points[1].y);
        path.lineTo(points[2].x, points[2].y);
        path.quadTo(centerX, centerY, points[3].x, points[3].y);
        path.lineTo(points[0].x, points[0].y);
        canvas.drawPath(path, mPaint);
        canvas.drawCircle((points[0].x+points[3].x)/2, (points[0].y+points[3].y)/2, mStrokeWidth/2, mPaint);
        canvas.drawCircle((points[1].x+points[2].x)/2, (points[1].y+points[2].y)/2, mDest.getWidth()/2, mPaint);
    }

    /**
     * ax=by=0 x^2+y^2=s/2
     * 
     * ==>
     * 
     * x=a^2/(a^2+b^2)*s/2
     * 
     * @param start
     * @param end
     * @return
     */
    private Point[] calculate(Point start, Point end) {
    	slope = (float) Math.atan((end.y-start.y)/(end.x-start.x));
    	float offsetX = (float) (mStrokeWidth/2 * Math.sin(slope));
    	float offsetY = (float) (mStrokeWidth/2 * Math.cos(slope));
    	
    	float offsetX1 = (float) (mDest.getWidth()/2 * Math.sin(Math.atan((end.y-start.y)/(end.x-start.x))));
    	float offsetY1 = (float) (mDest.getWidth()/2 * Math.cos(Math.atan((end.y-start.y)/(end.x-start.x))));
    	Point[] result = new Point[4];
    	result[0] = new Point(start.x - offsetX, start.y + offsetY);
    	result[1] = new Point(end.x - offsetX1, end.y + offsetY1);
    	result[2] = new Point(end.x + offsetX1, end.y - offsetY1);
    	result[3] = new Point(start.x + offsetX, start.y - offsetY);
        return result;
    }

    public void setTarget(Bitmap dest) {
        mDest = dest;
        targetWidth = dest.getWidth();
        targetHeight = dest.getHeight();

        mRadius = dest.getWidth() / 2;
        mStrokeWidth = mRadius;
    }

    public void init(float x, float y) {
        mBaseX = x + mDest.getWidth() / 2f;
        mBaseY = y - mDest.getWidth() / 2f;
        mTargetX = x;
        mTargetY = y - mStatusBarHeight;

        isDraw = true;
        drawDrop();
    }

    /**
     * move the drop
     * 
     * @param x
     * @param y
     */
    public void update(float x, float y) {

        mTargetX = x;
        mTargetY = y - mStatusBarHeight;
        drawDrop();
    }

    /**
     * reset datas
     */
    public void clearDatas() {
        mBaseX = -1;
        mBaseY = -1;
        mTargetX = -1;
        mTargetY = -1;
        mDest = null;
    }

    /**
     * remove DropCover
     */
    public void clearViews() {
        if (getParent() != null) {
            CoverManager.getInstance().getWindowManager().removeView(this);
        }
    }

    /**
     * finish drag event and start explosion
     * 
     * @param target
     * @param x
     * @param y
     */
    public void finish(View target, float x, float y) {
        double distance = Math.sqrt(Math.pow(mBaseX - mTargetX, 2) + Math.pow(mBaseY - mTargetY, 2));

        clearDatas();
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
            getHolder().unlockCanvasAndPost(canvas);
        }
        if (distance > mMaxDistance) {
            initExplosion(x, y);

            mThread = new ExplosionUpdateThread(getHolder(), this);
            mThread.setRunning(true);
            mThread.start();
        } else {
        	if (mOnDragCompeteListener != null)
                mOnDragCompeteListener.onDrag(slope);
            clearViews();
            target.setVisibility(View.VISIBLE);
        }

        isDraw = false;
    }

    public void setStatusBarHeight(int statusBarHeight) {
        mStatusBarHeight = statusBarHeight;
    }

    public void setOnDragCompeteListener(OnDragCompeteListener onDragCompeteListener) {
        mOnDragCompeteListener = onDragCompeteListener;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawDrop();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mThread != null) {
            mThread.setRunning(false);
            mThread = null;
        }
    }

    /**
     * init the explosion whit start position
     * 
     * @param x
     * @param y
     */
    public void initExplosion(float x, float y) {
        if (mExplosion == null || mExplosion.getState() == Explosion.STATE_DEAD) {
            mExplosion = new Explosion(EXPLOSION_SIZE, (int) x, (int) y);
        }
    }

    /**
     * call it to draw explosion
     * 
     * @param canvas
     * @return isAlive
     */
    public boolean render(Canvas canvas) {
        boolean isAlive = false;
        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        canvas.drawColor(Color.argb(0, 0, 0, 0)); // To make canvas transparent
        // render explosions
        if (mExplosion != null) {
            isAlive = mExplosion.draw(canvas);
        }
        return isAlive;
    }

    /**
     * update explosion
     */
    public void update() {
        // update explosions
        if (mExplosion != null && mExplosion.isAlive()) {
            mExplosion.update(getHolder().getSurfaceFrame());
        }
    }

    // private void displayFps(Canvas canvas, String fps) {
    // if (canvas != null && fps != null) {
    // Paint paint = new Paint();
    // paint.setARGB(255, 255, 255, 255);
    // canvas.drawText(fps, this.getWidth() - 50, 20, paint);
    // }
    // }

    /**
     * please call it before animation start
     * 
     * @param maxDistance
     */
    public void setMaxDragDistance(int maxDistance) {
        mMaxDistance = maxDistance;
    }

    class Point {
        float x, y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
