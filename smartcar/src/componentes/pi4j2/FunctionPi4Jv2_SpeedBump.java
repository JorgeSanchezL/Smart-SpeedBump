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

public class FunctionPi4Jv2_SpeedBump extends SpeedBump_Function implements ISignallable {

	protected Platforms platforms = null;
	protected Context pi4jContext = null;
	protected boolean isBlinking = false;
	protected ScheduledSignallerWorker blinkingWorker = null;
	
	protected int gpioPin = 17;
	protected DigitalOutput pin = null;

	public static FunctionPi4Jv2_SpeedBump build(String id, int gpioPin, SpeedBump speedBump, String mqttBrokerURL, Context pi4jContext) {
		FunctionPi4Jv2_SpeedBump f = new FunctionPi4Jv2_SpeedBump(id, gpioPin, FunctionStatus.OFF, speedBump, mqttBrokerURL, pi4jContext);
		return f;
	}
	
	public static FunctionPi4Jv2_SpeedBump build(String id, int gpioPin, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL, Context pi4jContext) {
		FunctionPi4Jv2_SpeedBump f = new FunctionPi4Jv2_SpeedBump(id, gpioPin, initialStatus, speedBump, mqttBrokerURL, pi4jContext);
		return f;
	}

	// We could add the pins here to be able to be configured
	protected FunctionPi4Jv2_SpeedBump(String id, int gpioPin, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL, Context pi4jContext) {
		super(id, initialStatus, speedBump, mqttBrokerURL);
        this.gpioPin = gpioPin;
		this.pi4jContext = pi4jContext;
		
		// Configure the GPIO pins for the speed bump function
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
		if (super.speedBump.isForced() || super.speedBump.isManual()) {
            return this;
        }
		this.cancelBlinking();
		super.enable();
		if (super.speedBump.isHighTraffic()) {
			this.pin.high();
		} else {
			if ( !this.isBlinking ) {
				this.blinkingWorker.start();
				this.isBlinking=true;
			}
		}
		return this;
	}

	@Override
	public IFunction disable() {
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
