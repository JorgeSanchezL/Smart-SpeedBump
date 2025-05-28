package componentes.SpeedBump.functions;

import interfaces.IFunction;
import componentes.SpeedBump.SpeedBump;
import interfaces.FunctionStatus;
import utils.MySimpleLogger;

public class Mode_Function implements IFunction {
    protected String id = null;
	protected SpeedBump speedBump = null;

	protected FunctionStatus initialStatus = null;
	protected FunctionStatus status = null;

	private String loggerId = null;

	
	public static Mode_Function build(String id, SpeedBump speedBump, String mqttBrokerURL) {
		return new Mode_Function(id, FunctionStatus.OFF, speedBump, mqttBrokerURL);
	}
	
	public static Mode_Function build(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		return new Mode_Function(id, initialStatus, speedBump, mqttBrokerURL);
	}

	protected Mode_Function(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
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
        if (this.speedBump.isForced()) {
            MySimpleLogger.warn(this.loggerId, "Cannot change speed bump mode because it is forced");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Mode set to manual");
		this.setStatus(FunctionStatus.ON);
		this.speedBump.setManual(true);
		return this;
	}

	@Override
	public IFunction disable() {
        if (this.speedBump.isForced()) {
            MySimpleLogger.warn(this.loggerId, "Cannot change speed bump mode because it is forced");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Mode set to automatic");
		this.setStatus(FunctionStatus.OFF);
		this.speedBump.setManual(false);
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
