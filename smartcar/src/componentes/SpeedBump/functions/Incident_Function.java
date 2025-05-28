package componentes.SpeedBump.functions;

import interfaces.IFunction;
import componentes.SpeedBump.SpeedBump;
import interfaces.FunctionStatus;
import utils.MySimpleLogger;

public class Incident_Function implements IFunction {
    protected String id = null;
	protected SpeedBump speedBump = null;

	protected FunctionStatus initialStatus = null;
	protected FunctionStatus status = null;

	private String loggerId = null;

	
	public static Incident_Function build(String id, SpeedBump speedBump, String mqttBrokerURL) {
		return new Incident_Function(id, FunctionStatus.OFF, speedBump, mqttBrokerURL);
	}
	
	public static Incident_Function build(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		return new Incident_Function(id, initialStatus, speedBump, mqttBrokerURL);
	}

	protected Incident_Function(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		this.id = id;
		this.initialStatus = initialStatus;
		this.loggerId = "Function " + id;
	}
		
	@Override
	public String getId() {
		return this.id;
	}
		
	@Override
	public IFunction enable() {
		MySimpleLogger.info(this.loggerId, "==> Forced mode set");
		this.setStatus(FunctionStatus.ON);
		this.speedBump.setForced(true);
		return this;
	}

	@Override
	public IFunction disable() {
		MySimpleLogger.info(this.loggerId, "==> Forced mode unset");
		this.setStatus(FunctionStatus.OFF);
		this.speedBump.setForced(false);
		return this;
	}
	
	protected IFunction _putIntoInitialStatus() {
		switch (this.initialStatus) {
		case ON:
			this.speedBump.setHighTraffic(true);
			this.enable();
			break;
		case OFF:
			this.disable();
			break;

		default:
			break;
		}
		
		return this;

	}

	@Override
	public FunctionStatus getStatus() {
		return this.status;
	}
	
	protected IFunction setStatus(FunctionStatus status) {
		this.status = status;
		return this;
	}
}
