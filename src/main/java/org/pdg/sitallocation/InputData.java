package org.pdg.sitallocation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputData {

	private List<SitRow> sitRows;
	private List<Group> groupList;

	InputData parseInput(String path) throws ParsingException, IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(path));) {

			String rows = reader.readLine();
			if (rows.matches("[\\d]+\\s[\\d]+")) {
				String[] rowsSize = rows.trim().split("\\s");
				int rowSize = Integer.parseInt(rowsSize[0]);
				int nrows = Integer.parseInt(rowsSize[1]);
				if (rowSize < 1 || nrows < 1)
					throw new ParsingException("Empty plane/rows? -->" + rows + "<--");
				sitRows = new ArrayList<SitRow>(nrows);
				for (int i = 0; i < nrows; i++) {
					sitRows.add(new SitRow(rowSize, 2, i));
				}
			} else {
				throw new ParsingException("Invalid row format -->" + rows + "<--");
			}
			String groupLine = reader.readLine();
			groupList = new ArrayList<Group>();

			int groupId = 0;
			while (groupLine != null) {
				if (groupLine.matches("[\\d]+[W]*[\\s[\\d]+[W]]*")) {
					String[] passengers = groupLine.trim().split("\\s");
					List<Passenger> passengerList = new ArrayList<Passenger>();
					for (int i = 0; i < passengers.length; i++) {
						String passenger = passengers[i];
						int passsengerId;
						boolean window;
						if (passenger.endsWith("W")) {
							passsengerId = Integer.parseInt(passenger.substring(0, passenger.length() - 1));
							window = true;
						} else {
							passsengerId = Integer.parseInt(passenger);
							window = false;
						}
						if (passsengerId == 0) {
							throw new ParsingException("0 is an invalid passenger Id!");
						}
						passengerList.add(new Passenger(passsengerId, window));
					}
					groupList.add(new Group(passengerList, groupId));
					groupId++;
				} else {
					throw new ParsingException("Invalid group format -->" + groupLine + "<--");
				}
				groupLine = reader.readLine();
			}
		} catch (IOException e) {
			throw new IOException("Something went wrong while accessing the file " + path, e);
		}
		return this;
	}

	public List<SitRow> getSitRows() {
		return sitRows;
	}

	public List<Group> getGroupList() {
		return groupList;
	}

}
