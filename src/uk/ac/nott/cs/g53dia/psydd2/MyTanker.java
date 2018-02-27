package uk.ac.nott.cs.g53dia.psydd2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.EmptyCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Task;
import uk.ac.nott.cs.g53dia.library.Well;
import uk.ac.nott.cs.g53dia.psydd2.utility.Position;
import uk.ac.nott.cs.g53dia.psydd2.utility.Utility;

/**
 * Custom {@link Tanker} with single agent AI
 */
public class MyTanker extends Tanker {
	
	private LinkedHashMap<FuelPump, Position> fuelPumps = new LinkedHashMap<>();
	private LinkedHashMap<Station, Position> stations = new LinkedHashMap<>();
	private LinkedHashMap<Well, Position> wells = new LinkedHashMap<>();
	
	private Action lastAction = null;
	private boolean triedToMove = false;
	private Position pos = new Position(0, 0), lastPos = pos.copy();
	private FuelPump home;
	
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
		if (actionFailed && triedToMove) {
			// If action fail, revert expected position
			pos = lastPos.copy();
		} else {
			// Set last position before changing pos
			lastPos = pos.copy();
		}
		
		// Look through view
		sense(view);
		
		ArrayList<Map.Entry<FuelPump, Position>> sortedFuelPumps = sortFuelPumps();
		ArrayList<Map.Entry<Station, Position>> sortedStations = sortStations();
		ArrayList<Map.Entry<Well, Position>> sortedWells = sortWells();
		
		Action action = act(sortedFuelPumps, sortedStations, sortedWells);
		
		int move = Utility.randBetween(0, 0);
		pos.move(move);
		lastAction = new MoveAction(move);
		return lastAction;
	}
	
	private void sense(Cell[][] view) {
		for (int i = 0; i < view.length; i++) {
			for (int j = 0; j < view[i].length; j++) {
				Cell cell = view[i][j];
				if ((cell instanceof EmptyCell)) {
					continue;
				}

//				System.out.printf("view[%3d][%3d] -- ", i - 20, 20 - j);
				Position cellPos = new Position(pos.x + (i - 20), pos.y + (20 - j));
				
				if (cell instanceof FuelPump) {
//					System.out.println("FuelPump");
					fuelPumps.putIfAbsent((FuelPump) cell, cellPos);
				} else if (cell instanceof Station) {
//					System.out.println("Station");
					boolean found = false;
					for (Map.Entry<Station, Position> s : stations.entrySet()) {
						if (s.getKey().equals(cell)) {
							found = true;
							break;
						}
					}
					if (!found) {
						stations.put((Station) cell, cellPos);
					}
				} else if (cell instanceof Well) {
//					System.out.println("Well");
					wells.putIfAbsent((Well) cell, cellPos);
				}
			}
		}
		
		System.out.println(pos);
		System.out.println(getPosition() + "\n");
	}
	
	private Action act(
		ArrayList<Map.Entry<FuelPump, Position>> fuelPumps,
		ArrayList<Map.Entry<Station, Position>> stations,
		ArrayList<Map.Entry<Well, Position>> wells
	) {
		
		// Sort stations by waste amount
		return null;
	}
	
	private Action checkFuel() {
		return null;
	}
	
	/**
	 * Sorts {@link #stations} map by if it has waste then by distance
	 *
	 * @return List where the first element is the closest station with waste
	 */
	private ArrayList<Map.Entry<Station, Position>> sortStations() {
		ArrayList<Entry<Station, Position>> sortedStations = new ArrayList<>(stations.entrySet());
		sortedStations.sort((a, b) -> {
			// To return:
			//   positive if a is better
			//   zero     if equal
			//   negative if b is better
			
			Task aTask = a.getKey().getTask();
			Task bTask = b.getKey().getTask();
			int aWaste = (aTask == null) ? 0 : aTask.getWasteRemaining();
			int bWaste = (bTask == null) ? 0 : bTask.getWasteRemaining();
			
			// If one has doesn't have waste
			if (aWaste == 0 || bWaste == 0) {
				// Return one with waste or equal
				return aWaste - bWaste;
			}
			
			int aDist = a.getValue().distanceTo(pos);
			int bDist = b.getValue().distanceTo(pos);
			
			return aDist - bDist;
		});
		
		return sortedStations;
	}
	
	/**
	 * Sorts the {@link #fuelPumps} map by distance from player
	 *
	 * @return List where the first element is closest fuel pump
	 */
	private ArrayList<Map.Entry<FuelPump, Position>> sortFuelPumps() {
		ArrayList<Entry<FuelPump, Position>> sortedPumps = new ArrayList<>(fuelPumps.entrySet());
		sortedPumps.sort(Comparator.comparingInt(a -> a.getValue().distanceTo(pos)));
		return sortedPumps;
	}
	
	/**
	 * Sorts the {@link #wells} map by distance from player
	 *
	 * @return List where the first element is the closet well
	 */
	private ArrayList<Map.Entry<Well, Position>> sortWells() {
		ArrayList<Entry<Well, Position>> sortedPumps = new ArrayList<>(wells.entrySet());
		sortedPumps.sort(Comparator.comparingInt(a -> a.getValue().distanceTo(pos)));
		return sortedPumps;
	}
}
