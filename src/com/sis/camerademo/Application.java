package com.sis.camerademo;

public class Application extends android.app.Application {
  public static android.content.Context context;
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();
  }
}
