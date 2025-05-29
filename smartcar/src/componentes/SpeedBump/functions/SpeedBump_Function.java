package componentes.SpeedBump.functions;

import interfaces.IFunction;

import org.json.JSONException;
import org.json.JSONObject;

import componentes.SpeedBump.SpeedBump;
import componentes.SpeedBump.mqtt.SpeedBump_MQTT;
import interfaces.FunctionStatus;
import utils.MySimpleLogger;

public class SpeedBump_Function implements IFunction {
    protected String id = null;
	protected SpeedBump speedBump = null;

	protected FunctionStatus initialStatus = null;
	protected FunctionStatus status = null;

	private String loggerId = null;

	protected SpeedBump_MQTT mqttClient = null;

	protected String topicFunction = utils.Configuration.TOPIC_BASE + "speedbump/" + this.speedBump.getId() + "/function/" + this.id + "/info";
	
	public static SpeedBump_Function build(String id, SpeedBump speedBump, String mqttBrokerURL) {
		return new SpeedBump_Function(id, FunctionStatus.OFF, speedBump, mqttBrokerURL);
	}
	
	public static SpeedBump_Function build(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		return new SpeedBump_Function(id, initialStatus, speedBump, mqttBrokerURL);
	}

	protected SpeedBump_Function(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		this.id = id;
		this.initialStatus = initialStatus;
		this.loggerId = "Function " + id;

		this.mqttClient = new SpeedBump_MQTT(id, speedBump, this, mqttBrokerURL);
		this.mqttClient.connect();

		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + speedBump.getRoadPlace().getRoad();
		String info = myTopic + "/info";
		String traffic = myTopic + "/traffic";
		
		myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + speedBump.getRoadPlace().getRoad();
		this.mqttClient.subscribe(info);
		this.mqttClient.subscribe(traffic);
	}
		
	@Override
	public String getId() {
		return this.id;
	}
		
	@Override
	public IFunction enable() {
        if (this.speedBump.isForced() || this.speedBump.isManual()) {
            MySimpleLogger.warn(this.loggerId, "Cannot activate speed bump because it is forced or manual");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Enable speed bump at maximum height");
		this.setStatus(FunctionStatus.ON);
		this.publishStatus();
		return this;
	}

	@Override
	public IFunction disable() {
        if (this.speedBump.isForced() || this.speedBump.isManual()) {
            MySimpleLogger.warn(this.loggerId, "Cannot deactivate speed bump because it is forced or manual");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Deactivate speed bump");
		this.setStatus(FunctionStatus.OFF);
		this.publishStatus();
		return this;
	}
	
	protected IFunction _putIntoInitialStatus() {
		switch (this.initialStatus) {
		case ON:
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
