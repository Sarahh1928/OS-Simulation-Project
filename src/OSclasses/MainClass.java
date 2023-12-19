package OSclasses;

import java.io.*;
import java.util.*;

public class MainClass {
	private static int timeSlice;
	private static int currenttime = 0;
	private static int id = 1;
	private static int start = 0;
	private static int Program1start = 0;
	private static int Program2start = 0;
	private static int Program3start = 0;
	private static boolean Program1arr = false;
	private static boolean Program2arr = false;
	private static boolean Program3arr = false;
	private final static Object[] memory = new Object[40];
	private final static Object[] disk = new Object[40];
	private final static Queue<Process> readyQueue = new LinkedList<>();
	private final static Queue<Process> blockedQueue = new LinkedList<>();
	private static Process currentProcess;
//	private int numProcesses = 0;
//	private Disk disk = new Disk();
//  private boolean isAvailable = true;
//	private static Semaphore fileMutex = new Semaphore(1);
//	private static Semaphore userInputMutex = new Semaphore(1);
//	private static Semaphore userOutputMutex = new Semaphore(1);
	private static int fileMutex = 1;
	private static int userInputMutex = 1;
	private static int userOutputMutex = 1;
	private static Process Process1;
	private static Process Process2;
	private static Process Process3;
	private static int[] fileblockedby;
	private static int[] userInputblockedby;
	private static int[] userOutputblockedby;
	private final static Queue<Process> fileprocesses = new LinkedList<>();
	private final static Queue<Process> userInputprocesses = new LinkedList<>();
	private final static Queue<Process> userOutputprocesses = new LinkedList<>();
	private static boolean lastone = false;
	private static Stack Stackp1 = new Stack();
	private static Stack Stackp2 = new Stack();
	private static Stack Stackp3 = new Stack();

	public static void main(String[] args) throws InterruptedException, IOException {
		print("Please enter the time slice:");
		Scanner scanner = new Scanner(System.in);
		timeSlice = scanner.nextInt();
		while (timeSlice == 0) {
			print("Please enter the time slice:");
			scanner = new Scanner(System.in);
			timeSlice = scanner.nextInt();
		}
		print("Please enter the Arrival time for Program_1:");
		scanner = new Scanner(System.in);
		Program1start = scanner.nextInt();
		print("Please enter the Arrival time for Program_2:");
		scanner = new Scanner(System.in);
		Program2start = scanner.nextInt();
		print("Please enter the Arrival time for Program_3:");
		scanner = new Scanner(System.in);
		Program3start = scanner.nextInt();
		interpret("src/resources");
	}

	public static void interpret(String programFile) throws InterruptedException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("src/OSclasses/disk.txt"));
		writer.write("");
		writer.close();
		File folder = new File(programFile);
		if (folder.isDirectory()) {
			for (File file : folder.listFiles()) {
				ProcessControlBlock newpro = new ProcessControlBlock(id, ProcessState.READY, 1, null);
				try (BufferedReader reader = new BufferedReader(new FileReader(programFile + "/" + file.getName()))) {
					String line = "";
					Stack stack = new Stack();
					while ((line = reader.readLine()) != null) {
						String[] parts = line.split(" ");
						for (int i = 0; i < parts.length; i++) {
							stack.push(parts[i]);
						}
					}
					Stack stack2 = new Stack();
					while (!stack.isEmpty()) {
						String part = (String) stack.pop();
						stack2.push(part);
					}
					if (file.getName().equals("Program_1.txt")) {
						Stackp1 = stack2;
					} else if (file.getName().equals("Program_2.txt")) {
						Stackp2 = stack2;
					} else if (file.getName().equals("Program_3.txt")) {
						Stackp3 = stack2;
					}
					currentProcess = new Process(file.getName(), 0, null, newpro);
					if (currentProcess.getPcb().getProcessID() == 1)
						Process1 = currentProcess;
					if (currentProcess.getPcb().getProcessID() == 2)
						Process2 = currentProcess;
					if (currentProcess.getPcb().getProcessID() == 3)
						Process3 = currentProcess;
					id++;
//					for (int i = 0; i < memory.length; i++) {
//						if (memory[i] instanceof Object[]) {
//							System.out.print("{");
//							for (int j = 0; j < ((Object[]) memory[i]).length; j++) {
//								System.out.print(((Object[]) memory[i])[j] + ",");
//							}
//							System.out.print("}");
//						} else {
//							System.out.print(memory[i] + " ");
//						}
//					}
				} catch (FileNotFoundException e) {
					print("Program file not found: " + programFile);
				} catch (IOException e) {
					print("Error reading program file: " + e.getMessage());
				}
			}
		}
		fileblockedby = new int[id];
		userInputblockedby = new int[id];
		userOutputblockedby = new int[id];
		if (Program1start == currenttime) {
			print(Process1.getName() + " arrived");
			readyQueue.add(Process1);
			Program1arr = true;
			if (memory.length - start < 19) {
				putintodisk(Process1);
			} else {
				Process1.setPlace("memory");
				int[] ad = { start, start + 19 };
				memory[start] = Process1.getPcb().getProcessID();
				start++;
				memory[start] = Process1.getPcb().getProcessState();
				start++;
				Process1.getPcb().setProgramCounter(start + 2);
				memory[start] = Process1.getPcb().getProgramCounter();
				start++;
				Process1.getPcb().setMemoryBoundaries(ad);
				start++;
				Stack temmp = new Stack();
				temmp = Stackp1;
				int i = 0;
				for (i = start; !temmp.isEmpty(); i++) {
					String instr = "";
					if (temmp.peek().equals("semWait")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("assign")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						if (temmp.peek().equals("readFile")) {
							instr += temmp.pop() + " ";
							instr += temmp.pop() + " ";
						} else {
							instr += temmp.pop() + " ";
						}
					} else if (temmp.peek().equals("semSignal")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("printFromTo")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("writeFile")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("print")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					}
					memory[i] = instr;
				}
				memory[Process1.getPcb().getMemoryBoundaries()[0] + 3] = Process1.getPcb().getMemoryBoundaries();
				start = Process1.getPcb().getMemoryBoundaries()[1] + 1;
			}
		}
		if (Program2start == currenttime) {
			print(Process2.getName() + " arrived");
			readyQueue.add(Process2);
			Program2arr = true;
			if (memory.length - start < 19) {
				putintodisk(Process2);
			} else {
				Process2.setPlace("memory");
				int[] ad = { start, start + 19 };
				memory[start] = Process2.getPcb().getProcessID();
				start++;
				memory[start] = Process2.getPcb().getProcessState();
				start++;
				Process2.getPcb().setProgramCounter(start + 2);
				memory[start] = Process2.getPcb().getProgramCounter();
				start++;
				Process2.getPcb().setMemoryBoundaries(ad);
				start++;
				Stack temmp = new Stack();
				temmp = Stackp2;
				int i = 0;
				for (i = start; !temmp.isEmpty(); i++) {
					String instr = "";
					if (temmp.peek().equals("semWait")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("assign")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						if (temmp.peek().equals("readFile")) {
							instr += temmp.pop() + " ";
							instr += temmp.pop() + " ";
						} else {
							instr += temmp.pop() + " ";
						}
					} else if (temmp.peek().equals("semSignal")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("printFromTo")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("writeFile")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("print")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					}
					memory[i] = instr;
				}
				memory[Process2.getPcb().getMemoryBoundaries()[0] + 3] = Process2.getPcb().getMemoryBoundaries();
				start = Process2.getPcb().getMemoryBoundaries()[1] + 1;
			}
		}
		if (Program3start == currenttime) {
			print(Process3.getName() + " arrived");
			readyQueue.add(Process3);
			Program3arr = true;
			if (memory.length - start < 19) {
				putintodisk(Process3);
			} else {
				Process3.setPlace("memory");
				int[] ad = { start, start + 19 };
				memory[start] = Process3.getPcb().getProcessID();
				start++;
				memory[start] = Process3.getPcb().getProcessState();
				start++;
				Process3.getPcb().setProgramCounter(start + 2);
				memory[start] = Process3.getPcb().getProgramCounter();
				start++;
				Process3.getPcb().setMemoryBoundaries(ad);
				start++;
				Stack temmp = new Stack();
				temmp = Stackp3;
				int i = 0;
				for (i = start; !temmp.isEmpty(); i++) {
					String instr = "";
					if (temmp.peek().equals("semWait")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("assign")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						if (temmp.peek().equals("readFile")) {
							instr += temmp.pop() + " ";
							instr += temmp.pop() + " ";
						} else {
							instr += temmp.pop() + " ";
						}
					} else if (temmp.peek().equals("semSignal")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("printFromTo")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("writeFile")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("print")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					}
					memory[i] = instr;
				}
				memory[Process3.getPcb().getMemoryBoundaries()[0] + 3] = Process3.getPcb().getMemoryBoundaries();
				start = Process3.getPcb().getMemoryBoundaries()[1] + 1;
			}
		}
		schedule();
	}

	public static void loadprogram() throws IOException {
		if (Program1start == currenttime && !Program1arr) {
			print(Process1.getName() + " arrived");
			readyQueue.add(Process1);
			Program1arr = true;
			if (memory.length - start < 19) {
				putintodisk(Process1);
			} else {
				Process1.setPlace("memory");
				int[] ad = { start, start + 19 };
				Process1.getPcb().setMemoryBoundaries(ad);
				memory[start] = Process1.getPcb().getProcessID();
				start++;
				memory[start] = Process1.getPcb().getProcessState();
				start++;
				Process1.getPcb().setProgramCounter(start + 2);
				memory[start] = Process1.getPcb().getProgramCounter();
				start++;
				start++;
				Stack temmp = new Stack();
				temmp = Stackp1;
				int i = 0;
				for (i = start; !temmp.isEmpty(); i++) {
					String instr = "";
					if (temmp.peek().equals("semWait")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("assign")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						if (temmp.peek().equals("readFile")) {
							instr += temmp.pop() + " ";
							instr += temmp.pop() + " ";
						} else {
							instr += temmp.pop() + " ";
						}
					} else if (temmp.peek().equals("semSignal")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("printFromTo")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("writeFile")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("print")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					}
					memory[i] = instr;
				}
				memory[Process1.getPcb().getMemoryBoundaries()[0] + 3] = Process1.getPcb().getMemoryBoundaries();
				start = Process1.getPcb().getMemoryBoundaries()[1] + 1;
			}
		}
		if (Program2start == currenttime && !Program2arr) {
			print(Process2.getName() + " arrived");
			readyQueue.add(Process2);
			Program2arr = true;
			if (memory.length - start < 19) {
				putintodisk(Process2);
			} else {
				Process2.setPlace("memory");
				int[] ad = { start, start + 19 };
				Process2.getPcb().setMemoryBoundaries(ad);
				memory[start] = Process2.getPcb().getProcessID();
				start++;
				memory[start] = Process2.getPcb().getProcessState();
				start++;
				Process2.getPcb().setProgramCounter(start + 2);
				memory[start] = Process2.getPcb().getProgramCounter();
				start++;
				start++;
				Stack temmp = new Stack();
				temmp = Stackp2;
				int i = 0;
				for (i = start; !temmp.isEmpty(); i++) {
					String instr = "";
					if (temmp.peek().equals("semWait")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("assign")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						if (temmp.peek().equals("readFile")) {
							instr += temmp.pop() + " ";
							instr += temmp.pop() + " ";
						} else {
							instr += temmp.pop() + " ";
						}
					} else if (temmp.peek().equals("semSignal")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("printFromTo")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("writeFile")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("print")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					}
					memory[i] = instr;
				}
				memory[Process2.getPcb().getMemoryBoundaries()[0] + 3] = Process2.getPcb().getMemoryBoundaries();
				start = Process2.getPcb().getMemoryBoundaries()[1] + 1;
			}
		}
		if (Program3start == currenttime && !Program3arr) {
			print(Process3.getName() + " arrived");
			readyQueue.add(Process3);
			Program3arr = true;
			if (memory.length - start < 19) {
				putintodisk(Process3);
			} else {
				Process3.setPlace("memory");
				int[] ad = { start, start + 19 };
				Process3.getPcb().setMemoryBoundaries(ad);
				memory[start] = Process3.getPcb().getProcessID();
				start++;
				memory[start] = Process3.getPcb().getProcessState();
				start++;
				Process3.getPcb().setProgramCounter(start + 2);
				memory[start] = Process3.getPcb().getProgramCounter();
				start++;
				start++;
				Stack temmp = new Stack();
				temmp = Stackp3;
				int i = 0;
				for (i = start; !temmp.isEmpty(); i++) {
					String instr = "";
					if (temmp.peek().equals("semWait")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("assign")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						if (temmp.peek().equals("readFile")) {
							instr += temmp.pop() + " ";
							instr += temmp.pop() + " ";
						} else {
							instr += temmp.pop() + " ";
						}
					} else if (temmp.peek().equals("semSignal")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("printFromTo")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("writeFile")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					} else if (temmp.peek().equals("print")) {
						instr += temmp.pop() + " ";
						instr += temmp.pop() + " ";
					}
					memory[i] = instr;
				}
				memory[Process3.getPcb().getMemoryBoundaries()[0] + 3] = Process3.getPcb().getMemoryBoundaries();
				start = Process3.getPcb().getMemoryBoundaries()[1] + 1;
			}
		}
	}
	
	public static void print(String x) {
		System.out.println(x);
	}

	public static void schedule() throws IOException, InterruptedException {

		while (readyQueue.isEmpty()) {
			currenttime++;
			loadprogram();
		}
		while ((!readyQueue.isEmpty() || !blockedQueue.isEmpty())
				|| Process1.getPcb().getProcessState() != ProcessState.FINISHED
				|| Process2.getPcb().getProcessState() != ProcessState.FINISHED
				|| Process3.getPcb().getProcessState() != ProcessState.FINISHED) {
			if(readyQueue.isEmpty()) {
				if(currenttime<Program1start||currenttime<Program2start||currenttime<Program3start) {
					while(currenttime!=Program1start&&currenttime!=Program2start&&currenttime!=Program3start) {
						print("it does");
						currenttime++;
					}
				}
				loadprogram();
			}
			currentProcess = readyQueue.poll();
			if (currentProcess.getPlace() == "disk") {
				swap(currentProcess);
			}
			Queue<Process> Temp = new LinkedList<>();
			System.out.print("blocked Queue = ");
			System.out.print("[ ");
			while (!blockedQueue.isEmpty()) {
				Process blocked = blockedQueue.poll();
				if (blockedQueue.isEmpty())
					System.out.print(blocked.toStringg());
				else
					System.out.print(blocked.toStringg() + " , ");
				Temp.add(blocked);
			}
			while (!Temp.isEmpty()) {
				blockedQueue.add(Temp.poll());
			}
			print(" ]");
			print(
					"-----------------------------------------------------------------------------------------------");
			System.out.print("ready Queue = ");
			System.out.print("[ ");
			while (!readyQueue.isEmpty()) {
				Process blocked = readyQueue.poll();
				if (readyQueue.isEmpty())
					System.out.print(blocked.toStringg());
				else
					System.out.print(blocked.toStringg() + " , ");
				Temp.add(blocked);
			}
			print(" ]");
			while (!Temp.isEmpty()) {
				readyQueue.add(Temp.poll());
			}
			print(
					"-----------------------------------------------------------------------------------------------");
			System.out.print("Currently executing: ");
			print(currentProcess.toStringg());
			if (Process1.getPcb().getProcessState()==ProcessState.FINISHED
					||Process2.getPcb().getProcessState()==ProcessState.FINISHED
					||Process3.getPcb().getProcessState()==ProcessState.FINISHED) {
				if (currentProcess.getPlace() == "disk") {
					Process temp=currentProcess;
					if(Process1.getPcb().getProcessState()==ProcessState.FINISHED) {
						currentProcess=Process1;
					}
					else if(Process2.getPcb().getProcessState()==ProcessState.FINISHED) {
						currentProcess=Process2;
					}
					else if(Process3.getPcb().getProcessState()==ProcessState.FINISHED) {
						currentProcess=Process3;
					}
					getfromdisk(temp);
					currentProcess=temp;
				}
			}
			translate();
			loadprogram();
			if (memory[currentProcess.getPcb().getProgramCounter()] == null
					|| memory[currentProcess.getPcb().getProgramCounter()] instanceof Object[]) {
				currentProcess.getPcb().setProcessState(ProcessState.FINISHED);
				if (Process1.getPlace() == "disk") {
					getfromdisk(Process1);
				}
				if (Process2.getPlace() == "disk") {
					getfromdisk(Process2);
				}
				if (Process3.getPlace() == "disk") {
					getfromdisk(Process3);
				}
			}
			
			if (currentProcess.getPcb().getProcessState() == ProcessState.READY) {
				readyQueue.add(currentProcess);
				if (currentProcess.getTime() >= timeSlice)
					currentProcess.setTime(0);
			}
		}
	}
	
	public static String readFile(String x) throws IOException {
		String data1="";
		try (BufferedReader newreader = new BufferedReader(
				new FileReader("src/resources/" + x + ".txt"))) {
			String line = "";
			while ((line = newreader.readLine()) != null) {
				data1 = data1 + " " + line;
			}
		} catch (FileNotFoundException e) {
			print("File not found: " + x);
		}
		return data1;
	}

	public static void translate() throws IOException, InterruptedException {
		memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = "Running";
		currentProcess.getPcb().setProcessState(ProcessState.RUNNING);
		print("The Memory");
		for (int i = 0; i < memory.length; i++) {
			if (memory[i] instanceof Object[]) {
				System.out.print("{" + ((Object[]) memory[i])[0] + " , " + ((Object[]) memory[i])[1] + "}");
			} else if (memory[i] instanceof int[]) {
				System.out.print("{" + ((int[]) memory[i])[0] + " , " + ((int[]) memory[i])[1] + "}");
			} else {
				System.out.print(memory[i] + " ");
			}
		}
		print("");
		String currentinst;
		int clearloc;
		while (currentProcess.getTime() < timeSlice) {
			if (memory[currentProcess.getPcb().getProgramCounter()] == null
					|| memory[currentProcess.getPcb().getProgramCounter()] instanceof Object[]) {
				currentProcess.getPcb().setProcessState(ProcessState.FINISHED);
				return;
			}
			clearloc = 0;
			lastone = false;
			for (int i = currentProcess.getPcb().getMemoryBoundaries()[0]
					+ 4; i < currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i++) {
				if (memory[i] != null) {
					clearloc = i;
					break;
				}
			}
//				print("currenttime ="+currenttime);
			loadprogram();
			currentProcess.getPcb().setProcessState(ProcessState.RUNNING);
			memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = currentProcess.getPcb().getProcessState();
			String[] parts = null;
			for (int i = currentProcess.getPcb().getMemoryBoundaries()[0]
					+ 4; i < currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i++) {
				if (memory[currentProcess.getPcb().getProgramCounter()] != null) {
//						print(memory[currentProcess.getPcb().getProgramCounter()]);
					parts = ((String) memory[currentProcess.getPcb().getProgramCounter()]).split(" ");
					break;
				} else {
					currentProcess.getPcb().setProgramCounter(currentProcess.getPcb().getProgramCounter() + 1);
				}
			}
			switch (parts[0]) {
			case "print":
				print("print");
				String variable1 = parts[1];
				int firstloc1 = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
				int value11 = 0;
				int i11 = 1;
				for (firstloc1 = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i11 <= 3
						&& memory[firstloc1] != null; i11++) {
					Object[] varii = (Object[]) memory[firstloc1];
					if (((String) varii[0]).equals(variable1)) {
						print(""+varii[1]+"");
						break;
					} else if (i11 == 3 && memory[firstloc1] == null) {
						print(variable1);
					}
					System.out.println();
					firstloc1++;
				}
				currentProcess.setTime(currentProcess.getTime() + 1);
				currentProcess.getPcb().setProgramCounter(currentProcess.getPcb().getProgramCounter() + 1);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 2] = currentProcess.getPcb()
						.getProgramCounter();
				break;
			case "assign":
				print("assign");
				String variable = parts[1];
				loadprogram();
				if (parts[2].equals("readFile")) {
					int firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
					String from = parts[3];
					String value = "";
					int i1 = 1;
					for (firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i1 <= 3
							&& memory[firstloc] != null && !((Object[]) memory[firstloc])[0].equals(variable); i1++) {
						Object[] varii = (Object[]) memory[firstloc];
						if (((String) varii[0]).equals(from)) {
							value = (String) varii[1];
						} else if (i1 == 3 && memory[firstloc] == null) {
							print("there is no value assigned to variable " + from);
						}
						firstloc++;
					}
					String data1 = readFile(value);
					
//					if (i1 < 3 && memory[firstloc] == null) {
//						Object[] varii = { (String) variable, data1 };
//						memory[firstloc] = varii;
//					}
					memory[clearloc] = "assign " + variable + " {" + data1 + "}";
					currentProcess.setTime(currentProcess.getTime() + 1);
					break;
				}
				String input = parts[2];
				if (input.equals("input")) {
					print("Please enter a value:");
					String value = input();
					try {
						int intValue = Integer.parseInt(value);
						int i1 = 1;
						int firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
						for (firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i1 < 3
								&& memory[firstloc] != null
								&& !((Object[]) memory[firstloc])[0].equals(variable); i1++) {
							firstloc++;
						}
//						if (i1 < 3) {
//							Object[] varii = { variable, intValue };
//							memory[firstloc] = varii;
//						}
						memory[clearloc] = "assign " + variable + " " + intValue;
					} catch (NumberFormatException e) {
						int i1 = 1;
						int firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
						for (firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i1 < 3
								&& memory[firstloc] != null
								&& !((Object[]) memory[firstloc])[0].equals(variable); i1++) {
							firstloc++;
						}
//						if (i1 < 3) {
//							Object[] varii = { variable, value };
//							memory[firstloc] = varii;
//						}
						memory[clearloc] = "assign " + variable + " " + value;
					}
				} else {
					try {
						int intValue = Integer.parseInt(input);
						int i1 = 1;
						int firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
						for (firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i1 < 3
								&& memory[firstloc] != null
								&& !((Object[]) memory[firstloc])[0].equals(variable); i1++) {
							firstloc++;
						}
						if (i1 < 3) {
							Object[] varii = { variable, intValue };
							memory[firstloc] = varii;
						}
					} catch (NumberFormatException e) {
						if (parts[2].contains("{")) {
							input = "";
							for (int m = 2; m < parts.length; m++) {
								input += parts[m] + " ";

							}
						}
						int i1 = 1;
						int firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
						for (firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i1 < 3
								&& memory[firstloc] != null
								&& !((Object[]) memory[firstloc])[0].equals(variable); i1++) {
							firstloc++;
						}
						if (i1 < 3) {
							Object[] varii = { variable, input };
							memory[firstloc] = varii;
						}
					}
					memory[clearloc] = null;
					currentProcess.getPcb().setProgramCounter(currentProcess.getPcb().getProgramCounter() + 1);
				}
				currentProcess.setTime(currentProcess.getTime() + 1);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 2] = currentProcess.getPcb()
						.getProgramCounter();
				break;
			case "writeFile":
				print("writeFile");
				String fileName = parts[1];
				String data = parts[2];
				int firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
				String value = "";
				int i1 = 1;
				for (firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i1 <= 3
						&& memory[firstloc] != null; i1++) {
					Object[] varii = (Object[]) memory[firstloc];
					if (((String) varii[0]).equals(fileName)) {
						value = (String) varii[1];
					} else if (i1 == 3 && memory[firstloc] == null) {
						print("there is no value assigned to variable " + fileName);
					}
					firstloc++;
				}
				if (i1 < 3 && memory[firstloc] == null) {
					Object[] varii = { (String) fileName, value };
					memory[firstloc] = varii;
				}
				firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
				String value1 = " ";
				i1 = 1;
				for (firstloc = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i1 <= 3
						&& memory[firstloc] != null; i1++) {
					Object[] varii = (Object[]) memory[firstloc];
					if (((String) varii[0]).equals(data)) {
						value1 = (String) varii[1];
					} else if (i1 == 3 && memory[firstloc] == null) {
						print("there is no value assigned to variable " + data);
					}
					firstloc++;
				}
				if (i1 < 3 && memory[firstloc] == null) {
					Object[] varii = { (String) data, value };
					memory[firstloc] = varii;
				}
				try {
					FileWriter myWriter = new FileWriter("src/resources/" + value + ".txt");
				} catch (IOException e) {
					e.printStackTrace();
				}
				writeDataToFile(value, value1);
				currentProcess.setTime(currentProcess.getTime() + 1);
				currentProcess.getPcb().setProgramCounter(currentProcess.getPcb().getProgramCounter() + 1);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 2] = currentProcess.getPcb()
						.getProgramCounter();
				break;
			case "printFromTo":
				print("printFromTo");
				int firstloc11 = currentProcess.getPcb().getMemoryBoundaries()[1] - 2;
				String from = parts[1];
				int value111 = 0;
				int i111 = 1;
				for (firstloc11 = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i111 <= 3
						&& memory[firstloc11] != null; i111++) {
					Object[] varii = (Object[]) memory[firstloc11];
					if (((String) varii[0]).equals(from)) {
						value111 = (int) varii[1];
						break;
					} else if (i111 == 3 && memory[firstloc11] == null) {
						print("there is no value assigned to variable " + from);
					}
					firstloc11++;
				}
				String to = parts[2];
				int value21 = 0;
				i111 = 1;
				for (firstloc11 = currentProcess.getPcb().getMemoryBoundaries()[1] - 2; i111 <= 3
						&& memory[firstloc11] != null; i111++) {
					Object[] varii = (Object[]) memory[firstloc11];
					if (varii[0].equals(to)) {
						value21 = (int) varii[1];
						break;
					} else if (i111 == 3 && memory[firstloc11] == null) {
						print("there is no value assigned to variable " + to);
					}
					firstloc11++;
				}

				if (value111 <= value21) {
					for (int j = value111; j < value21; j++) {
						System.out.print(j + " , ");
					}
					print(""+value21);
				} else {
					for (int j = value21; j < value111; j++) {
						System.out.print(j + " , ");
					}
					print(""+value111);
				}
				currentProcess.setTime(currentProcess.getTime() + 1);
				currentProcess.getPcb().setProgramCounter(currentProcess.getPcb().getProgramCounter() + 1);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 2] = currentProcess.getPcb()
						.getProgramCounter();
				break;
			case "semWait":
				print("wait");
				semWait(parts[1], currentProcess.getPcb().getProcessID());
				currentProcess.setTime(currentProcess.getTime() + 1);
				currentProcess.getPcb().setProgramCounter(currentProcess.getPcb().getProgramCounter() + 1);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 2] = currentProcess.getPcb()
						.getProgramCounter();
				if (currentProcess.getPcb().getProcessState() == ProcessState.BLOCKED)
					currentProcess.setTime(0);
				break;
			case "semSignal":
				print("signal");
				String signal = parts[1];
				print(signal);
				semSignal(signal);
				currentProcess.setTime(currentProcess.getTime() + 1);
				currentProcess.getPcb().setProgramCounter(currentProcess.getPcb().getProgramCounter() + 1);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 2] = currentProcess.getPcb()
						.getProgramCounter();
				break;
			default:
				print("Invalid command: " + parts[0]);
				break;
			}
			print(
					"-----------------------------------------------------------------------------------------------");
			currenttime++;
			if (!parts[0].equals("assign"))
				memory[clearloc] = null;
			if (currentProcess.getPcb().getProcessState() == ProcessState.BLOCKED)
				return;
		}
		currentProcess.getPcb().setProcessState(ProcessState.READY);
		memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = currentProcess.getPcb().getProcessState();
	}

	public static void putintodisk(Process hello) throws IOException {
		hello.getPcb().setMemoryBoundaries(null);
		hello.setPlace("disk");
		;
		File file = new File("src/OSclasses/disk.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;
		int emptyLineIndex = -1;

		for (int i = 0; (line = reader.readLine()) != null; i++) {
			if (line.trim().isEmpty()) {
				emptyLineIndex = i;
				break;
			}
		}

		reader.close();

		FileWriter writer = new FileWriter(file, true);
		BufferedWriter bufferedWriter = new BufferedWriter(writer);

		if (emptyLineIndex != -1) {
			for (int i = 0; i < emptyLineIndex; i++) {
				bufferedWriter.newLine();
			}
		}
		bufferedWriter.write("-------------------------");
		bufferedWriter.newLine();
		bufferedWriter.write(hello.getName());
		bufferedWriter.newLine();
		bufferedWriter.write(hello.printpcb(hello));
		bufferedWriter.newLine();
		String name = hello.getName();
		if (name.equals("Program_1.txt")) {
			Stack tobe = Stackp1;
			Stack helloidk = new Stack();
			String stackk = "";
			while (!tobe.isEmpty()) {
				helloidk.push(tobe.peek());
				stackk += " " + tobe.pop();
			}
			bufferedWriter.write(stackk);
		} else if (name.equals("Program_2.txt")) {
			Stack tobe = Stackp2;
			Stack helloidk = new Stack();
			String stackk = "";
			while (!tobe.isEmpty()) {
				helloidk.push(tobe.peek());
				stackk += " " + tobe.pop();
			}
			bufferedWriter.write(stackk);
		} else if (name.equals("Program_3.txt")) {
			Stack tobe = Stackp3;
			Stack helloidk = new Stack();
			String stackk = "";
			while (!tobe.isEmpty()) {
				helloidk.push(tobe.peek());
				stackk += " " + tobe.pop();
			}
			bufferedWriter.write(stackk);
		}
		bufferedWriter.newLine();
		bufferedWriter.write("-------------------------");
		bufferedWriter.close();
		print("Process " + hello.getPcb().getProcessID() + " was added to disk");
	}
	
	public static String input() {
		Scanner scanner = new Scanner(System.in);
		String value = scanner.nextLine();
		return value;
	}

	public static void swap(Process tobeswapped) throws FileNotFoundException {
		if (tobeswapped.getPlace().equals("disk")) {
			Queue<String> diskstring = new LinkedList<String>();
			String swith = "";
			Object[] vari1 = null;
			Object[] vari2 = null;
			Object[] vari3 = null;
			Object[] vari11 = null;
			Object[] vari12 = null;
			Object[] vari13 = null;
			try (BufferedReader reader = new BufferedReader(new FileReader("src/OSclasses/disk.txt"))) {
				String line = "";
				while ((line = reader.readLine()) != null) {
					if (!line.contains(tobeswapped.getName())) {
						diskstring.add(line);
					} else {
						line = reader.readLine();
						line = reader.readLine();
						String s = line;
						String variable = "";
						String value = "";
						boolean found = false;
						while (!((line = reader.readLine()).contains("-"))) {
							for (int i = 0; i < line.length(); i++) {
								if (line.charAt(i) == '=')
									found = true;
								else if (!found)
									variable += line.charAt(i);
								else
									value += line.charAt(i);
							}
							if (vari11 == null) {
								vari11 = new Object[2];
								vari11[0] = variable;
								vari11[1] = value;
							} else if (vari12 == null) {
								vari12 = new Object[2];
								vari12[0] = variable;
								vari12[1] = value;
							} else if (vari13 == null) {
								vari13 = new Object[2];
								vari13[0] = variable;
								vari13[1] = value;
							}
						}
						boolean found1 = false;
						Process swappedwith = null;
						if (!blockedQueue.isEmpty() || !readyQueue.isEmpty()) {
							swappedwith = blockedQueue.peek();
							if (!blockedQueue.isEmpty()) {
								while (!blockedQueue.isEmpty()) {
									if (!blockedQueue.peek().getPlace().equals("disk")) {
										tobeswapped.getPcb()
												.setProgramCounter(swappedwith.getPcb().getMemoryBoundaries()[0] + 4);
										int[] swappedwithbound = swappedwith.getPcb().getMemoryBoundaries();
										for (int i = swappedwith.getPcb().getMemoryBoundaries()[0]
												+ 4; i < swappedwith.getPcb().getMemoryBoundaries()[1] - 2; i++) {
											if (memory[i] != null)
												swith += memory[i] + " ";
										}
//										print("boundddd 1 " + tobeswapped.getPcb().getMemoryBoundaries());
										tobeswapped.getPcb().setMemoryBoundaries(swappedwithbound);
//										print("boundddd 1 " + tobeswapped.getPcb().getMemoryBoundaries());
										tobeswapped.setPlace("memory");
										memory[swappedwithbound[0]] = tobeswapped.getPcb().getProcessID();
										memory[swappedwithbound[0] + 1] = tobeswapped.getPcb().getProcessState();
										memory[swappedwithbound[0] + 2] = tobeswapped.getPcb().getProgramCounter();
										memory[swappedwithbound[0] + 3] = tobeswapped.getPcb().getMemoryBoundaries();
										int j = swappedwithbound[0] + 4;
										for (int k = j; k < swappedwithbound[1] - 2; k++) {
											memory[k] = null;
										}
										String[] parts = s.split(" ");
										for (int i = 0; i < parts.length; i++) {
											String instr = "";
											if (parts[i].equals("semWait")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												i = i + 1;
											} else if (parts[i].equals("assign")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												if (parts[i + 2].equals("readFile")) {
													instr += parts[i + 2] + " ";
													instr += parts[i + 3] + " ";
													i = i + 3;
												} else if (parts[i + 2].contains("{")) {
													for (int m = i + 2; m < parts.length; m++) {
														instr += parts[m] + " ";
														if (parts[m].contains("}")) {
															instr += parts[m] + " ";
															break;
														}
													}
												} else {
													instr += parts[i + 2] + " ";
													i = i + 2;
												}
											} else if (parts[i].equals("semSignal")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												i++;
											} else if (parts[i].equals("printFromTo")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												instr += parts[i + 2] + " ";
												i = i + 2;
											} else if (parts[i].equals("writeFile")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												instr += parts[i + 2] + " ";
												i = i + 2;
											} else if (parts[i].equals("print")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												i++;
											}
											if (instr != null && !instr.equals(" ") && !instr.equals("")) {
												memory[j] = instr;
												j++;
											}
										}
//										for (int m = 0; m < memory.length; m++)
//											print(m + " " + memory[m]);
										vari1 = (Object[]) memory[tobeswapped.getPcb().getMemoryBoundaries()[1] - 2];
										memory[swappedwithbound[1] - 2] = vari11;
										vari2 = (Object[]) memory[tobeswapped.getPcb().getMemoryBoundaries()[1] - 1];
										memory[swappedwithbound[1] - 1] = vari12;
										vari3 = (Object[]) memory[tobeswapped.getPcb().getMemoryBoundaries()[1]];
										memory[swappedwithbound[1]] = vari13;
										swappedwith.setPlace("disk");
										swappedwith.getPcb().setMemoryBoundaries(null);
										break;
									} else {
										Queue<Process> temp = new LinkedList<Process>();
										while (!blockedQueue.isEmpty()) {
											if (!found1 && blockedQueue.peek().getPlace().equals("memory")) {
												swappedwith = blockedQueue.peek();
												found1 = true;
											}
											temp.add(blockedQueue.poll());
										}
										while (!temp.isEmpty())
											blockedQueue.add(temp.poll());
										if (!found1) {
											break;
										}
									}
								}
							} else {
								boolean found11 = false;
								while (!readyQueue.isEmpty() && !found1 && !found11) {
									swappedwith = readyQueue.peek();
									if (readyQueue.peek().getPlace().equals("memory")) {
										tobeswapped.getPcb()
												.setProgramCounter(swappedwith.getPcb().getMemoryBoundaries()[0] + 4);
										int[] swappedwithbound = swappedwith.getPcb().getMemoryBoundaries();
										for (int i = swappedwith.getPcb().getMemoryBoundaries()[0]
												+ 4; i < swappedwith.getPcb().getMemoryBoundaries()[1] - 2; i++) {
											if (memory[i] != null)
												swith += memory[i] + " ";
										}
										tobeswapped.getPcb().setMemoryBoundaries(swappedwithbound);
										tobeswapped.setPlace("memory");
										int a = swappedwithbound[0];
										memory[swappedwithbound[0]] = tobeswapped.getPcb().getProcessID();
										memory[swappedwithbound[0] + 1] = tobeswapped.getPcb().getProcessState();
										memory[swappedwithbound[0] + 2] = tobeswapped.getPcb().getProgramCounter();
										memory[swappedwithbound[0] + 3] = tobeswapped.getPcb().getMemoryBoundaries();
										int j = swappedwithbound[0] + 4;
										for (int k = j; k < swappedwithbound[1] - 2; k++) {
											memory[k] = null;
										}
										String[] parts = s.split(" ");
										for (int i = 0; i < parts.length; i++) {
											String instr = "";
											if (parts[i].equals("semWait")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												i = i + 1;
											} else if (parts[i].equals("assign")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												if (parts[i + 2].equals("readFile")) {
													instr += parts[i + 2] + " ";
													instr += parts[i + 3] + " ";
													i = i + 3;
												} else if (parts[i + 2].contains("{")) {
													for (int m = i + 2; m < parts.length; m++) {
														instr += parts[m] + " ";
														if (parts[m].contains("}")) {
															instr += parts[m] + " ";
															break;
														}
													}
												} else {
													instr += parts[i + 2] + " ";
													i = i + 2;
												}
											} else if (parts[i].equals("semSignal")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												i++;
											} else if (parts[i].equals("printFromTo")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												instr += parts[i + 2] + " ";
												i = i + 2;
											} else if (parts[i].equals("writeFile")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												instr += parts[i + 2] + " ";
												i = i + 2;
											} else if (parts[i].equals("print")) {
												instr += parts[i] + " ";
												instr += parts[i + 1] + " ";
												i++;
											}
											if (instr != null && !instr.equals(" ") && !instr.equals("")) {
												memory[j] = instr;
												j++;
											}
										}
//										for (int m = 0; m < memory.length; m++)
//											print(m + " " + memory[m]);
										vari1 = (Object[]) memory[tobeswapped.getPcb().getMemoryBoundaries()[1] - 2];
										memory[swappedwithbound[1] - 2] = vari11;
										vari2 = (Object[]) memory[tobeswapped.getPcb().getMemoryBoundaries()[1] - 1];
										memory[swappedwithbound[1] - 1] = vari12;
										vari3 = (Object[]) memory[tobeswapped.getPcb().getMemoryBoundaries()[1]];
										memory[swappedwithbound[1]] = vari13;
										swappedwith.setPlace("disk");
										swappedwith.getPcb().setMemoryBoundaries(null);
										break;
									} else {
										Queue<Process> temp = new LinkedList<Process>();
										while (!readyQueue.isEmpty()) {
											if (!found11 && readyQueue.peek().getPlace().equals("memory")) {
												swappedwith = readyQueue.peek();
												found11 = true;
											}
											temp.add(readyQueue.poll());
										}
										while (!temp.isEmpty())
											readyQueue.add(temp.poll());
									}
								}
							}
							print("Process " + swappedwith.getPcb().getProcessID() + " and Process "
									+ tobeswapped.getPcb().getProcessID() + " were swapped together");
						}
						diskstring.add(swappedwith.getName());
						String pcbpr = swappedwith.printpcb(swappedwith);
						diskstring.add(pcbpr);
						diskstring.add(swith);
						if (vari1 != null) {
							diskstring.add(vari1[0] + "=" + vari1[1]);
						}
						if (vari2 != null) {
							diskstring.add(vari2[0] + "=" + vari2[1]);
						}
						if (vari3 != null) {
							diskstring.add(vari3[0] + "=" + vari3[1]);
						}
					}
					diskstring.add(line);
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter("src/OSclasses/disk.txt"));
				writer.write("");
				while (!diskstring.isEmpty()) {
					String idk = diskstring.poll();
					writer.write(idk);
					writer.newLine();
				}
				writer.close();
			} catch (IOException e) {
				print("Program file not found: disk");
			}
		}
	}

	public static void getfromdisk(Process getitt) {
		Queue<String> diskstring = new LinkedList<String>();
		Object[] vari11 = null;
		Object[] vari12 = null;
		Object[] vari13 = null;
		try (BufferedReader reader = new BufferedReader(new FileReader("src/OSclasses/disk.txt"))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (!line.contains(getitt.getName())) {
					diskstring.add(line);
				} else {
					line = reader.readLine();
					line = reader.readLine();
					String s = line;
					String variable = "";
					String value = "";
					boolean found = false;
					while (!((line = reader.readLine()).contains("-"))) {
						for (int i = 0; i < line.length(); i++) {
							if (line.charAt(i) == '=')
								found = true;
							else if (!found)
								variable += line.charAt(i);
							else
								value += line.charAt(i);
						}
						if (vari11 == null) {
							vari11 = new Object[2];
							vari11[0] = variable;
							vari11[1] = value;
						} else if (vari12 == null) {
							vari12 = new Object[2];
							vari12[0] = variable;
							vari12[1] = value;
						} else if (vari13 == null) {
							vari13 = new Object[2];
							vari13[0] = variable;
							vari13[1] = value;
						}
					}
					int[] swappedwithbound = currentProcess.getPcb().getMemoryBoundaries();
					getitt.getPcb().setProgramCounter(currentProcess.getPcb().getMemoryBoundaries()[0] + 4);
					getitt.getPcb().setMemoryBoundaries(swappedwithbound);
					getitt.setPlace("memory");
					memory[swappedwithbound[0]] = getitt.getPcb().getProcessID();
					memory[swappedwithbound[0] + 1] = getitt.getPcb().getProcessState();
					memory[swappedwithbound[0] + 2] = getitt.getPcb().getProgramCounter();
					memory[swappedwithbound[0] + 3] = getitt.getPcb().getMemoryBoundaries();
					int j = swappedwithbound[0] + 4;
					String[] parts = s.split(" ");
					for (int i = 0; i < parts.length; i++) {
						String instr = "";
						if (parts[i].equals("semWait")) {
							instr += parts[i] + " ";
							instr += parts[i + 1] + " ";
							i = i + 1;
						} else if (parts[i].equals("assign")) {
							instr += parts[i] + " ";
							instr += parts[i + 1] + " ";
							if (parts[i + 2].equals("readFile")) {
								instr += parts[i + 2] + " ";
								instr += parts[i + 3] + " ";
								i = i + 3;
							} else if (parts[i + 2].contains("{")) {
								for (int m = i + 2; m < parts.length; m++) {
									instr += parts[m] + " ";
									if (parts[m].contains("}")) {
										instr += parts[m] + " ";
										break;
									}
								}
							} else {
								instr += parts[i + 2] + " ";
								i = i + 2;
							}
						} else if (parts[i].equals("semSignal")) {
							instr += parts[i] + " ";
							instr += parts[i + 1] + " ";
							i++;
						} else if (parts[i].equals("printFromTo")) {
							instr += parts[i] + " ";
							instr += parts[i + 1] + " ";
							instr += parts[i + 2] + " ";
							i = i + 2;
						} else if (parts[i].equals("writeFile")) {
							instr += parts[i] + " ";
							instr += parts[i + 1] + " ";
							instr += parts[i + 2] + " ";
							i = i + 2;
						} else if (parts[i].equals("print")) {
							instr += parts[i] + " ";
							instr += parts[i + 1] + " ";
							i++;
						}
						if (instr != null && !instr.equals(" ") && !instr.equals("")) {
							memory[j] = instr;
							j++;
						}
					}
					memory[swappedwithbound[1] - 2] = vari11;
					memory[swappedwithbound[1] - 1] = vari12;
					memory[swappedwithbound[1]] = vari13;
				}
				diskstring.add(line);
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter("src/OSclasses/disk.txt"));
			writer.write("");
			while (!diskstring.isEmpty()) {
				String idk = diskstring.poll();
				writer.write(idk);
				writer.newLine();
			}
			writer.close();
			print("Process " + getitt.getPcb().getProcessID() + " was added to memory");
		} catch (IOException e) {
			print("Program file not found: disk");
		}

	}

	public static void writeDataToFile(String name, String data) throws IOException {
		String[] dataLine = data.split(" "); // split the data string into an array of words
//		print("///////////////////////////////////in writer/////////////////////////////");
		FileWriter file = null;
		try {
			file = new FileWriter("src/resources/" + name + ".txt");
			for (int i = 0; i < dataLine.length; i++) {
				String word = dataLine[i];
				if (word.equals("input")) {
					file.write(word + "\n");
				} else if (word.equals("print") && i + 1 < dataLine.length) {
					file.write(word + " " + dataLine[i + 1] + "\n");
					i++;
				} else if (word.equals("readFile") && i + 1 < dataLine.length) {
					file.write(word + " " + dataLine[i + 1] + "\n");
					i++;
				} else if (word.equals("writeFile") || word.equals("add")) {
					if (i + 2 < dataLine.length) {
						file.write(word + " " + dataLine[i + 1] + " " + dataLine[i + 2] + "\n");
						i += 2;
					}
				} else if (word.equals("userInput")) {
					file.write(word + "\n");
				} else if (word.equals("userOutput")) {
					file.write(word + "\n");
				} else if (word.equals("file")) {
					file.write(word + "\n");
				} else {
					file.write(word + " ");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (file != null) {
				file.close();
			}
		}
	}

	public synchronized static void semWait(String resourceName, int programFile) throws InterruptedException {
		switch (resourceName) {
		case "file":
			fileMutex--;
			fileblockedby[currentProcess.getPcb().getProcessID() - 1] = currentProcess.getPcb().getProcessID();
			if (fileMutex < 0 || !fileprocesses.isEmpty()) {
				currentProcess.getPcb().setProcessState(ProcessState.BLOCKED);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = currentProcess.getPcb()
						.getProcessState();
				blockedQueue.add(currentProcess);
				if (blockedQueue.peek() == null)
					blockedQueue.poll();
				fileprocesses.add(currentProcess);
			} else {
				fileblockedby[currentProcess.getPcb().getProcessID() - 1] = currentProcess.getPcb().getProcessID();
			}
			break;
		case "userInput":
			userInputMutex--;
			userInputblockedby[currentProcess.getPcb().getProcessID() - 1] = currentProcess.getPcb().getProcessID();
			if (userInputMutex < 0 || !userInputprocesses.isEmpty()) {
				currentProcess.getPcb().setProcessState(ProcessState.BLOCKED);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = currentProcess.getPcb()
						.getProcessState();
				blockedQueue.add(currentProcess);
				if (blockedQueue.peek() == null)
					blockedQueue.poll();
				userInputprocesses.add(currentProcess);
			} else {
				userInputblockedby[currentProcess.getPcb().getProcessID() - 1] = currentProcess.getPcb().getProcessID();
			}
			break;
		case "userOutput":
			userOutputMutex--;
			userOutputblockedby[currentProcess.getPcb().getProcessID() - 1] = currentProcess.getPcb().getProcessID();
			if (userOutputMutex < 0 || !userOutputprocesses.isEmpty()) {
				currentProcess.getPcb().setProcessState(ProcessState.BLOCKED);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = currentProcess.getPcb()
						.getProcessState();
				blockedQueue.add(currentProcess);
				if (blockedQueue.peek() == null)
					blockedQueue.poll();
				userOutputprocesses.add(currentProcess);
			} else {
				userOutputblockedby[currentProcess.getPcb().getProcessID() - 1] = currentProcess.getPcb()
						.getProcessID();
			}
			break;
		default:
			print("Invalid resource name: " + resourceName);
		}
	}

	public synchronized static void semSignal(String resourceName) {
		switch (resourceName) {
		case "file":
			fileMutex++;
			boolean filewaitedby = false;
			if (!fileprocesses.isEmpty()) {
				while (blockedQueue.peek().getName() != fileprocesses.peek().getName()) {
					fileprocesses.add(fileprocesses.poll());
				}
				for (int i = 0; i < fileblockedby.length; i++) {
					if (fileblockedby[i] == currentProcess.getPcb().getProcessID()) {
						filewaitedby = true;
						break;
					}
				}
				if (!filewaitedby)
					break;
				Process Temp = blockedQueue.poll();
				Temp.getPcb().setProcessState(ProcessState.READY);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = Temp.getPcb().getProcessState();
				fileprocesses.poll();
				readyQueue.add(Temp);
				if (Temp.getTime() == timeSlice)
					Temp.setTime(0);
				break;
			}

			break;
		case "userInput":
			userInputMutex++;
			boolean userInputwaitedby = false;
			if (!userInputprocesses.isEmpty()) {
				while (blockedQueue.peek().getName() != userInputprocesses.peek().getName()) {
					userInputprocesses.add(userInputprocesses.poll());
				}
				for (int i = 0; i < userInputblockedby.length; i++) {
					if (userInputblockedby[i] == currentProcess.getPcb().getProcessID()) {
						userInputwaitedby = true;
						break;
					}
				}
				if (!userInputwaitedby)
					break;
				Process Temp1 = blockedQueue.poll();
				Temp1.getPcb().setProcessState(ProcessState.READY);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = Temp1.getPcb().getProcessState();
				userInputprocesses.poll();
				readyQueue.add(Temp1);
				if (Temp1.getTime() == timeSlice)
					Temp1.setTime(0);
				break;
			}

			break;
		case "userOutput":
			userOutputMutex++;
			boolean userOutputwaitedby = false;
			if (!userOutputprocesses.isEmpty()) {
				while (blockedQueue.peek().getName() != userOutputprocesses.peek().getName()) {
					userInputprocesses.add(userOutputprocesses.poll());
				}
				for (int i = 0; i < userOutputblockedby.length; i++) {
					if (userOutputblockedby[i] == currentProcess.getPcb().getProcessID()) {
						userOutputwaitedby = true;
						break;
					}
				}
				if (!userOutputwaitedby)
					break;
				Process Temp = blockedQueue.poll();
				Temp.getPcb().setProcessState(ProcessState.READY);
				memory[currentProcess.getPcb().getMemoryBoundaries()[0] + 1] = Temp.getPcb().getProcessState();
				userOutputprocesses.poll();
				readyQueue.add(Temp);
				if (Temp.getTime() == timeSlice)
					Temp.setTime(0);
				break;
			}

			break;
		default:
			print("Invalid resource name: " + resourceName);
		}
	}
}