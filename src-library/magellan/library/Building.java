/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library;

import magellan.library.rules.BuildingType;

/**
 * A <code>Building</code> found in Atlantis reports.
 *
 * @author $Author: $
 * @version $Revision: 299 $
 */
public interface Building extends UnitContainer, HasRegion {

	/**
	 * DOCUMENT ME!
	 *
	 * @return the size of the building.
	 */
	public int getSize();

	/**
	 * DOCUMENT ME!
	 *
	 * @param iSize set the size of the building.
	 */
	public void setSize(int iSize);

	/**
	 * DOCUMENT ME!
	 *
	 * @return the cost of the building.
	 */
	public int getCost();

	/**
	 * DOCUMENT ME!
	 *
	 * @param iCost set the cost for the building.
	 */
	public void setCost(int iCost);

	/**
	 * Sets the region this building is in. If this building already has a region set, this method
	 * takes care of removing it from that region.
	 *
	 * @param region the region to the the building into.
	 */
	public void setRegion(Region region);

	/**
	 * Returns the <code>BuildingType</code> of this building.
	 *
	 * @return the <code>BuildingType</code> of this building
	 */
	public BuildingType getBuildingType();
	/**
	 * Get the region where this building is located.
	 *
	 * @return the region the building is in.
	 */
	public Region getRegion();
	
	/**
	 * Sets the trueBuildingType which es not realy a type but
	 * just a String
	 * only occurance now "Traumschlößchen",wahrerTyp
	 * Fiete 20060910
	 * 
	 * @param trueBuildingType  as string
	 */
	
	public void setTrueBuildingType(String trueBuildingType);
  
	/**
	 * Gets the trueBuildingType which es not realy a type but
	 * just a String
	 * only occurance now "Traumschlößchen",wahrerTyp
	 * Fiete 20060910
	 * 
	 * 
	 * @return String = trueBuildingType
	 */
	public String getTrueBuildingType();
	
	/**
	 * Returns a String representation of the Building object.
	 *
	 * @return the Building object as string.
	 */
	public String toString();
}
