package htwg.compsognathus.stringdala;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {

    float mStartDragX;
    OnSwipeOutListener mListener;

    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public void setOnSwipeOutListener(OnSwipeOutListener listener) {
        mListener = listener;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch(ev.getAction() & MotionEventCompat.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mStartDragX = ev.getX();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){

        if(getCurrentItem()==0 || getCurrentItem()==getAdapter().getCount()-1){
            final int action = ev.getAction();
            float x = ev.getX();
            switch(action & MotionEventCompat.ACTION_MASK){
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if (getCurrentItem()==0 && x>mStartDragX) {
                        mListener.onSwipeOutAtStart();
                    }
                    if (getCurrentItem()==getAdapter().getCount()-1 && x<mStartDragX){
                        mListener.onSwipeOutAtEnd();
                    }
                    break;
            }
        }else{
            mStartDragX=0;
        }
        return super.onTouchEvent(ev);

    }

    public interface OnSwipeOutListener {
        public void onSwipeOutAtStart();
        public void onSwipeOutAtEnd();
    }

}