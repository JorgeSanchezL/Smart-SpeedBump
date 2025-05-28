package componentes.SmartCar;

import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;

import componentes.RoadPlace;
import utils.MySimpleLogger;

public class SmartCar_InicidentNotifier extends SmartCar_MqttClient {
	
	public SmartCar_InicidentNotifier(String clientId, SmartCar smartcar, String brokerURL) {
		super(clientId, smartcar, brokerURL);
	}
	
	// 5.5 - Publica en alerts un evento de tráfico 
	public void alert(String smartCarID, String notificationType, RoadPlace place) {

		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + place.getRoad() + "/alerts";

		MqttTopic topic = myClient.getTopic(myTopic);

		String incidentID = java.util.UUID.randomUUID().toString();
		
		JSONObject pubMsg = new JSONObject();
		try {
			pubMsg.put("rt", "traffic::alert");
			pubMsg.put("incident-type", notificationType);
			pubMsg.put("id", incidentID);
			pubMsg.put("road", place.getRoad().substring(0,2));
			pubMsg.put("road-segment", place.getRoad());
			pubMsg.put("starting-position", place.getKm());
			pubMsg.put("ending-position", place.getKm());
			pubMsg.put("description", "Vehicle Crash");
			pubMsg.put("status", "Active");
			pubMsg.put("link", "/incident/" + incidentID);
			} catch (JSONException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}
		
   		int pubQoS = 0;
		MqttMessage message = new MqttMessage(pubMsg.toString().getBytes());
    	message.setQos(pubQoS);
    	message.setRetained(false);

    	// Publish the message
    	MySimpleLogger.trace(this.clientId, "Publishing to topic " + topic + " qos " + pubQoS);
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic.publish(message);
			MySimpleLogger.trace(this.clientId, pubMsg.toString());
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Este método se llama desde SmartCar para alertar de eventos de tráfico
	// notificationType puede ser "VEHICLE_IN" o "VEHICLE_OUT"
	public void traffic(String smartCarID, String notificationType, RoadPlace place) {

		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + place.getRoad() + "/traffic";

		MqttTopic topic = myClient.getTopic(myTopic);


		// publish incident 'basic'
		// TIP: habrá que adaptar este mensaje si queremos conectarlo al servicio de tráfico SmartTraffic PTPaterna,
		// para que siga la estructura allí propuesta (ver documento Seminario 3)
		JSONObject pubMsg = new JSONObject();
		try {
			pubMsg.put("action", notificationType);
			pubMsg.put("road", place.getRoad().substring(0,2));
			pubMsg.put("road-segment", place.getRoad());
			pubMsg.put("vehicle-id", smartCarID);
			pubMsg.put("position", place.getKm());
			pubMsg.put("role", "polisia vlc");
	   		} catch (JSONException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}
		
   		int pubQoS = 0;
		MqttMessage message = new MqttMessage(pubMsg.toString().getBytes());
    	message.setQos(pubQoS);
    	message.setRetained(false);

    	// Publish the message
    	MySimpleLogger.trace(this.clientId, "Publishing to topic " + topic + " qos " + pubQoS);
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic.publish(message);
			MySimpleLogger.trace(this.clientId, pubMsg.toString());
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}