package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.PointOfInterest;

import com.google.gson.Gson;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import service.DemoUser;
import utilities.DoubleW;
import utilities.LocationUtil;

public class FeedController extends Controller {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/CacheU";
	static final String USER = "root";
	static final String PASS = "password";
	
	Connection connection = null;
	PreparedStatement statement = null;
	
    public static Logger.ALogger logger = Logger.of("application.controllers.FeedController");
	private RuntimeEnvironment env;

    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    public FeedController(RuntimeEnvironment env) {
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
   
    @SecuredAction
    public Result viewPost(DoubleW postId) throws SQLException {
	String sql = "SELECT * FROM Posts WHERE postId=?";
    	connection = DriverManager.getConnection(DB_URL,USER,PASS);
    	statement = connection.prepareStatement(sql);
	statement.setDouble(1, postId.value.doubleValue());
	ResultSet rs = statement.executeQuery();

	while(rs.next()) {
            rs.get
	}

	// Construct JSON out of post details
	return ok("Post Details");
    }
 
    /**
     * This action only gets called if the user is logged in.
     *
     * @return
     * @throws SQLException 
     */
    @SecuredAction
    public Result post(DoubleW mLatitude, DoubleW mLongitude) throws SQLException {
    	RequestBody body = request().body();
    	logger.info(body.asJson().get("status").asText());
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//get current date time with Date()
		Date date = new Date();
		dateFormat.format(date);
		java.sql.Date dateDB = new java.sql.Date(date.getTime());
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
    	String postId = String.valueOf(r.nextInt());
    	String userId = "10203373126632130"; //TODO: Get using fb api on client
    	String timestamp = dateFormat.format(dateDB);
    	String latitude = mLatitude.javascriptUnbind();
    	String longitude = mLongitude.javascriptUnbind();
    	String pointOfInterest = "MyHouseID"; // TODO: Get using selection process on client
    	String postType = "0";
    	String text = body.asJson().get("status").asText();
    	String sql = "INSERT INTO Posts (postId, userId, timestamp, latitude, longitude, pointofinterest, postType, text) VALUES (?,?,?,?,?,?,?,?)";
    	connection = DriverManager.getConnection(DB_URL,USER,PASS);
    	statement = connection.prepareStatement(sql);
    	statement.setString(1, postId);
    	statement.setString(2, userId);
    	statement.setDate(3, dateDB);
    	statement.setDouble(4, mLatitude.value.doubleValue());
    	statement.setDouble(5, mLongitude.value.doubleValue());
    	statement.setString(6, pointOfInterest);
    	statement.setString(7, postType);
    	statement.setString(8, text);
    	
	statement.executeUpdate();
	statement.close();
	connection.close();
    	
    	return ok();
    }
    
    /**
     * Returns list of posts in the same area. For now limit to 10.
     * @throws SQLException 
     * @throws JSONException 
     * 
     */
    public Result feed(DoubleW maxId, DoubleW sinceId, DoubleW mLatitude, DoubleW mLongitude) throws SQLException, JSONException {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	connection = DriverManager.getConnection(DB_URL,USER,PASS);
    	statement = connection.prepareStatement("SELECT * FROM Posts LIMIT 10;");
		ResultSet rs = statement.executeQuery();
		// Sort/filter posts by location. TODO: DO THIS IN SQL EVENTUALLY!!
		JSONObject result = new JSONObject();
		JSONArray posts = new JSONArray();
		
		while (rs.next()) {
			// Use LocationUtil to determine distance
			String postId = rs.getString("postId");
			String userId = rs.getString("userId");
			Date timestamp = rs.getDate("timestamp");
			Double latitude = rs.getBigDecimal("latitude").doubleValue();
			Double longitude = rs.getBigDecimal("longitude").doubleValue();
			String poi = rs.getString("pointofinterest");
			String text = rs.getString("text");
			if (LocationUtil.distance(mLatitude.value, mLongitude.value, latitude, longitude, 'M') < 1) {
				JSONObject post = new JSONObject();
				post.put("postId", postId);
				post.put("userId", userId);
				post.put("timestamp", timestamp);
				post.put("latitude", latitude);
				post.put("longitude", longitude);
				post.put("pointofinterest", poi);
				post.put("text", text);
				posts.put(post);
			}
		}
		result.put("result", posts);
		statement.close();
		connection.close();
		return ok(result.toString());
    }
}
