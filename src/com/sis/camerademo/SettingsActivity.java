package com.sis.camerademo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

@SuppressWarnings("deprecation")
public class SettingsActivity extends Activity {

  private static boolean initialized = false; 
  
  public static int numcams = 0;
  public static Camera.Parameters[/*numcams*/] cameraParameters = null; 

  
  private Spinner cameraSpinner = null;
  public static CameraInfo [] cameraInfo = null;
  private static String[] cameraInfoStrings = null;
  
  
  
  private Spinner frameSizesSpinner = null;
  private static int[] selectedFrameSizeIndexes = null;
  
  
  private ComboBox serverNameCombo = null;
  private static ArrayList<String > serverNames = new ArrayList<String>();
  private static final String[] defaultServerNames = {
      "http://192.168.0.105:8082/test",
      "http://crkdev1.special-is.com:8082/test",
  };
  
  
  
  

  private Spinner streamFormatsSpinner = null;
  private static final String[] streamFormats = {
      "mjpeg",
      "matroska",
      "ffm",
      "avi",
      "flv",
      "asf",
  };
  private static int selectedStreamFormatIndex = 0;
  
  
  
  private Spinner videoCodecNamesSpinner = null;
  private static final String[] videoCodecNames = {
      "mjpeg",
      "ffv1",
      "libx264",
      "h263a",
  };
  private static int selectedVideoCodecIndex = 0;

  
  private Spinner videoQualitySpinner = null;
  private static final String[] videoQualityValues = {
      "10%",
      "20%",
      "30%",
      "40%",
      "50%",
      "60%",
      "70%",
      "80%",
      "90%",
     "100%",
  };
  private static int selectedVideoQualityIndex = 4;


  private Spinner videoBitrateSpinner = null;
  private static final String[] videoBitrateValues = {
      "2K",
      "5K",
      "10K",
      "20K",
      "50K",
      "100K",
      "128K",
      "160K",
      "200K",
      "256K",
      "300K",
      "400K",
      "500K",
      "600K",
      "700K",
      "800K",
      "900K",
       "1M",
  };
  private static int selectedVideoBitrateIndex = 6;

  
  private Spinner videoGopSizeSpinner = null;
  private static final String[] videoGopSizes = {
      "1",
      "3",
      "5",
      "10",
      "15",
      "20",
      "25",
      "30",
      "40",
      "50",
  };
  private static int selectedVideoGopSizeIndex = 3;
  
  
  
  
  private Button cancelButton = null;
  private Button okButton = null;
  

  public static interface StatusListener {
    public void OnCancelled();
    public void OnAccepteted();
    public void OnError();
  }
  
  public static StatusListener statusListener_;
  public static StreamOptions options = new StreamOptions();
   
  
  
  public static void show(StatusListener listener ) {
    
    SettingsActivity.statusListener_ = listener;

    Context context = Application.context;
    Intent intent = new Intent(context, SettingsActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
    context.startActivity(intent);
    
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    /* Remove title bar */
    requestWindowFeature(Window.FEATURE_NO_TITLE); 
    
    super.onCreate(savedInstanceState);
    
    if (!getCameraInfo()) {
      OnError();
      this.finish();
      return;
    }    

    if (numcams < 1) {
      MsgBox.show("ERROR:", "No cameras found on this device");
      OnError();
      this.finish();
      return;
    }

    if ( options.cameraId == -1 ) {
      options.cameraId = 0;
    }
    
    setContentView(R.layout.activity_settings);
    
    
    
    
    cameraSpinner = (Spinner) findViewById(R.id.cameraId);
    cameraSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cameraInfoStrings));
    cameraSpinner.setSelection(options.cameraId);

    cameraSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        selectedFrameSizeIndexes[options.cameraId] = frameSizesSpinner.getSelectedItemPosition();
        options.cameraId = cameraSpinner.getSelectedItemPosition();
        updateControls();
      }
      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
        updateControls();
      }
    });
    
    
    
    frameSizesSpinner = (Spinner) findViewById(R.id.FrameSize); 
    serverNameCombo = (ComboBox) findViewById(R.id.serverName);
    streamFormatsSpinner = (Spinner) findViewById(R.id.streamFormat);
    videoCodecNamesSpinner = (Spinner) findViewById(R.id.videoCodecName);
    videoQualitySpinner = (Spinner) findViewById(R.id.videoQuality);
    videoGopSizeSpinner = (Spinner) findViewById(R.id.videoGopSize);
    videoBitrateSpinner = (Spinner) findViewById(R.id.videoButrate);
    
    
    
    cancelButton = (Button) findViewById(R.id.Cancel);
    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        OnCancelled();
        SettingsActivity.this.finish();
      }
    });
 
    okButton = (Button) findViewById(R.id.Ok);
    okButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        OnAccepteted();
        SettingsActivity.this.finish();
      }
    });


    if ( !initialized ) {
      initialized = true;
      readPrefs();
    }
   
    updateControls();
  }


  private void OnCancelled() {
    if (statusListener_ != null) {
      statusListener_.OnCancelled();
    }
  }
  
  private void OnAccepteted() {
    savePrefs();
    if (statusListener_ != null) {
      statusListener_.OnAccepteted();
    }
  }
  
  private void OnError() {
    if (statusListener_ != null) {
      statusListener_.OnError();
    }
  }
  
  private static boolean getCameraInfo()
  {
    try {
      
      Camera c;
      
      if ( numcams < 1 && (numcams = Camera.getNumberOfCameras()) < 1) {
        return false;
      }      

      if ( cameraInfo == null ) {
        cameraInfo = new CameraInfo[numcams];
      }
      
      if ( cameraInfoStrings == null ) {
        cameraInfoStrings = new String[numcams];
      }
      
      if ( cameraParameters == null ) {
        cameraParameters = new Camera.Parameters[numcams];
      }
      
      if ( selectedFrameSizeIndexes == null ) {
        selectedFrameSizeIndexes = new int [numcams];
      }
      
      for (int i = 0; i < numcams; ++i) {

        selectedFrameSizeIndexes[i] = -1;
        
        if ( cameraInfo[i] == null ) {
          Camera.getCameraInfo(i, cameraInfo[i] = new CameraInfo());
        }
        
        if ( cameraInfoStrings[i] == null ) {
          String facingString = cameraInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT ? "FRONT" : "BACK";
          cameraInfoStrings[i] = String.format("%d : %s / %d deg", i, facingString, cameraInfo[i].orientation);
        }
        
        if ( cameraParameters[i] == null ) {
          if ((c = Camera.open(i)) != null) {
            cameraParameters[i] = c.getParameters();
            c.release();
          }
        }
      }
    }
    catch (Exception ex ) {
      ex.printStackTrace();
      toast.show("ERROR", "%s", ex.getMessage());
      return false;
    }
    
    return true;
  }

  
  private void readPrefs() {

    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        
    options.cameraId = prefs.getInt("cameraId", options.cameraId);
    selectedStreamFormatIndex = prefs.getInt("selectedStreamFormatIndex", selectedStreamFormatIndex);
    selectedVideoCodecIndex = prefs.getInt("selectedVideoCodecIndex", selectedVideoCodecIndex);
    selectedVideoQualityIndex = prefs.getInt("selectedVideoQualityIndex", selectedVideoQualityIndex);
    selectedVideoGopSizeIndex = prefs.getInt("selectedVideoGopSizeIndex", selectedVideoGopSizeIndex);
    selectedVideoBitrateIndex = prefs.getInt("selectedVideoGopSizeIndex", selectedVideoGopSizeIndex);

    for (int i = 0; i < numcams; ++i) {
      selectedFrameSizeIndexes[i] = prefs.getInt(String.format("FrameSizeIndex%d", i), selectedFrameSizeIndexes[i]);
    }
    

    //////

    if ((options.serverName = prefs.getString("Server", null)) == null) {
      options.serverName = defaultServerNames[0];
    }    
    

    serverNames.clear();   

    for ( int i = 0; i < 10 ; ++i ) {
      String serverName = prefs.getString(String.format("Server%d", i), null);
      if ( serverName == null ) {
        break;
      }
      serverNames.add(serverName);
    }

    if ( serverNames.size() < 1 ) {
      for ( int i= 0; i < defaultServerNames.length ; ++i ) {
        serverNames.add(defaultServerNames[i]);
      }
    }    
    
    //////
  }
  
  private void savePrefs() {
    
    options.cameraId = cameraSpinner.getSelectedItemPosition();

    selectedFrameSizeIndexes[options.cameraId] = frameSizesSpinner.getSelectedItemPosition();
    Camera.Size frameSize = cameraParameters[options.cameraId].getSupportedPreviewSizes()
        .get(selectedFrameSizeIndexes[options.cameraId]);
    options.frameWidth = frameSize.width;
    options.frameHeigt = frameSize.height;    

    

    options.streamFormat = streamFormats[selectedStreamFormatIndex = streamFormatsSpinner.getSelectedItemPosition()];
    options.videoCodec = videoCodecNames[selectedVideoCodecIndex = videoCodecNamesSpinner.getSelectedItemPosition()];
    options.videoQuality = str2num(videoQualityValues[selectedVideoQualityIndex = videoQualitySpinner.getSelectedItemPosition()]);
    options.videoGopSize = str2num(videoGopSizes[selectedVideoGopSizeIndex = videoGopSizeSpinner.getSelectedItemPosition()]);
    options.videoBitRate = str2num(videoGopSizes[selectedVideoBitrateIndex = videoBitrateSpinner.getSelectedItemPosition()]);
    
    
    SharedPreferences.Editor prefs = getPreferences(MODE_PRIVATE).edit();
    
    prefs.putInt("cameraId", options.cameraId);
    prefs.putInt("selectedStreamFormatIndex", selectedStreamFormatIndex);
    prefs.putInt("selectedVideoCodecIndex", selectedVideoCodecIndex);
    prefs.putInt("selectedVideoQualityIndex", selectedVideoQualityIndex);
    prefs.putInt("selectedVideoGopSizeIndex", selectedVideoGopSizeIndex);
    prefs.putInt("selectedVideoGopSizeIndex", selectedVideoGopSizeIndex);

    for (int i = 0; i < numcams; ++i) {
      prefs.putInt(String.format("FrameSizeIndex%d", i), selectedFrameSizeIndexes[i]);
    }
    
    if ((options.serverName = serverNameCombo.getText()) == null || options.serverName.isEmpty() ) {
      options.serverName = defaultServerNames[0];
    }
    prefs.putString("Server", options.serverName);

    
    if ( !serverNames.contains(options.serverName) ) {
      serverNames.add(0, options.serverName);
    }

    for ( int i = 0, n = serverNames.size() > 10 ? 10 : serverNames.size(); i < n; ++i ) {
      prefs.putString(String.format("Server%d", i), serverNames.get(i));
    }
   
    prefs.commit();    
  }
  
  
  
  
  
  
  private void updateControls()
  {
    if (options.cameraId >= 0) {
      
      int S = 640 * 480;
      int bestIndex = 0;
      int bestDS = Integer.MAX_VALUE;
      
      List<Camera.Size> supportedFrameSizes = cameraParameters[options.cameraId].getSupportedPreviewSizes();
      String[] frameSizeStrings = new String[supportedFrameSizes.size()];
      for (int i = 0, n = supportedFrameSizes.size(); i < n; ++i) {
        Camera.Size size = supportedFrameSizes.get(i);
        frameSizeStrings[i] = String.format("%dx%d", size.width, size.height);

        int s = size.width * size.height;
        int ds = Math.abs(s-S); 
        if ( ds < bestDS ) {
          bestDS = ds;
          bestIndex = i;
        }
      }
      
      frameSizesSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, frameSizeStrings));
      
      if ( selectedFrameSizeIndexes[options.cameraId] == -1 ) {
        selectedFrameSizeIndexes[options.cameraId] = bestIndex;
      }      
      
      frameSizesSpinner.setSelection(selectedFrameSizeIndexes[options.cameraId]);      
    }
    
    serverNameCombo.setItems(serverNames);
    serverNameCombo.setText(options.serverName);

    streamFormatsSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, streamFormats));
    streamFormatsSpinner.setSelection(selectedStreamFormatIndex);

    videoCodecNamesSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, videoCodecNames));
    videoCodecNamesSpinner.setSelection(selectedVideoCodecIndex);
    
    videoQualitySpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, videoQualityValues));
    videoQualitySpinner.setSelection(selectedVideoQualityIndex);
    
    videoGopSizeSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, videoGopSizes));
    videoGopSizeSpinner.setSelection(selectedVideoGopSizeIndex);
    
    videoBitrateSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, videoBitrateValues));
    videoBitrateSpinner.setSelection(selectedVideoBitrateIndex);
    
  }

  
  private static int str2num(String s)
  {
    int x = 0;
    int i = 0;
    
    char[] charArray = s.toCharArray();
    String snum = new String();
    String suffix = new String();
    
    while (i < charArray.length && charArray[i] >= '0' && charArray[i] <= '9') {
      snum += charArray[i++];
    }

    while (i < charArray.length ) {
      suffix += charArray[i++];
    }
    
    x = Integer.parseInt(snum);
    
    if (suffix.equals("K")) {
      x *= 1000;
    } else if (suffix.equals("M")) {
      x *= 1000000;
    }
    
    return x;
  }
  
}
