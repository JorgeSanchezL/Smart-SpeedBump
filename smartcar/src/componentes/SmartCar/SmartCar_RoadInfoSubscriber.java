package componentes.SmartCar;


import org.eclipse.paho.client.mqttv3.MqttMessage;
import utils.MySimpleLogger;
import org.json.JSONObject;

public class SmartCar_RoadInfoSubscriber extends SmartCar_MqttClient {

	protected SmartCar theSmartCar;
	
	public SmartCar_RoadInfoSubscriber(String clientId, SmartCar smartcar, String MQTTBrokerURL) {
		super(clientId, smartcar, MQTTBrokerURL);
		this.smartcar = smartcar;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		super.messageArrived(topic, message);
		String payload = new String(message.getPayload());
		
		// 5.5 - Otros vehículos que estén circulando por el mismo tramo deben recibir el mensaje a través del canal 'info'.
		if (topic.endsWith("/info")) {
			// Process the message
			MySimpleLogger.trace(this.clientId, "Processing message from topic: " + topic);

			JSONObject jsonObject = new JSONObject(payload);
			if (jsonObject.has("incident-type") && "TRAFFIC_ACCIDENT".equals(jsonObject.getString("incident-type"))) {
				MySimpleLogger.info(this.clientId, "Traffic accident alert received: " + jsonObject.toString());
			}
		} else {
			MySimpleLogger.trace(this.clientId, "Ignoring message from topic: " + topic);
		}
	}

	

}
