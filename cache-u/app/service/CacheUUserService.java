/**
 * Copyright 2012-2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package service;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import play.Logger;
import play.libs.F;
import scala.Some;
import securesocial.core.AuthenticationMethod;
import securesocial.core.BasicProfile;
import securesocial.core.OAuth1Info;
import securesocial.core.OAuth2Info;
import securesocial.core.PasswordInfo;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;
import securesocial.core.providers.UsernamePasswordProvider;
import securesocial.core.services.SaveMode;


/**
 * A Sample In Memory user service in Java
 *
 * Note: This is NOT suitable for a production environment and is provided only as a guide.
 * A real implementation would persist things in a database
 */
public class CacheUUserService extends BaseUserService<DemoUser> {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/CacheU";
	static final String USER = "root";
	static final String PASS = "password";
	
	Connection connection = null;
	PreparedStatement statement = null;
	
    public Logger.ALogger logger = play.Logger.of("application.service.InMemoryUserService");

    private HashMap<String, DemoUser> users = new HashMap<String, DemoUser>();
    private HashMap<String, Token> tokens = new HashMap<String, Token>();

    public CacheUUserService() {
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
    
    @Override
    public F.Promise<DemoUser> doSave(BasicProfile profile, SaveMode mode) {
        DemoUser result = null;
        if(logger.isDebugEnabled()){
            logger.debug("doSave: " + profile.userId());
        }
        if (mode == SaveMode.SignUp()) {
        	// Store userId, username, fname, lname, email
        	if(logger.isDebugEnabled()){
                logger.debug("SaveMod = SignUp");
            }
        	try {
        		connection = DriverManager.getConnection(DB_URL,USER,PASS);
        		// TODO Check if username exists. Use numbering system to differentiate same names
        		String defaultUsername = profile.firstName().get() + "." + profile.lastName().get();
            	String userId = profile.userId();
            	String fName = profile.firstName().get();
            	String lName = profile.lastName().get();
            	String email = profile.email().get();
            	String avatarUrl = profile.avatarUrl().get();
            	String authMethod = profile.authMethod().method();
            	String oAuth1Info = (profile.oAuth1Info().nonEmpty()) ? profile.oAuth1Info().get().toString() : null;
            	String oAuth2Info = (profile.oAuth2Info().nonEmpty()) ? profile.oAuth2Info().get().toString() : null;
            	String accessToken = profile.oAuth2Info().get().accessToken();
            	String tokenLifetime = profile.oAuth2Info().get().expiresIn().get().toString();
            	String passwordInfo = (profile.passwordInfo().nonEmpty()) ? profile.passwordInfo().get().toString() : null;
    			statement = connection.prepareStatement("INSERT INTO CacheUUser VALUES (" +
    					"'" + userId + "'," +
    					"'" + defaultUsername + "'," +
    					"'" + fName + "'," +
    					"'" + lName + "'," +
    					"'" + email + "'," +
    					"'" + avatarUrl + "'," + 
    					"'" + authMethod + "'," +
    					"'" + oAuth1Info + "'," +
    					"'" + oAuth2Info + "'," +
    					"'" + accessToken + "'," +
    					"" + Integer.parseInt(tokenLifetime) + "," +
    					"'" + passwordInfo + "'" + ");");
    			statement.executeUpdate();
    			statement.close();
    			connection.close();
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        
            result = new DemoUser(profile);
            //users.put(profile.providerId() + profile.userId(), result);
        } else if (mode == SaveMode.LoggedIn()) {
        	if(logger.isDebugEnabled()){
                logger.debug("SaveMod = LoggedIn");
            }
        	/*
            for (Iterator<DemoUser> it = users.values().iterator() ; it.hasNext() && result == null ; ) {
                DemoUser user = it.next();
                for ( BasicProfile p : user.identities) {
                    if ( p.userId().equals(profile.userId()) && p.providerId().equals(profile.providerId())) {
                        user.identities.remove(p);
                        user.identities.add(profile);
                        result = user;
                        break;
                    }
                }
            }*/
        	try {
        		ResultSet rs;
        		connection = DriverManager.getConnection(DB_URL,USER,PASS);
    			statement = connection.prepareStatement("SELECT COUNT(*) AS cnt FROM CacheUUser WHERE userId='" + profile.userId() + "'");
    			rs = statement.executeQuery();
    			rs.next();
    			// If found, create BasicProfile object
    			if (rs.getInt("cnt") != 0) {
    				System.out.println("---------Found USER");
    				statement = connection.prepareStatement("SELECT * FROM CacheUUser WHERE userId='" + profile.userId() + "'");
    				rs = statement.executeQuery();
    				rs.next();
    				Some<String> fname = new Some<String>(rs.getString("fname"));
    				Some<String> lname = new Some<String>(rs.getString("lname"));
    				Some<String> fullName = new Some<String>(fname.get() + " " + lname.get());
    				Some<String> email = new Some<String>(rs.getString("email"));
    				Some<String> avatarUrl = new Some<String>(rs.getString("avatarUrl"));
    				AuthenticationMethod authMethod = new AuthenticationMethod(rs.getString("authMethod"));
    				String accessToken = rs.getString("accessToken");
    				Some<Object> tokenLifetime = new Some<Object>(rs.getInt("tokenLifetime"));
    				Some<OAuth1Info> oAuth1Info = new Some<OAuth1Info>(new OAuth1Info("", ""));
    				Some<String> tokenType = new Some<String>(null);
    				Some<String> refreshToken = new Some<String>(null);
    				Some<OAuth2Info> oAuth2Info = new Some<OAuth2Info>(new OAuth2Info(accessToken, tokenType, tokenLifetime, refreshToken));
    				Some<PasswordInfo> passwordInfo = new Some<PasswordInfo>(new PasswordInfo("", "", new Some<String>("")));
    				// TODO add database columns for avatarUrl and OAuth. Need to potentially be able to extract this info
    				profile = new BasicProfile(profile.providerId(), profile.userId(), fname, lname, fullName, email, avatarUrl, authMethod, oAuth1Info, oAuth2Info, passwordInfo);
    				result = new DemoUser(profile);
    			}
    			statement.close();
    			connection.close();
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        } else if (mode == SaveMode.PasswordChange()) {
        	if(logger.isDebugEnabled()){
                logger.debug("SaveMod = PasswordChange");
            }
            for (Iterator<DemoUser> it =  users.values().iterator() ; it.hasNext() && result == null ; ) {
                DemoUser user = it.next();
                for (BasicProfile p : user.identities) {
                    if (p.userId().equals(profile.userId()) && p.providerId().equals(UsernamePasswordProvider.UsernamePassword())) {
                        user.identities.remove(p);
                        user.identities.add(profile);
                        result = user;
                        break;
                    }
                }
            }
        } else {
            throw new RuntimeException("Unknown mode");
        }
        return F.Promise.pure(result);
    }

    @Override
    public F.Promise<DemoUser> doLink(DemoUser current, BasicProfile to) {
        DemoUser target = null;
        
        for ( DemoUser u: users.values() ) {
            if ( u.main.providerId().equals(current.main.providerId()) && u.main.userId().equals(current.main.userId()) ) {
                target = u;
                break;
            }
        }

        if ( target == null ) {
            // this should not happen
            throw new RuntimeException("Can't find user : " + current.main.userId());
        }

        boolean alreadyLinked = false;
        for ( BasicProfile p : target.identities) {
            if ( p.userId().equals(to.userId()) && p.providerId().equals(to.providerId())) {
                alreadyLinked = true;
                break;
            }
        }
        if (!alreadyLinked) target.identities.add(to);
        return F.Promise.pure(target);
    }

    @Override
    public F.Promise<Token> doSaveToken(Token token) {
    	if(logger.isDebugEnabled()){
            logger.debug("doSaveToken: " + token.getUuid());
        }
        tokens.put(token.uuid, token);
        return F.Promise.pure(token);
    }

    @Override
    public F.Promise<BasicProfile> doFind(String providerId, String userId) {
    	ResultSet rs = null;
        BasicProfile found = null;
        if(logger.isDebugEnabled()){
            logger.debug("doFind: " + userId);
        }
    	try {
    		
    		connection = DriverManager.getConnection(DB_URL,USER,PASS);
			statement = connection.prepareStatement("SELECT COUNT(*) AS cnt FROM CacheUUser WHERE userId='" + userId + "'");
			rs = statement.executeQuery();
			rs.next();
			// If found, create BasicProfile object
			if (rs.getInt("cnt") != 0) {
				System.out.println("---------Found USER");
				statement = connection.prepareStatement("SELECT * FROM CacheUUser WHERE userId='" + userId + "'");
				rs = statement.executeQuery();
				rs.next();
				Some<String> fname = new Some<String>(rs.getString("fname"));
				Some<String> lname = new Some<String>(rs.getString("lname"));
				Some<String> fullName = new Some<String>(fname.get() + " " + lname.get());
				Some<String> email = new Some<String>(rs.getString("email"));
				Some<String> avatarUrl = new Some<String>(rs.getString("avatarUrl"));
				AuthenticationMethod authMethod = new AuthenticationMethod(rs.getString("authMethod"));
				String accessToken = rs.getString("accessToken");
				Some<Object> tokenLifetime = new Some<Object>(Integer.toString(rs.getInt("tokenLifetime")));
				Some<OAuth1Info> oAuth1Info = new Some<OAuth1Info>(new OAuth1Info("", ""));
				Some<OAuth2Info> oAuth2Info = new Some<OAuth2Info>(new OAuth2Info(accessToken, null, tokenLifetime, null));
				Some<PasswordInfo> passwordInfo = new Some<PasswordInfo>(new PasswordInfo("", "", new Some<String>("")));
				// TODO add database columns for avatarUrl and OAuth. Need to potentially be able to extract this info
				found = new BasicProfile(providerId, userId, fname, lname, fullName, email, avatarUrl, authMethod, oAuth1Info, oAuth2Info, passwordInfo);
			}
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	
        /*
        for ( DemoUser u: users.values() ) {
            for ( BasicProfile i : u.identities ) {
                if ( i.providerId().equals(providerId) && i.userId().equals(userId) ) {
                    found = i;
                    break;
                }
            }
        }*/
		
        return F.Promise.pure(found);
    }

    @Override
    public F.Promise<PasswordInfo> doPasswordInfoFor(DemoUser user) {
        throw new RuntimeException("doPasswordInfoFor is not implemented yet in sample app");
    }

    @Override
    public F.Promise<BasicProfile> doUpdatePasswordInfo(DemoUser user, PasswordInfo info) {
        throw new RuntimeException("doUpdatePasswordInfo is not implemented yet in sample app");
    }

    @Override
    public F.Promise<Token> doFindToken(String tokenId) {
    	if(logger.isDebugEnabled()){
            logger.debug("doFindToken: " + tokenId);
        }
        return F.Promise.pure(tokens.get(tokenId));
    }


    @Override
    public F.Promise<BasicProfile> doFindByEmailAndProvider(String email, String providerId) {
        BasicProfile found = null;
        if(logger.isDebugEnabled()){
            logger.debug("doFindByEmailAndProvider: " + email);
        }
        for ( DemoUser u: users.values() ) {
            for ( BasicProfile i : u.identities ) {
                if ( i.providerId().equals(providerId) && i.email().isDefined() && i.email().get().equals(email) ) {
                    found = i;
                    break;
                }
            }
        }

        return F.Promise.pure(found);
    }

    @Override
    public F.Promise<Token> doDeleteToken(String uuid) {
    	if(logger.isDebugEnabled()){
            logger.debug("doDeleteToken: " + uuid);
        }
        return F.Promise.pure(tokens.remove(uuid));
    }

    @Override
    public void doDeleteExpiredTokens() {
    	if(logger.isDebugEnabled()){
            logger.debug("doDeleteExpiredTokens");
        }
        Iterator<Map.Entry<String,Token>> iterator = tokens.entrySet().iterator();
        while ( iterator.hasNext() ) {
            Map.Entry<String, Token> entry = iterator.next();
            if ( entry.getValue().isExpired() ) {
                iterator.remove();
            }
        }
    }
}