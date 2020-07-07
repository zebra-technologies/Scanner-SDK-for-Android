package com.zebra.scannercontrol.app.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * Created by BPallewela on 12/13/2017.
 */

public class VerticalTextView extends androidx.appcompat.widget.AppCompatTextView {

    final boolean topDown;

    public VerticalTextView( Context context,
                             AttributeSet attrs )
    {
        super( context, attrs );
        final int gravity = getGravity();
        if ( Gravity.isVertical( gravity )
                && ( gravity & Gravity.VERTICAL_GRAVITY_MASK )
                == Gravity.BOTTOM )
        {
            setGravity(
                    ( gravity & Gravity.HORIZONTAL_GRAVITY_MASK )
                            | Gravity.TOP );
            topDown = false;
        }
        else
        {
            topDown = true;
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure( int widthMeasureSpec,
                              int heightMeasureSpec )
    {
        super.onMeasure( heightMeasureSpec,
                widthMeasureSpec );
        setMeasuredDimension( getMeasuredHeight(),
                getMeasuredWidth() );
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        TextPaint textPaint = getPaint();
        textPaint.setColor( getCurrentTextColor() );
        textPaint.drawableState = getDrawableState();

        canvas.save();

        if ( topDown )
        {
            canvas.translate( getWidth(), 0 );
            canvas.rotate( 90 );
        }
        else
        {
            canvas.translate( 0, getHeight() );
            canvas.rotate( -90 );
        }

        canvas.translate( getCompoundPaddingLeft(),
                getExtendedPaddingTop() );

        getLayout().draw( canvas );
        canvas.restore();
    }
}
