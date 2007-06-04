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

package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.rules.RegionType;
import magellan.library.utils.Direction;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;


/**
 * This class inspects ships and checks for overload, missing crew and bad routes.
 * 
 */
public class ShipInspector extends AbstractInspector implements Inspector {

	/** The singleton instance of the ShipInspector */
	public static final ShipInspector INSPECTOR = new ShipInspector();

	/**
	 * Returns an instance of ShipInspector.
	 * 
	 * @return The singleton instance of ShipInspector
	 */
	public static ShipInspector getInstance() {
		return INSPECTOR;
	}

	protected ShipInspector() {
	}

	/**
	 * Reviews the region for ships with problems. 
	 * 
	 * @see magellan.library.tasks.AbstractInspector#reviewRegion(magellan.library.Region, int)
	 */
	public List<AbstractProblem> reviewRegion(Region r, int type) {
		// we notify errors only
		if (type != Problem.ERROR) {
			return new ArrayList<AbstractProblem>();
		}

		// fail fast if prerequisites are not fulfilled
		if ((r == null) || (r.units() == null) || r.units().isEmpty()) {
			return new ArrayList<AbstractProblem>();
		}

		// this inspector is only interested in ships
		if ((r.ships() == null) || r.ships().isEmpty()) {
			return new ArrayList<AbstractProblem>();
		}

		List<AbstractProblem> problems = reviewShips(r);

		if (problems.isEmpty()) {
			return new ArrayList<AbstractProblem>();
		} else {
			return problems;
		}
	}

	private List<AbstractProblem> reviewShips(Region r) {
		List<AbstractProblem> problems = new ArrayList<AbstractProblem>(2);

		for (Iterator iter = r.ships().iterator(); iter.hasNext();) {
			Ship s = (Ship) iter.next();
			problems.addAll(reviewShip(s));
		}

		return problems;
	}

	private List<AbstractProblem> reviewShip(Ship s) {
		List<AbstractProblem> problems = new ArrayList<AbstractProblem>();
		int nominalShipSize = s.getShipType().getMaxSize();

		if (s.getSize() != nominalShipSize) {
			// ship will be built, so dont review ship
			return new ArrayList<AbstractProblem>();
		}

		if (s.modifiedUnits().isEmpty()) {
			problems.add(new CriticizedError(s.getRegion(), s, this,
					Resources.get("magellan.tasks.shipinspector.error.nocrew.description")));
		}

		if (s.getModifiedLoad() > (s.getMaxCapacity())) {
			problems.add(new CriticizedError(s.getRegion(), s, this,
					Resources.get("magellan.tasks.shipinspector.error.overloaded.description")));
		}

		problems.addAll(reviewMovingShip(s));
		return problems;
	}

	private List<AbstractProblem> reviewMovingShip(Ship s) {
		List<AbstractProblem> problems = new ArrayList<AbstractProblem>();
		if (s.getOwnerUnit() == null) {
			return problems;
		}

		List<CoordinateID> modifiedMovement = s.getOwnerUnit().getModifiedMovement();

		if (modifiedMovement.isEmpty()) {
			return problems;
		}

		Iterator movementIterator = modifiedMovement.iterator();
		movementIterator.next();
		CoordinateID nextRegionCoord = (CoordinateID) movementIterator.next();
		Region nextRegion = s.getRegion().getData().getRegion(nextRegionCoord);

		Rules rules = s.getData().rules;
		RegionType ebene = rules.getRegionType("Ebene"), wald = rules.getRegionType("Wald"), ozean = rules
				.getRegionType("Ozean");

		// TODO: We should consider harbours, too. But this is difficult because we don't know if
		// harbour owner is allied with ship owner etc. We better leave it up to the user to decide...
		if (s.getShoreId() != -1) {
			// If ship is shored, it can only move deviate by one from the shore direction and only
			// move to an ocean region
			Direction d = Regions.getDirectionObjectsOfCoordinates(modifiedMovement).get(0);

			if (Math.abs(s.getShoreId() - d.getDir()) > 1 && Math.abs(s.getShoreId() - d.getDir()) < 5) {
				problems.add(new CriticizedError(s.getRegion(), s, this,
						Resources.get("magellan.tasks.shipinspector.error.wrongshore.description")));
				return problems;
			}
			if (!nextRegion.getRegionType().equals(ozean)) {
				problems.add(new CriticizedError(s.getRegion(), s, this,
						Resources.get("magellan.tasks.shipinspector.error.noocean.description")));
				return problems;
			}
			if (movementIterator.hasNext()) {
				nextRegionCoord = (CoordinateID) movementIterator.next();
				nextRegion = s.getRegion().getData().getRegion(nextRegionCoord);
			} else
				nextRegion = null;
		}

		while (nextRegion != null) {
			// if ship is not a boat and on ocean , it can only move to ocean, plain or forest
			if (!(s.getType().equals(rules.getShipType("Boot")) || (nextRegion.getRegionType()
					.equals(ozean)
					|| nextRegion.getRegionType().equals(wald) || nextRegion.getRegionType()
					.equals(ebene)))) {
				problems.add(new CriticizedError(s.getRegion(), s, this,
						Resources.get("magellan.tasks.shipinspector.error.shipwreck.description")));
				return problems;
			}
			if (movementIterator.hasNext()) {
				nextRegionCoord = (CoordinateID) movementIterator.next();
				nextRegion = s.getRegion().getData().getRegion(nextRegionCoord);
			} else
				nextRegion = null;
		}

		return problems;
	}
}
