/**
 * @author Auguste Kiendrebeogo based on @author Matthew Kelly work
 * This class creates the values for the registers that will be used. 
 * It also creates and initializes the memory and its initial values.
 */

import java.util.*;

public class RegistersInit implements Comparable<RegistersInit> {

	// Variables
	public String regName, regFU; // Register name and FU used
	public int regType; // Register value and type (source or destination)
	public double regVal = 0;
	int [][] memory = new int[19][2]; // For memory and its values

	// Default constructor
	public RegistersInit() {
		this.regName = "";
		this.regFU = "";
		this.regVal = 0;
		this.regType = -1;
	}

	// Constructor with register name as parameter
	public RegistersInit(String regName) {
		this.regName = regName;
		this.regFU = "";
		this.regVal = 0;
		this.regType = -1;
	}

	// Constructor with register value as parameter
	public RegistersInit(int regVal) {
		this.regName = "";
		this.regFU = "";
		this.regVal = regVal;
		this.regType = -1;
	}

	// Constructor with multiple parameters
	public RegistersInit(String regName, String regFU, int regVal, int regType) {
		this.regName = regName;
		this.regFU = regFU;
		this.regVal = regVal;
		this.regType = regType;
	}
	
	// Initialize memory
	{
		memory[0][1] = 45;
		memory[1][1] = 12;
		memory[2][1] = 0;
		memory[3][1] = 0;
		memory[4][1] = 10;
		memory[5][1] = 135;
		memory[6][1] = 254;
		memory[7][1] = 127;
		memory[8][1] = 18;
		memory[9][1] = 4;
		memory[10][1] = 55;
		memory[11][1] = 8;
		memory[12][1] = 2;
		memory[13][1] = 98;
		memory[14][1] = 13;
		memory[15][1] = 5;
		memory[16][1] = 233;
		memory[17][1] = 158;
		memory[18][1] = 167;
	}
	
	
	@Override
	public String toString(){   //debugging method to see contents of register
		String ret = "name: " + regName + " value: " + regVal + " type: " + regType + " regFU: " + regFU;
		return ret;
	}

	@Override
	public int compareTo(RegistersInit register) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if(this.regName.substring(0,1).compareTo(register.regName.substring(0,1)) < 0 ) {
			return BEFORE;
		}
		else if (this.regName.substring(0,1).compareTo(register.regName.substring(0,1)) > 0) {
			return AFTER;
		}
		else {
			if(Integer.parseInt(this.regName.substring(1)) < Integer.parseInt(register.regName.substring(1)) ) {
				return BEFORE;
			}
			else if (Integer.parseInt(this.regName.substring(1)) > Integer.parseInt(register.regName.substring(1))) {
				return AFTER;
			}
			else {
				return EQUAL;
			}
		}
	}

}
