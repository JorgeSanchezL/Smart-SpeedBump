package interfaces;

import java.util.Collection;

public interface ISpeedBump {
    public String getId();
	
	public ISpeedBump start();
	public ISpeedBump stop();
		
	public ISpeedBump addFunction(IFunction f);
	public IFunction getFunction(String funcionId);
	public Collection<IFunction> getFunctions();
    
	public Boolean isHighTraffic();
	public Boolean isManual();
	public void setManual(Boolean manual);
	public Boolean isForced();
	public void setForced(Boolean forced);
}
