package componentes.SpeedBump;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;

import awsiotthing.AWSIoTThingStarter;
import componentes.RoadPlace;
import componentes.SpeedBump.mqtt.Device_MQTT;
import componentes.SpeedBump.rest.SpeedBump_APIREST;
import interfaces.IFunction;
import interfaces.ISpeedBump;
import utils.MySimpleLogger;


public class SpeedBump implements ISpeedBump {
	private int nearVehicles = 0;

	protected String brokerURL = null;

	protected Boolean manual = false;
	protected Boolean forced = false;
	protected Boolean high_traffic = false;

	protected String speedBumpID = null;
	protected RoadPlace rp = null;
	protected Device_MQTT mqttClient = null;
	protected SpeedBump_APIREST apiREST = null;

	protected AWSIotMqttClient awsIotMqttClient = null;

	protected Map<String, IFunction> functions = null;

	public static SpeedBump build(String id, String brokerURL, RoadPlace roadPlace) {
		return new SpeedBump(id, brokerURL, roadPlace);
	}

	public static SpeedBump build(String id, String brokerURL, int port, RoadPlace roadPlace) {
		return new SpeedBump(id, brokerURL, port, roadPlace);
	}

	public SpeedBump(String id, String brokerURL, RoadPlace roadPlace) {
		
		this.setId(id);
		this.brokerURL = brokerURL;

		
		this.apiREST = SpeedBump_APIREST.build(this);
		
		this.mqttClient = new Device_MQTT(id, this, this.brokerURL);
		this.mqttClient.connect();
		
		this.setRoadPlace(roadPlace);

		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		String info = myTopic + "/info";
		String traffic = myTopic + "/traffic";
		
		myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		this.mqttClient.subscribe(info);
		this.mqttClient.subscribe(traffic);

		this.awsIotMqttClient = AWSIoTThingStarter.initClient();
		if (this.awsIotMqttClient != null) {
			MySimpleLogger.info(id, "AWS IoT MQTT client initialized successfully.");
		} else {
			MySimpleLogger.error(id, "Failed to initialize AWS IoT MQTT client.");
		}
		
		try {
			awsIotMqttClient.connect();
			MySimpleLogger.info(id, "Client Connected to AWS IoT MQTT");

		} catch (AWSIotException e) {
			e.printStackTrace();
		}
	}
	
	public SpeedBump(String id, String brokerURL, int port, RoadPlace roadPlace) {
		
		this.setId(id);
		this.brokerURL = brokerURL;

		
		this.apiREST = SpeedBump_APIREST.build(this, port);
		
		this.mqttClient = new Device_MQTT(id, this, this.brokerURL);
		this.mqttClient.connect();		
		
		this.setRoadPlace(roadPlace);
		
		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		String info = myTopic + "/info";
		String traffic = myTopic + "/traffic";
		
		myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		this.mqttClient.subscribe(info);
		this.mqttClient.subscribe(traffic);

		this.awsIotMqttClient = AWSIoTThingStarter.initClient();
		if (this.awsIotMqttClient != null) {
			MySimpleLogger.info(id, "AWS IoT MQTT client initialized successfully.");
		} else {
			MySimpleLogger.error(id, "Failed to initialize AWS IoT MQTT client.");
		}

		try {
			awsIotMqttClient.connect();
			MySimpleLogger.info(id, "Client Connected to AWS IoT MQTT");

		} catch (AWSIotException e) {
			e.printStackTrace();
		}
	}
	
	public void setId(String speedBumpID) {
		this.speedBumpID = speedBumpID;
	}
	
	@Override
	public String getId() {
		return speedBumpID;
	}

	public void setRoadPlace(RoadPlace rp) {
		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		String info = myTopic + "/info";
		String traffic = myTopic + "/traffic";

		// If there is a previous road place, unsubscribe from its topics
		if(this.rp != null){
			this.mqttClient.unsubscribe(info);
			this.mqttClient.unsubscribe(traffic);
			
		}
		
		// Connect the subscriber to the new road place
		this.rp = rp;
		myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		this.mqttClient.subscribe(info);
		this.mqttClient.subscribe(traffic);
	}

	public RoadPlace getRoadPlace() {
		return rp;
	}

	public void addNearVehicle() {
		this.nearVehicles++;
		this.getFunction("f1").enable();
	}

	public void removeNearVehicle() {
		if (this.nearVehicles > 0) {
			this.nearVehicles--;
			if (this.nearVehicles <= 0) {
				this.getFunction("f1").disable();
			}
		} else {
			MySimpleLogger.warn(this.speedBumpID, "No vehicles to remove. Current count: " + this.nearVehicles);
			this.getFunction("f1").disable();
		}
	}

	@Override
	public Boolean isManual() {
		return manual;
	}
	
	@Override
	public void setManual(Boolean manual) {
		this.manual = manual;
		MySimpleLogger.info(this.speedBumpID, "Setting manual mode to " + manual);
	}

	@Override
	public Boolean isForced() {
		return forced;
	}

	@Override
	public void setForced(Boolean forced) {
		this.forced = forced;
		MySimpleLogger.info(this.speedBumpID, "Setting forced mode to " + forced);
	}

	@Override
	public Boolean isHighTraffic() {
		return high_traffic;
	}

	public void setHighTraffic(Boolean high_traffic) {
		this.high_traffic = high_traffic;
		MySimpleLogger.info(this.speedBumpID, "Setting high traffic mode to " + high_traffic);
	}

	@Override
	public ISpeedBump start() {
		this.mqttClient.connect();
		this.apiREST.start();
		return this;
	}

	@Override
	public ISpeedBump stop() {
		this.mqttClient.disconnect();
		this.apiREST.stop();
		return this;
	}

	protected Map<String, IFunction> localGetFunctions() {
		return this.functions;
	}
	
	protected void setFunctions(Map<String, IFunction> fs) {
		this.functions = fs;
	}

	@Override
	public Collection<IFunction> getFunctions() {
		if ( this.localGetFunctions() == null )
			return null;
		return this.localGetFunctions().values();
	}
	
	
	@Override
	public ISpeedBump addFunction(IFunction f) {
		if ( this.localGetFunctions() == null )
			this.setFunctions(new HashMap<String, IFunction>());
		this.localGetFunctions().put(f.getId(), f);
		return this;
	}
	
	
	@Override
	public IFunction getFunction(String funcionId) {
		if ( this.localGetFunctions() == null )
			return null;
		return this.localGetFunctions().get(funcionId);
	}

	public void publishToAWSIoT(String topic, String message) {
		if (awsIotMqttClient != null && awsIotMqttClient.getConnectionStatus() == AWSIotConnectionStatus.CONNECTED) {
			try {
				awsIotMqttClient.publish(topic, AWSIotQos.QOS0, message);
				MySimpleLogger.info(this.speedBumpID, "Message published to AWS IoT: " + message);
			} catch (AWSIotException e) {
				MySimpleLogger.error(this.speedBumpID, "Failed to publish message to AWS IoT: " + e.getMessage());
			}
		} else {
			MySimpleLogger.error(this.speedBumpID, "AWS IoT MQTT client is not connected.");
		}
	}
}
