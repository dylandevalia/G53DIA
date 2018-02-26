package uk.ac.nott.cs.g53dia.library;

/**
 * A environment cell which contains nothing.
 * <p>
 * Empty cells can be replaced by a fuelpump.
 *
 * @author Brian Logan
 */

/*
 * Copyright (c) 2010 Brian Logan (bsl@cs.nott.ac.uk)
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */

public class EmptyCell extends DefaultCell {
	
	private boolean viewable = false;
	private boolean onAxis = false;
	
	EmptyCell(Point pos) {
		super(pos);
	}
	
	public void setViewable(boolean viewable) {
		this.viewable = viewable;
	}
	
	public boolean isViewable() {
		return viewable;
	}
	
	public void setOnAxis(boolean onAxis) {
		this.onAxis = onAxis;
	}
	
	public boolean isOnAxis() {
		return onAxis;
	}
}
