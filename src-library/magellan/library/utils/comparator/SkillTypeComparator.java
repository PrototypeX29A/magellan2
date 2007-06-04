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

package magellan.library.utils.comparator;

import java.util.Comparator;

import magellan.library.Skill;
import magellan.library.rules.SkillType;


/**
 * A comparator imposing an ordering on Skill objects by comparing their types.
 * 
 * <p>
 * Note: this comparator can impose orderings that are inconsistent with equals.
 * </p>
 * 
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared objects have the
 * same type and they would be regarded as equal by this comparator, instead of 0 the result of
 * the sub-comparator's comparison is returned.
 * </p>
 *
 * @author Ulrich Küster
 */
public class SkillTypeComparator implements Comparator {
	private final Comparator typeCmp;
	private final Comparator subCmp;

	/**
	 * Creates a new SkillTypeComparator object.
	 *
	 * @param typeComparator used to compare the types of skills.
	 * @param subComparator if the typeComparator's comparison of the skill types yields 0, this
	 * 		  sub-comparator is applied to the skill objects if it is not <tt>null</tt>.
	 */
	public SkillTypeComparator(Comparator typeComparator, Comparator subComparator) {
		this.typeCmp = typeComparator;
		this.subCmp = subComparator;
	}

	/**
	 * Compares its two arguments for order according to their types.
	 *
	 * @param o1 an instance of class Skill.
	 * @param o2 an instance of class Skill.
	 *
	 * @return the result of the type comparator's comparison of the skill object types. If this
	 * 		   result is 0 and a subcomparator is specified that subcomparator is applied on the
	 * 		   skill objects.
	 */
	public int compare(Object o1, Object o2) {
		int retVal = 0;

		SkillType s1 = ((Skill) o1).getSkillType();
		SkillType s2 = ((Skill) o2).getSkillType();

		retVal = typeCmp.compare(s1, s2);

		if((retVal == 0) && (subCmp != null)) {
			retVal = subCmp.compare(o1, o2);
		}

		return retVal;
	}
}
