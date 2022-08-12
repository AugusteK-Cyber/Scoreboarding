/*
 * @author Auguste Kiendrebeogo
 * Divider Functional Unit Class
 */
public class DividerFU extends FUDriver {
	public DividerFU() {
		super("Divide");
		this.cycleCount = 40;
		this.type = 3;
	}

	public DividerFU(String FUName) {
		super(FUName);
		this.cycleCount = 40;
		this.type = 3;
	}
}
