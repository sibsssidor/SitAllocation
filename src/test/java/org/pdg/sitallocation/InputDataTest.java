package org.pdg.sitallocation;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class InputDataTest {

	@Test
	public void testParseCorrectInput() throws Exception {
		InputData inputData = (new InputData()).parseInput("src/test/resources/salloc.txt");
		assertEquals(7, inputData.getGroupList().size());
		assertEquals(3, inputData.getGroupList().get(0).getSortedPassengerList().size());
		assertEquals(false, inputData.getGroupList().get(0).getSortedPassengerList().get(0).isW());
		assertEquals(2, inputData.getGroupList().get(0).getSortedPassengerList().get(0).getId());
		assertEquals(true, inputData.getGroupList().get(0).getSortedPassengerList().get(2).isW());
		assertEquals(4, inputData.getSitRows().size());
		assertEquals(4, inputData.getSitRows().get(0).getSize());
		assertEquals(2, inputData.getSitRows().get(0).getWindowSits());
		assertEquals(4, inputData.getSitRows().get(3).getSize());
		assertEquals(2, inputData.getSitRows().get(3).getWindowSits());
	}

	@Test
	public void testEmptyRowInput() {
		try {
			InputData inputData = (new InputData()).parseInput("src/test/resources/emptyrows.txt");
			fail("Supposed to break...");
		} catch (ParsingException e) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testBadRowInput() {
		try {
			InputData inputData = (new InputData()).parseInput("src/test/resources/badrows.txt");
			fail("Supposed to break...");
		} catch (ParsingException e) {

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testZeroPassInput() {
		try {
			InputData inputData = (new InputData()).parseInput("src/test/resources/zeropass.txt");
			fail("Supposed to break...");
		} catch (ParsingException e) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
	}

}
