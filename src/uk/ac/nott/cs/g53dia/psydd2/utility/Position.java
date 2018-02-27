package uk.ac.nott.cs.g53dia.psydd2.utility;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.psydd2.MyMoveAction;

public class Position {
	
	public int x, y;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Takes a position and returns the {@link MoveAction}.DIRECTION from
	 * this position to the given position
	 *
	 * @param pos The position to get to
	 * @return The MoveAction direction towards the given position
	 */
	private static int positionToMoveAction(Position pos) {
		if (pos.x == 0 && pos.y > 0) {
			return MoveAction.NORTH;
		} else if (pos.x > 0 && pos.y > 0) {
			return MoveAction.NORTHEAST;
		} else if (pos.x > 0 && pos.y == 0) {
			return MoveAction.EAST;
		} else if (pos.x > 0 && pos.y < 0) {
			return MoveAction.SOUTHEAST;
		} else if (pos.x == 0 && pos.y < 0) {
			return MoveAction.SOUTH;
		} else if (pos.x < 0 && pos.y < 0) {
			return MoveAction.SOUTHWEST;
		} else if (pos.x < 0 && pos.y == 0) {
			return MoveAction.WEST;
		} else if (pos.x < 0 && pos.y > 0) {
			return MoveAction.NORTHWEST;
		} else {
			return -1;
		}
	}
	
	/**
	 * Converts a {@link MoveAction}.DIRECTION to a unit position
	 *
	 * @param moveAction The direction heading
	 * @return The unit position in the given direction
	 */
	private static Position moveActionToPosition(int moveAction) {
		switch (moveAction) {
			case MoveAction.NORTH:
				return new Position(0, 1);
			case MoveAction.NORTHEAST:
				return new Position(1, 1);
			case MoveAction.EAST:
				return new Position(1, 0);
			case MoveAction.SOUTHEAST:
				return new Position(1, -1);
			case MoveAction.SOUTH:
				return new Position(0, -1);
			case MoveAction.SOUTHWEST:
				return new Position(-1, -1);
			case MoveAction.WEST:
				return new Position(-1, 0);
			case MoveAction.NORTHWEST:
				return new Position(-1, 1);
			default:
				throw new IllegalArgumentException("Unknown MoveAction");
		}
	}
	
	public Position copy() {
		return new Position(x, y);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	/**
	 * Adds the values given to this vector's values
	 *
	 * @param x The x component
	 * @param y The y component
	 * @return This vector with the added components
	 */
	private Position add(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	/**
	 * Adds the given vector's values to this vector
	 *
	 * @param other The other vector to add numbers to
	 * @return This vector with the added values of the other vector
	 */
	private Position add(Position other) {
		return add(other.x, other.y);
	}
	
	/**
	 * Subtracts this vector with the given values
	 *
	 * @param x The x component
	 * @param y The y component
	 * @return This vector with the subtracted values
	 */
	private Position sub(double x, double y) {
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	/**
	 * Subtracts this vector with the values of the other given vector
	 *
	 * @param other The vector which values to subtract from this vector
	 * @return This vector with the subtracted values
	 */
	private Position sub(Position other) {
		return sub(other.x, other.y);
	}
	
	/**
	 * Calculates the distance between this vector and the given vector
	 * considering that diagonals cost the same as cardinal directions
	 *
	 * @param other The other vector
	 * @return The distance between this and the given vector
	 */
	public int distanceTo(Position other) {
		return Math.max(Math.abs(x - other.x), Math.abs(y - other.y));
	}
	
	public void move(int moveAction) {
		add(moveActionToPosition(moveAction));
	}
	
	public Action moveToward(Position other) {
		int direction = positionToMoveAction(other.copy().sub(this));
		if (direction < 0) {
			return null;
		}
		return new MyMoveAction(direction);
	}
}
