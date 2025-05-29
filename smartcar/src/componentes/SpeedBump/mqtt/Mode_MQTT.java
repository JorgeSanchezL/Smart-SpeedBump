package componentes.SpeedBump.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONObject;

import com.amazonaws.services.iot.client.AWSIotQos;

import awsiotthing.AWSIoTThingStarter;
import componentes.SpeedBump.SpeedBump;
import interfaces.IFunction;
import utils.MySimpleLogger;

public class Mode_MQTT implements MqttCallback {

	protected MqttClient myClient;
	protected String clientId = null;
	protected String brokerURL = null;

	protected SpeedBump speedBump = null;
	protected IFunction function = null;

    protected String topicFunction = utils.Configuration.TOPIC_BASE + "speedbump/" + this.speedBump.getId() + "/function/" + this.function.getId() + "/info";

	
	public Mode_MQTT(String clientId, SpeedBump speedBump, IFunction function, String MQTTBrokerURL) {
		this.clientId = clientId;
        this.speedBump = speedBump;
		this.function = function;
		this.brokerURL = MQTTBrokerURL;
	}


	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		String payload = new String(message.getPayload());
		
		MySimpleLogger.trace(this.clientId, "-------------------------------------------------");
		MySimpleLogger.trace(this.clientId, "| Topic:" + topic);
		MySimpleLogger.trace(this.clientId, "| Message: " + payload);
		MySimpleLogger.trace(this.clientId, "-------------------------------------------------");

        MySimpleLogger.warn(this.clientId, "Ignoring message from topic: " + topic);
	}

	
	
	@Override
	public void connectionLost(Throwable t) {
		MySimpleLogger.warn(this.clientId, "Connection lost!");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	}


	public void connect() {
		// setup MQTT Client
		MqttConnectOptions connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
//			connOpt.setUserName(M2MIO_USERNAME);
//			connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
		
		// Connect to Broker
		try {
			MqttDefaultFilePersistence persistence = null;
			try {
				persistence = new MqttDefaultFilePersistence("/tmp");
			} catch (Exception e) {
			}
			if ( persistence != null )
				myClient = new MqttClient(this.brokerURL, this.clientId, persistence);
			else
				myClient = new MqttClient(this.brokerURL, this.clientId);

			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		
		MySimpleLogger.trace(this.clientId, "Client connected to " + this.brokerURL);

	}
	
	
	public void disconnect() {
		
		// disconnect
		try {
			// wait to ensure subscribed messages are delivered
			Thread.sleep(120000);

			myClient.disconnect();
			MySimpleLogger.trace(this.clientId, "Client disconnected!");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	public void subscribe(String theTopic) {
		
		// subscribe to topic
		try {
			int subQoS = 0;
			myClient.subscribe(theTopic, subQoS);
			MySimpleLogger.trace(this.clientId, "Client subscribed to the topic " + theTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public void unsubscribe(String theTopic) {
		
		// unsubscribe to topic
		try {
			int subQoS = 0;
			myClient.unsubscribe(theTopic);
			MySimpleLogger.trace(this.clientId, "Client UNsubscribed from the topic " + theTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

    public void publishStatus(String funcion, JSONObject json) {
		MqttTopic topic = myClient.getTopic(this.topicFunction);
		MqttMessage message = new MqttMessage(json.toString().getBytes());
		message.setQos(0);
		message.setRetained(true);

		// Publish the message
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic.publish(message);
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String calculateTopic(IFunction f) {
		return utils.Configuration.TOPIC_BASE + "speedBump/" + this.speedBump.getId() + "/function/" + f.getId();
	}
}
