//package com.example.mifans.myimageload;
//
//import android.animation.AnimatorSet;
//import android.animation.ValueAnimator;
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Bitmap;
//import android.graphics.BitmapShader;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.RectF;
//import android.graphics.Shader;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.os.Build;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.animation.AnimationSet;
//import android.view.animation.LinearInterpolator;
//
//
//
//public class CircleView extends android.support.v7.widget.AppCompatImageView {
//    private Context mContext;
//
//
//
//
//    //默认不带圆框
//    private static final boolean HAS_BOX = false;
//    //默认的圆框颜色
//    private static final int BOX_COLOR = Color.BLUE;
//    //默认的圆框宽度
//    private static final int BOX_WIDTH = 2;
//    private boolean hasbox;
//    private int boxcolor;
//    private int boxwidth;
//
//    private Paint mbox;//画圆框的画笔
//    private Paint mcircle;//画动画的画笔；
//    private Paint mBitmapPaint;
//    //圆形半径
//    private int mRadius;
//    private float mradiusAnim;
//    //矩阵，主要用于缩小放大
//    private Matrix mMatrix;
//
//    //着色器
//    private BitmapShader mBitmapShader;
//
//    //view宽度
//    private int mWidth;
//    //控制圆框大小的动画
//    private ValueAnimator boxsizeAnim;
//    //控制原线颜色的动画
//    private ValueAnimator circleAnim;
//    //动画集合
//    private AnimatorSet animatorSet;
//
//    private  boolean hasDrawBitmap = false;
//
//    public CircleView(Context context) {
//        this(context, null);
//    }
//
//    public CircleView(Context context, AttributeSet attrs) {
//
//        this(context, attrs, 0);
//
//    }
//
//    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//
//        this.mContext = context;
//
//        init(context, attrs);
//
//    }
//
//    public void init(Context context, AttributeSet attrs) {
//        mMatrix = new Matrix();
//        mbox = new Paint();
//        mcircle = new Paint();
//        mcircle.setStyle(Paint.Style.STROKE);
//        mcircle.setStrokeWidth(4);
//        mbox.setStyle(Paint.Style.STROKE);
//        mBitmapPaint = new Paint();
//        mBitmapPaint.setAntiAlias(true);
//
//        TypedArray typedArray = context.obtainStyledAttributes(attrs,
//                R.styleable.CircleView);
//
////        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
////                        BODER_RADIUS_DEFAULT,
////                        getResources().getDisplayMetrics());将10dp转为安卓标准单位px
//        hasbox = typedArray.getBoolean(
//                R.styleable.CircleView_hasbox,
//                HAS_BOX);// 默认为不带圆框
//        boxcolor = typedArray.getInt(R.styleable.CircleView_boxcolor, BOX_COLOR);// 默认为蓝色
//        boxwidth = typedArray.getDimensionPixelSize(
//                R.styleable.CircleView_boxwidth,
//                BOX_WIDTH);
//        mbox.setStrokeWidth(boxwidth);
//        mbox.setColor(boxcolor);
//        mcircle.setColor(boxcolor);
//        typedArray.recycle();
//
//    }
//
//    @TargetApi(Build.VERSION_CODES.O)
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        mWidth = Math.min(getWidth(), getHeight());
//        mRadius = mWidth / 2;
//        if(hasbox) {
//            initAnimator();
//        }
//    }
//
//    /**
//     * 初始化BitmapShader
//     */
//    private void initBitmapShader() {
//        Drawable drawable = getDrawable();
//        if (drawable == null) {
//            return;
//        }
//
//        Bitmap bmp = drawableToBitamp(drawable);
//        // 将bmp作为着色器，就是在指定区域内绘制bmp
//        mBitmapShader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//        float scale = 1.0f;
//
//
//        // 拿到bitmap宽或高的小值
//        int bSize = Math.min(bmp.getWidth(), bmp.getHeight());
//        scale = mWidth * 1.0f / bSize;
//
//
//        // shader的变换矩阵，我们这里主要用于放大或者缩小
//        mMatrix.setScale(scale, scale);
//
//        // 设置变换矩阵
//        mBitmapShader.setLocalMatrix(mMatrix);
//        // 设置shader
//        mBitmapPaint.setShader(mBitmapShader);
//
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        if (getDrawable() == null) {
//            return;
//        }
//        initBitmapShader();
//        //画圆形图片
//
//
//        //如果由外框，画外框,
//        if (hasbox) {
//            //给外框留下空间
//
//            canvas.drawCircle(mRadius, mRadius, mRadius - 80-boxwidth, mbox);
//            canvas.drawCircle(mRadius, mRadius, mRadius - 80-boxwidth, mBitmapPaint);
//            hasDrawBitmap = true;
//
//            canvas.drawCircle(mRadius,mRadius,mradiusAnim,mcircle);
//
//        } else {
//            canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
//        }
//
//
//
//    }
//
//
////    @Override
////    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
////        super.onSizeChanged(w, h, oldw, oldh);
////
////        // 圆角图片的范围
////        if (type == TYPE_ROUND)
////            mRoundRect = new RectF(0, 0, w, h);
////    }
//
//    private Bitmap drawableToBitamp(Drawable drawable) {
//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable bd = (BitmapDrawable) drawable;
//            return bd.getBitmap();
//        }
//        int w = drawable.getIntrinsicWidth();
//        int h = drawable.getIntrinsicHeight();
//        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, w, h);
//        drawable.draw(canvas);
//        return bitmap;
//
//    }
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void initAnimator(){
//        boxsizeAnim = ValueAnimator.ofFloat(mRadius-80-boxwidth,mRadius-2);
////        boxsizeAnim.setDuration(1000);
//        boxsizeAnim.setRepeatMode(ValueAnimator.RESTART);
//        boxsizeAnim.setRepeatCount(ValueAnimator.INFINITE);
////        boxsizeAnim.setInterpolator(new LinearInterpolator());
//        boxsizeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                mradiusAnim = (float) animation.getAnimatedValue();
//                invalidate();
//                Log.d("testanim", String.valueOf((float)animation.getAnimatedValue()));
//            }
//        });
//        circleAnim  = ValueAnimator.ofArgb(boxcolor,0x00FFFFFF);
//        circleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                mcircle.setColor((Integer) animation.getAnimatedValue());
//
//            }
//        });
//        circleAnim.setRepeatCount(ValueAnimator.INFINITE);
//        circleAnim.setRepeatMode(ValueAnimator.RESTART);
//        animatorSet = new AnimatorSet();
//        animatorSet.setDuration(1000);
//        animatorSet.setInterpolator(new LinearInterpolator());
//        animatorSet.play(boxsizeAnim).with(circleAnim);
//        animatorSet.start();
//
//    }
//
//
//}