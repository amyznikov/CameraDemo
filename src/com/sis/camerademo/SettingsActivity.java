package com.sis.camerademo;

import java.util.ArrayList;
import java.util.List;

import com.sis.ffplay.CameraPreview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class SettingsActivity extends Activity {

  private static final String TAG = "camerademo/SettingsActivity"; 
  
  private static boolean initialized = false; 
  
  public static int numcams = 0;
  public static Camera.Parameters[] cameraParameters = null; 
  public static CameraInfo [] cameraInfo = null;

  private SpinBox cameraSpinner = null;
  private static String[] cameraInfoStrings = null;
  
  
  private SpinBox frameSizesSpinner = null;
  private static int[] selectedFrameSizeIndexes = null;
  private static String[][] supportedFrameSizeStrings = null;
  private static int [] bestFramesizeIndex = null;
  
  
  private ComboBox serverNameSpinner = null;
  private static ArrayList<String > serverNames = new ArrayList<String>();
  private static final String[] defaultServerNames = {
      "http://crkdev1.special-is.com:8082/test",
      "http://192.168.0.105:8082/test",
  };
  
  
  private SpinBox streamFormatsSpinner = null;
  private static final String[] streamFormats = CameraPreview.getSupportedStreamFormats();
  
  private SpinBox videoCodecSpinner = null;
  private static final String[] videoCodecNames = CameraPreview.getSupportedVideoCodecs();

  private SpinBox audioCodecSpinner = null;
  private static final String[] audioCodecNames = CameraPreview.getSupportedAudioCodecs();

  
  private SpinBox videoQualitySpinner = null;
  private SpinBox audioQualitySpinner = null;
  
  private SpinBox videoBitrateSpinner = null;
  private SpinBox audioBitrateSpinner = null;
  
  private SpinBox videoBufsSpinner = null;
  private static final String videoBufCounts[] = {
      "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "12", "15"
  };
  
  
  private SpinBox audioBufsSpinner = null;
  private static final String audioBufCounts[] = {
      "1", "2", "3", "4", "5", "10", "15", "20", "30", "50", "100", "150", "200", "250", "300", "350" 
  };
  
  
  
  private SpinBox videoGopSizeSpinner = null;
  
  private CheckBox expertModeCheckBox = null;
  private EditText ffoptsEditBox = null;
  private static String ffOptsBackup = null;
  
  private Button cancelButton = null;
  private Button okButton = null;

  public static StreamOptions options = new StreamOptions();
  
  
  

  public static interface StatusListener {
    public void OnCancelled();
    public void OnAccepteted();
    public void OnError();
  }
  
  public static StatusListener statusListener_;
   
  
  
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
    
    if ( !initialized ) {
      readPrefs();
      initialized = true;
    }

    setContentView(R.layout.activity_settings);
    
    
    cameraSpinner = (SpinBox) findViewById(R.id.cameraId);
    cameraSpinner.setItems(cameraInfoStrings);
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
    

    
    frameSizesSpinner = (SpinBox) findViewById(R.id.FrameSizes); 

    
    serverNameSpinner = (ComboBox) findViewById(R.id.serverName);
    serverNameSpinner.setItems(serverNames);
    serverNameSpinner.setText(options.sopt.server);

    
    streamFormatsSpinner = (SpinBox) findViewById(R.id.streamFormat);
    streamFormatsSpinner.setItems(streamFormats);
    streamFormatsSpinner.setSelection(guessIndexOf(options.sopt.format, streamFormats));

    
    videoCodecSpinner = (SpinBox) findViewById(R.id.videoCodecName);
    videoCodecSpinner.setItems(videoCodecNames);
    videoCodecSpinner.setSelection(guessIndexOf(options.sopt.vCodecName, videoCodecNames));
    videoCodecSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        options.sopt.vCodecName = videoCodecSpinner.getSelectedItemText();
        updateControls();
      }
      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });
    
    
    
    audioCodecSpinner = (SpinBox) findViewById(R.id.audioCodecName);
    audioCodecSpinner.setItems(audioCodecNames);
    audioCodecSpinner.setSelection(guessIndexOf(options.sopt.aCodecName, audioCodecNames));
    audioCodecSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        options.sopt.aCodecName = audioCodecSpinner.getSelectedItemText();
        updateControls();
      }
      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });
    
    
    videoQualitySpinner = (SpinBox) findViewById(R.id.videoQuality);
    videoBitrateSpinner = (SpinBox) findViewById(R.id.videoBitrate);
    videoGopSizeSpinner = (SpinBox) findViewById(R.id.videoGopSize);

    videoBufsSpinner = (SpinBox) findViewById(R.id.videoBufs);
    videoBufsSpinner.setItems(videoBufCounts);
    videoBufsSpinner.setSelection(guessIndexOf(String.format("%d",options.sopt.vBufferSize), videoBufCounts));
    
    
    audioQualitySpinner = (SpinBox) findViewById(R.id.audioQuality);
    audioBitrateSpinner = (SpinBox) findViewById(R.id.audioBitrate);

    audioBufsSpinner = (SpinBox) findViewById(R.id.audioBufs);
    audioBufsSpinner.setItems(audioBufCounts);
    audioBufsSpinner.setSelection(guessIndexOf(String.format("%d",options.sopt.aBufferSize), audioBufCounts));
    
    expertModeCheckBox = (CheckBox) findViewById(R.id.expertMode);
    expertModeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ffoptsEditBox.setVisibility(isChecked ? View.VISIBLE : View.GONE);
      }
    });
    
    
    ffoptsEditBox = (EditText)findViewById(R.id.ffoptsEditBox);
    ffoptsEditBox.setText(ffOptsBackup);
    if ( expertModeCheckBox.isChecked()) {
      ffoptsEditBox.setVisibility(View.VISIBLE);
    }
    else {
      ffoptsEditBox.setVisibility(View.GONE);
    }

    
    
    
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
      
      if (supportedFrameSizeStrings == null) {
        supportedFrameSizeStrings = new String[numcams][];
      }
      
      if ( bestFramesizeIndex == null ) {
        bestFramesizeIndex = new int[numcams];
        for (int i = 0; i < numcams; ++i) {
          bestFramesizeIndex[i] = -1;
        }
      }
      
      if ( selectedFrameSizeIndexes == null ) {
        selectedFrameSizeIndexes = new int [numcams];
        for (int i = 0; i < numcams; ++i) {
          selectedFrameSizeIndexes[i] = -1;
        }
      }
      
      for (int i = 0; i < numcams; ++i) {

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
        
        if ( supportedFrameSizeStrings[i] == null || bestFramesizeIndex[i] == -1 ) {
          
          int S = 640 * 480;
          int ds, bestDS = Integer.MAX_VALUE;
          int bestIndex = 0;
          
          List<Camera.Size> supportedFrameSizes = cameraParameters[options.cameraId].getSupportedPreviewSizes();
          
          supportedFrameSizeStrings[i] = new String[supportedFrameSizes.size()];
          
          for (int j = 0, n = supportedFrameSizes.size(); j < n; ++j) {

            Camera.Size size = supportedFrameSizes.get(j);
            supportedFrameSizeStrings[i][j] = String.format("%dx%d", size.width, size.height);

            if ((ds = Math.abs(size.width * size.height - S)) < bestDS) {
              bestDS = ds;
              bestIndex = j;
            }
          }
          bestFramesizeIndex[i] = bestIndex;
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
    
    if ((options.sopt.server = prefs.getString("server", options.sopt.server)) == null) {
      options.sopt.server = defaultServerNames[0];
    }
    
    if ((options.sopt.format = prefs.getString("format", options.sopt.format)) == null) {
      options.sopt.format = streamFormats[0];
    }

    if ((options.sopt.vCodecName = prefs.getString("VideoCodec", options.sopt.vCodecName)) == null) {
      options.sopt.vCodecName = videoCodecNames[0];
    }

    if ((options.sopt.aCodecName = prefs.getString("AudioCodec", options.sopt.aCodecName)) == null) {
      options.sopt.aCodecName = audioCodecNames[0];
    }        
    
    if ((options.sopt.vBufferSize = prefs.getInt("videoBufs", -1)) == -1) {
      options.sopt.vBufferSize = 3;
    }
    
    if ((options.sopt.aBufferSize = prefs.getInt("audioBufs", -1)) == -1) {
      options.sopt.aBufferSize = 150;
    }
    
    options.sopt.vQuality = prefs.getInt("VideoQuality", options.sopt.vQuality);
    options.sopt.aQuality = prefs.getInt("AudioQuality", options.sopt.aQuality);
    options.sopt.vBitRate = prefs.getInt("VideoBitrate", options.sopt.vBitRate);
    options.sopt.aBitRate = prefs.getInt("AudioBitrate", options.sopt.aBitRate);
    options.sopt.vBufferSize = prefs.getInt("VideoBufs", options.sopt.vBufferSize);
    options.sopt.aBufferSize = prefs.getInt("AudioBufs", options.sopt.aBufferSize);
    options.sopt.vGopSize = prefs.getInt("GopSize", options.sopt.vGopSize);
    ffOptsBackup = prefs.getString("ffopts", options.sopt.ffopts);
    
    serverNames.clear();   

    for ( int i = 0; i < 10 ; ++i ) {
      String serverName = prefs.getString(String.format("server%d", i), null);
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
    
    for (int i = 0; i < numcams; ++i) {
      selectedFrameSizeIndexes[i] = prefs.getInt(String.format("FrameSizeIndex%d", i), selectedFrameSizeIndexes[i]);
    }
  }
  
  
  
  private void savePrefs() {

    String serverName = serverNameSpinner.getText().toString();
    if (serverName == null || options.sopt.server.isEmpty()) {
      toast.show("No Server Name Specified");
      return;
    }

    SharedPreferences.Editor prefs = getPreferences(MODE_PRIVATE).edit();
    

    options.cameraId = cameraSpinner.getSelectedItemPosition();
    prefs.putInt("cameraId", options.cameraId);

    
    selectedFrameSizeIndexes[options.cameraId] = frameSizesSpinner.getSelectedItemPosition();
    prefs.putInt(String.format("FrameSizeIndex%d", options.cameraId), selectedFrameSizeIndexes[options.cameraId]);

    
    Camera.Size frameSize = cameraParameters[options.cameraId].getSupportedPreviewSizes()
        .get(selectedFrameSizeIndexes[options.cameraId]);
    options.frameWidth = frameSize.width;
    options.frameHeigt = frameSize.height;    


    
    prefs.putString("server", options.sopt.server = serverName);
    if (!serverNames.contains(serverName)) {
      serverNames.add(0, serverName);
    }
    for ( int i = 0, n = serverNames.size() > 10 ? 10 : serverNames.size(); i < n; ++i ) {
      prefs.putString(String.format("server%d", i), serverNames.get(i));
    }

    
    prefs.putString("format", options.sopt.format = streamFormats[streamFormatsSpinner.getSelectedItemPosition()]);    
    prefs.putString("VideoCodec", options.sopt.vCodecName = videoCodecNames[videoCodecSpinner.getSelectedItemPosition()]);
    prefs.putString("AudioCodec", options.sopt.aCodecName = audioCodecNames[audioCodecSpinner.getSelectedItemPosition()]);
    
    
    if (videoQualitySpinner.getVisibility() == View.VISIBLE) {
      prefs.putInt("VideoQuality", options.sopt.vQuality = str2num(videoQualitySpinner.getSelectedItemText()));
    }

    if (audioQualitySpinner.getVisibility() == View.VISIBLE) {
      prefs.putInt("AudioQuality", options.sopt.aQuality = str2num(audioQualitySpinner.getSelectedItemText()));
    }

    if (videoBitrateSpinner.getVisibility() == View.VISIBLE) {
      prefs.putInt("VideoBitrate", options.sopt.vBitRate = str2num(videoBitrateSpinner.getSelectedItemText()));
    }

    if (audioBitrateSpinner.getVisibility() == View.VISIBLE) {
      prefs.putInt("AudioBitrate", options.sopt.aBitRate = str2num(audioBitrateSpinner.getSelectedItemText()));
    }
    
    if (videoGopSizeSpinner.getVisibility() == View.VISIBLE) {
      prefs.putInt("GopSize", options.sopt.vGopSize = str2num(videoGopSizeSpinner.getSelectedItemText()));
    }

    if (videoBufsSpinner.getVisibility() == View.VISIBLE) {
      prefs.putInt("videoBufs", options.sopt.vBufferSize = str2num(videoBufsSpinner.getSelectedItemText()));
    }

    if (audioBufsSpinner.getVisibility() == View.VISIBLE) {
      prefs.putInt("audioBufs", options.sopt.aBufferSize = str2num(audioBufsSpinner.getSelectedItemText()));
    }
    
    if ( expertModeCheckBox.isChecked() ) {
      prefs.putString("ffopts", options.sopt.ffopts = ffOptsBackup = ffoptsEditBox.getText().toString());
    }
    else {
      options.sopt.ffopts = null;
    }
    
    prefs.commit();    
  }
  
  
  
  
  
  private static final int guessIndexOf(final String item, final String[] items) {
    for (int i = 0; i < items.length; ++i) {
      if (item.equals(items[i])) {
        return i;
      }
    }
    return 0;
  }
  
  private static final int guessIndexOf(int item, final int[] items) {
    for (int i = 0; i < items.length; ++i) {
      if (item == items[i]) {
        return i;
      }
    }
    return 0;
  }

  private static final String[] mkQualityStrings(final int[] values) {
    String[] items = new String[values.length];
    for ( int i = 0; i < values.length; ++i  ) {
      items[i] = String.format("%d%%", values[i]);
    }
    return items;
  }

  private static final String[] mkBitrateStrings(final int[] values) {
    String[] items = new String[values.length];
    for ( int i = 0; i < values.length; ++i  ) {
      items[i] = String.format("%d Bits/s", values[i]);
    }
    return items;
  }

  private static final String[] mkGopSizeStrings(final int[] values) {
    String[] items = new String[values.length];
    for ( int i = 0; i < values.length; ++i  ) {
      items[i] = String.format("%d", values[i]);
    }
    return items;
  }
  
  private void updateControls() {

    CameraPreview.CodecOpts copts;
    
    frameSizesSpinner.setItems(supportedFrameSizeStrings[options.cameraId]);
    frameSizesSpinner.setSelection(selectedFrameSizeIndexes[options.cameraId]);
    

    copts = CameraPreview.getSupportedCodecOptions(options.sopt.vCodecName);
    
    if (copts == null || copts.QualityValues == null) {
      videoQualitySpinner.setVisibility(View.GONE);
    } 
    else {
      videoQualitySpinner.setVisibility(View.VISIBLE);
      videoQualitySpinner.setItems(mkQualityStrings(copts.QualityValues));
      videoQualitySpinner.setSelection(guessIndexOf(options.sopt.vQuality, copts.QualityValues));
    }    
    
    if (copts == null || copts.BitRates == null) {
      videoBitrateSpinner.setVisibility(View.GONE);
    } 
    else {
      videoBitrateSpinner.setVisibility(View.VISIBLE);
      videoBitrateSpinner.setItems(mkBitrateStrings(copts.BitRates));
      videoBitrateSpinner.setSelection(guessIndexOf(options.sopt.vBitRate, copts.BitRates));
    }    
    
    if (copts == null || copts.GopSizes == null) {
      videoGopSizeSpinner.setVisibility(View.GONE);
    } 
    else {
      videoGopSizeSpinner.setVisibility(View.VISIBLE);
      videoGopSizeSpinner.setItems(mkGopSizeStrings(copts.GopSizes));
      videoGopSizeSpinner.setSelection(guessIndexOf(options.sopt.vGopSize, copts.GopSizes));
    }    
    
    
    if (options.sopt.vCodecName == null || options.sopt.vCodecName.isEmpty()
        || options.sopt.vCodecName.equals("none")) {
      videoBufsSpinner.setVisibility(View.GONE);
    }
    else {
      videoBufsSpinner.setVisibility(View.VISIBLE);
    }

    
    copts = CameraPreview.getSupportedCodecOptions(options.sopt.aCodecName);
    
    if (copts == null || copts.QualityValues == null) {
      audioQualitySpinner.setVisibility(View.GONE);
    } 
    else {
      audioQualitySpinner.setVisibility(View.VISIBLE);
      audioQualitySpinner.setItems(mkQualityStrings(copts.QualityValues));
      audioQualitySpinner.setSelection(guessIndexOf(options.sopt.aQuality, copts.QualityValues));
    }    
    
    if (copts == null || copts.BitRates == null) {
      audioBitrateSpinner.setVisibility(View.GONE);
    } 
    else {
      audioBitrateSpinner.setVisibility(View.VISIBLE);
      audioBitrateSpinner.setItems(mkBitrateStrings(copts.BitRates));
      audioBitrateSpinner.setSelection(guessIndexOf(options.sopt.aBitRate, copts.BitRates));
    }    
    
    if ( expertModeCheckBox.isChecked()) {
      ffoptsEditBox.setText(options.sopt.ffopts);
    }

    if (options.sopt.aCodecName == null || options.sopt.aCodecName.isEmpty()
        || options.sopt.aCodecName.equals("none")) {
      audioBufsSpinner.setVisibility(View.GONE);
    }
    else {
      audioBufsSpinner.setVisibility(View.VISIBLE);
    }
    
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
