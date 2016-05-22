package com.sis.camerademo;

import com.sis.ffplay.CameraPreview;

public class StreamOptions {
  public int cameraId;
  public int frameWidth, frameHeigt;
  public CameraPreview.StreamOptions sopt;

  public StreamOptions() {
    sopt = new CameraPreview.StreamOptions();
  }
}
