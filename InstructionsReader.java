/**
 * @author Auguste Kiendrebeogo based on @author Matthew Kelly work
 * This class reads the MIPS instructions using parsing and decodes the 
 * different messages for the diverse operations.
 */

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;

public class InstructionsReader {

	// Variables
	String instrCommand;
	String opcode, opcodeStr;
	String [] splitInstructions = null, splitStr = null, splitOperands = null, splitOps = null;
	RegistersInit r1, r2, r3;
	int issue, readOper, exec, writeBack, cycleCount, typeOfFUUsed, regexVal1, regexVal2;
	FUDriver FUUsed;

	// Find pattern from Regex expression
	Pattern p1 = Pattern.compile("^(.*)([0-9]+)([(])([0-9]+)([)])");
	Pattern p2 = Pattern.compile("^(.*)([0-9]+)");

	// Default constructor
	public InstructionsReader() {

	}

	// Constructor with string parameter
	public InstructionsReader(String str) throws Exception{
		// Initialize all stages to 0
		issue = readOper = exec = writeBack = 0;
		// For regex expression matching
		Matcher m1 = null, m2 = null;

		// Initialize registers from the read file
		r1 = new RegistersInit();
		r2 = new RegistersInit();
		r3 = new RegistersInit();

		// Split instructions strings
		splitStr = str.split(";"); // Split the instructions from potential comments
		this.instrCommand = splitStr[0]; // Store the instructions in this variable
		splitOperands = splitStr[0].split("[\\s]+", 2); // Split opcode of the instruction from the rest
		splitOps = splitOperands[1].split(",",3); // Get all parts of the instructions

		// Give the value corresponding to the index in the parentheses its value for memory
		if (splitOps[1].contains("(") && splitOps[1].contains(")")) {
			m1 = p1.matcher(splitOps[1]);
			if (m1.find()) {
				regexVal1 = Integer.parseInt(m1.group(4));
				r1.regVal = r1.memory[regexVal1][1];
			}
			else {
				throw new IllegalStateException("No match for Offset(addr) operation");
			}
		}
	
		// Give the value corresponding to immediate 64 its value to memory
		if (splitOps[1].matches("[0-9]+")) {
			m2 = p2.matcher(splitOps[1]);
			if (m2.find()) {
				regexVal2 = Integer.parseInt(m1.group(2));
				r1.regVal = r1.memory[regexVal2][1];
			}
			else {
				throw new IllegalStateException("No match for immediate IMM64 operation");
			}
		}
		
		opcode = splitOperands[0]; // Store opcode of the instruction
		r1.regName = splitOps[0]; // Store name of the register from the instruction

		// Remove extra-spaces from splitting the instructions
		for(int i = splitOps.length-1; i >= 0; i--){
			splitOps[i] = splitOps[i].trim();
		}

		//
		if(splitOps.length > 2){
			r2.regName = splitOps[1]; // For immediate

			// Look for an offset '#' character
			boolean foundPoundSign = false;
			for(int i = 0; i < splitOps[2].length(); i++) {
				if (splitOps[2].charAt(i) == '#'){
					foundPoundSign = true;
					break;
				}
			}
			if(foundPoundSign){
				// Get rid of the hash symbol if found and get the integer value
				r3.regVal += Integer.parseInt(splitOps[2].replaceFirst("#",""));
			}
			else {
				r3.regName = splitOps[2];
				//if (splitOps[2].matches("[0-9]+")) {
				//	r3.regVal = splitOps[2].
				//}
			}
		}
		else {// less than two operands
			boolean foundPar = false; // we found a parenthesis?
			boolean foundPound = false; // we found a pound/hash symbol?
			for (int i = 0; i < splitOps[1].length(); i++) {
				if (splitOps[1].charAt(i) == '('){
					foundPar = true;
				}
				if (splitOps[1].charAt(i) == '#'){
					foundPound = true;
				}
			}
			// handle Offset
			if (foundPar) {
				String [] offsetVal = splitOps[1].split("\\("); //delimit offset value and parenthesis
				int displ = Integer.parseInt(offsetVal[0]); //get the numerical value of the offset
				r2.regVal += displ;
				r2.regName = offsetVal[1].replaceFirst("\\)",""); //get rid of closing paren
			} 
			else if ( foundPound) { //extract the value from an immediate
				r2.regVal += Integer.parseInt(splitOps[1].replaceFirst("#","")); //get rid of pound sign
			} 
			else{
				r2.regName = splitOps[1];
			}
		}

		// Set register types
		if( opcode.equals("S.D") || opcode.equals("SD") || opcode.equals("s.d") || opcode.equals("sd")) {
			r1.regType = 0; // source
			r2.regType = 1; // source
			r3.regType = 1; // source
		} else {
			r1.regType = 0; // destination
			r2.regType = 1; // source
			r3.regType = 1; // source
		}

		//associate register values
		if (splitStr.length > 1) {
			splitInstructions = splitStr[1].split(" ");
			if (splitInstructions.length != 0) {
				for (int i =0; i< splitInstructions.length; i++) {
					// TODO - what if it does not match

					//remove extra comma that is appended if multiple init values are set
					splitInstructions[i] = splitInstructions[i].replace(",","");

					String [] regNameValueSplit = splitInstructions[i].split("=");
					if (regNameValueSplit.length > 1) {
						if (r1.regName.equals(regNameValueSplit[0])) {
							r1.regVal += Integer.parseInt(regNameValueSplit[1],10);
						} 
						else if (r2.regName.equals(regNameValueSplit[0])) {
							r2.regVal += Integer.parseInt(regNameValueSplit[1],10);
						} 
						else {	
							r3.regVal += Integer.parseInt(regNameValueSplit[1],10);
						}
					}
				}
			}
		}

		//associate type of functional unit used w/ instruction
		if(opcode.equals("L.D") || opcode.equals("LD") || opcode.equals("l.d") || opcode.equals("ld")
				|| opcode.equals("S.D") || opcode.equals("SD") || opcode.equals("s.d") || opcode.equals("sd")
				|| opcode.equals("SW")  || opcode.equals("LI") || opcode.equals("sw")  || opcode.equals("li") 
				|| opcode.equals("LW") || opcode.equals("lw") || opcode.equals("ADD") || opcode.equals("ADDI") || opcode.equals("add") 
				|| opcode.equals("addi") || opcode.equals("SUB") || opcode.equals("sub")){
			typeOfFUUsed = 0;
			cycleCount = 0; // 1 cycles starting at 0
		}
		else if(opcode.equals("SUB.D") || opcode.equals("ADD.D") || opcode.equals("sub.d") || opcode.equals("add.d")) {
			typeOfFUUsed = 1;
			cycleCount = 1; // 2 cycles starting at 0
		} 
		else if(opcode.equals("MUL.D") || opcode.equals("MULT.D") || opcode.equals("*MUL.D")
				|| opcode.equals("mul.d") || opcode.equals("mult.d") || opcode.equals("*mul.d")) {
			typeOfFUUsed = 2;
			cycleCount = 9; // 10 cycles starting at 0
		} 
		else if(opcode.equals("DIV.D") || opcode.equals("div.d")){
			typeOfFUUsed = 3;
			cycleCount = 39; // 40 cycles starting at 0
		}
		else {
			throw new Exception("Error trying to figure out function unit for instruction" + splitOps[0]);
		}

		//translate opcodeMneumonics to human readable strings
		if(opcode.equals("L.D") || opcode.equals("LD") || opcode.equals("l.d") || opcode.equals("ld")){
			opcodeStr = "Load";
		}
		else if(opcode.equals("S.D") || opcode.equals("SD") || opcode.equals("s.d") || opcode.equals("sd")){
			opcodeStr = "Store";
		}
		else if(opcode.equals("MUL.D") || opcode.equals("MULT.D") || opcode.equals("*MUL.D")
				|| opcode.equals("mul.d") || opcode.equals("mult.d") || opcode.equals("*mul.d")){
			opcodeStr = "Mult";
		}
		else if(opcode.equals("SUB.D") || opcode.equals("SUB") || opcode.equals("sub.d") || opcode.equals("add.d")){
			opcodeStr = "Sub";
		}
		else if(opcode.equals("DIV.D") || opcode.equals("div.d")){
			opcodeStr = "Div";
		}
		else if(opcode.equals("ADD.D") || opcode.equals("ADD") || opcode.equals("ADDI")
				|| opcode.equals("add.d") || opcode.equals("add") || opcode.equals("addi")){
			opcodeStr = "Add";
		}
		else {
			throw new UnrecognizedOperationException();
		}
	}

	public RegistersInit dest() { //return the appropriate register based on the type of instruction
		if (r1.regType == 0){
			return r1;
		} //first register is destination
		else {
			return r3;
		} //third register contains destination register
	}

	public RegistersInit sourcej() {//return the appropriate register based on the type of instruction
		if (r1.regType == 0){
			return r2;
		} //first source is found in second register
		else {
			return r1;
		} //first source is found in first register
	}

	public RegistersInit sourcek() {//return the appropriate register based on the type of instruction
		if (r1.regType == 0){
			return r3;
		} //second source is found in third register
		else {
			return r2;
		} //second source if found in second register
	}


	@Override
	public String toString(){
		String ret =  instrCommand + " Issue: " + issue + " Read Operands: " + readOper + 
				" Execute: " + exec + " WriteBack: " + writeBack + 
				" Type of FU needed: " + typeOfFUUsed + "\n\t" + r1 + "\n\t" + r2 + "\n\t" + r3; 
		if (FUUsed != null) {
			ret = "FuUsed: " + FUUsed.FUName + ret;
		} 
		else {
			ret = "No FU assigned "  + ret;
		}
		return ret;
	}

}


class UnrecognizedOperationException extends Exception{
	public UnrecognizedOperationException(){
		System.out.println("You tried to execute an assembly operation that this code doesn't know."); 
	}
	public UnrecognizedOperationException(String msg){
		super(msg);
	}

}
