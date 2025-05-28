package componentes.SpeedBump;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import componentes.RoadPlace;
import componentes.SpeedBump.mqtt.SpeedBump_RoadInfoSubscriber;
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
	protected SpeedBump_RoadInfoSubscriber subscriber = null;
	protected SpeedBump_APIREST apiREST = null;

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

		this.setRoadPlace(new RoadPlace("R5s1", 0));

		this.apiREST = SpeedBump_APIREST.build(this);

		this.subscriber = new SpeedBump_RoadInfoSubscriber(id + ".info-subscriber", this, this.brokerURL);
		this.subscriber.connect();		
	}
	
	public SpeedBump(String id, String brokerURL, int port, RoadPlace roadPlace) {
		
		this.setId(id);
		this.brokerURL = brokerURL;

		this.setRoadPlace(new RoadPlace("R5s1", 0));

		this.apiREST = SpeedBump_APIREST.build(this, port);

		this.subscriber = new SpeedBump_RoadInfoSubscriber(id + ".info-subscriber", this, this.brokerURL);
		this.subscriber.connect();		
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
		String signals = myTopic + "/signals";
		String traffic = myTopic + "/traffic";
		String alerts = myTopic + "/alerts";

		// If there is a previous road place, unsubscribe from its topics
		if(this.rp != null){
			this.subscriber.unsubscribe(info);
			this.subscriber.unsubscribe(signals);
			this.subscriber.unsubscribe(traffic);
			this.subscriber.unsubscribe(alerts);
			
		}
		
		// Connect the subscriber to the new road place
		this.rp = rp;
		myTopic =  "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + rp.getRoad();
		this.subscriber.subscribe(info);
		this.subscriber.subscribe(signals);
		this.subscriber.subscribe(traffic);
		this.subscriber.subscribe(alerts);
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
		this.subscriber.connect();
		this.apiREST.start();
		return this;
	}

	@Override
	public ISpeedBump stop() {
		this.subscriber.disconnect();
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
}
