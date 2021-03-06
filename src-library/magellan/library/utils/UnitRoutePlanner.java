/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.utils;

import java.awt.Component;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.rules.RegionType;
import magellan.library.utils.RoutePlanner.Costs;
import magellan.library.utils.guiwrapper.RoutingDialogData;
import magellan.library.utils.guiwrapper.RoutingDialogDataPicker;

/**
 * Handles user input for changing unit movement orders.
 *
 * @author Andreas
 * @author stm
 */
public class UnitRoutePlanner {

  private static int getModifiedRadius(Unit unit, boolean onRoad) {
    return unit.getData().getGameSpecificStuff().getMovementEvaluator().getModifiedRadius(unit,
        onRoad);
  }

  /**
   * Returns <code>true</code> if this unit can move at all and is of a privileged faction.
   */
  public static boolean canPlan(Unit unit) {
    if (unit.getRegion() == null)
      return false;

    if (UnitRoutePlanner.getModifiedRadius(unit, false) < 1)
      return false;

    if (unit.getFaction() != null)
      return unit.getFaction().isPrivileged();

    return false;
  }

  /**
   * Shows a dialog that lets the user select a destination for the unit and adds according movement
   * orders to all provided units.
   *
   * @param unit This unit is used as a reference for determining speed
   * @param data GameData containing the units
   * @param ui The Component that is used as parent for the Dialog.
   * @param otherUnits The created orders will also be added to these units.
   * @param picker The user dialog that will be displayed
   * @return <code>true</code> if orders were changed (i.e., the user didn't abort and a path existed)
   */
  public boolean planUnitRoute(Unit unit, GameData data, Component ui, Collection<Unit> otherUnits,
      RoutingDialogDataPicker picker) {
    // check for island regions
    Region start = unit.getRegion();
    Collection<Region> island = new LinkedList<Region>();

    if (start.getIsland() != null) {
      island.addAll(start.getIsland().regions());
      island.remove(start);
    } else {
      Map<CoordinateID, ? extends Region> m = Islands.getIsland(start);

      if (m != null) {
        island.addAll(m.values());
        island.remove(start);
      }
    }

    if (island.size() == 0)
      return false;

    picker.initialize(data, island, true);
    // get the data:
    RoutingDialogData options = picker.showRoutingDialog();

    if (options != null) {
      List<String> orders =
          getOrders(unit, data, start.getCoordinate(), options.getDestination(), ui, options
              .useRange(), options.getMode(), options.useVorlage());

      OrderChanger changer = getOrderChanger(unit);
      changer.setLongOrders(unit, orders, options.replaceOrders());

      // change other units' orders
      if (otherUnits != null) {
        for (Unit u : otherUnits) {
          if (!u.equals(unit)) {
            changer.setLongOrders(u, orders, options.replaceOrders());
          }
        }
      }
      return true;
    }

    return false;
  }

  private OrderChanger getOrderChanger(Unit u) {
    return u.getData().getGameSpecificStuff().getOrderChanger();
  }

  /**
   * Creates movement orders for a unit.
   *
   * @param unit The unit for which a route is planned
   * @param data GameData containing the units
   * @param start The region where to start, not necessarily equal to the ship's region
   * @param destination The target region
   * @param ui The parent component for message panes
   * @param useRange If this is <code>true</code>, the orders are split into multiple orders, so that
   *          the ship's range is not exceeded.
   * @param mode a combination of {@link RoutePlanner#MODE_CONTINUOUS},
   *          {@link RoutePlanner#MODE_RETURN}, {@link RoutePlanner#MODE_STOP}
   * @param useVorlage If this is <code>true</code>, Vorlage meta commands are produced.
   * @return The list of new orders.
   */
  public List<String> getOrders(Unit unit, GameData data, CoordinateID start,
      CoordinateID destination, Component ui, boolean useRange, int mode, boolean useVorlage) {
    // find a path
    Map<ID, RegionType> excludeMap = Regions.getNonLandRegionTypes(data.getRules());

    int speed = Math.max(1, UnitRoutePlanner.getModifiedRadius(unit, false));
    int speedRoad = Math.max(1, UnitRoutePlanner.getModifiedRadius(unit, true));
    List<Region> path = Regions.getLandPath(data, start, destination, excludeMap, speed, speedRoad);

    if (path == null || path.size() <= 1) {
      if (ui != null) {
        // No path could be found from start to destination region.
        JOptionPane.showMessageDialog(ui, Resources
            .get("util.unitrouteplanner.msg.nopathfound.text"), Resources
                .get("util.unitrouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
      }
      return null;
    }

    // optionally find a return path
    List<Region> returnPath = null;
    if ((mode & RoutePlanner.MODE_RETURN) > 0) {
      returnPath = Regions.getLandPath(data, destination, start, excludeMap, speed, speedRoad);
      path.addAll(returnPath);
    }

    // adjust the cost function used for movement
    Costs costs = new LandCosts(unit);
    if (!useRange) {
      costs = RoutePlanner.ZERO_COSTS;
    } else if (speed <= 0) {
      // couldn't determine shiprange
      JOptionPane.showMessageDialog(ui, Resources
          .get("util.unitrouteplanner.msg.unitrangeiszero.text"), Resources
              .get("util.unitrouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
      costs = RoutePlanner.ZERO_COSTS;
    }

    // create orders
    List<String> orders = new LinkedList<String>();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);

    return orders;
  }

  /**
   * Cost function for overland movement. Accounts for roads and the movement range of the unit
   * provided in the constructor.
   *
   * @author stm
   */
  public static class LandCosts implements RoutePlanner.Costs {

    private Unit unit;

    /**
     * Creates costs for a unit on land.
     *
     * @param unit
     */
    public LandCosts(Unit unit) {
      this.unit = unit;
    }

    public boolean isExhausted(LinkedList<Region> curPath) {
      Region lastR = null;
      boolean onlyStreets = true;
      for (Region r : curPath) {
        if (lastR != null) {
          if (!Regions.isCompleteRoadConnection(lastR, r)) {
            onlyStreets = false;
          }
        }
        lastR = r;
      }
      return curPath.size() - 1 > Math.max(1, UnitRoutePlanner.getModifiedRadius(unit, onlyStreets));
    }
  }

}
