package org.pdg.sitallocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Group {

	private final List<Passenger> passengerList;
	private final int groupId;

	public int groupSize() {
		return passengerList.size();
	}

	public List<Passenger> getSortedPassengerList() {
		List<Passenger> clonedList = new ArrayList<Passenger>(passengerList.size());
		clonedList.addAll(passengerList);
		clonedList.sort(Comparator.comparing(Passenger::isW));
		return clonedList;
	}

	public int nWindowPreferences() {
		int npreferences = 0;
		for (Passenger passenger : passengerList) {
			if (passenger.isW())
				npreferences++;
		}
		return npreferences;
	}

	public Group(List<Passenger> passengerList, int id) {
		this.passengerList = passengerList;
		this.groupId = id;
	}

	public int getGroupId() {
		return groupId;
	}

	public int isThereWindowPreferencesInt() {
		return (nWindowPreferences() > 0) ? 1 : 0;
	}
	
	public boolean isThereWindowPreferences() {
		return (nWindowPreferences() > 0) ? true : false;
	}

}
