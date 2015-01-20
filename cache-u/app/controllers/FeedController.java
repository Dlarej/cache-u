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
    	String postId = "testPostId";
    	String userId = "";
    	String timestamp = String.valueOf(System.currentTimeMillis());
    	String latitude = mLatitude.toString();
    	String longitude = mLongitude.toString();
    	String pointOfInterest = "Avant Gardeners";
    	String postType = "1";
    	String anonymous = "0";
    	connection = DriverManager.getConnection(DB_URL,USER,PASS);
    	statement = connection.prepareStatement("INSERT INTO Posts VALUES (" +
				"'" + postId + "'," +
				"'" + userId + "'," +
				"'" + timestamp + "'," +
				"" + latitude + "," +
				"" + longitude + "," +
				"'" + pointOfInterest + "'," + 
				"" + postType + ");");
		statement.executeUpdate();
		statement.close();
		connection.close();
    	
    	return ok();
    }
}
