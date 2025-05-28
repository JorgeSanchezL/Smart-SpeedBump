package componentes.SmartCar;

import componentes.RoadPlace;
import utils.MySimpleLogger;


public class SmartCar {


	protected String brokerURL = null;

	protected String smartCarID = null;
	protected RoadPlace rp = null;	// simula la ubicación actual del vehículo
	protected SmartCar_RoadInfoSubscriber subscriber = null;
	protected SmartCar_InicidentNotifier notifier = null;
	
	public SmartCar(String id, String brokerURL) {
		
		this.setSmartCarID(id);
		this.brokerURL = brokerURL;
		
		this.notifier = new SmartCar_InicidentNotifier(id + ".incident-notifier", this, this.brokerURL);
		this.notifier.connect();

		this.subscriber = new SmartCar_RoadInfoSubscriber(id + ".info-subscriber", this, this.brokerURL);
		this.subscriber.connect();		
	}
	
	
	public void setSmartCarID(String smartCarID) {
		this.smartCarID = smartCarID;
	}
	
	public String getSmartCarID() {
		return smartCarID;
	}

	// Método para cambiar la ubicación actual del vehículo y notificar la entrada en el nuevo segmento
	public void setCurrentRoadPlace(RoadPlace rp) {
		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		String info = myTopic + "/info";
		String signals = myTopic + "/signals";
		String traffic = myTopic + "/traffic";
		String alerts = myTopic + "/alerts";
		// Si ya hay una posición actual, significa que el vehículo está circulando y tenemos que notificar su salida del segmento actual
		if(this.rp != null){
			this.subscriber.unsubscribe(info);
			this.subscriber.unsubscribe(signals);
			this.subscriber.unsubscribe(traffic);
			this.subscriber.unsubscribe(alerts);
			this.notifier.traffic(this.smartCarID, "VEHICLE_OUT", this.rp);
			
		}
		// Haya o no haya una posición actual, notificamos la entrada en el nuevo segmento
		this.rp = rp;
		myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		this.subscriber.subscribe(info);
		this.subscriber.subscribe(signals);
		this.subscriber.subscribe(traffic);
		this.subscriber.subscribe(alerts);
		this.notifier.traffic(this.smartCarID, "VEHICLE_IN", rp);
	}

	// Método para iniciar la marcha del vehículo y notificar su entrada en el segmento actual
	// hace lo mismo que setCurrentRoadPlace pero con diferentes argumentos
	// 5.1 - Configura el vehículo para que ... reporte su ubicación.
	public void getIntoRoad(String road, int km) {
		RoadPlace new_rp = new RoadPlace(road, km);
		this.setCurrentRoadPlace(new_rp);
	}

	// Método para parar el vehículo y notificar su salida del segmento actual
	public void stop(){
		String myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		String info = myTopic + "/info";
		String signals = myTopic + "/signals";
		String traffic = myTopic + "/traffic";
		String alerts = myTopic + "/alerts";
		if(this.rp != null){
			this.subscriber.unsubscribe(info);
			this.subscriber.unsubscribe(signals);
			this.subscriber.unsubscribe(traffic);
			this.subscriber.unsubscribe(alerts);
			this.notifier.traffic(this.smartCarID, "VEHICLE_OUT", this.rp);
		}
	}

	public RoadPlace getCurrentPlace() {
		return rp;
	}

	public void changeKm(int km) {
		this.getCurrentPlace().setKm(km);
	}
	
	public void notifyIncident(String incidentType) {
		if ( this.notifier == null )
			return;
		
		this.notifier.alert(this.getSmartCarID(), incidentType, this.getCurrentPlace());
	}

}