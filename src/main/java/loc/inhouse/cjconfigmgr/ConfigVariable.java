package loc.inhouse.cjconfigmgr;

public class ConfigVariable<T> {
	private T value;

	public ConfigVariable (T value){
		this.value = value;
	}

	public T getValue(){ return value; }
	public void setValue(T value){ this.value = value; }
}
