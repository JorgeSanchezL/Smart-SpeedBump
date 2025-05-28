package componentes.SpeedBump.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Put;

import componentes.Recurso;
import componentes.SpeedBump.SpeedBump;
import interfaces.IFunction;
import interfaces.ISpeedBump;
import utils.MySimpleLogger;

public class Functions_Resource extends Recurso {

    protected String clientId = null;
	protected String brokerURL = null;

	protected SpeedBump speedBump = null;

	public static final String ID = "FUNCTION-ID";
	public static final String PATH = SpeedBump_Resource.PATH + "/function/{" + Functions_Resource.ID + "}";

	public static JSONObject serialize(IFunction f) {
		JSONObject jsonResult = new JSONObject();
		try {
			jsonResult.put("id", f.getId());
			jsonResult.put("status", f.getStatus());
		} catch (JSONException e) {
		}
		return jsonResult;
	}

	protected IFunction getFuncion() {
		ISpeedBump speedBump = this.getSpeedBump_RESTApplication().getSpeedBump();
		String funcionId = getAttribute(Functions_Resource.ID);
		return speedBump.getFunction(funcionId);
	}
    
    @Get
    public Representation get() {
		IFunction f = this.getFuncion();
		if ( f == null ) {
			return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_NOT_FOUND);
		}

    	JSONObject resultJSON = Functions_Resource.serialize(f);
    	this.setStatus(Status.SUCCESS_OK);
        return new StringRepresentation(resultJSON.toString(), MediaType.APPLICATION_JSON);
    }
    
	@Put
	public Representation put(Representation entity) {
		IFunction f = this.getFuncion();

		if (this.speedBump == null || !this.speedBump.isManual() || !this.speedBump.isForced()) {
			MySimpleLogger.warn("SpeedBump-Function", "Cannot modify SpeedBump '" + this.speedBump.getId() + "'. SpeedBump is not manual or forced.");
			return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_FORBIDDEN);
		}

		JSONObject payload = null;
		try {
			payload = new JSONObject(entity.getText());
			String action = payload.getString("action");
			
			if ( action.equalsIgnoreCase("enable") ) {
				f.enable();
			} else if ( action.equalsIgnoreCase("disable") ) {
				f.disable();
			} else {
				MySimpleLogger.warn("SpeedBump-Function", "Could not recognize the action '" + payload + "'. Only accepted: enable or disable");
				return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			
		} catch (JSONException | IOException e) {
			MySimpleLogger.warn("SpeedBump-Function", "Could not recognize the action '" + payload + "'. Only accepted:  enable or disable");
				return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_BAD_REQUEST);
		}

    	JSONObject resultJSON = SpeedBump_Resource.serialize(this.speedBump);
		
		MySimpleLogger.info("SpeedBump-Function", "Modified SpeedBump '" + this.speedBump.getId() + "' with action: " + payload.toString());
		
    	this.setStatus(Status.SUCCESS_OK);
        return new StringRepresentation(resultJSON.toString(), MediaType.APPLICATION_JSON);
	}

	@Options
	public void describe() {
		Set<Method> meths = new HashSet<Method>();
		meths.add(Method.GET);
		meths.add(Method.PUT);
		meths.add(Method.OPTIONS);
		this.getResponse().setAllowedMethods(meths);
	}
}
