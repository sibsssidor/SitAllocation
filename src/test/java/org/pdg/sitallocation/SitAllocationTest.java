package org.pdg.sitallocation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class SitAllocationTest {

	
	@Test
	public void testSolveRealFligthAssignment() throws ParsingException, IOException {
		SitAllocation sitAllocation = new SitAllocation();
		InputData inputData = (new InputData()).parseInput("src/test/resources/realplane.txt");
		sitAllocation.setGroupList(inputData.getGroupList()).setSitRows(inputData.getSitRows());
		double[] results = sitAllocation.solveProblem();
		assertEquals(97.89, results[0], 0.01);
		assertEquals(100, results[1], 0.0001);
	}
	
	@Ignore
	@Test
	public void testSolveTestFligthAssignment() throws ParsingException, IOException {
		SitAllocation sitAllocation = new SitAllocation();
		InputData inputData = (new InputData()).parseInput("src/test/resources/salloc.txt");
		sitAllocation.setGroupList(inputData.getGroupList()).setSitRows(inputData.getSitRows());
		double[] results = sitAllocation.solveProblem();
		assertEquals(100, results[0], 0.01);
		assertEquals(100, results[1], 0.0001);
	}

}
