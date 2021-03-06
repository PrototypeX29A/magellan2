// class magellan.library.utils.mapping.RegionNameMapping
// created on 19.05.2008
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
package magellan.library.utils.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.utils.Score;
import magellan.library.utils.logging.Logger;

/**
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 */

public class BuildingIDMapping implements DataMapping {
  private static BuildingIDMapping singleton = new BuildingIDMapping();

  public static BuildingIDMapping getSingleton() {
    return BuildingIDMapping.singleton;
  }

  @Override
  public String toString() {
    return "BuildingID";
  }

  public CoordinateID getMapping(GameData fromData, GameData toData, int level) {
    Collection<Score<CoordinateID>> list = getMappings(fromData, toData, level);
    if (list.isEmpty())
      return null;
    return Collections.max(list).getKey();
  }

  public Collection<Score<CoordinateID>> getMappings(GameData fromData, GameData toData, int level) {
    Map<CoordinateID, Score<CoordinateID>> translationMap =
        new Hashtable<CoordinateID, Score<CoordinateID>>();

    // loop regions in fromData
    for (Region region : fromData.getRegions()) {
      CoordinateID coord = region.getCoordinate();

      if (coord.getZ() == level) {
        for (Building building : region.buildings()) {
          Building sameBuilding = toData.getBuilding(building.getID());
          if (sameBuilding != null) {
            Region sameRegion = sameBuilding.getRegion();
            if (sameRegion != null) {
              CoordinateID foundCoord = sameRegion.getCoordinate();
              CoordinateID translation =
                  CoordinateID.create(foundCoord.getX() - coord.getX(), foundCoord.getY()
                      - coord.getY());

              Score<CoordinateID> score = translationMap.get(translation);
              if (score == null) {
                score = new Score<CoordinateID>(translation);
                translationMap.put(translation, score);
              }
              score.addScore(1);
            }
          }
        }
      }
    }

    for (Score<CoordinateID> val : translationMap.values()) {
      Logger.getInstance(this.getClass()).finest("translation (" + toString() + "): " + val);
    }

    return translationMap.values();
  }
}
