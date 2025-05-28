package componentes.SpeedBump.rest;

import java.util.Arrays;
import java.util.HashSet;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.service.CorsService;

import componentes.SpeedBump.SpeedBump;
import utils.MySimpleLogger;

public class SpeedBump_RESTApplication extends Application {

	protected SpeedBump speedBump = null;
	private String loggerId = null;
	
	public SpeedBump_RESTApplication(SpeedBump speedBump) {
		this.speedBump = speedBump;
		this.loggerId = speedBump.getId() + "-apiREST";
		
	    CorsService corsService = new CorsService();         
	    corsService.setAllowedOrigins(new HashSet(Arrays.asList("*")));
	    corsService.setAllowedCredentials(true);
	    getServices().add(corsService);
	    
	}
	

	/**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot() {
        // Create a router Restlet that routes each call to a new instance.
        Router router = new Router(getContext());

        // Defines Routes to the different Resources
        router.attach(SpeedBump_Resource.PATH, SpeedBump_Resource.class);
        MySimpleLogger.trace(this.loggerId, "Registrada ruta " + SpeedBump_Resource.PATH + " en api REST");

        return router;
    }
	
    
    public SpeedBump getSpeedBump() {
    	return this.speedBump;
    }
}
