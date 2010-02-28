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

package magellan.library.gamebinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.Region.Visibility;
import magellan.library.rules.BuildingType;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.utils.Direction;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Regions;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 345 $
 */
public class EresseaPostProcessor {
  private static final Logger log = Logger.getInstance(EresseaPostProcessor.class);

  protected EresseaPostProcessor() {
  }

  private static final EresseaPostProcessor singleton = new EresseaPostProcessor();

  /**
   * DOCUMENT-ME
   */
  public static EresseaPostProcessor getSingleton() {
    return EresseaPostProcessor.singleton;
  }

  /**
   * This method tries to fix some issues that arise right after reading a report file. It scans
   * messages for herbs, removes dummy units, creates temp units and tries to detect if resources
   * should be set to zero because they are not in the report.
   * 
   * @param data
   */
  public void postProcess(GameData data) {
    if (data == null)
      throw new NullPointerException();

    resolveWraparound(data);

    cleanAstralSchemes(data);

    postProcessMessages(data);

    /*
     * retrieve the temp units mentioned in the orders and create them as TempUnit objects
     */
    int sortIndex = 0;
    List<Unit> sortedUnits = new LinkedList<Unit>(data.getUnits());
    Collections.sort(sortedUnits, new SortIndexComparator<Unit>(IDComparator.DEFAULT));

    for (Unit unit : sortedUnits) {
      unit.setSortIndex(sortIndex++);
      sortIndex = unit.extractTempUnits(data, sortIndex);
    }

    /*
     * 'known' information does not necessarily show up in the report. e.g. depleted region
     * resources are not mentioned although we actually know that the resource is available with an
     * amount of 0. Resolve this ambiguity here:
     */
    if ((data != null) && (data.getRegions() != null)) {
      /* ItemType sproutResourceID = */data.rules.getItemType("Sch��linge", true);
      /* ItemType treeResourceID = */data.rules.getItemType("B�ume", true);
      /* ItemType mallornSproutResourceID = */data.rules.getItemType("Mallornsch��linge", true);
      /* ItemType mallornTreeResourceID = */data.rules.getItemType("Mallorn", true);

      for (Region region : data.getRegions()) {
        /*
         * first determine whether we know everything about this region
         */
        if (region.getVisibility() == Visibility.UNIT) {
          /*
           * now patch as much missing information as possible
           */
          // FIXME (stm) 2006-10-28: this has bitten us already
          // check what is visible in what visibility
          // (lighthouse, neigbbour, travel)

          // the following tags seem to be present under undefined visibility
          // even if they are zero (but only if region is not an ocean):
          // Bauern, Silber, Unterh, Rekruten, Pferde, (Lohn)
          {
            if (region.getPeasants() < 0) {
              region.setPeasants(0);
            }

            if (region.getSilver() < 0) {
              region.setSilver(0);
            }

            if (region.getWage() < 0) {
              // TODO: should we set this to 10 instead?
              region.setWage(0);
            }

            if (region.getHorses() < 0) {
              region.setHorses(0);
            }
          }

          if (region.getSprouts() < 0) {
            region.setSprouts(0);
          }
          if (region.getTrees() < 0) {
            region.setTrees(0);
          }
        }
      }
    }
  }

  private void postProcessMessages(GameData data) {
    /* scan the messages for additional information */
    if (data.getFactions() != null) {
      for (Faction faction : data.getFactions()) {

        if (faction.getMessages() != null) {
          for (Message message : faction.getMessages()) {

            if (message.getMessageType() != null) {
              switch (((message.getMessageType().getID()).intValue())) {
              case 1511758069: // xyz "findet 5 Schneekristalle"
              case 18362:
              case 861989530: // xyz kann keine Kr�uter finden
                // a herb was found in a region
              case 1349776898: // xyz stellt fest, dass es hier viele Schneekristalle gibt
                // a certain amount of herbs has been detected in a region
                if ((message.getAttributes() != null)
                    && message.getAttributes().containsKey("region")) {
                  String str = message.getAttributes().get("region");
                  CoordinateID coord = CoordinateID.parse(str, ",");

                  if (coord == null) {
                    coord = CoordinateID.parse(str, " ");
                  }

                  Region r = data.getRegion(coord);

                  if (r != null) {
                    String value = message.getAttributes().get("herb");

                    if (value != null) {
                      ItemType type = data.rules.getItemType(StringID.create(value), true);
                      r.setHerb(type);
                    }

                    if (((message.getMessageType().getID()).intValue()) == 1349776898) {
                      // a certain amount of herbs has been detected in a region
                      String amount = message.getAttributes().get("amount");

                      if (amount != null) {
                        r.setHerbAmount(amount);
                      }
                    } else if (((message.getMessageType().getID()).intValue()) == 861989530) {
                      // no herbs at all
                      r.setHerbAmount("keine");
                    }
                  }
                }

                break;
              case 865172808: // X gelang es, folgende Informationen �ber Y herauszufinden
                // fix spy messages which lack the spy attribute (see Magellan bug #333 and Eressea
                // bug #1604)
                Unit spy = null;
                Unit target = null;
                String spyId = message.getAttributes().get("spy");
                if (spyId != null) {
                  spy = data.getUnit(UnitID.createUnitID(spyId, 10, data.base));
                  String targetId = message.getAttributes().get("target");
                  if (targetId != null) {
                    target = data.getUnit(UnitID.createUnitID(targetId, 10, data.base));
                    if (spy == null || target == null || spy.getFaction() == null) {
                      EresseaPostProcessor.log
                          .warn("spy message without spy or target: " + message);
                    } else {
                      for (Message msg2 : spy.getFaction().getMessages()) {
                        if (targetId.equals(msg2.getAttributes().get("target"))) {
                          if (msg2.getAttributes().get("spy") != null) {
                            if (!spyId.equals(msg2.getAttributes().get("spy"))) {
                              EresseaPostProcessor.log.warn("message " + message.getID()
                                  + " seems to belong to " + msg2.getAttributes().get("spy")
                                  + " and " + spyId);
                            }
                          } else {
                            switch (((msg2.getMessageType().getID()).intValue())) {
                            case 387085007: // Y geh�rt der Partei F an
                            case 467205397: // Y beherrscht ...
                            case 743495578: // Im Gep�ck von Y sind ...
                              msg2.getAttributes().put("spy",
                                  String.valueOf(((IntegerID) spy.getID()).intValue()));
                              break;
                            }
                          }
                        }
                      }
                    }
                  }
                }
                break;
              }
            }
          }
        }
      }
    }
  }

  private void resolveWraparound(GameData data) {
    if (data.getRegions().size() == 0)
      return;

    Map<Long, Region> idMap = null;
    idMap = setUpIDMap(data);

    // find all regions with "wrap" as visibility string, update their neighbors and make themselves
    // wrapping regions
    Map<Region, Region> toDelete = new LinkedHashMap<Region, Region>();
    for (Region wrappingRegion : data.getRegions()) {
      if (Region.VIS_STR_WRAP.equals(wrappingRegion.getVisibilityString())) {
        if (idMap == null || idMap.isEmpty()) {
          log.error("wrapping region found, but no region IDs");
          return;
        }
        Region original = idMap.get(wrappingRegion.getUID());
        if (idMap.get(wrappingRegion.getUID()) == null) {
          log.warn("wrapping region without actual region");
          continue;
        }
        toDelete.put(wrappingRegion, original);
        Map<Direction, Region> neighbors =
            Regions.getCoordinateNeighbours(data.regions(), wrappingRegion.getCoordinate());
        for (Direction d : neighbors.keySet()) {
          neighbors.get(d).addNeighbor(d.add(3), idMap.get(wrappingRegion.getUID()));
        }
      }
    }
    for (Region r : toDelete.keySet()) {
      data.makeWrapper(r, toDelete.get(r));
    }
    toDelete.clear();
    for (Region curRegion : data.getRegions()) {
      for (Direction d : curRegion.getNeighbors().keySet()) {
        Region neighbor = curRegion.getNeighbors().get(d);
        if (Regions.getDist(neighbor.getCoordinate(), curRegion.getCoordinate()) > 1) {
          CoordinateID wrapperID = curRegion.getID().translateInLayer(d.toCoordinate());
          if (!data.wrappers().containsKey(wrapperID)) {
            if (data.getRegion(wrapperID) != null) {
              log.warn(neighbor + " should be connected by a wrapper to " + curRegion
                  + " but there is already a region at " + wrapperID);
            } else {
              Region wrapper = MagellanFactory.createWrapper(wrapperID, neighbor.getUID(), data);
              log.finest(neighbor + " should be connected by a wrapper to " + curRegion);
              toDelete.put(wrapper, neighbor);
            }
          }
        }
      }
    }

    for (Region r : toDelete.keySet()) {
      data.makeWrapper(r, toDelete.get(r));
    }
  }

  private Map<Long, Region> setUpIDMap(GameData data) {
    Map<Long, Region> result = new HashMap<Long, Region>(data.getRegions().size() * 5 / 4 + 5, .8f);
    // for each ID in the report, put at least one into the map. Prefer the one with lowest distance
    for (Region r : data.getRegions()) {
      if (r.hasUID()) {
        Region old = result.get(r.getUID());
        if (old == null || !Region.VIS_STR_WRAP.equals(r.getVisibilityString())) {
          result.put(r.getUID(), r);
        }
      }
    }
    return result;
  }

  /**
   * DOCUMENT-ME
   */
  public void postProcessAfterTrustlevelChange(GameData data) {
    // initialize fog-of-war cache (FIXME(pavkovic): Do it always?)
    // clear all fog-of-war caches
    if (data.getRegions() != null) {
      for (Region r : data.getRegions()) {
        r.setFogOfWar(-1);
      }
    }

    // intialize the fog-of-war cache for all regions that are covered by lighthouses
    if (data.getBuildings() != null) {
      BuildingType type = data.rules.getBuildingType(EresseaConstants.B_LIGHTHOUSE);
      RegionType oceanType = data.rules.getRegionType(EresseaConstants.RT_OCEAN);
      Comparator<Unit> sortIndexComparator = new SortIndexComparator<Unit>(IDComparator.DEFAULT);

      if (type != null) {
        for (Building b : data.getBuildings()) {
          if (type.equals(b.getType()) && (b.getSize() >= 10)) {
            int personCounter = 0;
            int perceptionSkillLevel = 0;
            List<Unit> sortedInmates = new LinkedList<Unit>(b.units());
            Collections.sort(sortedInmates, sortIndexComparator);

            for (Iterator<Unit> inmates = sortedInmates.iterator(); inmates.hasNext()
                && (personCounter < 4); personCounter++) {
              Unit inmate = inmates.next();
              Skill perceptionSkill =
                  inmate.getSkill(data.rules.getSkillType(EresseaConstants.S_WAHRNEHMUNG, true));

              if (perceptionSkill != null) {
                perceptionSkillLevel = Math.max(perceptionSkill.getLevel(), perceptionSkillLevel);
              }
            }

            int maxRadius = (int) Math.min(Math.log10(b.getSize()) + 1, perceptionSkillLevel / 3);

            if (maxRadius > 0) {
              // FIXME(stm) getAllNeighbours misst die Pfadl�nge, wir brauchen aber alle Regionen
              // mit dem richtigen Abstand der Koordinaten.
              Map<CoordinateID, Region> regions =
                  Regions.getAllNeighbours(data.regions(), b.getRegion().getCoordinate(),
                      maxRadius, null);

              for (Region r : regions.values()) {
                if ((oceanType == null) || oceanType.equals(r.getType())) {
                  if (r.getVisibility().greaterEqual(Visibility.LIGHTHOUSE)) {
                    r.setFogOfWar(0);
                  }
                }
              }
            }
          }
        }
      }
    }

    // intialize the fog-of-war cache for all regions where units or ships traveled through
    for (Region r : data.getRegions()) {
      if (r.getTravelThru() != null) {
        initTravelThru(data, r, r.getTravelThru());
      }

      if (r.getTravelThruShips() != null) {
        initTravelThru(data, r, r.getTravelThruShips());
      }
    }
  }

  private void initTravelThru(GameData data, Region region, Collection<Message> travelThru) {
    for (Message mes : travelThru) {
      // fetch ID of Unit or Ship from Message of type "<name> (<id>)"
      String s = mes.getText();
      int startpos = s.lastIndexOf("(") + 1;
      int endpos = s.length() - 1;

      if ((startpos > -1) && (endpos > startpos)) {
        try {
          // message text always use the report base
          ID id = EntityID.createEntityID(s.substring(startpos, endpos), data.base);

          if ((data.getUnit(id) != null) && (data.getUnit(id).getFaction().isPrivileged())) {
            // fast return
            region.setFogOfWar(0);

            return;
          } else {
            Ship ship = data.getShip(id);

            if (ship != null) {
              for (Unit unit : ship.units()) {
                if ((unit).getFaction().isPrivileged()) {
                  // fast return
                  region.setFogOfWar(0);

                  return;
                }
              }
            }
          }
        } catch (NumberFormatException e) {
        }
      }
    }
  }

  /**
   * Deletes the schemes of astral regions in the case one of the following inconsistencies is
   * detected.<br />
   * 1. more than 19 schemes per astral region<br />
   * 2. the region with the scheme coordinates has terrain ocean or firewall<br />
   * 3. layout, i.e. two scheme of the same astral region with a distance of >4<br />
   * 4. a scheme is seen from more than 2 astral regions. Only the scheme of the affected astral
   * regions are deleted the others will remain to allow a mapping of astral to real space.<br />
   * Not implemented:<br />
   * 5. global layout (similar algorithm as layout but normalizing all schemes into the area of one
   * astral region before) -> nearly impossible to determine the wrong schemes<br />
   * 6. different scheme/region name of same coordinate -> may not be an wrong scheme, only outdated
   * name.
   * 
   * @param gd
   */
  private void cleanAstralSchemes(GameData gd) {
    gd.rules.getRegionType(EresseaConstants.RT_FIREWALL);
    Map<CoordinateID, Collection<Region>> schemeMap =
        new HashMap<CoordinateID, Collection<Region>>();
    for (Region region : gd.getRegions()) {
      if ((region.getCoordinate().getZ() == 1) && (region.schemes().size() > 0)) {
        // Check 1. (number)
        if (region.schemes().size() > 19) {
          // Inconsistency of type 1. found
          EresseaPostProcessor.log
              .warn("EresseaPostProcessor: Astral schemes inconsistency type: too many schemes");
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
          continue;
        }
        // Check 2. (terrain) and 3. (extension)
        boolean inconsistent = false;
        CoordinateID.Triplet min = null;
        CoordinateID.Triplet max = null;

        for (Scheme scheme : region.schemes()) {
          CoordinateID schemeID = scheme.getCoordinate();
          Region schemeRegion = gd.getRegion(schemeID);
          // Check 2. (terrain)
          if (schemeRegion != null) {
            RegionType rt = schemeRegion.getRegionType();
            if (!rt.isAstralVisible() && !rt.equals(RegionType.unknown)) {
              inconsistent = true;
              break;
            }
          }
          // Check 3. (extension)
          if (min == null || max == null) {
            min = schemeID.toTriplet();
            max = schemeID.toTriplet();
            min.z = max.z = schemeID.getX() + schemeID.getY();
          } else {
            min.x = Math.min(min.x, schemeID.getX());
            min.y = Math.min(min.y, schemeID.getY());
            min.z = Math.min(min.z, schemeID.getX() + schemeID.getY());
            max.x = Math.max(max.x, schemeID.getX());
            max.y = Math.max(max.y, schemeID.getY());
            max.z = Math.max(max.z, schemeID.getX() + schemeID.getY());
          }
        }
        if (inconsistent) {
          // Inconsistency of type 2. found
          EresseaPostProcessor.log
              .warn("EresseaPostProcessor: Astral schemes inconsistency type: terrain");
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
          continue;
        }
        boolean centerFound = false;
        for (int x = max.x - 2; (x <= min.x + 2) && !centerFound; x++) {
          for (int y = max.y - 2; (y <= min.y + 2) && !centerFound; y++) {
            if ((max.z - 2 <= x + y) && (x + y <= min.z + 2)) {
              centerFound = true;
            }
          }
        }
        if (!centerFound) {
          // Inconsistency of type 3. found
          EresseaPostProcessor.log
              .warn("EresseaPostProcessor: Astral schemes inconsistency type: layout");
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
          continue;
        }
        /*
         * none of the other inconsistency checks fail. therefore we can check for type 4 now. (this
         * check eleminates the schemes from all astral regions having the scheme. Therefore
         * eleminating other inconsistencies first allow to probably save some right schemes from
         * beeing deleted) We only prepare the a map here that maps schemeIDs to regions
         */
        for (Scheme scheme : region.schemes()) {
          Collection<Region> regionCol = schemeMap.get(scheme.getCoordinate());
          if (regionCol == null) {
            regionCol = new ArrayList<Region>(2);
            schemeMap.put(scheme.getCoordinate(), regionCol);
          }
          regionCol.add(region);
        }
      }
    }
    for (Collection<Region> regionCol : schemeMap.values()) {
      if (regionCol.size() > 2) {
        // Inconsistency of type 4. found
        EresseaPostProcessor.log
            .warn("EresseaPostProcessor: Astral schemes inconsistency type: scheme too often");
        for (Region region : regionCol) {
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
        }
      }
    }
  }
  // private void postProcessMessages(GameData data) {
  // herb information from FIND HERBS/FORSCHE KR�UTER or MAKE HERBS/MACHE KR�UTER
  // item information from SHOW/ZEIGE
  // race information from SHOW/ZEIGE
  // unit information from SPY/SPIONIERE
  // }
}
