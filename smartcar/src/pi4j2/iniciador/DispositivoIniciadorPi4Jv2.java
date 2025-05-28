package pi4j2.iniciador;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;

import componentes.SpeedBump.SpeedBump;
import componentes.pi4j2.FunctionPi4Jv2_Incident;
import componentes.pi4j2.FunctionPi4Jv2_Mode;
import componentes.pi4j2.FunctionPi4Jv2_SpeedBump;
import interfaces.FunctionStatus;

public class DispositivoIniciadorPi4Jv2 {

	public static void main(String[] args) {

		if ( args.length < 4 ) {
			System.out.println("Usage: java -jar dispositivo.jar device deviceIP rest-port mqttBroker");
			System.out.println("Example: java -jar dispositivo.jar ttmi050 ttmi050.iot.upv.es 8182 tcp://ttmi008.iot.upv.es:1883");
			return;
		}

		String deviceId = args[0];
		String deviceIP = args[1];
		String port = args[2];
		String mqttBroker = args[3];
		

		// Configuramos el contexto/plataforma del GPIO de la Raspberry
		Context pi4jContext =  Pi4J.newAutoContext();
		//Platforms platforms = pi4jContext.platforms();

		
		SpeedBump s = SpeedBump.build(deviceId, mqttBroker, Integer.parseInt(port), null);

		// Añadimos funciones al dispositivo
		// f1 - GPIO_17
		FunctionPi4Jv2_SpeedBump f1 = FunctionPi4Jv2_SpeedBump.build("f1", 17, FunctionStatus.OFF, s, mqttBroker, pi4jContext);
		s.addFunction(f1);

		// f2 - GPIO_13
		FunctionPi4Jv2_Mode f2 = FunctionPi4Jv2_Mode.build("f2", 17, FunctionStatus.OFF, s, mqttBroker, pi4jContext);
		s.addFunction(f2);

		// f3 - GPIO_15
		FunctionPi4Jv2_Incident f3 = FunctionPi4Jv2_Incident.build("f3", 17, FunctionStatus.OFF, s, mqttBroker, pi4jContext);
		s.addFunction(f3);
		
		// Arrancamos el dispositivo
		s.start();
	}

}
