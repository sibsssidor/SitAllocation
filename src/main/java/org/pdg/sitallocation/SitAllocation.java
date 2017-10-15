package org.pdg.sitallocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

public class SitAllocation {

	private static final int ADD_VARS = 3;
	static final Logger LOG = LoggerFactory.getLogger(SitAllocation.class);
	LinearProgramSolver solver = SolverFactory.getSolver("CPLEX");

	public static void main(String[] args) {
		SitAllocation sitAllocation = new SitAllocation();
		try {
			InputData inputData = (new InputData()).parseInput(args[0]);
			sitAllocation.setGroupList(inputData.getGroupList()).setSitRows(inputData.getSitRows());
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return;
		}

		sitAllocation.solveProblem();

	}

	double[] solveProblem() {
		Solution solution = solveGroupAssignment();
		double[] results = solution.getVariables();

		Map<Integer, List<Group>> rowToGroups = mapSolutionToAssignment(results);
		int allocatedPassengers = rowToGroups.values().stream()
				.flatMapToInt(groupList -> groupList.stream().mapToInt(group -> group.groupSize())).sum();

		assignSits(rowToGroups);

		int[] unallocatedAssign = assignUnallocatedSits(rowToGroups);

		double totSatisfactionRate = 100 * (solution.getScore() / (allocatedPassengers + unallocatedAssign[0] + unallocatedAssign[1]));
		double aboardSatisfactionRate = 100 * (solution.getScore() / (allocatedPassengers + unallocatedAssign[1]));


		LOG.info("Satisfied passengers:" + solution.getScore() + " passengers left behind : " + unallocatedAssign[0] + " assigned:" + allocatedPassengers);
		LOG.info("Total Satisfaction rate :" + totSatisfactionRate);
		LOG.info("Aboard Satisfaction rate :" + aboardSatisfactionRate);

		int nvars = groupList.size() + ADD_VARS;
		for (int i = 0; i < results.length / (nvars); i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < (nvars); j++) {
				sb.append(results[i * (nvars) + j] + " ");
			}
			LOG.debug("solver results: " + sb.toString());
		}

		for (SitRow sitRow : sitRows) {
			StringBuilder sb = new StringBuilder();
			sb.append("Row:" + sitRow.getId() + " --> ");
			int[] assignment = sitRow.getPassengerAssignment();
			for (int j = 0; j < assignment.length; j++) {
				sb.append(j + "," + assignment[j] + " ");
			}
			LOG.info(sb.toString());
		}
		return new double[]{totSatisfactionRate, aboardSatisfactionRate};
	}

	public List<SitRow> getSitRows() {
		return sitRows;
	}

	public SitAllocation setSitRows(List<SitRow> sitRows) {
		this.sitRows = sitRows;
		return this;
	}

	public SitAllocation setGroupList(List<Group> groupList) {
		this.groupList = groupList;
		return this;
	}

	private List<SitRow> sitRows;
	private List<Group> groupList;

	Map<Integer, List<Group>> mapSolutionToAssignment(double[] solution) {
		Map<Integer, List<Group>> rowToGroups = new HashMap<Integer, List<Group>>();
		int totVars = groupList.size() + ADD_VARS;

		for (int i = 0; i < solution.length / (totVars); i++) {
			List<Group> rowGroupList = rowToGroups.get(i);
			if (rowGroupList == null) {
				rowGroupList = new ArrayList<Group>();
			}
			for (int j = 0; j < groupList.size(); j++) {
				if (solution[i * totVars + j] == 1) {
					rowGroupList.add(groupList.get(j));
				}
			}
			rowToGroups.put(i, rowGroupList);
		}
		return rowToGroups;
	}

	void assignSits(Map<Integer, List<Group>> rowToGroups) {
		for (int rowNumber : rowToGroups.keySet()) {
			List<Group> assignedGroups = rowToGroups.get(rowNumber);
			SitRow sitRow = sitRows.get(rowNumber);
			int rowSize = sitRow.getSize();
			int nGroups = assignedGroups.size();
			LOG.debug("Row number " + rowNumber + " number of groups: " + nGroups);
			// if the group is full row
			if (nGroups == 1 && assignedGroups.iterator().next().groupSize() == sitRow.getSize()) {
				Group group = assignedGroups.get(0);
				LOG.debug("Assigning full row group " + group.getGroupId());
				// select w passengers, assign the first one to the left, the
				// second one to the right
				List<Passenger> passengerList = group.getSortedPassengerList();
				int nPassegners = passengerList.size();
				for (int i = 0; i < nPassegners; i++) {
					/// start from the last (window requests) and assign window
					/// sits (last and first)
					sitRow.assignSit((rowSize - 1 + i) % rowSize, passengerList.get(nPassegners - 1 - i).getId());
				}
			} else {
				// groups with window passengers last
				assignedGroups.sort(Comparator.comparing(Group::isThereWindowPreferences));
				// put the last group at the end of the row
				Group lastGroup = assignedGroups.get(nGroups - 1);
				LOG.debug("Assigning partial row group " + lastGroup.getGroupId());
				List<Passenger> passengerList = lastGroup.getSortedPassengerList();
				int nPassegners = passengerList.size();
				for (int i = 0; i < nPassegners; i++) {
					/// start from the last (window requests) and assign window
					/// sits (wrap around, last and first)
					sitRow.assignSit(rowSize - 1 - i, passengerList.get(nPassegners - 1 - i).getId());
				}
				// move backwards (groups with window sits first)
				int rowOffSet = 0;
				for (int i = nGroups - 2; i >= 0; i--) {
					Group group = assignedGroups.get(i);
					LOG.debug("Assigning partial row group " + group.getGroupId());
					passengerList = group.getSortedPassengerList();
					nPassegners = passengerList.size();
					for (int j = 0; j < nPassegners; j++) {
						/// start from the last (window requests) and assign
						/// starting from left
						sitRow.assignSit(rowOffSet + j, passengerList.get(nPassegners - 1 - j).getId());
					}
					rowOffSet += nPassegners;
				}
			}
		}
	}

	int[] assignUnallocatedSits(Map<Integer, List<Group>> rowToGroups) {

		List<int[]> emptySits = getEmptySits();
		Set<Integer> allocatedGropupIds = rowToGroups.values().stream()
				.flatMap(groupList -> groupList.stream().map(group -> new Integer(group.getGroupId())))
				.collect(Collectors.toSet());

		int[] passengers = this.groupList.stream().mapToInt(group -> group.getGroupId())
				.filter(groupId -> !allocatedGropupIds.contains(groupId)).flatMap(groupId -> groupList.get(groupId)
						.getSortedPassengerList().stream().mapToInt(passenger -> passenger.getId()))
				.toArray();

		int p = 0;
		int s = 0;

		while (p < passengers.length && s < emptySits.size()) {
			int[] emptySit = emptySits.get(s);
			LOG.trace("Assignning unallocated sit on row " + emptySit[0] + " sit " + emptySit[1] + " to "
					+ passengers[p]);
			sitRows.get(emptySit[0]).assignSit(emptySit[1], passengers[p]);
			p++;
			s++;
		}
		return new int[] { passengers.length - s, s };
	}

	/**
	 * List of unassigned sits (passenger 0)
	 * 
	 * @return row number, sit number
	 */
	List<int[]> getEmptySits() {
		List<int[]> emptySits = new ArrayList<int[]>();
		for (int i = 0; i < sitRows.size(); i++) {
			int[] assignement = sitRows.get(i).getPassengerAssignment();
			for (int j = 0; j < assignement.length; j++) {
				if (assignement[j] == 0)
					emptySits.add(new int[] { i, j });
			}
		}
		return emptySits;
	}

	Solution solveGroupAssignment() {
		int[] groupSizes = groupList.stream().mapToInt(group -> group.groupSize()).toArray();
		int[] windowPreferences = groupList.stream().mapToInt(group -> group.nWindowPreferences()).toArray();
		int[] isTherewindowPreferences = groupList.stream().mapToInt(group -> group.isThereWindowPreferencesInt())
				.toArray();
		int[] rowSizes = sitRows.stream().mapToInt(row -> row.getSize()).toArray();
		int[] rowsWindows = sitRows.stream().mapToInt(row -> row.getWindowSits()).toArray();

		int nVariablesRow = groupList.size() + ADD_VARS;
		int constrainSize = sitRows.size() * (nVariablesRow);

		byte[][] groupToRowFit = genMij(groupSizes, rowSizes);
		double[] goalArray = genGoalCoefficients(groupSizes, nVariablesRow, constrainSize, groupToRowFit,
				windowPreferences);

		LinearProgram lp = new LinearProgram(goalArray);
		String cname;
		for (int i = 0; i < sitRows.size(); i++) {

			double[] constrainArray = new double[constrainSize];

			double[] qVariableArray = new double[constrainSize];

			int rowVarIndex = i * (nVariablesRow);
			int assignmentWeigth = 0;

			int addVarOffset = rowVarIndex + groupList.size();

			for (int j = 0; j < groupList.size(); j++) {
				assignmentWeigth = rowVarIndex + j;
				constrainArray[assignmentWeigth] = groupSizes[j];
				// All assignment variables are binary
				lp.setBinary(assignmentWeigth);
				// Constrain for q variables
				qVariableArray[assignmentWeigth] = groupToRowFit[i][j]
						* (windowPreferences[j] - isTherewindowPreferences[j]) + isTherewindowPreferences[j];
			}
			qVariableArray[addVarOffset] = -1;
			qVariableArray[addVarOffset + 1] = 1;
			qVariableArray[addVarOffset + 2] = 0;
			lp.setInteger(addVarOffset);
			lp.setInteger(addVarOffset + 1);

			cname = "windowsits" + i;
			LOG.trace(constrainArrayToString(cname, qVariableArray, rowSizes[i]));
			lp.addConstraint(new LinearEqualsConstraint(qVariableArray, rowsWindows[i], cname));

			cname = "csize" + i;
			LOG.trace(constrainArrayToString(cname, constrainArray, rowSizes[i]));
			lp.addConstraint(new LinearSmallerThanEqualsConstraint(constrainArray, rowSizes[i], cname));

			// positive split variable limits
			constrainArray = new double[constrainSize];
			constrainArray[addVarOffset] = 1;
			// constrainArray[addVarOffset + 2] = -rowsWindows[i];
			constrainArray[addVarOffset + 2] = -(rowSizes[i] - rowsWindows[i]);
			cname = "qplus" + i;
			LOG.trace(constrainArrayToString(cname, constrainArray, 0));
			lp.addConstraint(new LinearSmallerThanEqualsConstraint(constrainArray, 0, cname));

			// negative split variable limits
			constrainArray = new double[constrainSize];
			constrainArray[addVarOffset + 1] = 1;
			constrainArray[addVarOffset + 2] = rowsWindows[i];
			cname = "qminus" + i;
			LOG.trace(constrainArrayToString(cname, constrainArray, rowsWindows[i]));
			lp.addConstraint(new LinearSmallerThanEqualsConstraint(constrainArray, rowsWindows[i], cname));

			// positive split variable >= 0
			constrainArray = new double[constrainSize];
			constrainArray[addVarOffset] = 1;
			cname = "qplusgz" + i;
			LOG.trace(constrainArrayToString(cname, constrainArray, 0));
			lp.addConstraint(new LinearBiggerThanEqualsConstraint(constrainArray, 0, cname));

			// negative split variable >= 0
			constrainArray = new double[constrainSize];
			constrainArray[addVarOffset + 1] = 1;
			cname = "qminusgz" + i;
			LOG.trace(constrainArrayToString(cname, constrainArray, 0));
			lp.addConstraint(new LinearBiggerThanEqualsConstraint(constrainArray, 0, cname));

			// sign variable binary
			lp.setBinary(addVarOffset + 2);
		}

		for (int j = 0; j < groupList.size(); j++) {
			double[] constrainArray = new double[constrainSize];
			for (int i = 0; i < sitRows.size(); i++) {
				constrainArray[i * (nVariablesRow) + j] = 1;
			}
			cname = "uniqueGroupAssignment" + j;
			LOG.trace(constrainArrayToString(cname, constrainArray, 1));
			lp.addConstraint(new LinearSmallerThanEqualsConstraint(constrainArray, 1, cname));
		}

		lp.setMinProblem(true);
		double[] sol = solver.solve(lp);
		double score = 0;
		for (int i = 0; i < goalArray.length; i++) {
			score += sol[i] * goalArray[i];
		}		
		return new Solution(sol, -score);
	}

	private double[] genGoalCoefficients(int[] groupSizes, int nVariablesRow, int constrainSize, byte[][] Mij,
			int[] windowPreferences) {
		double[] goalArray = new double[constrainSize];
		for (int i = 0; i < sitRows.size(); i++) {
			for (int j = 0; j < groupList.size(); j++) {
				goalArray[i * nVariablesRow + j] = (1 - Mij[i][j]) * Math.max(windowPreferences[j] - 1, 0)
						- groupSizes[j];
			}
			// positive part of window sit allocation
			goalArray[i * nVariablesRow + groupList.size()] = 1;
		}
		return goalArray;
	}

	private byte[][] genMij(int[] groupSizes, int[] rowSizes) {
		byte[][] groupToRowFit = new byte[sitRows.size()][groupList.size()];
		for (int i = 0; i < sitRows.size(); i++) {
			for (int j = 0; j < groupList.size(); j++) {
				if (groupSizes[j] == rowSizes[i]) {
					groupToRowFit[i][j] = 1;
				}
			}
		}
		return groupToRowFit;
	}

	private String constrainArrayToString(String prefix, double[] input, double value) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix + " " + value + " --> ");
		for (double x : input) {
			sb.append(x + " : ");
		}
		return sb.toString();
	}

}
