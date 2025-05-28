package componentes.pi4j2;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.platform.Platforms;

import componentes.SpeedBump.SpeedBump;
import componentes.SpeedBump.functions.SpeedBump_Function;
import interfaces.FunctionStatus;
import interfaces.IFunction;

public class FunctionPi4Jv2_Mode extends SpeedBump_Function implements ISignallable {

	protected Platforms platforms = null;
	protected Context pi4jContext = null;
	protected boolean isBlinking = false;
	protected ScheduledSignallerWorker blinkingWorker = null;
	
	protected int gpioPin = 17;
	protected DigitalOutput pin = null;

	public static FunctionPi4Jv2_Mode build(String id, int gpioPin, SpeedBump speedBump, String mqttBrokerURL, Context pi4jContext) {
		FunctionPi4Jv2_Mode f = new FunctionPi4Jv2_Mode(id, gpioPin, FunctionStatus.OFF, speedBump, mqttBrokerURL, pi4jContext);
		return f;
	}
	
	public static FunctionPi4Jv2_Mode build(String id, int gpioPin, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL, Context pi4jContext) {
		FunctionPi4Jv2_Mode f = new FunctionPi4Jv2_Mode(id, gpioPin, initialStatus, speedBump, mqttBrokerURL, pi4jContext);
		return f;
	}

	protected FunctionPi4Jv2_Mode(String id, int gpioPin, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL, Context pi4jContext) {
		super(id, initialStatus, speedBump, mqttBrokerURL);
		this.gpioPin = gpioPin;
		this.pi4jContext = pi4jContext;
		
		DigitalState initialDigitalState = ( this.initialStatus == FunctionStatus.OFF ) ? DigitalState.LOW : DigitalState.HIGH;
		
		DigitalOutputConfigBuilder pinConfig = DigitalOutput.newConfigBuilder(pi4jContext)
			      .id(this.id)
			      .name(this.id)
			      .address(this.gpioPin)
			      .shutdown(initialDigitalState)
			      .initial(initialDigitalState)
			      .provider("pigpio-digital-output");
			      
		this.pin = pi4jContext.create(pinConfig);
		
		this.blinkingWorker = new ScheduledSignallerWorker(1000);
		this.blinkingWorker
			.addSignallable(this);
	}
	
	@Override
	public IFunction enable() {
		if (this.speedBump.isForced()) {
            return this;
        }
		this.cancelBlinking();
		super.enable();
		this.pin.high();
		return this;
	}

	@Override
	public IFunction disable() {
		 if (this.speedBump.isForced()) {
            return this;
        }
		this.cancelBlinking();
		super.disable();
		this.pin.low();
		return this;
	}
	
	protected void cancelBlinking() {
		this.blinkingWorker.stop();
		this.isBlinking=false;
	}
	
	@Override
	public ISignallable signal() {
		this.pin.toggle();
		return this;
	}

}
