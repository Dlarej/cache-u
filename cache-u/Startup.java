import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import play.*;
import play.db.DB;

public class Startup extends GlobalSettings {
	@Override
	public void onStart(Application app) {
		System.out.println("STARTING UP");
	}
}
