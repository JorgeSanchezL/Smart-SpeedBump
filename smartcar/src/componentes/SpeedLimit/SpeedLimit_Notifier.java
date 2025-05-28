package componentes.SpeedLimit;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;

import componentes.RoadPlace;
import utils.MySimpleLogger;

public class SpeedLimit_Notifier extends SpeedLimit_MqttClient {
    
    public SpeedLimit_Notifier(String clientId, SpeedLimit speedLimit, String brokerURL) {
        super(clientId, speedLimit, brokerURL);
    }

    // Publica en signals el limite de velocidad
    public void alert(String signalID, RoadPlace place) {

        String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + place.getRoad() + "/signals";

        MqttTopic topic = myClient.getTopic(myTopic);

        JSONObject pubMsg = new JSONObject();
        try {
            pubMsg.put("rt", "traffic-signal");
            pubMsg.put("id", signalID);
            pubMsg.put("road", place.getRoad().substring(0,2));
            pubMsg.put("road-segment", place.getRoad());
            pubMsg.put("signal-type", "SPEED_LIMIT");
            pubMsg.put("starting-position", place.getKm());
            pubMsg.put("ending-position", place.getKm());
            pubMsg.put("value", speedLimit.getSpeedLimit());
            } catch (JSONException e1) {
            e1.printStackTrace();
        }
        
        int pubQoS = 0;
        MqttMessage message = new MqttMessage(pubMsg.toString().getBytes());
        message.setQos(pubQoS);
        message.setRetained(false);

        MySimpleLogger.trace(this.clientId, "Publishing to topic " + topic + " qos " + pubQoS);
        MqttDeliveryToken token = null;
        try {
            token = topic.publish(message);
            MySimpleLogger.trace(this.clientId, pubMsg.toString());
            token.waitForCompletion();
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
