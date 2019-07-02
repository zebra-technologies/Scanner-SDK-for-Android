package com.zebra.scannercontrol.app.barcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Class to represent barcodes created by API
 */
public class BarCodeView extends View {

    private static final int LEFT_BORDER = 8; // %
    private static final int TOP_BORDER = 10; // %
    private static final int RIGHT_BORDER = 8; // %
    private static final int BOTTOM_BORDER = 10; // %
    private static final float MAX_X_SIZE = 95.0f; // mm
    private static final float MAX_Y_SIZE = 22.0f; // mm
    private static final float MIN_STRIP_WIDTH = 3.0f; // pixels

    private GenerateBarcode128B barcode;
    private RectF rc;
    private Paint paint;

    private int maxXSize;
    private int maxYSize;

    private int previousXSize = 0;
    private int currentXSize = 0;
    private int previousYSize = 0;
    private int currentYSize = 0;
    private boolean isScaled = false;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    public BarCodeView(Context context, GenerateBarcode128B barcode) {

        super(context);

        this.barcode = barcode;
        this.rc = new RectF();
        this.paint = new Paint();
        this.maxXSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, MAX_X_SIZE, getResources().getDisplayMetrics()));
        this.maxYSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, MAX_Y_SIZE, getResources().getDisplayMetrics()));

        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mGestureDetector.setOnDoubleTapListener(mDoubleTapListener);

    }

    public int getXSize () {
        return currentXSize;
    }

    public int getYSize () {
        return currentYSize;
    }

    public void setSize (int x, int y) {
        previousXSize = currentXSize = x;
        previousYSize = currentYSize = y;
        isScaled = true;
    }

    @Override
    protected void onDraw (Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        int width = barcode.getWidth();
        int [] zebra = barcode.getArray();

        int leftBorder = canvas.getWidth() * LEFT_BORDER / 100;
        int rightBorder = canvas.getWidth() * RIGHT_BORDER / 100;

        boolean skipResizing = false;

        // max possible size
        int barcodeWidth = canvas.getWidth() - leftBorder - rightBorder;
        // check we don't go out of bounds
        if (isScaled) {
            if (currentXSize < barcodeWidth) {
                barcodeWidth = currentXSize;
            }
            else {
                skipResizing = true;
            }
        }
        // check we don't exceed max value unless resized manually
        if (!isScaled && barcodeWidth > maxXSize) {
            barcodeWidth = maxXSize;
        }

        float minStripWidth = ((float)barcodeWidth) / width;
        if (minStripWidth < MIN_STRIP_WIDTH) {
            if (!isScaled) {
                barcodeWidth = Math.round(MIN_STRIP_WIDTH * width);
            }
            else if (previousXSize > currentXSize) {
                skipResizing = true;
            }
        }

        int topBorder = canvas.getHeight() * TOP_BORDER / 100;
        int bottomBorder = canvas.getHeight() * BOTTOM_BORDER / 100;

        // max possible size
        int barcodeHeight = canvas.getHeight() - topBorder - bottomBorder;
        // check we don't go out of bounds
        if (isScaled) {
            if (currentYSize < barcodeHeight) {
                barcodeHeight = currentYSize;
            }
            else {
                skipResizing = true;
            }
        }
        // check we don't exceed max value unless resized manually
        if (!isScaled && barcodeHeight > maxYSize) {
            barcodeHeight = maxYSize;
        }

        if (skipResizing) {
            barcodeWidth = currentXSize = previousXSize;
            barcodeHeight = currentYSize = previousYSize;
            minStripWidth = ((float)barcodeWidth) / width;
        }
        else {
            // remember actual size
            previousXSize = currentXSize = barcodeWidth;
            previousYSize = currentYSize = barcodeHeight;
        }

        float posX = (canvas.getWidth() - barcodeWidth - leftBorder - rightBorder) / 2 + leftBorder;
        float posY = (canvas.getHeight() - barcodeHeight - topBorder - bottomBorder) / 2 + topBorder;

        for (int symbol: zebra) {
            for (int i=0; i<6; i++, symbol>>=2) {
                float stripWidth = ((symbol&0x3)+1)*minStripWidth;
                rc.set(posX, posY, posX+stripWidth, posY+barcodeHeight);
                if (i%2 == 0) {
                    paint.setColor(Color.BLACK);
                }
                else {
                    paint.setColor(Color.WHITE);
                }
                canvas.drawRect(rc, paint);
                posX += stripWidth;
            }
        }
        // The last strip
        int symbol = zebra[zebra.length-1] >> 12;
        float stripWidth = ((symbol&0x3)+1)*minStripWidth;
        rc.set(posX, posY, posX+stripWidth, posY+barcodeHeight);
        paint.setColor(Color.BLACK);
        canvas.drawRect(rc, paint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        retVal = mGestureDetector.onTouchEvent(event) | retVal;
        return retVal | super.onTouchEvent(event);
    }

    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float prevSpan = detector.getPreviousSpan();
            float currSpan = detector.getCurrentSpan();

            if ((prevSpan-currSpan) > 0) {
                // Decreasing size
                currentXSize -= Math.round(((float) currentXSize) * (prevSpan - currSpan) * 2 / getWidth());
                currentYSize -= Math.round(((float) currentYSize) * (prevSpan - currSpan) * 2 / getWidth());
                isScaled = true;
            }
            else if ((prevSpan-currSpan) < 0) {
                // Increasing size
                currentXSize += Math.round(((float) currentXSize) * (currSpan - prevSpan) * 2 / getWidth());
                currentYSize += Math.round(((float) currentYSize) * (currSpan - prevSpan) * 2 / getWidth());
                isScaled = true;
            }
            invalidate();
            return true;
        }
    };

    private final OnDoubleTapListener mDoubleTapListener = new OnDoubleTapListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isScaled = false;
            invalidate();
            return true;
        }
    };

    private final GestureDetector.OnGestureListener mGestureListener = new GestureDetector.OnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    };
}
