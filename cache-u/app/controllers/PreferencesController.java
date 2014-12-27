package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class PreferencesController extends Controller {

    public static Result index() {
        return ok("Hello, World. Welcome to the CacheUApiController");
    }
    
}
