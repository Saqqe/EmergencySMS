package com.saqib.lab4;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Taken from Stackoverflow, Date:2015-01-07, Time: 17:50
 * http://stackoverflow.com/questions/5335306/how-can-i-get-an-event-in-android-spinner-when-the-current-selected-item-is-sele
 */
public class MySpinner extends Spinner{

    OnItemSelectedListener listener;

    public MySpinner(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position)
    {
        super.setSelection(position);

        if (position == getSelectedItemPosition())
        {
            listener.onItemSelected(null, null, position, 0);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener)
    {
        this.listener = listener;
    }
}