package componentes.SpeedBump.rest;

import org.restlet.Component;
import org.restlet.data.Protocol;

import componentes.SpeedBump.SpeedBump;
import utils.MySimpleLogger;

public class SpeedBump_APIREST {
    protected Component component = null;
	protected SpeedBump_RESTApplication app = null;
	protected int port = 8080;
	protected SpeedBump speedBump = null;
	
	private String loggerId = null;
	
	public static SpeedBump_APIREST build(SpeedBump s) {
		return new SpeedBump_APIREST(s);
	}
	
	public static SpeedBump_APIREST build(SpeedBump s, int port) {
		SpeedBump_APIREST api = new SpeedBump_APIREST(s);
		api.setPort(port);
		return api;
	}
	
	protected SpeedBump_APIREST(SpeedBump s) {
		this.speedBump = s;
		this.loggerId = s.getId() + "-apiREST";
	}
	
	protected void setPort(int port) {
		this.port = port;
	}
	
	public void start() {
		
		if ( component == null ) {
			
			// Create a new Component.
			component = new Component();
	
			// Add a new HTTP server listening on port x.
			component.getServers().add(Protocol.HTTP, port);
	
			// Attach the REST application.
			app = new SpeedBump_RESTApplication(this.speedBump);
	
			component.getDefaultHost().attach("", app);
	
			// Start the component.
			try {
				component.start();
				MySimpleLogger.info(this.loggerId, "Iniciado servicio REST en puerto " + port);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}

	}
	
	
	public void stop() {
		MySimpleLogger.info(this.loggerId, "Detenido servicio REST en puerto " + port);
	}
}
