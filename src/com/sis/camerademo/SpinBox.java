package com.sis.camerademo;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SpinBox extends LinearLayout {

  private static final String xmlns_android = "http://schemas.android.com/apk/res/android";
  // private static final String TAG = "camerademo/SpinBox";
      
  private TextView caption;
  private Spinner spinner;
  

  public SpinBox(Context context) {
    super(context);
    construct(context, null);
  }

  public SpinBox(Context context, AttributeSet attrs) {
    super(context, attrs);
    construct(context, attrs);
  }

  public SpinBox(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    construct(context, attrs);
  }

  
  public void setText(String text) {
    caption.setText(text);
  }

  public void setAdapter (SpinnerAdapter adapter) {
    spinner.setAdapter(adapter);
  }

  public void setItems(final String[] items ) {
    setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, items));
  }
  
  public int getSelectedItemPosition() {
    return spinner.getSelectedItemPosition();
  }

  public String getSelectedItemText() {
    Object obj = spinner.getSelectedItem();
    return obj == null ? null : obj.toString();
  }
  
  public void setSelection(int position) {
    spinner.setSelection(position);
  }

  public void setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener l) {
    spinner.setOnItemSelectedListener(l);
  }  
  

  private void construct(Context context, AttributeSet attrs) {
    
    setOrientation(attrs);
    
    addView(caption = new TextView(context), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);    
    addView(spinner = new Spinner(context), LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);    

    setCaption(caption, attrs);
  }
  
  private void setOrientation(AttributeSet attrs) {
    int orientation = LinearLayout.HORIZONTAL;
    if (attrs != null) {
      String s = attrs.getAttributeValue(xmlns_android, "orientation");
      if (s != null && (s.equals("vertical") || s.equals("1"))) {
        orientation = LinearLayout.VERTICAL;
      }
    }
    setOrientation(orientation);
  }

  private void setCaption(TextView v, AttributeSet attrs) {
    String s;
    if ( attrs == null || (s = attrs.getAttributeValue(xmlns_android, "hint" )) == null ) {
      s = "android:hint";
    }
    v.setText(s);
  }
  
  
  
  
}
