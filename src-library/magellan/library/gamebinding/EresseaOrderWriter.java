// class magellan.library.gamebinding.EresseaOrderWriter
// created on 17.04.2008
//
// Copyright 2003-2008 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library.gamebinding;

import magellan.library.utils.OrderWriter;

/**
 * @author Thoralf Rickert
 * @version 1.0, 17.04.2008
 */
public class EresseaOrderWriter extends OrderWriter implements GameSpecificOrderWriter {
  private static final EresseaOrderWriter instance = new EresseaOrderWriter();
  /** Current ECheck version */
  public static final String ECHECKVERSION = "4.3.2";

  /**
   * 
   */
  protected EresseaOrderWriter() {
    super();
  }

  /**
   * Returns the instance of this class.
   */
  public static GameSpecificOrderWriter getSingleton() {
    return EresseaOrderWriter.instance;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificOrderWriter#getCheckerName()
   */
  @Override
  public String getCheckerName() {
    return "ECheck";
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificOrderWriter#useChecker()
   */
  @Override
  public boolean useChecker() {
    return true;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificOrderWriter#getCheckerDefaultParameter()
   */
  @Override
  public String getCheckerDefaultParameter() {
    return " -s -l -w4 -v" + EresseaOrderWriter.ECHECKVERSION;
  }

}
