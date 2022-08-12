/**
 * @author Auguste Kiendrebeogo based on @author Matthew Kelly work
 * This class contains the different functional Units attributes
 */

import java.util.ArrayList;

public class FUDriver {

	// Variables
	public boolean status = false;
	int releaseTime = 0;
	public String Op = "", Fi = "", Fj = "", Fk = "", Qj = "", Qk = "", Rj = "", Rk = "", FUName = "";
	public int time = -1, cycleCount = 0, type = 0;

	// Default constructor
	public FUDriver() {

	}


	// Constructor with 1 parameter
	public FUDriver(String fUName) {
		this.FUName = fUName;
	}

	// Reset all values of functional units
	public void resetValues(){
		this.Fi = this.Fj = this.Fk = this.Qj = this.Qk = this.Rj = this.Rk = this.Op = "";
		this.status = false;
		this.time = -1;
	}

	@Override
	public String toString(){//this function is used for debugging, gets all values of fu and outputs
		String ret = "";
		if (this.time == -1) {ret += " ";}
		else { ret += Integer.toString(this.time);}
		ret += this.time + " ";
		ret += this.FUName + this.spaces(8-this.FUName.length())+"| ";
		ret += this.Op + " ";
		ret += this.Fi + " ";
		ret += this.Fj + " ";
		ret += this.Fk + " ";
		ret += this.Qj + " ";
		ret += this.Qk + " ";
		ret += this.Rj + " ";
		ret += this.Rk + " ";
		return ret;
	}

	public ArrayList toArray(){
		ArrayList a = new ArrayList(11);
		if (this.time == -1) {
			a.add(0, "");
		}
		else {
			a.add(0,Integer.toString(this.time));
		}
		a.add(1, this.FUName);

		if(this.status){
			a.add(2,"Yes");
		}
		else {
			a.add(2,"No");
		}
		a.add(3, this.Op);
		a.add(4, this.Fi);
		a.add(5, this.Fj);
		a.add(6, this.Fk);
		a.add(7, this.Qj);
		a.add(8, this.Qk);
		a.add(9, this.Rj);
		a.add(10, this.Rk);
		return a;
	}

	public String spaces(int n){
		String s = "";
		for(int i=n; i>0; i--){s+=" ";}
		return s;
	}
}
