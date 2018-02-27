package uk.ac.nott.cs.g53dia.psydd2;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.ActionFailedException;
import uk.ac.nott.cs.g53dia.library.Environment;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.Tanker;

public class MyMoveAction implements Action {
	
	private int direction;
	
	public MyMoveAction(int dir) {
		this.direction = dir;
	}
	
	@Override
	public void execute(Environment env, Tanker tanker)
		throws ActionFailedException {
		
		new MoveAction(direction).execute(env, tanker);
	}
	
	public int getDirection() {
		return direction;
	}
}
