package interfaces;

public enum FunctionStatus {
	
	ON("on"),
	OFF("off"),;

	private String name = null;
	private FunctionStatus(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public FunctionStatus getStatus(String code) {
		for(FunctionStatus f : FunctionStatus.values())
			if ( f.name().equalsIgnoreCase(code) || f.getName().equalsIgnoreCase(code) )
				return f;
		return null;
	}

}
