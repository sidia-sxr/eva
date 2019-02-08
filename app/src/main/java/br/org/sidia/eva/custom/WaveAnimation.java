package br.org.sidia.eva.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.Choreographer;
import android.view.animation.DecelerateInterpolator;

public class WaveAnimation extends Drawable implements Animatable, ValueAnimator.AnimatorUpdateListener {

    private static final float WAVE_HEIGHT_FACTOR = 0.2f;
    private static final float WAVE_SPEED_FACTOR = 0.02f;
    private static final int UNDEFINED_VALUE = Integer.MIN_VALUE;
    private int mWidth, mHeight;
    private int mWaveHeight = UNDEFINED_VALUE;
    private int mWaveLength = UNDEFINED_VALUE;
    private int mWaveStep = UNDEFINED_VALUE;
    private int mWaveOffset = 0;
    private int mWaveLevel = 0;
    private float mProgress = 0;
    private ValueAnimator mValueAnimator = null;
    private Drawable mDrawable;
    private Paint mPaint;
    private Bitmap mMask;
    private Matrix mMatrix = new Matrix();
    private boolean mRunning = false;
    private ColorFilter mColorFilter = null;
    private float mtimeAnimation = 0;
    private Context mContext;

    private Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long l) {
            invalidateSelf();
            if (mRunning) {
                Choreographer.getInstance().postFrameCallback(this);
            }
        }
    };

    public WaveAnimation(Context context, int imgRes) {
        mContext = context;
        init(imgRes);
        start();
        setAmplitude(3);
        setProgress(0);
    }

    private void init(int imRes) {
        setBackground(imRes);
    }

    public void setBackground(int Res) {
        Drawable drawable;
        drawable = mContext.getDrawable(Res);


        final PorterDuffXfermode mPorterDuffmode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        mDrawable = drawable;
        mMatrix.reset();
        mPaint = new Paint();
        mPaint.setFilterBitmap(false);
        mPaint.setColor(Color.BLACK);
        mPaint.setXfermode(mPorterDuffmode);

        mWidth = mDrawable.getIntrinsicWidth();
        mHeight = mDrawable.getIntrinsicHeight();

        if (mWidth > 0 && mHeight > 0) {
            mWaveLength = mWidth;
            mWaveHeight = Math.max(8, (int) (mHeight * WAVE_HEIGHT_FACTOR));
            mWaveStep = Math.max(1, (int) (mWidth * WAVE_SPEED_FACTOR));
            updateBackground(mWidth, mWaveLength, mWaveHeight);

        }
        invalidateSelf();

    }

    public void customWaveAnimation() {
        // setAmplitude(2);
        // setLength(3);
        // setSpeed(2);
    }

    public void setSpeed(int step) {
        mWaveStep = Math.min(step * 2, mWidth / 2);

    }

    public void setAmplitude(int amplitude) {
        amplitude = Math.max(1, Math.min(amplitude, mHeight / 2));
        int height = amplitude * 2;
        if (mWaveHeight != height) {
            mWaveHeight = height;
            updateBackground(mWidth, mWaveLength, mWaveHeight);
            invalidateSelf();
        }
    }

    public void setLength(int length) {
        length = Math.max(8, Math.min(mWidth * 2, length * 60));
        if (length != mWaveLength) {
            mWaveLength = length;
            updateBackground(mWidth, mWaveLength, mWaveHeight);
            invalidateSelf();
        }
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mDrawable.setBounds(left, top, right, bottom);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateBounds(bounds);
    }

    private void updateBounds(Rect bounds) {
        if (bounds.width() <= 0 || bounds.height() <= 0) {
            return;
        }

        if (mWidth < 0 || mHeight < 0) {
            mWidth = bounds.width();
            mHeight = bounds.height();
            if (mWaveHeight == UNDEFINED_VALUE) {
                mWaveHeight = Math.max(8, (int) (mHeight * WAVE_HEIGHT_FACTOR));
            }

            if (mWaveLength == UNDEFINED_VALUE) {
                mWaveLength = mWidth;
            }

            if (mWaveStep == UNDEFINED_VALUE) {
                mWaveStep = Math.max(1, (int) (mWidth * WAVE_SPEED_FACTOR));
            }
            updateBackground(mWidth, mWaveLength, mWaveHeight);
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.CLEAR);
        mDrawable.draw(canvas);
        mDrawable.setColorFilter(mColorFilter);

        if (mProgress <= 0.1f) {
            return;
        }

        int sc = canvas.saveLayer(0, 0, mWidth, mHeight, null);

        if (mWaveLevel > 0) {
            canvas.clipRect(0, mWaveLevel, mWidth, mHeight);
        }

        mDrawable.draw(canvas);

        if (mProgress >= 0.999f) {
            return;
        }

        mWaveOffset += mWaveStep;
        if (mWaveOffset > mWaveLength) {
            mWaveOffset -= mWaveLength;
        }

        if (mMask != null) {
            mMatrix.setTranslate(-mWaveOffset, mWaveLevel);
            canvas.drawBitmap(mMask, mMatrix, mPaint);
        }

        canvas.restoreToCount(sc);
    }

    @Override
    protected boolean onLevelChange(int level) {
        setProgress(level / 10f);
        return true;
    }

    @Override
    public void setAlpha(int i) {
        mDrawable.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mColorFilter = colorFilter;
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void start() {
        mRunning = true;
        Choreographer.getInstance().postFrameCallback(mFrameCallback);
    }

    public void animateTo(float value) {
        mtimeAnimation = mProgress;
        mValueAnimator = getDefaultAnimator(value);
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.start();

    }

    @Override
    public void stop() {
        mRunning = false;
        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        setProgress((float) (animation.getAnimatedValue()) + mtimeAnimation);

        if (!mRunning) {
            invalidateSelf();
        }
    }

    private ValueAnimator getDefaultAnimator(float value) {
        final float diff;
        diff = value - mProgress;

        ValueAnimator animator = ValueAnimator.ofFloat(0, diff);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration((long) (8000 * Math.abs(diff)));
        return animator;
    }

    public void setProgress(float progress) {
        mProgress = progress;
        mWaveLevel = mHeight - (int) ((mHeight + mWaveHeight) * mProgress);
        invalidateSelf();
    }

    private void updateBackground(int width, int length, int height) {
        if (width <= 0 || length <= 0 || height <= 0) {
            mMask = null;
            return;
        }

        final int count = (int) Math.ceil((width + length) / (float) length);
        Bitmap mbitmap = Bitmap.createBitmap(length * count, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mbitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int amplitude = height / 2;
        Path path = new Path();
        path.moveTo(0, amplitude);
        final float stepX = length / 4f;
        float x = 0;
        float y = -amplitude;
        for (int i = 0; i < count * 2; i++) {
            x += stepX;
            path.quadTo(x, y, x + stepX, amplitude);
            x += stepX;
            y = mbitmap.getHeight() - y;
        }
        path.lineTo(mbitmap.getWidth(), height);
        path.lineTo(0, height);
        path.close();
        c.drawPath(path, p);
        mMask = mbitmap;
    }
}
