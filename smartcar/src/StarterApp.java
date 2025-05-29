import componentes.RoadPlace;
import componentes.SmartCar.SmartCar;
import componentes.SpeedBump.SpeedBump;
import componentes.SpeedBump.functions.Incident_Function;
import componentes.SpeedBump.functions.Mode_Function;
import componentes.SpeedBump.functions.SpeedBump_Function;
import componentes.SpeedLimit.SpeedLimit;
import componentes.pi4j2.FunctionPi4Jv2_Incident;
import componentes.pi4j2.FunctionPi4Jv2_Mode;
import componentes.pi4j2.FunctionPi4Jv2_SpeedBump;
import interfaces.FunctionStatus;

public class StarterApp {
    public static void main(String[] args) throws Exception {

		if ( args.length < 2 )
		{
			System.out.println("Usage: SmartCarStarterApp <smartCarID> <brokerURL>");
			System.exit(1);
		}

		String speedBumpID = args[0];
		//String smartCarID = args[0];
		String brokerURL = args[1];

        //SmartCar sc1 = new SmartCar(smartCarID, brokerURL);
		//SpeedLimit sp1 = new SpeedLimit(new RoadPlace("R1s2a", 300), 100, "sp1", brokerURL);
		SpeedBump s = SpeedBump.build(speedBumpID, brokerURL, new RoadPlace("R1s2a", 300));
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		// Añadimos funciones al dispositivo
		SpeedBump_Function f1 = SpeedBump_Function.build("f1", FunctionStatus.OFF, s, brokerURL);
		s.addFunction(f1);

		Mode_Function f2 = Mode_Function.build("f2", FunctionStatus.OFF, s, brokerURL);
		s.addFunction(f2);

		Incident_Function f3 = Incident_Function.build("f3", FunctionStatus.OFF, s, brokerURL);
		s.addFunction(f3);
		
		// Arrancamos el dispositivo
		s.start();

		// 5.1 - Configura el vehículo para que inicie en un punto específico del PTPaterna
		//sc1.getIntoRoad("R1s2a", 300);

		// 5.3 - Ubica dicha señal en un tramo en el que haya vehículos.
		// y reporta un límite de velocidad inferior al existente en la vía.
		//sp1.setSpeedLimit(40);

		// 5.5 - Haz que un vehículo reporte un accidente en un tramo.
		//sc1.notifyIncident("TRAFFIC_ACCIDENT");
    }
}
