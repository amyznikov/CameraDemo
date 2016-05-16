package com.sis.camerademo;

import com.sis.ffplay.CameraPreview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {

  private static final String TAG = "camerademo/MainActivity";
  
  private static int cameraId =-1;
  //private static String serverName = "http://crkdev1.special-is.com:8082/test";
//  private static String serverName = "http://192.168.0.105:8082/test";
//  private static String codecOpts = "-f matroska -c:v libx264 -crf 35";
  
  
  private CameraPreview cameraPreview = null;
  private Button settingsButton = null;
  private Button controlButton = null;
  
  
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    
    log.d(TAG, "onCreate(): savedInstanceState is %s", savedInstanceState == null ? "null" : "NOT NULL");
    
    /* Remove title bar */
    requestWindowFeature(Window.FEATURE_NO_TITLE); 
    
    /* Fix display orientation */
    //setRequestedOrientation(getResources().getConfiguration().orientation);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    
    
    
    cameraPreview = (CameraPreview)findViewById(R.id.CameraPreview);
    cameraPreview.setEventListener(new CameraPreview.EventListener() {
      
      @Override
      public void onStreamStarted() {
        log.d(TAG, "onStreamStarted()");
        settingsButton.setEnabled(false);
        controlButton.setEnabled(true);
        controlButton.setText("Stop Stream");
      }
      
      @Override
      public void onStreamFinished() {
        log.d(TAG, "onStreamFinished()");
        settingsButton.setEnabled(true);
        controlButton.setEnabled(true);
        controlButton.setText("Start Stream");
      }
      
      @Override
      public void onPreviewStarted() {
        log.d(TAG, "onPreviewStarted()");
      }
      
      @Override
      public void onPreviewFinished() {
        log.d(TAG, "onPreviewFinished()");
      }

      @Override
      public void onStreamStateChaged(int state, int reason) {
        log.d(TAG, "StreamStateChaged(): state=%d reason=%d", state, reason);
        
        if ( state == CameraPreview.STREAM_STATE_IDLE && reason != 0 ) {
          toast.show("Stream finished: error code=%d", reason);
        }
        else {
          toast.show("Stream state change: state=%d reson=%d", state, reason);
        }
      }
    });
    
    
    settingsButton = (Button)findViewById(R.id.settings);
    settingsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onSettingsButtonClicked();
      }
    });

    controlButton  = (Button)findViewById(R.id.control);
    controlButton.setText("Start Stream");
    controlButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onControlButtonClicked();
      }
    });
    
    
    if ( cameraId == -1 ) {
      
      SettingsActivity.show(new SettingsActivity.StatusListener() {
        @Override
        public void OnCancelled() {
          MainActivity.this.finish();
        }
        @Override
        public void OnAccepteted() {
          MainActivity.cameraId = SettingsActivity.options.cameraId;
          // wait for OnResume()
        }
        @Override
        public void OnError() {
          MainActivity.this.finish();
        }
      });
    }
    
  }

  @Override
  protected void onStart() {
    log.d(TAG, "OnStart()");
    super.onStart();
  }
  
  @Override
  protected void onResume() {
    log.d(TAG, "onResume()");
    super.onResume();
    
    if ( cameraId != -1 && cameraPreview.state() != CameraPreview.STATE_PREVIEW ) {
      cameraPreview.startPreview(cameraId, SettingsActivity.options.frameWidth, SettingsActivity.options.frameHeigt);
    }
  }
  
  @Override
  protected void onRestart() {
    log.d(TAG, "onRestart()");
    super.onRestart();
  }
  
  @Override
  protected void onPause() {
    log.d(TAG, "onPause()");
    super.onPause();
  }
  
  @Override
  protected void onStop() {
    log.d(TAG, "onStop()");
    super.onStop();
    cameraPreview.stopPreview();
  }
  
  @Override
  protected void onDestroy() {
    log.d(TAG, "onDestroy()");
    super.onDestroy();
  }
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void onSettingsButtonClicked() {
    
    cameraPreview.stopPreview();    
    
    SettingsActivity.show(new SettingsActivity.StatusListener() {
      @Override
      public void OnCancelled() {
        MainActivity.this.finish();
      }
      @Override
      public void OnAccepteted() {
        MainActivity.cameraId = SettingsActivity.options.cameraId;
        // Now wait for OnResume()
      }
      @Override
      public void OnError() {
        toast.show("Unexpected Error");
      }
    });
  }

  
  protected void onControlButtonClicked() {
    
    switch (cameraPreview.state()) {
    case CameraPreview.STATE_IDLE:
      break;

    case CameraPreview.STATE_PREVIEW: {
      CameraPreview.StreamOptions opts = new CameraPreview.StreamOptions();
      
      opts.format = SettingsActivity.options.streamFormat;
      opts.codec = SettingsActivity.options.videoCodec;
      opts.quality = SettingsActivity.options.videoQuality;
      opts.gopsize = SettingsActivity.options.videoGopSize;
      opts.bitrate = SettingsActivity.options.videoBitRate;
      opts.ffopts = null;
      
      cameraPreview.startStream(SettingsActivity.options.serverName, opts);
    }
      break;
      
    case CameraPreview.STATE_STREAMING:
      cameraPreview.stopStream();
      break;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  


  
  //////////////////////////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
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
