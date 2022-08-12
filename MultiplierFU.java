/*
 * @author Auguste Kiendrebeogo
 * Multiplier Functional Unit Class
 */
public class MultiplierFU extends FUDriver{
	public MultiplierFU() {
		super("Mult");
		this.cycleCount = 10;
		this.type = 2;
	}

	public MultiplierFU(String FUName){
		super(FUName);
		this.cycleCount = 10;
		this.type = 2;
	}
}