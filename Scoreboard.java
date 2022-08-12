/**
 * @author Auguste Kiendrebeogo based on @author Matthew Kelly work
 * This class is in charge of all the scoreboarding operations. 
 * The GUI is also created here.
 */

import java.util.*;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

public class Scoreboard implements ActionListener, Cloneable {

	FUDriver [] functUnits;   //the functional units available to the system
	int [][] memoryValues; // Values in memory
	RegistersInit init = new RegistersInit(); // To get values in memory
	InstructionsReader [] instructions; //the instructions in the text file stored as an array
	ArrayList<RegistersInit> registers = new ArrayList<RegistersInit>();
	ArrayList<Integer> jReads = new ArrayList<Integer>();
	ArrayList<Integer> kReads = new ArrayList<Integer>();
	static final int INSTRUCTIONS_VISIBLE_AT_A_TIME = 10; //determines when the UI should scroll, this value is in the specs
	static final String LABEL_TABLE1 = "Instruction status:";     //labels for the GUI
	static final String LABEL_TABLE2 = "Functional unit status:";
	static final String LABEL_TABLE3 = "Register result status:";
	static final String LABEL_TABLE4 = "Values in memory:";
	static final String LABEL_TABLE5 = "Operations calculations results:";
	// For number of each FU available
	int numIntegerFUs = (SimulatorDriver.count1 + SimulatorDriver.count2 + SimulatorDriver.count3),
			numFPAddSubFUs = SimulatorDriver.count4, numFPMultFUs = SimulatorDriver.count5, 
			numFPDivFUs = SimulatorDriver.count6; 
	// Operations variables
	int intVar1 = 0, intVar2 = 0, integerResult = 0;
	double FPVar1 = 0, FPVar2 = 0, FPResult = 0;

	JFrame frame;  //the UI element on which the other elements will be attached
	int clock;     //the current clock cycle of the system


	public Scoreboard() throws Exception {
		throw new Exception("A scoreboard must be implemented with Instruction[] parameters");
	}

	public Scoreboard(InstructionsReader [] instr){
		//SimulatorDriver sd = null;
		clock = 0; //initialize the clock
		functUnits = new FUDriver[SimulatorDriver.totalCount]; //setup the function unit array for proceeding fu types
		functUnits = SimulatorDriver.functUnits;  // The functional units available to the system
		instructions = instr; //associate the instructions passed in with the instance
		memoryValues = new int[19][2];
		memoryValues = init.memory;

		determineRegistersUsedInInstructions();
	}

	public void display(){
		frame = new JFrame();    
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTable table1 = this.displayInstructionsStatus(this.instructions);//instruction statuses
		JTable table2 = this.displayFunctionalUnitStatus(); //functional unit statuses
		JTable table3 = this.displayRegisterResultStatus(); //register result status
		JTable table4 = this.displayMemoryValues();
		JTable table5 = this.displayOperationsResults();


		//multi-line headers, allows Java Swing to display multiple lines in a header
		MultiLineHeader renderer = new MultiLineHeader();
		Enumeration enume = table1.getColumnModel().getColumns();
		while (enume.hasMoreElements()) {
			((TableColumn)enume.nextElement()).setHeaderRenderer(renderer);
		}
		Enumeration enume2 = table2.getColumnModel().getColumns();
		while (enume2.hasMoreElements()) {            
			((TableColumn)enume2.nextElement()).setHeaderRenderer(renderer);
		}
		// *************

		Button incClock = new Button("> Increment Clock");  //init UI element to advance clock
		incClock.addActionListener(this);                   //attach event to this UI element
		Container c = this.frame.getContentPane();          //get UI pane to which UI elements will be added
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));    //allow everything to be automatically aligned

		// ADD TABLE1 to UI
		c.add(new Label(Scoreboard.LABEL_TABLE1));            //top instructions table
		c.add(table1.getTableHeader());

		JScrollPane scrollableInstructions = new JScrollPane(table1);

		//show 7 instructions at a time. The header is 2.5 rows, 16px is height of a row
		scrollableInstructions.setPreferredSize(new Dimension(scrollableInstructions.getPreferredSize().width,(int)(16.0*9.5)));

		//Assure that the most recently modified instruction is visible
		int scrollIncrementsNeeded=0;   //increment this with each instruction that is not visible
		for(int instruction=Scoreboard.INSTRUCTIONS_VISIBLE_AT_A_TIME; instruction<this.instructions.length; instruction++){
			if(this.instructions[instruction-1].issue > 0){ //count the number of instructions already issued to use as a
				scrollIncrementsNeeded++;                   // basis for how far down to scroll
			}
		}

		//dynamically show the instruction with the most recent activity
		JViewport jvp = scrollableInstructions.getViewport();   //get window contents
		Point p = jvp.getViewPosition();                        //get current display position of scrolling pane
		p.setLocation(p.x, p.y+table1.getRowHeight()*scrollIncrementsNeeded);   //revise the vertical position of the scrolling pane point element
		jvp.setViewPosition(p);                                 //apply the modified view point to the window contents
		scrollableInstructions.setViewport(jvp);                //set the modified viewport to the scrollable pane
		//End of visibility assurance code

		c.add(scrollableInstructions);//add the instructions table with modified viewport coords to the view

		// ADD TABLE2 to UI
		c.add(new Label(Scoreboard.LABEL_TABLE2)); 
		c.add(new Label(Scoreboard.LABEL_TABLE2));  //for some reason this isn't showing up unless added twice
		c.add(table2.getTableHeader());             //add the table's label as a component
		c.add(table2);                              //add table's contents to GUI

		// ADD TABLE3 to UI
		c.add(new Label(Scoreboard.LABEL_TABLE3));
		c.add(table3.getTableHeader()); //add the table's header as a component
		c.add(table3);                  //add table's contents to GUI
		c.add(incClock);                //add button to GUI

		// ADD TABLE4 to UI
		c.add(new Label(Scoreboard.LABEL_TABLE4));            // Memory values table
		c.add(table4.getTableHeader());
		c.add(table4);                              //add table's contents to GUI

		// ADD TABLE5 to UI
		c.add(new Label(Scoreboard.LABEL_TABLE5));            // Operations values table
		c.add(table5.getTableHeader());
		c.add(table5);                              //add table's contents to GUI
		this.frame.pack();              //condense everything for alignment

		this.frame.setVisible(true);
	}

	public void hide(){
		this.frame.setVisible(false); //hides the GUI from the user
	}

	public void show(){
		this.frame.setVisible(true); //shows the GUI to the user
	}

	// Display Instructions status
	public JTable displayInstructionsStatus(InstructionsReader [] instr){
		String [] columnHeaders = {"Instructions","Issue","Read\nOper","Exec\nComp","Write\nResult"};

		Object [][] data = new Object[instr.length][8];

		//set the values in the GUI for each instruction's stage
		for(int ii=0; ii<instr.length; ii++){
			data[ii][0] = instr[ii].instrCommand;
			if(instr[ii].issue == 0) {
				data[ii][1] = "";
			} else{
				data[ii][1] = instr[ii].issue;
			}
			if(instr[ii].readOper == 0) {
				data[ii][2] = "";
			} else{
				data[ii][2] = instr[ii].readOper;
			}
			if(instr[ii].exec == 0) {
				data[ii][3] = "";
			} else{
				data[ii][3] = instr[ii].exec;
			}
			if(instr[ii].writeBack == 0){
				data[ii][4] = "";
			}else{
				data[ii][4] = instr[ii].writeBack;
			}
		}
		JTable table = new JTable(data,columnHeaders);
		return table;
	}

	// Display FUs
	public JTable displayFunctionalUnitStatus(){

		String [] columnHeaders = {"Time","Name","Busy","Op","dest\nFi","S1\nFj",
				"S2\nFk","FU\nQj","FU\nQk","Fj?\nRj","Fk\nRk"
		};

		Object[][]data = new Object[this.functUnits.length][11];
		//for each functional unit
		for(int i = 0; i < this.functUnits.length; i++){
			//fetch arraylist version of function unit's data
			Object [] d = functUnits[i].toArray().toArray();
			for(int di=0; di<d.length; di++){
				data[i][di] = d[di];
			}
		}

		JTable table = new JTable(data,columnHeaders);
		return table;
	}

	// Display registers' while scoreboarding
	public JTable displayRegisterResultStatus(){
		//Dynamically create table headers based on register usage
		String [] columnHeaders = new String[registers.size()+2];
		columnHeaders[0] = "Clock"; 
		columnHeaders[1] = "";
		for(int ii=0, chi=2; chi<columnHeaders.length; chi++, ii++){
			columnHeaders[chi] = registers.get(ii).regName;
		}

		Object[][]data = new Object[1][columnHeaders.length];

		//reset all values in table
		data[0][0] = Integer.toString(this.clock);
		data[0][1] = "FU";  //functional unit title for register result table
		for(int ii=2; ii<columnHeaders.length; ii++){
			data[0][ii] = registers.get(ii-2).regFU; //add the registers' contents to the GUI
		}     

		JTable table = new JTable(data,columnHeaders); //create a SWING table object to pass to the GUI
		return table;
	}

	// Display values in memory before any operation
	public JTable displayMemoryValues(){
		String [] columnHeaders = {"M0","M1","M2","M3","M4","M5","M6","M7","M8","M9","M10",
				"M11","M12","M13","M14","M15","M16","M17","M18"};

		Object[][]data = new Object[1][columnHeaders.length];

		//reset all values in table
		for(int i = 0; i < columnHeaders.length; i++){
			columnHeaders[i] = columnHeaders[i];
		}
		for(int i = 0; i < columnHeaders.length; i++){
			data[0][i] = String.valueOf(init.memory[i][1]); //add the memory's contents to the GUI
		}     
		JTable table = new JTable(data,columnHeaders);
		return table;
	}

	// Display values in registers after all the operations are finished
	public JTable displayOperationsResults(){
		String [] columnHeaders = new String[registers.size()];
		for(int i = 0; i < columnHeaders.length; i++){
			columnHeaders[i] = registers.get(i).regName;
		}

		Object[][]data = new Object[1][columnHeaders.length];

		for(int i = 0; i < columnHeaders.length; i++){
			data[0][i] = String.valueOf(registers.get(i).regVal); //add the register values to the GUI
		}     
		JTable table = new JTable(data,columnHeaders);
		return table;
	}


	public void processInstructions() {  //instruction logic
		//First, clear the Operands freed by exiting instructions, so other instructions can get into a Read Stage
		for(int ji=0; ji<jReads.size(); ji++){
			functUnits[jReads.get(ji)].Rj="Yes";
		}
		jReads.clear();

		for(int ki=0; ki<kReads.size(); ki++){
			functUnits[kReads.get(ki)].Rk="Yes";
		}
		kReads.clear();

		//cycle through the instructions determining at what stage of execution they're in
		for(int i = 0; i < instructions.length; i++) {
			if(instructions[i].issue == 0){ //the instruction has not been issued
				if ( checkIssue(instructions[i]) == true ){ //instruction is able to be issued
					bookKeepIssue(instructions[i]);
					break;  //found an issuable instruction, get out of instr FOR loop
				} else {
					break; //found instruction that has not yet been issued but cannot currently be, escape out of FOR loop
				}
			}else if(instructions[i].readOper == 0){          //the instruction has not been read
				if ( checkRead(instructions[i]) == true ){    //instruction can be read
					bookKeepRead(instructions[i]);
					instructions[i].FUUsed.time = instructions[i].cycleCount+1; //set the delay value of the functional unit based on the instruction that has secured it
				}
			}else if ( (instructions[i].cycleCount > 0) && (instructions[i].exec == 0 ) ){//the instruction either has a latency and/or has not been executed
				// Run an exec cycle, decrement the delay of typeOfFUUsed
				instructions[i].cycleCount--; //decrement instruction's execution counter
				instructions[i].FUUsed.time--; //decrement the functional unit's counter to correspond to the instruction
			}else if ((instructions[i].cycleCount == 0) && (instructions[i].exec == 0)){//instruction has no latency remaining and has not yet been executed
				// This is the last one => just set the execComp to the time in bookKeepExec
				bookKeepExec(instructions[i]);
				instructions[i].FUUsed.time--; //decrement
			}else if (instructions[i].writeBack == 0){ //instruction has not yet written the result
				if ( checkWrite(instructions[i]) == true) { //check for memory aliasing
					bookKeepWrite(instructions[i]);
					carryOperations(instructions[i]);
				}
			}
		}   //end for loop
	}

	boolean checkIssue(InstructionsReader instr){
		boolean issueable = false;  //can the instruction be issued?
		boolean freefu = false; //is a functional unit of the type needed free?
		boolean wawhaz = false; //boolean for write-after-write hazard
		int possibleFUindex = 0;

		//See if there is a free register
		for (int i = 0; i < functUnits.length ; i++) {
			//determine if a functional unit of the right type is free and is not set to release in this clock cycle
			if ((functUnits[i].type == instr.typeOfFUUsed) && ( functUnits[i].status == false) 
					&& (functUnits[i].releaseTime != this.clock) ) {
				if (instr.typeOfFUUsed == 0 && numIntegerFUs != 0) {
					freefu = true;          //specify that a functional unit of the right type has been found
					possibleFUindex = i;    //specify the index of the functional unit to be used in respect to the array of all FUs
					numIntegerFUs--;
				}
				if (instr.typeOfFUUsed == 1 && numFPAddSubFUs != 0) {
					freefu = true;          //specify that a functional unit of the right type has been found
					possibleFUindex = i;    //specify the index of the functional unit to be used in respect to the array of all FUs
					numFPAddSubFUs--;
				}
				if (instr.typeOfFUUsed == 2 && numFPMultFUs != 0) {
					freefu = true;          //specify that a functional unit of the right type has been found
					possibleFUindex = i;    //specify the index of the functional unit to be used in respect to the array of all FUs
					numFPMultFUs--;
				}
				if (instr.typeOfFUUsed == 3 && numFPDivFUs != 0) {
					freefu = true;          //specify that a functional unit of the right type has been found
					possibleFUindex = i;    //specify the index of the functional unit to be used in respect to the array of all FUs
					numFPDivFUs--;
				}
				break;                  //we found a functional unit, so can break from the FOR loop
			}
		}

		//See if none wants to write to the same place
		for (int j = 0; j < instructions.length; j++) {
			if ( (instructions[j].issue != 0 ) &&                           //if instruction has been issued
					(instructions[j].dest().regName.equals(instr.dest().regName)) && // and destinations are the same
					(instructions[j].writeBack == 0)) {                      // and result has not yet been written
				wawhaz = true;                                          //there's a write after write hazard
			}
		}
		// Restore the # of FUs to it precedent number if WAW Hazard
		if (wawhaz == true) {
			if (instr.typeOfFUUsed == 0) {
				numIntegerFUs++;
			}
			if (instr.typeOfFUUsed == 1) {
				numFPAddSubFUs++;
			}
			if (instr.typeOfFUUsed == 2) {
				numFPMultFUs++;
			}
			if (instr.typeOfFUUsed == 3) {
				numFPDivFUs++;
			}
		}

		issueable = (freefu && !wawhaz); //determine whether the instruction should be issued

		if(issueable){
			instr.FUUsed = functUnits[possibleFUindex]; //secure a functional unit
		}

		return issueable;
	}

	boolean checkRead(InstructionsReader instr){
		boolean readable = true;    //default to the reigsters being ready. Check below and set if not ready

		if (!instr.sourcej().regName.equals("")) { //assure that the source register's name is not blank
			if (instr.FUUsed.Rj.equals("No")) {
				readable = false;           //instruction cannot procede, as registers are not ready
			}
		}

		if (!instr.sourcek().regName.equals("")) { //assure that the second source register's name is not blank
			if (instr.FUUsed.Rk.equals("No")) {
				readable = false;       //instruction cannot procede, as registers are not ready
			}
		}

		return readable;
	}

	boolean checkWrite(InstructionsReader instr){//checks for memory aliasing
		boolean writable = true;
		for (int i = 0; i < instructions.length; i++) {
			// only check issued before the current
			if  ( (instructions[i].issue > 0 ) && (instructions[i].issue < instr.issue) 
					&& ( (instructions[i].readOper == 0) ||(instructions[i].readOper == this.clock ) )) {
				if (instructions[i].sourcej().regName.equals(instr.dest().regName)){writable = false;}
				if (instructions[i].sourcek().regName.equals(instr.dest().regName)){writable = false;}
				//TODO - check for those that had direct addressing of a STORE -> no name for the register
			}
		}
		// Restore the FU for future use
		if (writable == true) {
			if (instr.typeOfFUUsed == 0) {
				numIntegerFUs++;
			}
			if (instr.typeOfFUUsed == 1) {
				numFPAddSubFUs++;
			}
			if (instr.typeOfFUUsed == 2) {
				numFPMultFUs++;
			}
			if (instr.typeOfFUUsed == 3) {
				numFPDivFUs++;
			}
		}
		return writable;
	}

	void bookKeepIssue(InstructionsReader instr){
		instr.issue = this.clock; //set the instruction's issue value to the current clock
		instr.FUUsed.status = true; //assign the functional unit to prevent other instructions from using
		instr.FUUsed.Op = new String(instr.opcodeStr); //tell the FU the operand name
		instr.FUUsed.Fi = new String(instr.dest().regName); //tell the operand the first input register name
		instr.FUUsed.Fj = new String(instr.sourcej().regName);//tell the operand the second input register name
		instr.FUUsed.Fk = new String(instr.sourcek().regName);//tell the operand the third input register name

		if (!instr.sourcej().regName.equals("")) {
			instr.FUUsed.Qj = findRegFU(instr.sourcej().regName);
			// if no unit is responsible for this operand, then set ready
			if (instr.FUUsed.Qj.equals("") ) {
				instr.FUUsed.Rj = "Yes";
			} else {
				instr.FUUsed.Rj = "No";
			}
		} else {
			instr.FUUsed.Qj = "";
			instr.FUUsed.Rj = "";
		}

		if (!instr.sourcek().regName.equals("")) {
			instr.FUUsed.Qk = findRegFU(instr.sourcek().regName);
			// if no unit is responsible for this operand, then set ready
			if (instr.FUUsed.Qk.equals("") ) {
				instr.FUUsed.Rk = "Yes";
			} else {
				instr.FUUsed.Rk = "No";
			}
		} else {
			instr.FUUsed.Qk = "";
			instr.FUUsed.Rk = "";
		}

		setRegFU(instr.dest().regName, instr.FUUsed.FUName);
	}

	void bookKeepRead(InstructionsReader instr){
		instr.readOper = this.clock; //set the instruction's read value to the current clock value
		if (!instr.sourcej().regName.equals("")) {
			instr.FUUsed.Rj="No";
		}
		if (!instr.sourcek().regName.equals("")) {
			instr.FUUsed.Rk="No";
		}
		instr.FUUsed.Qj="";
		instr.FUUsed.Qk="";
	}

	void bookKeepExec(InstructionsReader instr){		
		instr.exec = this.clock; //set the instruction's exec value to the current clock value
	}

	void bookKeepWrite(InstructionsReader instr){
		instr.writeBack = this.clock; //set the instruction's write value to the current clock value

		for(int fu=0; fu< functUnits.length; fu++ ) { //poll through all of the functional units
			if (functUnits[fu].Qj.equals(instr.FUUsed.FUName)) {
				jReads.add(fu);
			}
			if (functUnits[fu].Qk.equals(instr.FUUsed.FUName)) {
				kReads.add(fu);
			}
		}
		instr.FUUsed.releaseTime = this.clock;
		instr.FUUsed.resetValues();
		clearRegFU(instr.dest().regName);
	}

	void clearRegFU(String regName) {
		Iterator<RegistersInit > itr = registers.iterator(); //create iterator to allow polling through registers

		while (itr.hasNext()) {  //as long as there are more registers,
			RegistersInit tReg = itr.next();    //get the next register
			if (tReg.regName.equals(regName)) { //if the functional unit is equivalent to the fu's name,
				tReg.regFU="";        // then clear the register's functional unit value
			}
		}
	}

	String findRegFU(String regName) {
		String regFUResult = "";
		Iterator<RegistersInit> itr = registers.iterator(); //create a means to poll through registers

		while (itr.hasNext()) {
			RegistersInit tReg = itr.next();
			if (tReg.regName.equals(regName)) {
				regFUResult = tReg.regFU;
			}
		}

		return regFUResult;
	}

	void setRegFU(String regName, String fuName) {
		Iterator<RegistersInit > itr = registers.iterator();

		while (itr.hasNext()) {
			RegistersInit  tReg = itr.next();
			if (tReg.regName.equals(regName)) {
				tReg.regFU = fuName;
			}
		}
	}

	public void incrementClock() {
		this.clock++;        // increment clock       
		this.processInstructions();//call to logic, perform any actions appropriate for active instructions

		this.hide(); //destroy/hide the current GUI
		this.display();	  //show the current state of the scoreboard
	}

	@Override
	public String toString(){
		String ret = "";             
		for(int fs = 0; fs < this.functUnits.length; fs++){
			ret += functUnits[fs] + "\r\n";
		}    
		return ret;
	}

	@Override
	public void actionPerformed(ActionEvent e) { //performed when UI button is pused
		if(!checkDone()) {
			this.incrementClock();
		}
	}


	// Find the registers that were used in the instructions
	public void determineRegistersUsedInInstructions() {
		//scans through all of the instructions and adds each register opcode to scoreboard's register array
		for(int i = 0; i < instructions.length; i++){
			if (!instructions[i].dest().regName.equals("")) {
				RegistersInit tempReg = new RegistersInit(instructions[i].dest().regName);
				if (registers.size() == 0) {
					registers.add(tempReg);
				} 
				else {
					boolean in = false;
					Iterator<RegistersInit> itr = registers.iterator();

					while (itr.hasNext()) {
						RegistersInit tReg = itr.next();
						if (tReg.regName.equals(tempReg.regName)) {
							in = true;
						}
					}

					if (!in) {registers.add(tempReg);}
				}    //end else
			}//end if
		}   //end for
		Collections.sort(registers);
	}

	// Check if all instructions have been proceeded
	public boolean checkDone () {
		//scans through all of the instructions and reads the writeResult attribute to determine whether
		// all of the instruction are completed
		int totalDone = 0;      //number of instructions completed, increments each time on is found
		boolean done = true;    //set to false if all instructions have not completed

		for (int instruction=0; instruction < instructions.length; instruction++) {//scan through all instructions
			if ( instructions[instruction].writeBack != 0 ) {
				totalDone++;
			}       //increment if the instruction is done
		}
		if (totalDone != instructions.length) {
			done = false;
		}   //check to see if all instructions are done, set flag if so

		return done;
	}

	// Operations are carried out in this method
	public void carryOperations(InstructionsReader inst) {
		// Load a floating point value
		if (inst.opcode.equals("L.D") || inst.opcode.equals("l.d") 
				|| inst.opcode.equals("L.W") || inst.opcode.equals("l.w")) {
			if (inst.r1.regVal == 0) {
				inst.r1.regVal = init.memory[inst.regexVal1][1];
			}
			integerResult = (int) inst.r1.regVal;
			giveOperationIntegerResultToCorrespondingRegister(inst);
		}
		// Store a Floating-Point value 
		if (inst.opcode.equals("S.D") || inst.opcode.equals("s.d") 
				|| inst.opcode.equals("S.W") || inst.opcode.equals("s.w")) {
			if (inst.r1.regVal == 0) {
				inst.r1.regVal = init.memory[inst.regexVal1][1];
			}
			integerResult = (int) inst.r1.regVal;
			giveOperationIntegerResultToCorrespondingRegister(inst);
		}
		// Load a 64 bit Integer Immediate
		if (inst.opcode.equals("L.I") || inst.opcode.equals("l.i")) {
			if (inst.r1.regVal == 0) {
				inst.r1.regVal = init.memory[inst.regexVal2][1];
			}
			integerResult = (int) inst.r1.regVal;
			giveOperationIntegerResultToCorrespondingRegister(inst);
		}
		//  Integer Add
		if (inst.opcode.equals("ADD") || inst.opcode.equals("add")) {
			inst.r2.regVal = var1(inst);
			inst.r3.regVal = var2(inst);
			integerResult = (int) (inst.r2.regVal + inst.r3.regVal);
			giveOperationIntegerResultToCorrespondingRegister(inst);
		}
		//  Integer Subtract
		if (inst.opcode.equals("SUB") || inst.opcode.equals("sub")) {
			inst.r2.regVal = var1(inst);
			inst.r3.regVal = var2(inst);
			integerResult = (int) (inst.r2.regVal - inst.r3.regVal);
			giveOperationIntegerResultToCorrespondingRegister(inst);
		}
		// Integer Add with Immediate
		if (inst.opcode.equals("ADDI") || inst.opcode.equals("addi")) {
			inst.r2.regVal = var1(inst);
			inst.r3.regVal = var2(inst);
			integerResult = (int) (inst.r2.regVal + inst.r3.regVal);
			giveOperationIntegerResultToCorrespondingRegister(inst);
		}
		// Floating Point Add
		if (inst.opcode.equals("ADD.D") || inst.opcode.equals("add.d")) {
			inst.r2.regVal = var1(inst);
			inst.r3.regVal = var2(inst);
			FPResult = inst.r2.regVal + inst.r3.regVal;
			giveOperationFPResultToCorrespondingRegister(inst);
		}
		// Floating Point Subtract
		if (inst.opcode.equals("SUB.D") || inst.opcode.equals("sub.d")) {
			inst.r2.regVal = var1(inst);
			inst.r3.regVal = var2(inst);
			FPResult = inst.r2.regVal - inst.r3.regVal;
			giveOperationFPResultToCorrespondingRegister(inst);
		}
		// Floating-Point Multiplication
		if (inst.opcode.equals("MUL.D") || inst.opcode.equals("mul.d")) {
			inst.r2.regVal = var1(inst);
			inst.r3.regVal = var2(inst);
			FPResult = inst.r2.regVal * inst.r3.regVal;
			giveOperationFPResultToCorrespondingRegister(inst);
		}
		// Floating-Point Division
		if (inst.opcode.equals("DIV.D") || inst.opcode.equals("div.d")) {
			inst.r2.regVal = var1(inst);
			inst.r3.regVal = var2(inst);
			FPResult = inst.r2.regVal / inst.r3.regVal;
			giveOperationFPResultToCorrespondingRegister(inst);
		}	
	}

	// Give the integer operation result to the corresponding register
	void giveOperationIntegerResultToCorrespondingRegister(InstructionsReader inst) {
		for (RegistersInit s: registers) {
			if (inst.r1.regName.equals(s.regName)) {
				s.regVal = integerResult; // Give value of load integer to corresponding register
			}
		}
	}

	// Give the floating-point operation result to the corresponding register
	void giveOperationFPResultToCorrespondingRegister(InstructionsReader inst) {
		for (RegistersInit s: registers) {
			if (inst.r1.regName.equals(s.regName)) {
				s.regVal = (int)FPResult; // Give value of load integer to corresponding register
			}
		}
	}

	// Get R2 floating point value
	double var1(InstructionsReader inst) {
		for (RegistersInit s: registers) {
			if (inst.r2.regName.equals(s.regName)) {
				inst.r2.regVal = s.regVal; // Give value of load integer to corresponding register
			}
		}
		return inst.r2.regVal;
	}

	// Get R3 floating point value
	double var2(InstructionsReader inst) {
		for (RegistersInit s: registers) {
			if (inst.r3.regName.equals(s.regName)) {
				inst.r3.regVal = s.regVal; // Give value of load integer to corresponding register
			}
		}
		return inst.r3.regVal;
	}
}
