package org.pdg.sitallocation;

public class SitRow {
	
	private final int size;
	private final int windowSits;
	private final int[] passengerAssignment;
	private final int id;
	
	public int[] getPassengerAssignment() {
		return passengerAssignment;
	}

	public int getSize() {
		return size;
	}

	public int getWindowSits() {
		return windowSits;
	}

	public SitRow(int size, int windowSits, int id) {
		super();
		this.size = size;
		this.windowSits = windowSits;
		this.passengerAssignment = new int[size];
		this.id = id;
	}
	
	public void assignSit(int position, int passenger)
	{
		passengerAssignment[position] = passenger;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	

}
