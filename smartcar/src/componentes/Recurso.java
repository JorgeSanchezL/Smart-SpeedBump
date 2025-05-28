package componentes;

import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import componentes.SpeedBump.rest.SpeedBump_RESTApplication;

public abstract class Recurso extends ServerResource {

	public SpeedBump_RESTApplication getSpeedBump_RESTApplication() {
		return (SpeedBump_RESTApplication) this.getApplication();
	}
	
	protected Representation generateResponseWithErrorCode(Status s) {
		setStatus(s);
		return new EmptyRepresentation();
	}


}
