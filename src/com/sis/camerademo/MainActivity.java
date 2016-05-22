package com.sis.camerademo;

import com.sis.ffplay.CameraPreview;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {

  private static final String TAG = "camerademo/MainActivity";
  
  private static int stream_start_icon_index = 0;
  private static int stream_stop_icon_index = 1;
  
  private CameraPreview cameraPreview = null;

  private LinearLayout streamStatusView = null;
  
  private RelativeLayout controlLayout = null;
  
  private ImageButton streamStatsButton = null;
  private ImageButton controlButton = null;
  private ImageButton settingsButton = null;
  

  private Runnable streamStatusUpdateRunnable = null;
  private Runnable controlRunnable = null;
  private Handler updateHandler = new Handler();
  
  // stream status text views
  TextView connectionStateView;
  TextView framesReadView;
  TextView framesSentView;
  TextView bytesReadView;
  TextView bytesSentView;
  TextView inputFpsView;
  TextView outputFpsView;
  TextView inputBitrateView;
  TextView outputBitrateView;
  
  
  private SettingsActivity_StatusListener settingsActivityStatusListener = new SettingsActivity_StatusListener();
  private WindowManager windowManager = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    
    log.d(TAG, "onCreate(): savedInstanceState is %s", savedInstanceState == null ? "null" : "NOT NULL");
    
    /* Remove title bar */
    requestWindowFeature(Window.FEATURE_NO_TITLE); 
    

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

    
    cameraPreview = (CameraPreview)findViewById(R.id.CameraPreview);
    cameraPreview.setEventListener(new CameraPreview.EventListener() {
      
      @Override
      public void onStreamStarted() {
        settingsButton.setVisibility(View.GONE);
        controlButton.getDrawable().setLevel(stream_stop_icon_index);
      }
      
      @Override
      public void onStreamFinished() {
        settingsButton.setVisibility(View.VISIBLE);
        controlButton.getDrawable().setLevel(stream_start_icon_index);
      }
      
      @Override
      public void onPreviewStarted() {
      }
      
      @Override
      public void onPreviewFinished() {
      }

      @Override
      public void onStreamStateChaged(int state, int reason) {
        
        if ( state == CameraPreview.STREAM_STATE_IDLE && reason != 0 ) {
          toast.show("Stream finished: %s", CameraPreview.getErrMsg(reason));
        }
        else if( reason != 0 ) {
          toast.show("Stream: state=%s reason=%s", CameraPreview.getStreamStatusString(state), CameraPreview.getErrMsg(reason));
        }
        else {
          toast.show("Stream: state=%s", CameraPreview.getStreamStatusString(state));
        }
        
        if ( connectionStateView.getVisibility() == View.VISIBLE ) {
          setText(connectionStateView, "STATUS: %s", CameraPreview.getStreamStatusString(state));
        }
        
      }
    });
    
    cameraPreview.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {

        if (controlRunnable != null) {
          updateHandler.removeCallbacks(controlRunnable);
          controlRunnable = null;
        }

        if (controlLayout.getVisibility() != View.VISIBLE) {
          controlLayout.setVisibility(View.VISIBLE);
        }

        controlRunnable = new Runnable() {
          @Override
          public void run() {
            controlRunnable = null;
            if (controlLayout.getVisibility() != View.GONE) {
              controlLayout.setVisibility(View.GONE);
            }
          }
        };
        
        updateHandler.postDelayed(controlRunnable, 3 * 1000);
      }
    });
    
    
    
    
    streamStatusView = (LinearLayout)findViewById(R.id.streamStatusView);
    connectionStateView = (TextView )findViewById(R.id.connectionState);
    framesReadView = (TextView )findViewById(R.id.framesRead);
    framesSentView = (TextView )findViewById(R.id.framesSent);
    bytesReadView = (TextView )findViewById(R.id.bytesRead);
    bytesSentView = (TextView )findViewById(R.id.bytesSent);
    inputFpsView = (TextView )findViewById(R.id.inputFps);
    outputFpsView  = (TextView )findViewById(R.id.outputFps);
    inputBitrateView = (TextView )findViewById(R.id.inputBitrate);
    outputBitrateView = (TextView )findViewById(R.id.outputBitrate);
    streamStatusView.setVisibility(View.GONE);
    
    
    
    
    
    controlLayout = (RelativeLayout)findViewById(R.id.controlLayout);
    

    streamStatsButton = (ImageButton)findViewById(R.id.streamStatus);
    streamStatsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onStreamStatsButtonClicked();
      }
    });
    

    controlButton  = (ImageButton)findViewById(R.id.control);
    controlButton.getDrawable().setLevel(stream_start_icon_index);
    controlButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onControlButtonClicked();
      }
    });

    
    settingsButton = (ImageButton)findViewById(R.id.settings);
    settingsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onSettingsButtonClicked();
      }
    });
    
    
    if (SettingsActivity.cameraInfo == null || SettingsActivity.options == null || SettingsActivity.options.cameraId == -1) {
      SettingsActivity.show(settingsActivityStatusListener);
    }
    
  }

  @Override
  protected void onStop() {
    log.d(TAG, "onStop()");
    stopPreview();
    super.onStop();
  }
  
  @Override
  protected void onResume() {
    log.d(TAG, "onResume()");
    super.onResume();
    startPreview();
  }
  
   
  //////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  private void onSettingsButtonClicked() {
    stopPreview();    
    SettingsActivity.show(settingsActivityStatusListener);
  }

  
  protected void onControlButtonClicked() {
    
    switch (cameraPreview.state()) {
    case CameraPreview.STATE_IDLE:
      break;

    case CameraPreview.STATE_PREVIEW: {
      cameraPreview.startStream(SettingsActivity.options.sopt);
    }
      break;
      
    case CameraPreview.STATE_STREAMING:
      cameraPreview.stopStream();
      break;
    }
  }

  private void onStreamStatsButtonClicked() {
    if ( streamStatusView.getVisibility() == View.VISIBLE ) {
      stopStreamStatusUpdate();
      streamStatusView.setVisibility(View.GONE);
    }
    else {
      streamStatusView.setVisibility(View.VISIBLE);
      startStreamStatusUpdate();
    }
  }
  
  private void startStreamStatusUpdate()
  {
    refreshStreamStats();
    
    if ( streamStatusUpdateRunnable == null ) {
      
      streamStatusUpdateRunnable = new Runnable() {
        @Override
        public void run() {
          refreshStreamStats();
          updateHandler.postDelayed(streamStatusUpdateRunnable, 1 * 1000);
        }
      };
      
      updateHandler.postDelayed(streamStatusUpdateRunnable, 1 * 1000);
    }
  }
  
  private void stopStreamStatusUpdate()
  {
    if ( streamStatusUpdateRunnable != null ) {
      updateHandler.removeCallbacks(streamStatusUpdateRunnable);
      streamStatusUpdateRunnable = null;
    }
  }
  
  static CameraPreview.StreamStatus stats = new CameraPreview.StreamStatus(); 
  private void refreshStreamStats()
  {
    if ( cameraPreview.getStreamStatus(stats) ) {
      setText(connectionStateView, "%s", CameraPreview.getStreamStatusString(stats.state));
      setText(framesReadView,      "Frames read  : %9d", stats.framesRead);
      setText(framesSentView,      "Frames sent  : %9d", stats.framesSent);
      setText(bytesReadView,       "Bytes  read  : %9d", stats.bytesRead);
      setText(bytesSentView,       "Bytes  sent  : %9d", stats.bytesSent);
      setText(inputFpsView,        "Input  fps   : %f", stats.inputFps);
      setText(outputFpsView,       "Output fps   : %f", stats.outputFps);
      setText(inputBitrateView, "Input  Bitrate  : %s", br2str(stats.inputBitrate));
      setText(outputBitrateView, "Output Bitrate : %s", br2str(stats.outputBitrate));
    }
  }

  private static void setText(TextView textView, String format, Object... args) {
    textView.setText(String.format(format, args));
  }
  
  private static String br2str(int bitrate) {
    if ( bitrate < 1000 ) {
      return String.format("%d Bits/s", bitrate);
    }
    if ( bitrate < 1000000 ) {
      return String.format("%g KBit/s", ((double)bitrate)/1000.0);
    }
    return String.format("%g MBit/s", ((double)bitrate)/1000000.0);
  }  
  
  private int getDeviceDefaultOrientation() {

    Configuration config = getResources().getConfiguration();

    int rotation = windowManager.getDefaultDisplay().getRotation();

    if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
        && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
        || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
            && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
      return Configuration.ORIENTATION_LANDSCAPE;
    }
    return Configuration.ORIENTATION_PORTRAIT;

  }
  
  private int getCorrectOrientation() {
    int naturalOrientation = getDeviceDefaultOrientation();
    int correctOrientation = naturalOrientation;

    if (SettingsActivity.cameraInfo != null && SettingsActivity.options != null
        && SettingsActivity.options.cameraId >= 0) {
      int cameraId = SettingsActivity.options.cameraId;

      switch (SettingsActivity.cameraInfo[cameraId].orientation) {
      case 90:
      case 270:
        if (naturalOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
          correctOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
          correctOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        break;
      }
    }
    return correctOrientation;
  }

  private void setCorrectOrientation() {
    int currentOritation = getResources().getConfiguration().orientation;
    int correctOritation = getCorrectOrientation();
    if ( currentOritation !=  correctOritation ) {
      setRequestedOrientation(correctOritation);
      
      
    }
  }
  
  
  private void startPreview()
  {
    if (SettingsActivity.cameraInfo != null && SettingsActivity.options != null
        && SettingsActivity.options.cameraId != -1 && cameraPreview.state() != CameraPreview.STATE_PREVIEW) {

      setCorrectOrientation();

      cameraPreview.startPreview(SettingsActivity.options.cameraId, SettingsActivity.options.frameWidth,
          SettingsActivity.options.frameHeigt);
    }

    controlLayout.setVisibility(View.GONE);

    if (streamStatusView.getVisibility() == View.VISIBLE) {
      startStreamStatusUpdate();
    }
  }
  
  private void stopPreview()
  {
    cameraPreview.stopPreview();

    if (controlRunnable != null) {
      updateHandler.removeCallbacks(controlRunnable);
      controlRunnable = null;
    }

    stopStreamStatusUpdate();
  }

  
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private class SettingsActivity_StatusListener implements SettingsActivity.StatusListener
  {
    @Override
    public void OnCancelled() {
      MainActivity.this.finish();
    }
    @Override
    public void OnAccepteted() {
      setCorrectOrientation();
    }
    @Override
    public void OnError() {
      toast.show("Unexpected Error");
    }
  };
  


  
  //////////////////////////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    //getMenuInflater().inflate(R.menu.main, menu);
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
