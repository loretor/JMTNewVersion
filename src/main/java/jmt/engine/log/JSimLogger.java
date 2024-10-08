/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package jmt.engine.log;

import java.io.*;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;

import jmt.framework.data.MacroReplacer;

import org.apache.log4j.*;

/**
 * <p><b>Name:</b> JSimLogger</p>
 * <p><b>Description:</b>
 * A wrapper aroung log4j API to provide logging capabilities.
 * </p>
 * <p><b>Date:</b> 11/dic/2009
 * <b>Time:</b> 20.17.54</p>
 * @author Bertoli Marco [marco.bertoli@neptuny.com]
 * @version 1.0
 */
public class JSimLogger implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String STD_LOGGER = "jmt.engine";
	public static final String LOG4J_CONF = "log4j.conf";
	private static String delimiter = ",";
	private static String decimalSeparator = ".";
	private static String debugLogPath = "JSIM_debug.csv";

	private transient org.apache.log4j.Logger logger;
	static {
		// Initialize only if somebody did not already initialize this.
		if (!LogManager.getCurrentLoggers().hasMoreElements()) {
			URL propsUrl = JSimLogger.class.getResource(LOG4J_CONF);
			boolean initialized = false;
			if (propsUrl != null) {
				Properties p = new Properties();
				try {
					InputStream is = propsUrl.openStream();
					p.load(is);
					is.close();

					// Replaces macros like work directory
					Properties replaced = new Properties();
					for (Entry<Object, Object> e : p.entrySet()) {
						replaced.put(e.getKey(), MacroReplacer.replace((String)e.getValue()));
					}
					PropertyConfigurator.configure(replaced);
					initialized = true;
				} catch (IOException ex) {
					System.err.println("Error while initializing logger form URL: " + propsUrl);
					ex.printStackTrace();
				}
			}
			if (!initialized) {
				System.out.println("Cannot find logProperties, using defaults");
				//set stdout defaults
				Properties p = new Properties();
				p.setProperty("log4j.rootLogger", "DEBUG, stdout");
				//p.setProperty("log4j.rootLogger","ALL, stdout");
				p.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
				p.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
				p.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%-5p [%t]- %m (%F:%L)%n");
				PropertyConfigurator.configure(p);
			}
		}
	}

	public static void resetDebugLoggerStorePath(String newPath) {
		String correctedPath = newPath.replace("\\", System.getProperty("file.separator"));
		debugLogPath = correctedPath + System.getProperty("file.separator") + "JSIM_debug.csv";
		File oldEventTraceFile = new File(debugLogPath);
		if (oldEventTraceFile.exists()) {
			oldEventTraceFile.delete();
		}
		writeCSVHeaderIfNotExists(debugLogPath);
	}

	private static void writeCSVHeaderIfNotExists(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			try (FileWriter writer = new FileWriter(filePath, true)) {
				writer.append("dispatchOrReceive").append(delimiter)
						.append("sender").append(delimiter)
						.append("eventname").append(delimiter)
						.append("receiver").append(delimiter)
						.append("timestamp").append("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void setDelimiter(String newDelimiter) {
		delimiter = newDelimiter;
	}

	public static void setDecimalSeparator(String newDecimalSeparator) {
		decimalSeparator = newDecimalSeparator;
	}

	public static String getDecimalSeparator() {
		return decimalSeparator;
	}

	public static void logCustomDebug(String sendOrReceive, String sender, String eventName, String receiver, String timeStamp) {
		try (FileWriter writer = new FileWriter(debugLogPath, true)) {
			writer.append(sendOrReceive).append(delimiter)
					.append(sender).append(delimiter)
					.append(eventName).append(delimiter)
					.append(receiver).append(delimiter)
					.append(timeStamp).append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private JSimLogger() {
		logger = Logger.getRootLogger();
	}

	private JSimLogger(String loggerName) {
		logger = Logger.getLogger(loggerName);
	}

	public static JSimLogger getRootLogger() {
		JSimLogger mylogger = new JSimLogger();
		return mylogger;
	}

	public static JSimLogger getLogger() {
		return getLogger(JSimLogger.STD_LOGGER);
	}

	public static JSimLogger getLogger(Object caller) {
		JSimLogger mylogger = null;
		if (caller != null) {
			mylogger = JSimLogger.getLogger(caller.getClass());
		} else {
			mylogger = JSimLogger.getLogger();
		}
		return mylogger;
	}

	public static JSimLogger getLogger(Class<?> callerClass) {
		JSimLogger mylogger = new JSimLogger(callerClass.getName());
		return mylogger;
	}

	public static JSimLogger getLogger(String loggerName) {
		JSimLogger mylogger = new JSimLogger(loggerName);
		return mylogger;
	}

	// printing methods:
	public void debug(Object message) {
		logger.debug(message);
	}

	public void info(Object message) {
		logger.info(message);
	}

	public void warn(Object message) {
		logger.warn(message);
	}

	public void error(Object message) {
		logger.error(message);
	}

	public void error(Throwable th) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		th.printStackTrace(pw);
		String buffer = sw.toString();
		logger.error(buffer);
	}

	public static void logCalenderOn() {
		LoggerStateManager.getInstance().setLogCalenderOn(true);
	}

	public static void logCalenderOff() {
		LoggerStateManager.getInstance().setLogCalenderOn(false);
	}

	public boolean isLogCalenderOn() {
		return LoggerStateManager.getInstance().isLogCalenderOn();
	}

	public void error(String message, Throwable th) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(message);
		th.printStackTrace(pw);
		String buffer = sw.toString();
		logger.error(buffer);
	}

	public void fatal(Object message) {
		logger.fatal(message);
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	// --- Following methods handles recreation of transient logger object after serialization ------------------
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(logger.getName());
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		String name = (String) in.readObject();
		if (name == null) {
			logger = Logger.getRootLogger();
		} else {
			logger = Logger.getLogger(name);
		}
	}
	// ----------------------------------------------------------------------------------------------------------

}
