package uk.ac.nott.cs.g53dia.psydd2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.DisposeWasteAction;
import uk.ac.nott.cs.g53dia.library.EmptyCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.LoadWasteAction;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.RefuelAction;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Task;
import uk.ac.nott.cs.g53dia.library.Well;
import uk.ac.nott.cs.g53dia.psydd2.utility.Position;

/**
 * Custom {@link Tanker} with single agent AI
 */
public class MyTanker extends Tanker {
	
	private static final double FUEL_BUFFER_PERCENT = 0.1;
	private static final double FUEL_BUFFER = Tanker.MAX_FUEL * FUEL_BUFFER_PERCENT;
	
	private Position pos = new Position(0, 0), lastPos = pos.copy();
	
	private LinkedHashMap<FuelPump, Position> fuelPumps = new LinkedHashMap<>();
	private LinkedHashMap<MyStation, Position> stations = new LinkedHashMap<>();
	private LinkedHashMap<Well, Position> wells = new LinkedHashMap<>();
	
	private Action lastAction = null;
	private boolean triedToMove = false;
	private boolean pickedUp = false;
	
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
		
		ArrayList<Map.Entry<FuelPump, Position>> sortedFuelPumps = sortFuelPumps(pos);
		ArrayList<Map.Entry<MyStation, Position>> sortedStations = sortStations(pos);
		ArrayList<Map.Entry<Well, Position>> sortedWells = sortWells(pos);
		
		Action action = act(sortedFuelPumps, sortedStations, sortedWells);
		
		if (action == null) {
			action = new MyMoveAction(MoveAction.NORTH);
		}
		
		triedToMove = false;
		if (action instanceof MyMoveAction) {
			triedToMove = true;
			pos.move(((MyMoveAction) action).getDirection());
		}
		lastAction = action;
		return action;
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
					for (Map.Entry<MyStation, Position> s : stations.entrySet()) {
						if (s.getKey().equals(cell)) {
							found = true;
							s.getKey().updateStation((Station) cell);
							break;
						}
					}
					if (!found) {
						stations.put(new MyStation((Station) cell), cellPos);
					}
				} else if (cell instanceof Well) {
//					System.out.println("Well");
					wells.putIfAbsent((Well) cell, cellPos);
				}
			}
		}

//		System.out.println(pos);
//		System.out.println(getPosition() + "\n");
	}
	
	private Action act(
		ArrayList<Map.Entry<FuelPump, Position>> fuelPumps,
		ArrayList<Map.Entry<MyStation, Position>> stations,
		ArrayList<Map.Entry<Well, Position>> wells
	) {
		Action finalAction;
		
		finalAction = fuelCheck(fuelPumps);
		if (finalAction != null) {
			return finalAction;
		}
		
		finalAction = stationCheck(fuelPumps, stations);
		if (finalAction != null) {
			return finalAction;
		}
		
		finalAction = wasteCheck(stations, wells);
		if (finalAction != null) {
			return finalAction;
		}
		
		finalAction = explore();
		if (finalAction != null) {
			return finalAction;
		}
		
		// Sort stations by waste amount
		return null;
	}
	
	/**
	 * Checks if tanker needs fuel and goes to refuel if it does
	 *
	 * @param fuelPumps List of fuelPumps sorted by distance from tanker
	 * @return {@link MyMoveAction} or {@link RefuelAction} if fuel is needed
	 * or {@code null} if not
	 */
	private Action fuelCheck(ArrayList<Map.Entry<FuelPump, Position>> fuelPumps) {
		double fuelLevel = getFuel();
		int distance = fuelPumps.get(0).getValue().copy().distanceTo(pos);
		
		// If too far from fuel pump
		if (fuelLevel <= distance) {
			Action action = pos.moveToward(fuelPumps.get(0).getValue());
			
			if (action == null) {
				// At fuel pump
				return new RefuelAction();
			} else {
				return action;
			}
		}
		
		// Don't need to refuel
		return null;
	}
	
	/**
	 * Checks if the tanker is on a station with a task and gets waste. If it
	 * has waste, it moves towards the nearest well and dumps waste
	 *
	 * @param stations The list of stations sorted by distance from tanker
	 * @param wells    The list of wells sorted by distance from tanker
	 * @return {@link LoadWasteAction} if on a station and can get waste,
	 * {@link DisposeWasteAction} if on a well, {@code null} else
	 */
	private Action wasteCheck(
		ArrayList<Map.Entry<MyStation, Position>> stations,
		ArrayList<Map.Entry<Well, Position>> wells
	) {
		// If carrying no waste
		if (getWasteLevel() == 0) {
			return null;
		}
		
		// TODO: Search through nearby stations if have room to spare
		// vvv Thus carrying waste vvv
		
		// If on a well
		if (wells.get(0).getValue().distanceTo(pos) == 0) {
			return new DisposeWasteAction();
		}
		
		// Move towards well
		return pos.moveToward(wells.get(0).getValue());
	}
	
	private Action stationCheck(
		ArrayList<Map.Entry<FuelPump, Position>> fuelPumps,
		ArrayList<Map.Entry<MyStation, Position>> stations
	) {
		if (
			stations.get(0).getKey().getTask() == null
				|| getWasteLevel() == Tanker.MAX_WASTE
				|| getFuel() < stations.get(0).getValue().distanceTo(pos)
			) {
			// If no stations have a task
			// or waste level is max
			// or nearest station with task is out of reach
			return null;
		}
		
		// If on a station
		if (pos.distanceTo(stations.get(0).getValue()) == 0) {
			return new LoadWasteAction(stations.get(0).getKey().getTask());
		}
		
		// Pick station to go to
		for (Entry<MyStation, Position> station : stations) {
			MyStation s = station.getKey();
			Position p = station.getValue();
			
			// If no tasks, exit
			if (s.getTask() == null) {
				return null;
			}
			
			// If stations is too far away, ignore
			int distToStation = p.distanceTo(pos);
			if (distToStation > getFuel()) {
				continue;
			}
			
			// vvv Is within fuel range and has a task vvv
			
			// If tanker has fuel, limit range
			double range = (getWasteLevel() > 0)
				? Tanker.MAX_FUEL * FUEL_BUFFER_PERCENT
				: getFuel();
			
			// Calculate dist pos->station->fuel pump
			ArrayList<Map.Entry<FuelPump, Position>> pumps = sortFuelPumps(p);
			int distStationToPump = p.distanceTo(pumps.get(0).getValue());
			if (distToStation + distStationToPump > range) {
				continue;
			}
			
			// Found nearest pump that can go to station with enough fuel
			// to get to pump afterwards. Move towards it
			return pos.moveToward(p);
		}
		
		return null;
	}
	
	private Action explore() {
		return null;
	}
	
	
	/**
	 * Sorts {@link #stations} map by if it has waste then by distance
	 *
	 * @return List where the first element is the closest station with waste
	 */
	private ArrayList<Map.Entry<MyStation, Position>> sortStations(Position distFrom) {
		ArrayList<Entry<MyStation, Position>> sortedStations = new ArrayList<>(stations.entrySet());
		sortedStations.sort((a, b) -> {
			// To return:
			//   positive if a is better
			//   zero     if equal
			//   negative if b is better
			
			// TODO: Improve calculation weighing up amount at station vs travel costs
			
			Task aTask = a.getKey().getTask();
			Task bTask = b.getKey().getTask();
			int aWaste = (aTask == null) ? 0 : aTask.getWasteRemaining();
			int bWaste = (bTask == null) ? 0 : bTask.getWasteRemaining();
			
			// If one has doesn't have waste
			if (aWaste == 0 || bWaste == 0) {
				// Return one with waste or equal
				return bWaste - aWaste;
			}
			
			int aDist = a.getValue().distanceTo(distFrom);
			int bDist = b.getValue().distanceTo(distFrom);
			
			return aDist - bDist;
		});
		
		return sortedStations;
	}
	
	/**
	 * Sorts the {@link #fuelPumps} map by distance from player
	 *
	 * @return List where the first element is closest fuel pump
	 */
	private ArrayList<Map.Entry<FuelPump, Position>> sortFuelPumps(Position distFrom) {
		ArrayList<Entry<FuelPump, Position>> sortedPumps = new ArrayList<>(fuelPumps.entrySet());
		sortedPumps.sort(Comparator.comparingInt(a -> a.getValue().distanceTo(distFrom)));
		return sortedPumps;
	}
	
	/**
	 * Sorts the {@link #wells} map by distance from player
	 *
	 * @return List where the first element is the closet well
	 */
	private ArrayList<Map.Entry<Well, Position>> sortWells(Position distFrom) {
		ArrayList<Entry<Well, Position>> sortedPumps = new ArrayList<>(wells.entrySet());
		sortedPumps.sort(Comparator.comparingInt(a -> a.getValue().distanceTo(distFrom)));
		return sortedPumps;
	}
	
	private double getFuel() {
		return super.getFuelLevel() - FUEL_BUFFER;
	}
}
