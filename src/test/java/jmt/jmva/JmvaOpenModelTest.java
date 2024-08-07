package jmt.jmva;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import jmt.jmva.analytical.CommandLineSolver;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class JmvaOpenModelTest {

	@Test
	public void randomModel_open_1() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();

		File input = File.createTempFile("randomModel_open_1", ".xml");
		File expected = new File(getClass().getResource("randomModel_open_1-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_open_1-input.xml").toURI()), input);

		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}

	@Test
	public void randomModel_open_2() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();

		File input = File.createTempFile("randomModel_open_2", ".xml");
		File expected = new File(getClass().getResource("randomModel_open_2-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_open_2-input.xml").toURI()), input);

		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}

	@Test
	public void randomModel_open_3() throws IOException, URISyntaxException {
		CommandLineSolver solver = new CommandLineSolver();

		File input = File.createTempFile("randomModel_open_3", ".xml");
		File expected = new File(getClass().getResource("randomModel_open_3-expectedOutput.xml").toURI());
		FileUtils.copyFile(new File(getClass().getResource("randomModel_open_3-input.xml").toURI()), input);

		assertTrue("Solver was not able to solve file!", solver.solve(input));
		assertTrue(": Real output differs from expected!\n"
				+ "Expected: \n" + FileUtils.readFileToString(expected)
				+ "\nActual: \n" + FileUtils.readFileToString(input), 
				FileUtils.contentEquals(input, expected));			
	}

}
