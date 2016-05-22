package com.sis.camerademo;

import java.util.ArrayList;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

/* See 
 *  http://www.gnugu.com/node/57 
 * */

public class ComboBox extends LinearLayout {

  private static final String xmlns_android = "http://schemas.android.com/apk/res/android";
  // private static final String TAG = "camerademo/ComboBox";
  
  private TextView caption_;
  private EditText text_;
  private ImageButton button_;
  private ListPopupWindow list_;
  LinearLayout cblayout = null;
  String[] items_;

  public ComboBox(Context context) {
    super(context);
    construct(context, null);
  }

  public ComboBox(Context context, AttributeSet attrs) {
    super(context, attrs);
    construct(context, attrs);
  }

  public ComboBox(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    construct(context, attrs);
  }

  
  public void setItems(ArrayList<String> arraylist) {
    list_.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
        items_ = arraylist.toArray(new String[arraylist.size()])));
  }

  public String getText() {
    return text_.getText().toString();
  }

  public void setText(String text) {
    text_.setText(text);
  }
  
  private void construct(Context context, AttributeSet attrs) {

    if ( attrs != null ) {
      
      String caption = attrs.getAttributeValue(xmlns_android, "hint");
      if ( caption == null ) {
        cblayout = this;
      }
      else {
        setOrientation(attrs);
        addView(caption_ = new TextView(context), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(cblayout = new LinearLayout(context), LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        caption_.setText(caption);
      }
    }
    
    cblayout.addView(text_ = new EditText(context),
        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
    text_.setMaxLines(1);
    text_.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    text_.setRawInputType(InputType.TYPE_TEXT_VARIATION_URI);

    
    
    cblayout.addView(button_ = new ImageButton(context),
        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    button_.setImageResource(android.R.drawable.arrow_down_float);
    button_.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (items_ != null) {
          list_.show();
        }
      }
    });    

    list_ = new ListPopupWindow(context);
    list_.setAnchorView(text_);
    list_.setModal(true);
    list_.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        if (items_ != null && position < items_.length) {
          text_.setText(items_[position]);
          list_.dismiss();
        }
      }
    });
  }

  private int setOrientation(AttributeSet attrs) {
    int orientation = LinearLayout.VERTICAL;
    if (attrs != null) {
      String s = attrs.getAttributeValue(xmlns_android, "orientation");
      if (s != null && (s.equals("horizontal") || s.equals("0"))) {
        orientation = LinearLayout.HORIZONTAL;
      }
    }
    setOrientation(orientation);
    return orientation;
  }
  
}
