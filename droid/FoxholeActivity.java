import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.google.gson.stream.*;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class FoxholeActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		try {
			Reader reader = new InputStreamReader(getAssets().open("manifest.webapp"));
			Gson gson = new GsonBuilder().create();
			WebAppManifest manifest = gson.fromJson(reader, WebAppManifest.class);
			reader.close();
			if(manifest.getLaunchPath() == null){
				manifest.setLaunchPath(parseRawJsonForMozActivities().get(0));
			}
			
	        WebView container = (WebView) findViewById(R.id.foxhole_container);
	        container.getSettings().setJavaScriptEnabled(true);
	        container.loadUrl("file:///android_asset"+manifest.getLaunchPath());
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
