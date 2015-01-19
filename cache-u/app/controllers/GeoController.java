package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import models.PointOfInterest;

import com.google.gson.Gson;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import service.DemoUser;
import utilities.DoubleW;
import views.html.index;
import utilities.LocationUtil;

public class GeoController extends Controller {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/CacheU";
	static final String USER = "root";
	static final String PASS = "password";
	
	Connection connection = null;
	PreparedStatement statement = null;
	
    public static Logger.ALogger logger = Logger.of("application.controllers.GeoController");
	private RuntimeEnvironment env;

    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    public GeoController(RuntimeEnvironment env) {
        this.env = env;
    	if(logger.isDebugEnabled()){
            logger.debug("Starting up CacheUUserService");
        }
    	try {
    		System.out.println("Connecting to Database: " + DB_URL);
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * This action only gets called if the user is logged in.
     *
     * @return
     */

    @SecuredAction
    public Result nearby(DoubleW mLatitude, DoubleW mLongitude) {
    	ArrayList<PointOfInterest> list = new ArrayList<PointOfInterest>();
    	ResultSet rs;
        if(logger.isDebugEnabled()){
            logger.debug("access granted to nearby");
        }
        try {
			connection = DriverManager.getConnection(DB_URL,USER,PASS);
			statement = connection.prepareStatement("SELECT * FROM PointsOfInterest;");
			rs = statement.executeQuery();
			String locationId, locationName, description;
			double latitude, longitude;
			while(rs.next()) {
	        	locationId = rs.getString("locationId");
	        	locationName = rs.getString("locationName");
	        	latitude = rs.getDouble("latitude");
	        	longitude = rs.getDouble("longitude");
	        	description = rs.getString("description");
	        	
	        	if (LocationUtil.distance(mLatitude.value, mLongitude.value, latitude, longitude, 'M') < 1) {
	        		list.add(new PointOfInterest(locationId, 
	        									locationName, 
	        									latitude, 
	        									longitude, 
	        									description));
	        	}
	        	
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String jsonResult = new Gson().toJson(list);
        return ok(jsonResult);
    }
}
