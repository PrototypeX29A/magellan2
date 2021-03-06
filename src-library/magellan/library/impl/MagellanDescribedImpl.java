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

package magellan.library.impl;

import magellan.library.Described;
import magellan.library.ID;
import magellan.library.rules.ObjectType;

/**
 * A class representing a uniquely identifiable object with a modifiable name and description.
 */
public abstract class MagellanDescribedImpl extends ObjectType implements Described {
  protected String description;

  /**
   * Constructs a new described object that is uniquely identifiable by the specified id.
   */
  public MagellanDescribedImpl(ID id) {
    super(id);
  }

  /**
   * Sets the description of this object.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the description of this object.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns a copy of this described object.
   * 
   * @throws CloneNotSupportedException DOCUMENT-ME
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
