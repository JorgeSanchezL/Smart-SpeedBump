package componentes.SpeedBump.functions;

import interfaces.IFunction;
import componentes.SpeedBump.SpeedBump;
import interfaces.FunctionStatus;
import utils.MySimpleLogger;

public class SpeedBump_Function implements IFunction {
    protected String id = null;
	protected SpeedBump speedBump = null;

	protected FunctionStatus initialStatus = null;
	protected FunctionStatus status = null;

	private String loggerId = null;

	
	public static SpeedBump_Function build(String id, SpeedBump speedBump, String mqttBrokerURL) {
		return new SpeedBump_Function(id, FunctionStatus.OFF, speedBump, mqttBrokerURL);
	}
	
	public static SpeedBump_Function build(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
		return new SpeedBump_Function(id, initialStatus, speedBump, mqttBrokerURL);
	}

	protected SpeedBump_Function(String id, FunctionStatus initialStatus, SpeedBump speedBump, String mqttBrokerURL) {
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
        if (this.speedBump.isForced() || this.speedBump.isManual()) {
            MySimpleLogger.warn(this.loggerId, "Cannot activate speed bump because it is forced or manual");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Enable speed bump at maximum height");
		this.setStatus(FunctionStatus.ON);
		return this;
	}

	@Override
	public IFunction disable() {
        if (this.speedBump.isForced() || this.speedBump.isManual()) {
            MySimpleLogger.warn(this.loggerId, "Cannot deactivate speed bump because it is forced or manual");
            return this;
        }
		MySimpleLogger.info(this.loggerId, "==> Deactivate speed bump");
		this.setStatus(FunctionStatus.OFF);
		return this;
	}
	
	protected IFunction _putIntoInitialStatus() {
		switch (this.initialStatus) {
		case ON:
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
