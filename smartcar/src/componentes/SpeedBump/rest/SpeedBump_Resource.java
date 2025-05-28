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
import interfaces.ISpeedBump;
import utils.MySimpleLogger;

public class SpeedBump_Resource extends Recurso {
	public static final String PATH = "/speed_bump";

	public static JSONObject serialize(ISpeedBump s) {
		JSONObject jsonResult = new JSONObject();
		try {
			jsonResult.put("speed_bump_id", s.getId());
			jsonResult.put("traffic_mode", s.isHighTraffic() ? "high" : "normal");
			jsonResult.put("manual", s.isManual());
			jsonResult.put("forced", s.isForced());
			if ( s.getFunctions() != null ) {
				jsonResult.put("funciones", s.getFunctions());
			}
		} catch (JSONException e) {
		}
		return jsonResult;
	}

	public ISpeedBump getSpeedBump() {
		return this.getSpeedBump_RESTApplication().getSpeedBump();
	}
    
    @Get
    public Representation get() {

    	// Obtenemos el dispositivo
		ISpeedBump d = this.getSpeedBump();

		// Construimos el mensaje de respuesta
    	JSONObject resultJSON = SpeedBump_Resource.serialize(d);    	
    	
		// Si todo va bien, devolvemos el resultado calculado
    	this.setStatus(Status.SUCCESS_OK);
        return new StringRepresentation(resultJSON.toString(), MediaType.APPLICATION_JSON);
    }

	@Put
	public Representation put(Representation entity) {
		ISpeedBump speedBump = this.getSpeedBump();

		if (speedBump == null || !speedBump.isManual() || !speedBump.isForced()) {
			MySimpleLogger.warn("SpeedBump-Function", "Cannot modify SpeedBump '" + speedBump.getId() + "'. SpeedBump is not manual or forced.");
			return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_FORBIDDEN);
		}

		JSONObject payload = null;
		try {
			payload = new JSONObject(entity.getText());
			String action = payload.getString("accion");
			
			if ( action.equalsIgnoreCase("updateManualState") ) {
				processUpdateManualState(payload, speedBump);
			} else if ( action.equalsIgnoreCase("updateForcedState") ) {
				processUpdateForcedState(payload, speedBump);
			} else if ( action.equalsIgnoreCase("enable") ) {
				processEnable(payload, speedBump);
			} else if ( action.equalsIgnoreCase("disable") ) {
				processDisable(payload, speedBump);
			} else {
				MySimpleLogger.warn("SpeedBump-Function", "Could not recognize the action '" + payload + "'. Only accepted: updateManualState, updateForcedState, enable or disable");
				return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			
		} catch (JSONException | IOException e) {
			MySimpleLogger.warn("SpeedBump-Function", "Could not recognize the action '" + payload + "'. Only accepted: updateManualState, updateForcedState, enable or disable");
				return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_BAD_REQUEST);
		}

    	JSONObject resultJSON = SpeedBump_Resource.serialize(speedBump);
		
		MySimpleLogger.info("SpeedBump-Function", "Modified SpeedBump '" + speedBump.getId() + "' with action: " + payload.toString());
		
    	this.setStatus(Status.SUCCESS_OK);
        return new StringRepresentation(resultJSON.toString(), MediaType.APPLICATION_JSON);
	}

	private Representation processUpdateManualState(JSONObject payload, ISpeedBump speedBump) throws JSONException {
		if ( !payload.has("newState") ) {
			MySimpleLogger.warn("SpeedBump-Function", "Cannot modify SpeedBump '" + speedBump.getId() + "'. Missing 'newState' in payload.");
			return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		speedBump.setManual(payload.getBoolean("newState"));
		if (payload.getBoolean("newState")) {
			speedBump.getFunction("f2").enable();
		} else {
			speedBump.getFunction("f2").disable();
		}
		return null;
	}

	private Representation processUpdateForcedState(JSONObject payload, ISpeedBump speedBump) throws JSONException {
		if ( !payload.has("newState") ) {
			MySimpleLogger.warn("SpeedBump-Function", "Cannot modify SpeedBump '" + speedBump.getId() + "'. Missing 'newState' in payload.");
			return this.generateResponseWithErrorCode(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		speedBump.setForced(payload.getBoolean("newState"));
		if (payload.getBoolean("newState")) {
			speedBump.getFunction("f3").enable();
		} else {
			speedBump.getFunction("f3").disable();
		}
		return null;
	}

	private Representation processEnable(JSONObject payload, ISpeedBump speedBump) throws JSONException {
		speedBump.getFunction("f1").enable();
		return null;
	}

	private Representation processDisable(JSONObject payload, ISpeedBump speedBump) throws JSONException {
		speedBump.getFunction("f1").disable();	
		return null;
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
