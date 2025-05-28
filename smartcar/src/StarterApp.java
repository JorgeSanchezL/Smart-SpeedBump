import componentes.RoadPlace;
import componentes.SmartCar.SmartCar;
import componentes.SpeedLimit.SpeedLimit;

public class StarterApp {
    public static void main(String[] args) throws Exception {

		if ( args.length < 2 )
		{
			System.out.println("Usage: SmartCarStarterApp <smartCarID> <brokerURL>");
			System.exit(1);
		}

		String smartCarID = args[0];
		String brokerURL = args[1];

        SmartCar sc1 = new SmartCar(smartCarID, brokerURL);
		SpeedLimit sp1 = new SpeedLimit(new RoadPlace("R1s2a", 300), 100, "sp1", brokerURL);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		// 5.1 - Configura el vehículo para que inicie en un punto específico del PTPaterna
		sc1.getIntoRoad("R1s2a", 300);

		// 5.3 - Ubica dicha señal en un tramo en el que haya vehículos.
		// y reporta un límite de velocidad inferior al existente en la vía.
		sp1.setSpeedLimit(40);

		// 5.5 - Haz que un vehículo reporte un accidente en un tramo.
		sc1.notifyIncident("TRAFFIC_ACCIDENT");
    }
}
