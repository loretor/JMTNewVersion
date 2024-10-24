/**
 * 
 */
package jmt.engine.jwat.input;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import jmt.engine.jwat.MatrixObservations;
import jmt.engine.jwat.Observation;
import jmt.framework.data.MacroReplacer;
import jmt.gui.jwat.JWATConstants;

import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author Maevar
 *
 */
public class IntervalInputLoader extends InputLoader implements JWATConstants {

	private int minInd;
	private int maxInd;

	/**
	 * @param param
	 * @param fileName
	 * @param map
	 * @param prg
	 */
	public IntervalInputLoader(Parameter param, String fileName, VariableMapping[] map, ProgressShow prg) throws FileNotFoundException {
		super(param, fileName, map, prg);
		minInd = param.getOptions()[0];
		maxInd = param.getOptions()[1];
	}

	/* (non-Javadoc)
	 * @see jwat.Input.workerLoader#construct()
	 */
	@Override
	public Object construct() {
		int i, j;
		boolean[] sel = param.getVarSelected();
		FileWriter w = null;
		try {
			w = new FileWriter(MacroReplacer.replace(LOG_FILE_NAME));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		PatternMatcherInput input = new PatternMatcherInput("");
		Perl5Compiler myComp = new Perl5Compiler();
		Perl5Matcher myMatch = new Perl5Matcher();
		String line = "";
		String[] regExp = param.getRegularExp();
		String[] separator = param.getSeparator();
		double[] lineValue = new double[param.getNumVarSelected()];
		String[] lineToken = new String[param.getNumVar()];
		String parseToken = null;
		MatrixObservations m = null;

		try {
			initShow(maxInd - minInd + 1);
			countObs = 0;
			totalRaw = 0;
			line = reader.readLine();

			for (i = 0; line != null && i < (minInd - 1); i++) {
				line = reader.readLine();
			}

			while (totalRaw + minInd <= maxInd) {
				if (isCanceled()) {
					try {
						w.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					valori = null;
					msg = INPUT_MSG_ABORT;
					return null;
				}
				input.setInput(line);
				totalRaw++;
				countObs++;
				if (totalRaw % getStep() == 0) {
					updateInfos(totalRaw, "<HTML># observations read: " + totalRaw + "<p># errors: " + (totalRaw - countObs) + "</HTML>", false);
				}
//Read strings and check their format
for (i = 0; i < param.getNumVar(); i++) {
//Read the token with the separators (if defined) whatever is inside
					if (separator[i] != null) {
						parseToken = separator[i];
					} else {
						parseToken = regExp[i];
					}

					if (myMatch.contains(input, myComp.compile(parseToken))) {
						if (sel[i]) {
//Take the token
							lineToken[i] = myMatch.getMatch().toString();
							if (separator[i] != null) {
// If the token had separators, remove them
								lineToken[i] = lineToken[i].substring(1, lineToken[i].length() - 1);
//And I take what interests me of what remains
								if (myMatch.contains(lineToken[i], myComp.compile(regExp[i]))) {
									lineToken[i] = myMatch.getMatch().toString();
								} else {
//Wrong line, decrease the number of obs
									countObs--;

									try {
										w.write("Error in row " + (totalRaw + minInd) + " : Element " + i + " is wrong\n");
									} catch (IOException e) {
										e.printStackTrace();
									}
									break;
								}
							}
						}
					} else {
//Wrong line, decrease the number of obs
						countObs--;
						try {
							w.write("Error in row " + (totalRaw + minInd) + " : Line does not match format (element " + i + " not found)\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
				}
				j = 0;
				//Riga Corretta
				if (i == param.getNumVar()) {
					if (input.endOfInput()) {
						//Converte i valori (stringa) letti
						for (i = 0; i < param.getNumVar(); i++) {
							if (sel[i]) {
								if (map[i] == null) {
									lineValue[j++] = Double.parseDouble(lineToken[i]);
								} else {
									lineValue[j++] = map[i].addNewValue(lineToken[i]);
								}
							}
						}
						valori.add(new Observation(lineValue, countObs));
					} else {
						//Riga sbagliata, decremento il num di oss
						countObs--;
						try {
							w.write("Error in row " + (totalRaw + minInd) + " : Too many fields\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				line = reader.readLine();
			}
			try {
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//Wrong format, no element loaded
			if (valori.size() == 0) {
				closeView();
				valori = null;
				msg = INPUT_MSG_ABORT_WRONG_FORMAT;
				return null;
			}

			updateInfos(totalRaw, "Calculating Statistics...", false);
			m = new MatrixObservations(valori.toArray(new Observation[valori.size()]), param.getSelName(), param.getSelType(), map);
			updateInfos(totalRaw + 1, "Done", true);
			return m;
		} catch (Exception e) {
			closeView();
			try {
				w.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			valori = null;
			msg = INPUT_MSG_FAIL;
			return null;
		} catch (OutOfMemoryError err) {
			closeView();
			try {
				w.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			valori = null;
			msg = "Out of memory. Try with more memory";
			return null;
		}

	}

}
