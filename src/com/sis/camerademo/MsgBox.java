package com.sis.camerademo;

import android.app.AlertDialog;

public class MsgBox
{
  public static void show(String title, String message, final Object... args) 
  {
    log.d("camerademo/MsgBox", "Application.context is %s", Application.context == null ? "null" : "NOT NULL");
    
    new AlertDialog.Builder(Application.context)
          .setMessage(String.format(message, args))
          .setTitle(title)
          .setNeutralButton("Close", null)
          .create()
          .show();
  }

}
