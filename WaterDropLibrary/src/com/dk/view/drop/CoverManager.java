package com.dk.view.drop;

import java.lang.reflect.Field;

import com.dk.view.drop.DropCover.OnDragCompeteListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class CoverManager {
    private static CoverManager mCoverManager;
    private static Bitmap mDest;
    private DropCover mDropCover;
    private WindowManager mWindowManager;
    
    
	
	Handler handler = new Handler();

    private CoverManager() {

    }

    public WindowManager getWindowManager() {
        return mWindowManager;
    }

    public static CoverManager getInstance() {
        if (mCoverManager == null) {
            mCoverManager = new CoverManager();
        }
        return mCoverManager;
    }

    public void init(Activity activity) {
        if (mDropCover == null) {
            mDropCover = new DropCover(activity);
        }
        mDropCover.setStatusBarHeight(getStatusBarHeight(activity));
    }

    public void start(View target, float x, float y, DropCover.OnDragCompeteListener onDragCompeteListener) {
        if (mDropCover != null && mDropCover.getParent() == null) {
            mDropCover.setOnDragCompeteListener(onDragCompeteListener);
        } else {
            return;
        }

        mDest = drawViewToBitmap(target);
        target.setVisibility(View.INVISIBLE);
        mDropCover.setTarget(mDest);
        int[] locations = new int[2];
        target.getLocationOnScreen(locations);
        attachToWindow(target.getContext());
        mDropCover.init(locations[0], locations[1]);
    }

    public void update(float x, float y) {
        mDropCover.update(x, y);
    }

    public void finish(final View target, float x, float y) {
        
        mDropCover.setOnDragCompeteListener(new OnDragCompeteListener() {
			
			@Override
			public void onDrag(final float slope) {
				// TODO Auto-generated method stub
				System.out.println("slope:"+slope);
				SpringSystem springSystem = SpringSystem.create();
				final Spring spring = springSystem.createSpring();
				final float minOffset = -5;
				final float maxOffset = 5;
				SpringListener listener = new SpringListener() {
					
					@Override
					public void onSpringUpdate(Spring spring) {
						// TODO Auto-generated method stub
						float value = (float) spring.getCurrentValue();
						float transX = (float) ((maxOffset - minOffset)*value*Math.cos(slope));
						float transY = (float) ((maxOffset - minOffset)*value*Math.sin(slope));
						ViewHelper.setTranslationX(target, transX);
						ViewHelper.setTranslationY(target, transY);
					}

					@Override
					public void onSpringAtRest(Spring spring) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onSpringActivate(Spring spring) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onSpringEndStateChange(Spring spring) {
						// TODO Auto-generated method stub
						
					}
				};
				spring.setCurrentValue(0).setEndValue(0).addListener(listener);
				SpringConfig springConfig = spring.getSpringConfig();
				springConfig.friction = 1.9f;
				springConfig.tension = 800;
				
				spring.setEndValue(1);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						spring.setEndValue(0);
					}
				}, 100);
				/*if(Math.abs(slope) > 1){
					Animation animation = AnimationUtils.loadAnimation(target.getContext(), R.anim.shake_y);
					target.startAnimation(animation);
				}else{
					Animation animation = AnimationUtils.loadAnimation(target.getContext(), R.anim.shake_x);
					target.startAnimation(animation);
				}*/
			}
		});
        mDropCover.finish(target, x, y);
    }
    
    private Bitmap drawViewToBitmap(View view) {
        if (mDropCover == null) {
            mDropCover = new DropCover(view.getContext());
        }
        int width = view.getWidth();
        int height = view.getHeight();
        if (mDest == null || mDest.getWidth() != width || mDest.getHeight() != height) {
            mDest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        Canvas c = new Canvas(mDest);
        view.draw(c);
        return mDest;
    }

    private void attachToWindow(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mDropCover == null) {
            mDropCover = new DropCover(context);
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mWindowManager.addView(mDropCover, params);
    }

    public boolean isRunning() {
        if (mDropCover == null) {
            return false;
        } else if (mDropCover.getParent() == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * please call it before animation start
     * 
     * Notice: the unit is frame.
     * 
     * @param maxDistance
     */
    public void setExplosionTime(int lifeTime) {
        Particle.setLifeTime(lifeTime);
    }

    public void setMaxDragDistance(int maxDistance) {
        if (mDropCover != null) {
            mDropCover.setMaxDragDistance(maxDistance);
        }
    }

    public static int getStatusBarHeight(Activity activity) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;// Ĭ��Ϊ38��ò�ƴ󲿷��������?

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = activity.getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }
}
