package OSclasses;

import java.util.Stack;

public class Process {
	private String name;
	private int time;
	private String place;
    private ProcessControlBlock pcb;

    public Process(String name,int time,String place,ProcessControlBlock pcb) {
    	this.name=name;
    	this.time=time;
    	this.place=place;
    	this.pcb=pcb;
    }

	public String toStringg() {
    	return "name= "+this.name;
    }

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProcessControlBlock getPcb() {
		return pcb;
	}

	public void setPcb(ProcessControlBlock pcb) {
		this.pcb = pcb;
	}

    public String printpcb(Process tobeprinted) {
    	return "{"+tobeprinted.getPcb().getProcessID()+","+tobeprinted.getPcb().getProcessState()+"}";
    }
    
}