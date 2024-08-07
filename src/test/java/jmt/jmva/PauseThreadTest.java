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

package jmt.jmva;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import jmt.jmva.engine.DispatcherJMVAschema;

import org.junit.Test;

/**
 * @author Stefano Omini
 */
public class PauseThreadTest {

	private static final int MAX_WAIT_TIME = 15000;

	private static void test1() {
		int n = 5;
		Thread1 t1 = new Thread1(n);
		Thread2 t2 = new Thread2(n);
		t1.start();
		t2.start();
	}

	@Test
	public void testPausing1SecondSleepAndCheckSimProgress() throws InterruptedException, URISyntaxException {

		DispatcherJMVAschema disp = new DispatcherJMVAschema(new File(getClass().getResource("randomModel_open_1.xml").toURI()));

		Thread3 t3 = new Thread3(disp);
		Thread4 t4 = new Thread4(disp);
		t3.start();
		t4.start();
		t4.join(MAX_WAIT_TIME);
		if (t4.isAlive()) {
			t4.finished = true;
			fail("Thread did not finish");
		}
	}

	@Test
	public void testPausing5SecondsSleepAndAbortAllMeasures() throws InterruptedException, URISyntaxException {

		DispatcherJMVAschema disp = new DispatcherJMVAschema(new File(getClass().getResource("randomModel_open_1.xml").toURI()));

		Thread3 t3 = new Thread3(disp);
		Thread5 t5 = new Thread5(disp);
		t3.start();
		t5.start();
		t5.join(MAX_WAIT_TIME);
		if (t5.isAlive()) {
			t5.finished = true;
			fail("Thread did not finish");
		}
	}

	public static void main(String[] args) throws InterruptedException {
		//test1();
	}

}

class Thread1 extends Thread {

	int models;

	public Thread1(int n) {
		models = n;

	}

	@Override
	public void run() {

		BatchTest.comboTest(1, 1, models, 1, 1);

	}

}

class Thread2 extends Thread {

	int models;

	public Thread2(int n) {
		models = n;

	}

	@Override
	public void run() {

		//boolean finished = false;
		double progress = 0.0;

		do {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/*if (NetSystem.pause()) {
				if (NetSystem.getNetworkList().size() != 0) {
					QueueNetwork net = NetSystem.getNetworkList().get(0);
					try {
						progr = NetSystem.checkProgress(net);
						System.out.println("PROGRESS: " + Double.toString(progr));
						if (progr == 1) {
							//finished = true;
							models--;

						}
					} catch (NetException e) {
						e.printStackTrace();
					}

				}

				NetSystem.restartFromPause();
			} else {
				if (progr > 0) {
					System.out.println("PROGRESS: finished");
					break;
				}
			} */

		} while (models > 0);

	}

}

class Thread3 extends Thread {

	private DispatcherJMVAschema disp;

	public Thread3(DispatcherJMVAschema disp) {
		this.disp = disp;
	}

	@Override
	public void run() {

		disp.solveModel();

	}

}

class Thread4 extends Thread {

	private DispatcherJMVAschema disp;
	boolean finished = false;

	public Thread4(DispatcherJMVAschema disp) {
		this.disp = disp;
	}

	@Override
	public void run() {

		double progress = 0.0;

		while (!finished) {

			progress = disp.checkSimProgress();

			System.out.println("Progress: " + Double.toString(progress));
			disp.refreshTempMeasures();
			disp.printTempMeasures();

			if (progress >= 1.0) {
				finished = true;
				System.out.println("Finished");
				break;
			}

			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}

class Thread5 extends Thread {

	private DispatcherJMVAschema disp;
	boolean finished = false;

	public Thread5(DispatcherJMVAschema disp) {
		this.disp = disp;
	}

	@Override
	public void run() {

		double progress = 0.0;

		while (!finished) {

			progress = disp.checkSimProgress();

			System.out.println("Progress: " + Double.toString(progress));
			disp.refreshTempMeasures();
			disp.printTempMeasures();

			try {
				sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (disp.abortAllMeasures()) {
				finished = true;
				System.out.println("Finished");
			}

		}

	}

}
