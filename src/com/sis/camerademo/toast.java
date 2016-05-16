package com.sis.camerademo;

public final class toast
{
  private static android.widget.Toast toast_ = null;

  public static final void show(final String format, final Object... args)
  {
    if (format == null || format.isEmpty()) {
      if (toast_ != null) {
        toast_.cancel();
      }
    }
    else {

      if (toast_ == null) {
        toast_ = android.widget.Toast.makeText(Application.context, String.format(format, args),
            android.widget.Toast.LENGTH_LONG);
      }
      else {
        toast_.setText(String.format(format, args));
      }

      toast_.show();
    }
  }
}
