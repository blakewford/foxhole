import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.google.gson.stream.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;
import android.net.Uri;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

public class FoxholeActivity extends Activity{

	public final static String FIREFOX_PACKAGE_NAME = "org.mozilla.firefox";
	public final static String FIREFOX_CLASS_NAME = "org.mozilla.firefox.App";	
	
	private String mLaunchPath;
	private File mExternalStorageDirectory;

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mExternalStorageDirectory = new File(Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name));
		try {
			Reader reader = new InputStreamReader(getAssets().open("manifest.webapp"));
			Gson gson = new GsonBuilder().create();
			WebAppManifest manifest = gson.fromJson(reader, WebAppManifest.class);
			reader.close();
			if(manifest.getLaunchPath() == null){
				manifest.setLaunchPath(parseRawJsonForMozActivities().get(0));
			}
			mLaunchPath = manifest.getLaunchPath();
	        
	        WebView container = (WebView) findViewById(R.id.foxhole_container);
	        container.getSettings().setJavaScriptEnabled(true);
	        container.loadUrl("file:///android_asset"+mLaunchPath);
		} catch (Exception e) {
			//Do nothing!
		}
    }
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu){
		new MenuInflater(this).inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		if(hasFirefox()){
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getResources().getString(R.string.loading));
			dialog.show();
			new Thread(new Runnable(){	
					@Override
					public void run() {
				        copySourceToExternalStorage("");
				        dialog.cancel();
				        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(new File(mExternalStorageDirectory+mLaunchPath)), "text/html");
				        intent.setClassName(FIREFOX_PACKAGE_NAME, FIREFOX_CLASS_NAME);
				        startActivity(intent);
					}			
				}
			).start();
		}else{
			Toast.makeText(this, R.string.not_installed, Toast.LENGTH_LONG).show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean hasFirefox(){
        PackageManager pm = getPackageManager();
        try{
               pm.getPackageInfo(FIREFOX_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
               return true;
        }
        catch (PackageManager.NameNotFoundException e){
               return false;
        }
	}
    
    private void copySourceToExternalStorage(String root){
		try {
			AssetManager manager = getAssets();
	    	String[] sourceFiles = manager.list(root);
	    	for(String file: sourceFiles){
				String path = root+"/"+file;
				if(root == "")
					path = path.substring(1);
				File destinationFile = new File(mExternalStorageDirectory+"/"+path);
				if(manager.list(path).length != 0){
					destinationFile.mkdirs();
					copySourceToExternalStorage(path);
					continue;
				}
				try{
					InputStream	stream = manager.open(path);
			        ArrayList<Byte> binary = new ArrayList<Byte>();
			        while(stream.available() > 0){
			        	binary.add((byte)stream.read());
			        }
			        stream.close();
			        if(!destinationFile.exists())
			        	destinationFile.createNewFile();
			        FileOutputStream outStream = new FileOutputStream(destinationFile);
			        byte[] bytes = new byte[binary.size()];
			        for(int i =0; i < binary.size(); i++){
			        	bytes[i] = binary.get(i);
			        }
			        outStream.write(bytes); 
			        outStream.close();
				}catch(Exception e){
					//Do nothing!
				}
	    	}
		} catch (Exception e) {
			//Do nothing!
		}
    }
	
	private ArrayList<String> parseRawJsonForMozActivities(){
		ArrayList<String> activities = new ArrayList<String>();
		try{
			InputStreamReader reader = new InputStreamReader(getAssets().open("manifest.webapp"));
			JsonReader raw = new JsonReader(reader);
			raw.beginObject();
			
			while(raw.hasNext()){
				if(raw.nextName().equals("activities")){
					raw.beginObject();
					while(raw.hasNext()){
						raw.nextName();
						raw.beginObject();
						while(raw.hasNext()){
							String name = raw.nextName();
							if(name.equals("href")){
								activities.add(raw.nextString());
							}
							else
								raw.skipValue();
						}
						raw.endObject();
					}
					raw.endObject();
				}
				else
					raw.skipValue();
			}
			raw.endObject();
			reader.close();
		}catch (Exception e) {
			//Do Nothing!
		}
		
		return activities;
	}
}
