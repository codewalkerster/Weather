package com.hardkernel.odroid.weather;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MyLinearLayout extends LinearLayout {
    
    private boolean mTouching;

    public MyLinearLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mTouching = false;
    }
    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mTouching = false;
    }
    public MyLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mTouching = false;
    }
    

    long startTime;
    static final int MAX_DURATION = 200;
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub


        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startTime = System.currentTimeMillis();             
        }
        else if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            if(System.currentTimeMillis() - startTime <= MAX_DURATION)
            {
                mTouching = !mTouching;
            }       
        }
        return super.dispatchTouchEvent(ev);
    }
    
    public boolean isTouching() {
        return mTouching;
    }
}
