package uk.ac.nott.cs.g53dia.psydd2;

import java.util.Random;
import javax.swing.WindowConstants;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.ActionFailedException;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.EmptyCell;
import uk.ac.nott.cs.g53dia.library.Environment;
import uk.ac.nott.cs.g53dia.library.OutOfFuelException;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.TankerViewer;

public class MySimulator {
	
	public static void main(String[] args) {
		
		final int DELAY = 1;
		final int DURATION = 10000;
		
		Random rnd = new Random(19960203); // new Random(System.nanoTime());
		Environment environment = new Environment(Tanker.MAX_FUEL / 2, rnd);
		MyTanker tanker = new MyTanker(rnd);
		TankerViewer tankerViewer = new TankerViewer(tanker);
		tankerViewer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		while (environment.getTimestep() < DURATION) {
			// Update environment and draw
			environment.tick();
			Cell[][] view = environment.getView(tanker.getPosition(), Tanker.VIEW_RANGE);
			
			// Draw view range and axis
			showTankerView(view, true);
			
			// Update the GUI
//			tankerViewer.tick(environment);
			
			// Get taker's action based on its view
			Action action = tanker.senseAndAct(view, environment.getTimestep());
			
			// Try perform action
			try {
				action.execute(environment, tanker);
			} catch (OutOfFuelException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			} catch (ActionFailedException e) {
				System.err.println(e.getMessage());
			}
			
			// Delay before next update
			try {
				Thread.sleep(DELAY);
			} catch (Exception e) {
				System.err.println("Thread failed to sleep");
			}
			
			// Undo view range UI
			showTankerView(view, false);
		}
		
		tankerViewer.tick(environment);
	}
	
	private static void showTankerView(Cell[][] view, boolean show) {
		for (int i = 0; i < view.length; i++) {
			for (int j = 0; j < view[i].length; j++) {
				if (view[i][j] instanceof EmptyCell) {
					((EmptyCell) view[i][j]).setViewable(show);
					if (i == (view.length / 2) || j == (view[i].length / 2)) {
						((EmptyCell) view[i][j]).setOnAxis(show);
					}
				}
			}
		}
	}
}
