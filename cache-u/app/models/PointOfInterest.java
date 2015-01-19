package models;

public class PointOfInterest {
	public String locationId;
	public String locationName;	
	public double latitude;
	public double longitude;
	public String description;
	
	public PointOfInterest(String lId, String lName, 
		double longitude, double latitude, String desc) {
		
		this.locationId = lId;
		this.locationName = lName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.description = desc;
	}
}
