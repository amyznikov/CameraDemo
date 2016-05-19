package com.sis.camerademo;

import java.util.ArrayList;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Spinner;

/* See 
 *  http://www.gnugu.com/node/57 
 * */

public class ComboBox extends LinearLayout {

  private EditText text_;
  private ImageButton button_;
  private ListPopupWindow list_;
  String[] items_;

  public ComboBox(Context context) {
    super(context);
    construct(context);
  }

  public ComboBox(Context context, AttributeSet attrs) {
    super(context, attrs);
    construct(context);
  }

  public ComboBox(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    construct(context);
  }

  private void construct(Context context) {
    
    setOrientation(HORIZONTAL);
    setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    text_ = new EditText(context);
    //text_.setSingleLine();
    text_.setMaxLines(1);
    
    text_.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI); 
    text_.setRawInputType(InputType.TYPE_TEXT_VARIATION_URI); 
    addView(text_, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));


    button_ = new ImageButton(context);
    button_.setImageResource(android.R.drawable.arrow_down_float);
    button_.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if ( items_ != null ) {
          list_.show();
        }
      }
    });
    
    addView(button_, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    
    list_ = new ListPopupWindow(context);
    list_.setAnchorView(text_);
    list_.setModal(true);
    list_.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        if ( items_ != null && position < items_.length ) {
          text_.setText(items_[position]);
          list_.dismiss();
        }
      }
    });
  }

  
  public void setItems(ArrayList<String> arraylist) {
    list_.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
        items_ = arraylist.toArray(new String[arraylist.size()])));
  }

  /**
   * Gets the text in the combo box.
   *
   * @return Text.
   */
  public String getText() {
      return text_.getText().toString();
  }

  /**
   * Sets the text in combo box.
   */
  public void setText(String text) {
      text_.setText(text);
  }
}
