package uk.ac.nott.cs.g53dia.psydd2;

import java.util.ArrayList;
import java.util.Random;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.EmptyCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Well;

/**
 * Custom {@link Tanker} with single agent AI
 */
public class MyTanker extends Tanker {
	
	private ArrayList<FuelPump> fuelPumps = new ArrayList<>();
	private ArrayList<Station> stations = new ArrayList<>();
	private ArrayList<Well> wells = new ArrayList<>();
	
	/** Agent's previous action */
	private Action lastAction = null;
	
	MyTanker(Random r) {
		this.r = r;
	}
	
	/**
	 * Evaluates its surroundings and returns the {@link Action} that the tank should execute
	 *
	 * @param view     the cells the Tanker can currently see
	 * @param timeStep The current time-step
	 * @return The {@link Action} the tanker should execute
	 *
	 * @see uk.ac.nott.cs.g53dia.library.FallibleAction
	 * @see uk.ac.nott.cs.g53dia.library.MoveAction
	 * @see uk.ac.nott.cs.g53dia.library.MoveTowardsAction
	 * @see uk.ac.nott.cs.g53dia.library.DisposeWasteAction
	 * @see uk.ac.nott.cs.g53dia.library.LoadWasteAction
	 * @see uk.ac.nott.cs.g53dia.library.RefuelAction
	 */
	@Override
	public Action senseAndAct(Cell[][] view, long timeStep) {
		if (actionFailed) {
			actionFailed = false;
			return lastAction;
		}
		
		for (int i = 0; i < view.length; i++) {
			for (int j = 0; j < view[i].length; j++) {
				Cell cell = view[i][j];
				if (!(cell instanceof EmptyCell)) {
					System.out.printf("view[%3d][%3d] -- ", i - 20, 20 - j);
					if (cell instanceof FuelPump) {
						System.out.println("FuelPump");
					} else if (cell instanceof Station) {
						((Station) cell).getTask();
						System.out.println("Station");
					} else if (cell instanceof Well) {
						System.out.println("Well");
					}
				}
			}
		}
		
		System.out.println(getPosition() + "\n");
		
		lastAction = new MoveAction(MoveAction.NORTH);
		return lastAction;
	}
	
	/**
	 * If the tanker failed its action, call this method
	 */
	public void failedAction() {
		actionFailed = true;
	}
}
