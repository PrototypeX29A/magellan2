// class magellan.library.gamebinding.AllanonGameSpecificRules
// created on 19.04.2009
//
// Copyright 2003-2009 by magellan project team
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

import magellan.library.Region;
import magellan.library.rules.RegionType;

/**
 * This class implements all Allanon specific rule informations.
 *
 * @author Thoralf Rickert
 * @version 1.0, 19.04.2009
 */
public class AllanonGameSpecificRules implements GameSpecificRules {
  private static GameSpecificRules _instance = null;
  private AllanonGameSpecificRules() {}
  
  public static synchronized GameSpecificRules getInstance() {
    if (_instance == null) _instance = new AllanonGameSpecificRules();
    return _instance;
  }
  
  
  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxWorkers(magellan.library.Region)
   */
  public Integer getMaxWorkers(Region region) {
    if((region.getTrees() != -1) && (region.getSprouts() != -1) && (region.getType() != null)) {
      if((region.getTrees() != -1) && (region.getSprouts() != -1) && (region.getType() != null)) {
        int inhabitants  = ((RegionType)region.getType()).getInhabitants();
        int trees = Math.max(region.getTrees(),0);
        int sprouts = Math.max(region.getSprouts(),0);
        
        return getMaxWorkers(inhabitants, trees, sprouts);
      }
    }
    return null;
  }
  
  /**
   * Max workers is the region specific number of max inhabitants minus 10 places for a tree and 4
   * for a sprout.
   */
  private Integer getMaxWorkers(int inhabitants, int trees, int sprouts) {
    return Math.max(inhabitants,0) - (10 *  Math.max(trees,0)) - (4 *  Math.max(sprouts,0));
  }
  
  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxEntertain(magellan.library.Region)
   */
  public Integer getMaxEntertain(Region region) {
    return getEntertain(((RegionType)region.getType()).getInhabitants(),region.getTrees(),region.getSprouts(),region.getPeasantWage());
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxOldEntertain(magellan.library.Region)
   */
  public Integer getMaxOldEntertain(Region region) {
    return getEntertain(((RegionType)region.getType()).getInhabitants(),region.getOldTrees(),region.getOldSprouts(),region.getPeasantWage());
  }
  
  /**
   * 
   */  
  private Integer getEntertain(int inhabitants, int trees, int sprouts, int wage) {
    int maxWorkers = getMaxWorkers(inhabitants, trees, sprouts);
    
    System.err.println("max workers: "+maxWorkers);
    System.err.println("max entertain: "+(maxWorkers * wage * 0.05));
    
    return (int)Math.round(maxWorkers * wage * 0.05);
  }

}
