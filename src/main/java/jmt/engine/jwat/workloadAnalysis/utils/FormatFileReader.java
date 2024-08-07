package jmt.engine.jwat.workloadAnalysis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FormatFileReader {
	private int numeroVariabili;
	private ArrayList<String> names;
	private ArrayList<String> comments;
	private ArrayList<String> delimiters;
	private ArrayList<String> regExpr;
	private ArrayList<String> defaults;
	private ArrayList<String> replaces;
	private boolean[] selected;
	private int[] types;
	private File fileName;
	private int current = -1;

	public FormatFileReader(String fileN) {
		fileName = new File(fileN);
		// Load data from file format
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			// Retrieve number of variables
			numeroVariabili = Integer.parseInt(reader.readLine());
			types = new int[numeroVariabili];
			selected = new boolean[numeroVariabili];
			names = new ArrayList<String>();
			comments = new ArrayList<String>();
			delimiters = new ArrayList<String>();
			regExpr = new ArrayList<String>();
			defaults = new ArrayList<String>();
			replaces = new ArrayList<String>();
			// Retrieve single variable information
			for (int i = 0; i < numeroVariabili; i++) {
				names.add(reader.readLine());
				types[i] = Integer.parseInt(reader.readLine());
				selected[i] = (Integer.parseInt(reader.readLine()) == 0 ? false : true);
				comments.add(reader.readLine());
				delimiters.add(reader.readLine());
				regExpr.add(reader.readLine());
				defaults.add(reader.readLine());
				replaces.add(reader.readLine());
			}
			current = 0;
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public String getName() {
		if (current != -1) {
			return names.get(current);
		} else {
			return null;
		}
	}

	public int getType() {
		if (current != -1) {
			return types[current];
		} else {
			return -1;
		}
	}

	public boolean getSelect() {
		if (current != -1) {
			return selected[current];
		} else {
			return false;
		}
	}

	public String getComment() {
		if (current != -1) {
			return comments.get(current);
		} else {
			return null;
		}
	}

	public String getDelimiters() {
		if (current != -1) {
			return delimiters.get(current);
		} else {
			return null;
		}
	}

	public String getRegExpr() {
		if (current != -1) {
			return regExpr.get(current);
		} else {
			return null;
		}
	}

	public String getDefaults() {
		if (current != -1) {
			return defaults.get(current);
		} else {
			return null;
		}
	}

	public String getReplace() {
		if (current != -1) {
			return replaces.get(current);
		} else {
			return null;
		}
	}

	public boolean next() {
		if (current < names.size() - 1) {
			current++;
			return true;
		}
		return false;
	}

	public int getNumVars() {
		if (current != -1) {
			return names.size();
		}
		return 0;
	}
}
