/**
 * @author Auguste Kiendrebeogo based on @author Matthew Kelly work
 * “thePairedElectron” (https://github.com/thePairedElectron/ScoreBoardingViz )
 * 
 * 
 * This is the main class of the project. It reads the file necessary to the program.
 * The user inputs the number of functional units needed for the program.
 * Then, it initializes all the functional units available to the program.
 * 
 * Program requirements:
 * Inputs
Your program should take the following inputs (how it takes them is up to you):
The number of units per functional unit
Text file with a program written using MIPS instructions outlined above
You do not have to account for MIPS instructions not outlined in this document

Outputs
Your program should output the following:
The cycle each instruction completed the different stages (Issue, Read Operand, Execute, Write Back)
Ideally in the same format as done in the class examples.
Final values in the FP and Integer registers.

 */

import java.util.*;
import java.io.*;

public class SimulatorDriver {

	// Source file
	static String file = "data.txt";
	// Variables for functional units
	static int count1 = 0, count2 = 0, count3 = 0, count4 = 0, 
			count5 = 0, count6 = 0, totalCount = 0, minimumCyclesNecessary = 0;
	static IntegerFU [] intAddSubFU = null;
	static IntegerFU [] loadsFU = null, storesFU = null;
	static FPAdderFU [] fpAddSubFU = null;
	static MultiplierFU [] mulFU = null;
	static DividerFU [] divFU = null;
	static FUDriver [] functUnits = null;   //the functional units available to the system

	// Main function
	public static void main(String[] args) throws Exception{

		// Variables
		int fileSize = 0, instrCount = 0; // File size and instructions counter variables
		int addIntFU = 0, loadIntFU = 0, storeIntFU = 0, addFPFU = 0, mulFPFU = 0, divFPFU = 0; // Choice for number of units per FU
		int FPRegCount = 0, IntRegCount = 0;
		String str1, str2; // String variable for reading file lines

		// Let user choose the number of units per functional unit
		try {

			Scanner numFU = new Scanner(System.in);  // Create a Scanner object
			do {
				System.out.println("\t\t\tSCOREBOARD SIMULATOR\n\n");
				System.out.println("Choose the number of units per functional unit\n" 
						+"\nThe number of Integer FU and Floating-Point FU cannot exceed 32 for each\n\n");
				System.out.print("Enter the number of integer adders: ");
				addIntFU = numFU.nextInt();  // Read user input
				System.out.print("\nEnter the number of integer loads: ");
				loadIntFU = numFU.nextInt();  // Read user input
				System.out.print("\nEnter the number of integer stores: ");
				storeIntFU = numFU.nextInt();  // Read user input
				System.out.print("\nEnter the number of floating-point adders: ");
				addFPFU = numFU.nextInt();  // Read user input
				System.out.print("\nEnter the number of floating-point multipliers: ");
				mulFPFU = numFU.nextInt();  // Read user input
				System.out.print("\nEnter the number of floating-point dividers: ");
				divFPFU = numFU.nextInt();  // Read user input

				// Integer and Floating-Point Registers cannot exceed 32 each
				IntRegCount = addIntFU + loadIntFU + storeIntFU;
				FPRegCount = addFPFU + mulFPFU + divFPFU;
				System.out.println("\n\n");
			} while(IntRegCount > 32 || FPRegCount > 32);

			numFU.close();
			System.out.println("\n");

		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("\nPlease insert integer values only!");
		}

		// Create and initialize the different sets of functional units available
		intAddSubFU = new IntegerFU[addIntFU];
		loadsFU = new IntegerFU[loadIntFU];
		storesFU = new IntegerFU[storeIntFU];
		fpAddSubFU = new FPAdderFU[addFPFU];
		mulFU = new MultiplierFU[mulFPFU];
		divFU = new DividerFU[divFPFU];

		for (int i = 0; i < addIntFU; i++) {
			intAddSubFU[i] = new IntegerFU("Integer");
			count1++;
		}
		for (int i = 0; i < loadIntFU; i++) {
			loadsFU[i] = new IntegerFU("Load"+ (i+1));
			count2++;
		}
		for (int i = 0; i < storeIntFU; i++) {
			storesFU[i] = new IntegerFU("Store"+ (i+1));
			count3++;
		}
		for (int i = 0; i < addFPFU; i++) {
			fpAddSubFU[i] = new FPAdderFU("FloatingPoint"+ (i+1));
			count4++;
		}
		for (int i = 0; i < mulFPFU; i++) {
			mulFU[i] = new MultiplierFU("Multiply"+ (i+1));
			count5++;
		}
		for (int i = 0; i < divFPFU; i++) {
			divFU[i] = new DividerFU("Divide"+ (i+1));
			count6++;
		}

		// To determine the minimum number of cycles necessary for running the program
		//minimumCyclesNecessary = (4*count1) + (4*count2) + (4*count3) + (3+(2*count4)) + (3+(2*count5)) + (3+(40*count6));
		// Total number of FUs available
		totalCount = count1 + count2 + count3 + count4 + count5 + count6; 
		functUnits = new FUDriver[totalCount]; //setup the function unit array for proceeding fu types

		// Store the instantiated FUs in the FU array
		int c1 = count1+count2, c2 = count1+count2+count3, c3 = count1+count2+count3+count4,
				c4 = count1+count2+count3+count4+count5;
		for (int i = 0; i < count1; i++) {
			functUnits[i] = intAddSubFU[i];
		}
		for (int i = count1, j = 0; i < c1; i++, j++) {
			functUnits[i] = loadsFU[j];
		}
		for (int i = c1, j = 0; i < c2; i++, j++) {
			functUnits[i] = storesFU[j];
		}
		for (int i = c2, j = 0; i < c3; i++, j++) {
			functUnits[i] = fpAddSubFU[j];
		}
		for (int i = c3, j = 0; i < c4; i++, j++) {
			functUnits[i] = mulFU[j];
		}
		for (int i = c4, j = 0; i < totalCount; i++, j++) {
			functUnits[i] = divFU[j];
		}


		// Open file and read it for file size. Catch potential file exceptions
		try{
			// Open the file
			FileInputStream fstream = new FileInputStream(SimulatorDriver.file);
			DataInputStream input1 = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(input1));

			// Read number of lines in file line by line. Do not count empty lines
			while ((str1 = br.readLine()) != null){
				if (!str1.equals("")) {
					fileSize++;
				}
			}
			// Close stream
			input1.close();

		}
		catch (Exception e){
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

		// Initialize an array of instructions using the size of the file
		InstructionsReader[] instrArray = new InstructionsReader[fileSize];


		// Open file again and read it for instructions count. Catch potential file exceptions
		try{
			FileInputStream fstream = new FileInputStream(SimulatorDriver.file);
			DataInputStream input2 = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(input2));

			// Read number of instructions in file line by line. Do not count empty lines
			while ((str2 = br.readLine()) != null)   {
				if (!str2.equals("")) {
					// Instructions objects created
					instrArray[instrCount] = new InstructionsReader(str2);
					instrCount++;
				}
			}
			// Close stream
			input2.close();

		}
		catch (Exception e){
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
			System.exit(-1);
		}

		// Print instructions from the array of instructions
		for(int i = 0; i < instrArray.length; i++) {
			System.out.println(instrArray[i]);
		}

		// Get the instructions parsed
		Scoreboard s = new Scoreboard(instrArray);
		s.display();    //display the scoreboard obj in a Swing window

	}

}
