package componentes.SpeedLimit;
import componentes.RoadPlace;

// 5.3 - Modifica el proyecto smartcar para crear otro nuevo dispositivo de tipo 'señal de tráfico - límite de velocidad'.

public class SpeedLimit {
    protected RoadPlace rp;
    protected int speedLimit;
    protected String speedLimitID = null;
    protected String brokerURL = null;
    protected SpeedLimit_Notifier notifier = null;


    public SpeedLimit(RoadPlace rp, int speedLimit, String id, String brokerURL) {
        this.rp = rp;
        this.speedLimit = speedLimit;

        this.setSpeedLimitID(id);
		this.brokerURL = brokerURL;
		
		this.notifier = new SpeedLimit_Notifier(speedLimitID, this, brokerURL);
		this.notifier.connect();
    }

    public String getSpeedLimitID() {
        return speedLimitID;
    }

    public void setSpeedLimitID(String speedLimitID) {
        this.speedLimitID = speedLimitID;
    }

    public RoadPlace getLocation() {
        return rp;
    }

    public void setLocation(RoadPlace rp) {
        this.rp = rp;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
        this.notifier.alert(this.speedLimitID, rp);
    }
}
