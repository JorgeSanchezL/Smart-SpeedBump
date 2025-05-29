package componentes.SpeedBump.functions;

import interfaces.IFunction;

import org.json.JSONException;
import org.json.JSONObject;

import componentes.SpeedBump.SpeedBump;
import componentes.SpeedBump.mqtt.Mode_MQTT;
import interfaces.FunctionStatus;
import utils.MySimpleLogger;

public class Mode_Function implements IFunction {
    protected String id = null;
	protected SpeedBump speedBump = null;

	protected FunctionStatus initialStatus = null;
	protected FunctionStatus status = null;

	private String loggerId = null;

	protected Mode_MQTT mqttClient = null;
	
	protected String topicFunction = utils.Configuration.TOPIC_BASE + "speedbump/" + this.speedBump.getId() + "/function/" + this.id + "/info";

	public static Mode_Function build(String id, SpeedBump speedBump, String mqttBrokerURL) {
		return new Mode_Function(id, FunctionStatus.OFF, speedBump, mqttBrokerURL);
	}
	
	public static Mode_Function build(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		return new Mode_Function(id, initialStatus, speedBump, mqttBrokerURL);
	}

	protected Mode_Function(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		this.id = id;
		this.initialStatus = initialStatus;
		this.loggerId = "Function " + id;

		this.mqttClient = new Mode_MQTT(id, speedBump, this, mqttBrokerURL);
		this.mqttClient.connect();
	}
		
	@Override
	public String getId() {
		return this.id;
	}
		
	@Override
	public IFunction enable() {
        if (this.speedBump.isForced()) {
            MySimpleLogger.warn(this.loggerId, "Cannot change speed bump mode because it is forced");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Mode set to manual");
		this.setStatus(FunctionStatus.ON);
		this.speedBump.setManual(true);
		this.publishStatus();
		return this;
	}

	@Override
	public IFunction disable() {
        if (this.speedBump.isForced()) {
            MySimpleLogger.warn(this.loggerId, "Cannot change speed bump mode because it is forced");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Mode set to automatic");
		this.setStatus(FunctionStatus.OFF);
		this.speedBump.setManual(false);
		this.publishStatus();
		return this;
	}
	
	protected IFunction _putIntoInitialStatus() {
		switch (this.initialStatus) {
		case ON:
			this.speedBump.setHighTraffic(true);
			this.enable();
			break;
		case OFF:
			this.disable();
			break;

		default:
			break;
		}
		
		return this;

	}

	@Override
	public FunctionStatus getStatus() {
		return this.status;
	}
	
	protected IFunction setStatus(FunctionStatus status) {
		this.status = status;
		return this;
	}

	private void publishStatus() {
		JSONObject pubMsg = new JSONObject();
		try {
			pubMsg.put("action", this.getStatus().name());
	   		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		this.mqttClient.publishStatus(this.getId(), pubMsg);
		this.speedBump.publishToAWSIoT(topicFunction, pubMsg.toString());
	}
}
