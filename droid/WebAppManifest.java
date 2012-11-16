public class WebAppManifest {
	private String name;
	private String description;
	private String type;
	private String launch_path;
	/*
	"developer": {
		"name":"",
		"url":""
	},
	"icons":{
		"60":"/icon.png"
	}
	*/
	
	public String getName(){
		return name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getType(){
		return type;
	}
	
	public String getLaunchPath(){
		return launch_path;
	}
	
	public void setLaunchPath(String launchPath){
		launch_path = launchPath; 
	}
}
