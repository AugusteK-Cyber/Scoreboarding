/*
 * @author Auguste Kiendrebeogo
 * Floating-Point Adder Functional Unit Class
 */
public class FPAdderFU extends FUDriver {
	public FPAdderFU(){
		super("Add");
		this.cycleCount = 2;
		this.type = 1;
	}

	public FPAdderFU(String FUName){
		super(FUName);
		this.cycleCount = 2;
		this.type = 1;
	}
}