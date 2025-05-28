package componentes.SpeedBump.mqtt;


import org.eclipse.paho.client.mqttv3.MqttMessage;
import utils.MySimpleLogger;
import org.json.JSONObject;

import componentes.SpeedBump.SpeedBump;

public class SpeedBump_RoadInfoSubscriber extends SpeedBump_MqttClient {

	protected SpeedBump speedBump;
	
	public SpeedBump_RoadInfoSubscriber(String clientId, SpeedBump speedBump, String MQTTBrokerURL) {
		super(clientId, speedBump, MQTTBrokerURL);
		this.speedBump = speedBump;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		super.messageArrived(topic, message);
		String payload = new String(message.getPayload());
		
		if (topic.endsWith("/info")) {
			JSONObject jsonObject = new JSONObject(payload);
			if (jsonObject.has("type") && !"ROAD_INCIDENT".equals(jsonObject.getString("type"))) {
				processRoadIncidentMessage(topic, jsonObject);
				return;
			} else if (jsonObject.has("type") && "ROAD_STATUS".equals(jsonObject.getString("type"))) {
				processRoadStatusMessage(topic, jsonObject);
				return;
			} else {
				MySimpleLogger.trace(this.clientId, "Ignoring message from topic: " + topic);
				return; // Ignore messages that are not related to road incidents
			}

		} else if (topic.endsWith("/traffic")) {
			JSONObject jsonObject = new JSONObject(payload);
			if (jsonObject.has("role") && "EmergencyVehicle".equals(jsonObject.getString("type"))) {
				processEmergencyVehicleInfoMessage(topic, jsonObject);
				return;
			} else {
				processVehicleInfoMessage(topic, jsonObject);
			}
		} else {
			MySimpleLogger.trace(this.clientId, "Ignoring message from topic: " + topic);
		}
	}

	protected void processRoadIncidentMessage(String topic, JSONObject jsonObject) throws Exception {
		// Process the message
		MySimpleLogger.trace(this.clientId, "Processing road incident message from topic: " + topic);
		if (jsonObject.has("incident-type") && "TRAFFIC_ACCIDENT".equals(jsonObject.getString("incident-type"))) {
			MySimpleLogger.info(this.clientId, "Traffic accident alert received: " + jsonObject.toString());

			if (jsonObject.has("status") && "ACTIVE".equals(jsonObject.getString("status"))) {
				MySimpleLogger.info(this.clientId, "Traffic accident is active, enabling speed bump function.");
				// If a traffic accident is detected, enable the speed bump function in high traffic mode
				this.speedBump.setHighTraffic(true);
				this.speedBump.getFunction("f1").enable();
				this.speedBump.getFunction("f3").enable(); // Set the speed bump to forced mode so it cannot be disabled
			} else {
				MySimpleLogger.info(this.clientId, "Traffic accident is not active, disabling speed bump function.");
				// If a traffic accident is no longer active, disable the forced mode and let the speed bump function return to normal
				this.speedBump.getFunction("f3").disable();
			}
		}
	}

	protected void processRoadStatusMessage(String topic, JSONObject jsonObject) throws Exception {
		// Process the message
		MySimpleLogger.trace(this.clientId, "Processing road status message from topic: " + topic);
		if (jsonObject.has("status") && "HIGH_TRAFFIC".equals(jsonObject.getString("status"))) {
			MySimpleLogger.info(this.clientId, "High traffic alert received, setting speed bump to high traffic mode.");
			this.speedBump.setHighTraffic(true);
		} else if (jsonObject.has("status") && "NORMAL".equals(jsonObject.getString("status"))) {
			MySimpleLogger.info(this.clientId, "Normal traffic status received, setting speed bump to normal mode.");
			this.speedBump.setHighTraffic(false);
		}
	}

	protected void processVehicleInfoMessage(String topic, JSONObject jsonObject) throws Exception {
		// Process the message
		MySimpleLogger.trace(this.clientId, "Processing vehicle info message from topic: " + topic);
		if (jsonObject.has("action") && "VEHICLE_IN".equals(jsonObject.getString("action"))) {
			this.speedBump.addNearVehicle();
		} else if (jsonObject.has("action") && "VEHICLE_OUT".equals(jsonObject.getString("action"))) {
			this.speedBump.removeNearVehicle();
		} else {
			MySimpleLogger.trace(this.clientId, "Ignoring message from topic: " + topic);
			return; // Ignore messages that are not related to vehicle info
		}
	}

		protected void processEmergencyVehicleInfoMessage(String topic, JSONObject jsonObject) throws Exception {
		// Process the message
		MySimpleLogger.trace(this.clientId, "Processing vehicle info message from topic: " + topic);
		if (jsonObject.has("action") && "VEHICLE_IN".equals(jsonObject.getString("action"))) {
			this.speedBump.setHighTraffic(true);
			this.speedBump.getFunction("f1").enable();
			this.speedBump.getFunction("f3").enable(); // Set the speed bump to forced mode so it cannot be disabled
		} else if (jsonObject.has("action") && "VEHICLE_OUT".equals(jsonObject.getString("action"))) {
			this.speedBump.getFunction("f3").disable();
		} else {
			MySimpleLogger.trace(this.clientId, "Ignoring message from topic: " + topic);
			return; // Ignore messages that are not related to vehicle info
		}
	}
}