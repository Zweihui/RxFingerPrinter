package com.zwh.rxfingerprinter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Zhangwh on 2016/12/29 0029.
 * email:616505546@qq.com
 */

public class FingerPrinterView extends View{

  public final static int STATE_NO_SCANING = 0;

  public final static int STATE_WRONG_PWD = 1;

  public final static int STATE_CORRECT_PWD = 2;

  public final static int STATE_SCANING = 3;

  public int mCurrentState = STATE_NO_SCANING;
  private Resources mResources;

  public static int DEFAULT_DURATION = 700;
  private Animation mShakeAnim = null; // 抖动的动画
  ValueAnimator valueAnimator;
  private float mFraction = 0f,mFraction2 = 1f;
  private int scaningCount = 0;
  float scale =1.0f;
  private boolean isAnim=true; //判断是否要继续动画
  private boolean isScale=false; //判断是否要缩放
  private Paint mBitPaint;
  private Bitmap mFingerRed,mFingerGreen, mFingerGrey;
  private Rect mSrcRect, mDestRect;
  private int mBitWidth, mBitHeight;
  private int mWidth, mHeight;
  OnStateChangedListener listener;

  public FingerPrinterView(Context context) {
    this(context,null);
  }

  public FingerPrinterView(Context context, AttributeSet attrs) {
    this(context, attrs,0);
  }

  public FingerPrinterView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mResources = getResources();
    //mShakeAnim = AnimationUtils.loadAnimation(context, R.anim.anim_lockpattern_shake_x);
    initBitmap();
    initPaint();
  }

  private void initPaint() {
    mBitPaint = new Paint();
    // 防抖动
    mBitPaint.setDither(true);
    // 开启图像过滤
    mBitPaint.setFilterBitmap(true);
  }

  private void initBitmap() {
    mFingerRed = ((BitmapDrawable) mResources.getDrawable(R.drawable.finger_red)).getBitmap();
    mFingerGreen = ((BitmapDrawable) mResources.getDrawable(R.drawable.finger_green)).getBitmap();
    mFingerGrey = ((BitmapDrawable) mResources.getDrawable(R.drawable.finger_grey)).getBitmap();
    mBitWidth = mFingerRed.getWidth();
    mBitHeight = mFingerRed.getHeight();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mWidth = w;
    mHeight = h;
    mFingerRed = setScale(mFingerRed);
    mFingerGreen = setScale(mFingerGreen);
    mFingerGrey = setScale(mFingerGrey);
    mBitWidth = mFingerRed.getWidth();
    mBitHeight = mFingerRed.getHeight();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mBitPaint.setAlpha(255);
    mDestRect = new Rect((int) (mBitHeight*(1-mFraction2)), (int) (mBitHeight*(1-mFraction2)), (int) (mBitHeight*mFraction2), (int) (mBitHeight*mFraction2));
    mSrcRect = new Rect(0, 0, mBitWidth, mBitHeight);
    canvas.drawBitmap(mFingerGrey, mSrcRect, mDestRect, mBitPaint);
    if(true){
      if(scaningCount == 0){
        mDestRect = new Rect(0, 0, mBitWidth, (int) (mBitHeight*mFraction));
        mSrcRect = new Rect(0, 0, mBitWidth, (int) (mBitHeight*mFraction));
        canvas.drawBitmap(mFingerGreen, mSrcRect, mDestRect, mBitPaint);
      }else if(scaningCount%2==0){
        if(mFraction<=0.5){
          mBitPaint.setAlpha((int) (255*(1-mFraction)));
          canvas.drawBitmap(mFingerRed, mSrcRect, mDestRect, mBitPaint);
        }else{
          mBitPaint.setAlpha((int) (255*mFraction));
          canvas.drawBitmap(mFingerGreen, mSrcRect, mDestRect, mBitPaint);
        }
      }else{
          if(mFraction<=0.5){
            mBitPaint.setAlpha((int) (255*(1-mFraction)));
            canvas.drawBitmap(mFingerGreen, mSrcRect, mDestRect, mBitPaint);
          }else{
            mBitPaint.setAlpha((int) (255*mFraction));
            canvas.drawBitmap(mFingerRed, mSrcRect, mDestRect, mBitPaint);
          }

      }

    }
    if(isScale){
      if(mCurrentState == STATE_WRONG_PWD){
        canvas.drawBitmap(mFingerRed, mSrcRect, mDestRect, mBitPaint);
      }
      if(mCurrentState == STATE_CORRECT_PWD){
        canvas.drawBitmap(mFingerGreen, mSrcRect, mDestRect, mBitPaint);
      }
    }
  }

  public void startScaning() {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.f, 100.f);
    valueAnimator.setDuration(DEFAULT_DURATION);
    valueAnimator.setInterpolator(new LinearInterpolator());
    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @SuppressLint("NewApi")
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        mFraction = valueAnimator.getAnimatedFraction();
        invalidate();
      }
    });
    valueAnimator.addListener(new Animator.AnimatorListener() {
      @Override public void onAnimationStart(Animator animator) {
        if(isScale){
          isScale = false;
        }
      }

      @Override public void onAnimationEnd(Animator animator) {
        mFraction = 0;
        scaningCount++;
        if(mCurrentState == STATE_WRONG_PWD&&scaningCount%2==1){
          isScale = true;
          isAnim = false;
          startScale();
        }
        if(mCurrentState == STATE_CORRECT_PWD&&scaningCount%2==0){
          isScale = true;
          isAnim = false;
          startScale();
        }
        if(isAnim){
          startScaning();
        }

      }

      @Override public void onAnimationCancel(Animator animator) {

      }

      @Override public void onAnimationRepeat(Animator animator) {

      }
    });
    if (!valueAnimator.isRunning()) {
      valueAnimator.start();
    }
  }

  private void startScale() {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.f, 100.f);
    valueAnimator.setDuration(500);
    valueAnimator.setInterpolator(new OvershootInterpolator(1.2f));
    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        mFraction2 = 0.85f+0.15f*valueAnimator.getAnimatedFraction();
        invalidate();
      }
    });
    valueAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        if(listener!=null){
          listener.onChange(mCurrentState);
        }
      }
    });
    if (!valueAnimator.isRunning()) {
      valueAnimator.start();
    }
  }

  private void startReset() {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.f, 100.f);
    valueAnimator.setDuration(DEFAULT_DURATION);
    valueAnimator.setInterpolator(new LinearInterpolator());
    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        mFraction = 1-valueAnimator.getAnimatedFraction();
        invalidate();
      }
    });
    valueAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);

        startScaning();
      }
    });
    if (!valueAnimator.isRunning()) {
      valueAnimator.start();
    }
  }

  public void setState(int state) {
    mCurrentState = state;
    switch (state) {
      case STATE_SCANING:
        startScaning();
        break;
      case STATE_WRONG_PWD:

        break;
      case STATE_CORRECT_PWD:

        break;
      case STATE_NO_SCANING:
        resetConfig();
        break;
      default:
        break;
    }
  }

  public int getState(){
    return mCurrentState;
  }

  /**
   * 处理图片缩放
   */
  private Bitmap setScale(Bitmap a) {
    scale = ((float)(mWidth) / mBitWidth);
    Matrix mMatrix = new Matrix();
    mMatrix.postScale(scale, scale);
    Bitmap bmp = Bitmap.createBitmap(a, 0, 0, a.getWidth(), a.getHeight(),
        mMatrix, true);
    return bmp;
  }

  public void resetConfig(){
    mCurrentState = STATE_NO_SCANING;
    startReset();
    mFraction = 0f;
    mFraction2 = 1f;
    scaningCount = 0;
    scale =1.0f;
    isAnim=true;
    isScale=false;

  }

  public void setOnStateChangedListener(OnStateChangedListener listener){
    this.listener = listener;
  }

  public interface OnStateChangedListener {
    public void onChange(int state);
  }

}
