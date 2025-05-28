package interfaces;

public interface IFunction {
	
	public String getId();
	
	public IFunction enable();
	public IFunction disable();
	
	public FunctionStatus getStatus();
}
