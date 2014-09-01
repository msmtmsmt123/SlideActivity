package org.wangfan.lightwb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * 
 * ����ʵ�ֿ��Ի�����Activity��
 * <p>
 * ʹ�÷����� </br>
 * <ul>
 * 1.����Ҫ�����رյ�Activity�̳�SlideActivity��</br>
 * 2.��AndroidManifest.xml��ΪActivity���á�����͸���������⣨&lt;item
 * name=��android:windowIsTranslucent��&gt;true&lt;/item&gt;����</br> 3.ʹ��
 * {@link #setLeftShadow(Drawable)}������Ӱ��
 * </ul>
 * </p>
 * <p>
 * ���⣬����{@link #drawShadow(Canvas, View, Drawable)}����ʵ���Զ�����Ӱ���ơ�����
 * {@link #computeSpringbackBoundary(int)}�����Զ���ص�����
 * </p>
 * 
 * @author wangfan </br>wangfansh@foxmail.com
 * 
 */
public class SlideActivity extends Activity {
    /** ���Fling�ٶȴ��ڴ��ٶȣ���ر�Activity */
    public static final int MIN_CLOSE_VELOCITY = 1000;
    /** fling�����ر�ʱ���ٶ� */
    public static final int FLING_CLOSE_VELOCITY = 800;
    /** �����ص�ʱ���ٶ� */
    public static final int SPRINGBACK_VELOCITY = 700;
    /** ����ʱ�ĵײ㱳��RGB��ɫ */
    public static final int DEFAULT_BACKGROUND_RGB = 0xFF000000;
    /** ����ʱ͸���Ƚ������ʼAlphaֵ */
    public static final int DEFAULT_ALPHA = 160;
    private ViewGroup mDecorView;
    private Window mWindow;
    private GestureDetector mGD;
    private OverScroller mScroller;
    private boolean mIsFirstScroll = true;
    private SlideLayout mSlideLayout;
    private Drawable mLeftShadow;
    // �����Ƕ��ж�
    private MoveDetector mMoveDetector;
    @SuppressWarnings("unused")
    private boolean mAllowSlide = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    /**
     * ��ȡ��ǰ������Ӱ��
     * 
     * @return ��ǰ������Ӱ
     */
    public Drawable getLeftShadow() {
        return mLeftShadow;
    }

    /**
     * ������Ҫ��ʾ��������Ӱ��Ĭ������Ӱ��ʾ��
     * 
     * @param mLeftShadow
     *            ��ӰDrawable
     */
    public void setLeftShadow(Drawable mLeftShadow) {
        this.mLeftShadow = mLeftShadow;
    }

    // ���û��������࣬���뵽DecorView�������ݵ��С���Ӧ�������ƣ�ִ�л�����������Ӱ��
    class SlideLayout extends FrameLayout {
        public SlideLayout(Context context) {
            super(context);
            init();
        }

        // ��ʼ����
        // �滻��DecorView�����ݵ��в����Զ���Layout������ʵ����߿���Ӱ��
        private void init() {
            View DecorLayout = mDecorView.getChildAt(0);
            mDecorView.removeView(DecorLayout);
            this.addView(DecorLayout);
            mDecorView.addView(this);
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            boolean superReturn = super.drawChild(canvas, child, drawingTime);
            drawShadow(canvas, child, mLeftShadow);
            return superReturn;
        }

        // ���غ��򻬶���
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            int moveStatus = mMoveDetector.getMoveStatus(ev);
            if (moveStatus == MoveDetector.HOR) {
                return true;
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
                if (-mSlideLayout.getScrollX() < computeSpringbackBoundary(mDecorView
                        .getWidth())) {
                    mScroller.startScroll(mSlideLayout.getScrollX(), 0,
                            -mSlideLayout.getScrollX(), 0, SPRINGBACK_VELOCITY);
                } else {
                    mScroller.startScroll(mSlideLayout.getScrollX(), 0,
                            -(mDecorView.getWidth() - Math.abs(mSlideLayout
                                    .getScrollX())), 0, SPRINGBACK_VELOCITY);
                }
                ViewCompat.postOnAnimation(mDecorView, ViewScroller);
                mIsFirstScroll = true;
            }
            mGD.onTouchEvent(ev);
            return true;
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            int color = computeBackgroundArgb(l, mDecorView.getWidth(),
                    DEFAULT_ALPHA, DEFAULT_BACKGROUND_RGB);
            this.setBackgroundColor(color);
        }
    }

    /**
     * ����ʱ���㱳��argb��ɫ�����Ǵ˷���ʵ���Զ�����㡣
     * 
     * @param scrollX
     *            ��ǰˮƽ����ֵ��
     * @param totalWidth
     *            ���Ի������ܿ�ȡ�
     * @param maxAlpha
     *            alpha���ֵ����Χ0��255��Ĭ��ֵ160��
     * @param baseBgRgb
     *            ������ɫ,��͸����Ĭ��ֵ0xFF000000;
     * @return ���غϳɵ�argbֵ��
     */
    protected int computeBackgroundArgb(int scrollX, int totalWidth,
            int maxAlpha, int baseBgRgb) {
        float percent = Math.abs(scrollX) / (float) totalWidth;
        int alpha = (int) ((1 - percent) * maxAlpha);
        return (alpha << 24) | (baseBgRgb & 0x00FFFFFF);
    }

    /**
     * ����ص����ߡ����Ǵ˷�����ʵ���Լ��Ļص����޼��㷽���� Ĭ�ϵ�ʵ����DecorView��һ�롣
     * 
     * @param DecorViewWidth
     *            DecorView���
     * @return �ص�����
     */
    protected int computeSpringbackBoundary(int DecorViewWidth) {
        return DecorViewWidth / 2;
    }

    /**
     * ������Ӱ�����Ǵ˷�������ʵ���Լ��Ļ��ơ�
     * 
     * @param canvas
     * @param child
     *            Activity�����ݲ��֡�
     * @param leftShadow
     *            �������ӰͼƬ���μ���{@link setLeftShadow}
     */
    protected void drawShadow(Canvas canvas, View child, Drawable leftShadow) {
        if (leftShadow != null) {
            leftShadow.setBounds(-leftShadow.getIntrinsicWidth(),
                    child.getTop(), 0, child.getBottom());
            leftShadow.draw(canvas);
        }
    }

    // Activity��ʼ��
    private void init() {
        mWindow = this.getWindow();
        mDecorView = (ViewGroup) this.getWindow().getDecorView();
        mDecorView.setBackgroundColor(Color.TRANSPARENT);
        WindowManager.LayoutParams wlp = mWindow.getAttributes();
        wlp.format = PixelFormat.TRANSLUCENT;// window�ı���Ҳ����͸��
        mWindow.setAttributes(wlp);
        mGD = new GestureDetector(this, new MyGestureListener());
        mScroller = new OverScroller(this);
        mSlideLayout = new SlideLayout(this);
        mMoveDetector = new MoveDetector(this);
    }

    // ���������ִ�й�����
    Runnable ViewScroller = new Runnable() {
        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                mSlideLayout.scrollTo(mScroller.getCurrX(), 0);
                if (mScroller.getCurrX() <= -mDecorView.getWidth())
                    finishActivity();
                ViewCompat.postOnAnimation(mDecorView, this);
            }
        }
    };

    /**
     * �޶����ر�Activity
     */
    public void finishActivity() {
        this.finish();
        this.overridePendingTransition(0, 0);
    }

    // ���������ж���
    class MoveDetector {
        // δʶ����
        public static final int UNKNOWN = 0;
        // ����
        public static final int HOR = 1;
        // ����
        public static final int VER = 2;
        // Ĭ�����Ƕȡ����С������Ƕȣ����ж�Ϊ���򻬶���������Ϊ���򻬶���
        public static final int DEFAULT_MAX_ANGLE = 30;
        private float downX;
        private float downY;
        private float moveOffsetX;
        private float moveOffsetY;
        private int status;
        private int maxInterceptAngle;
        private int touchSlop;

        public MoveDetector(Context context) {
            this(context, DEFAULT_MAX_ANGLE);
        }

        public MoveDetector(Context context, int maxInterceptAngle) {
            this.maxInterceptAngle = maxInterceptAngle;
            this.touchSlop = ViewConfiguration.get(context)
                    .getScaledTouchSlop();
        }

        public int getMaxInterceptAngle() {
            return maxInterceptAngle;
        }

        public void setMaxInterceptAngle(int maxInterceptAngle) {
            this.maxInterceptAngle = maxInterceptAngle;
        }

        // ����TouchEvent�жϻ�������
        public int getMoveStatus(MotionEvent ev) {
            switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                status = UNKNOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                moveOffsetX = ev.getRawX() - downX;
                moveOffsetY = ev.getRawY() - downY;
                if ((moveOffsetX * moveOffsetX + moveOffsetY * moveOffsetY) < (touchSlop * touchSlop))
                    return status;
                double angle = Math.atan2(Math.abs(moveOffsetY),
                        Math.abs(moveOffsetX));
                double angle2 = 180 * angle / Math.PI;
                if (angle2 < maxInterceptAngle)
                    status = HOR;
                else
                    status = VER;
                break;
            }
            return status;
        }
    }

    // ����ʶ����
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            if (mSlideLayout.getScrollX() >= 0 && distanceX > 0)
                return false;
            // �������ٻ���ʱ�����ٵ����⡣����touchSlop���ƶ����롣
            if (mIsFirstScroll && -distanceX > 1) {
                distanceX = 0;
                mIsFirstScroll = false;
            }
            mSlideLayout.scrollBy((int) distanceX, 0);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if (velocityX > MIN_CLOSE_VELOCITY) {
                mScroller.abortAnimation();
                mScroller.startScroll(mSlideLayout.getScrollX(), 0,
                        -(mDecorView.getWidth() - Math.abs(mSlideLayout
                                .getScrollX())), 0, FLING_CLOSE_VELOCITY);
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
