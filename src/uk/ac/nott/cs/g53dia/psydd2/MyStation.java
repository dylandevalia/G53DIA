package uk.ac.nott.cs.g53dia.psydd2;

import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Task;

/**
 * My wrapper for the station cell
 */
public class MyStation {
	
	private final int COOL_DOWN = (int) (1 / 0.001);
	
	/** The station cell */
	private Station station;
	/** Number of steps since last checked on */
	private int coolDown = COOL_DOWN;
	
	MyStation(Station station) {
		this.station = station;
	}
	
	public Task getTask() {
		return station.getTask();
	}
	
	public boolean equals(Object o) {
		return station.equals(o);
	}
	
	public void updateStation(Station station) {
		this.station = station;
		coolDown = COOL_DOWN;
	}
	
	public void tick() {
		coolDown--;
	}
	
	public boolean shouldRecheck() {
		return coolDown <= 0;
	}
}
