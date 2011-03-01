package magellan.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.impl.MagellanBuildingImpl;
import magellan.library.impl.MagellanShipImpl;
import magellan.library.impl.SpellBuilder;
import magellan.library.io.GameDataReader;
import magellan.library.rules.BuildingType;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.MagellanFactory;

public class GameDataBuilder {

  private static final int BASE_ROUND = 360;

  public GameData createSimplestGameData() throws Exception {
    return createSimplestGameData(BASE_ROUND);
  }

  /**
   * Creates a GameData object that is always postProcessed with one faction, one island, one
   * region, and one unit
   */
  public GameData createSimplestGameData(int round) throws Exception {
    return createSimplestGameData(round, true);
  }

  /**
   * Creates a GameData object that is always postProcessed with one faction, one island, one
   * region, and (if <code>addUnit</code>) one unit
   */
  public GameData createSimplestGameData(int round, boolean addUnit) throws Exception {
    return createSimplestGameData("Eressea", round, addUnit, true);
  }

  /**
   * Creates a GameData object that is always postProcessed with one faction, one island, one
   * region, and (if <code>addUnit</code>) one unit
   */
  public GameData createSimplestGameData(String gameName, int round, boolean addUnit)
      throws Exception {
    return createSimplestGameData(gameName, round, addUnit, true);
  }

  /**
   * Creates a GameData object with one faction, one island, one region, and (if
   * <code>addUnit</code>) one unit.
   */
  private GameData createSimplestGameData(String gameName, int round, boolean addUnit,
      boolean postProcess) throws Exception {
    final GameData data = new GameDataReader(null).createGameData(gameName);

    data.base = 36;
    // this is sadly needed
    // IDBaseConverter.setBase(data.base);

    data.noSkillPoints = true;

    data.setLocale(Locale.GERMAN);

    final EresseaDate ed = new EresseaDate(round);
    ed.setEpoch(2);
    data.setDate(ed);

    // data.setCurTempID
    // data.mailTo
    // data.mailSubject

    // data.addFaction
    final Faction faction = addFaction(data, "iLja", "Faction_867718", "Meermenschen", 1);

    final Island island = addIsland(data, 1, "Island_1");

    final Region region_0_0 = addRegion(data, "0 0", "Region_0_0", "Gletscher", 1);
    region_0_0.setIsland(island);

    if (addUnit) {
      addUnit(data, "1", "Unit_1", faction, region_0_0);
    }

    if (postProcess) {
      data.postProcess();
    }
    return data;
  }

  /**
   * Creates a GameData object with one unit which has Hiebwaffen 4 (+3), Segeln - (-3), Magie 4,
   * Steinbau -. in round {@link #BASE_ROUND}.
   */
  public GameData createSimpleGameData() throws Exception {
    return createSimpleGameData(BASE_ROUND);
  }

  /**
   * Creates a GameData object with one unit which has Hiebwaffen 4 (+3), Segeln - (-3), Magie 4,
   * Steinbau -.
   */
  public GameData createSimpleGameData(int round) throws Exception {
    return createSimpleGameData(round, true);
  }

  /**
   * Creates a GameData object where all units have Hiebwaffen 4 (+3), Segeln - (-3), Magie 4,
   * Steinbau -. Add a unit if <code>addUnit</code>.
   */
  public GameData createSimpleGameData(int round, boolean addUnit) throws Exception {
    return createSimpleGameData("Eressea", round, addUnit);
  }

  /**
   * Creates a GameData object of the specified type where all units have Hiebwaffen 4 (+3), Segeln
   * - (-3), Magie 4, Steinbau -. Add a unit if <code>addUnit</code>.
   */
  public GameData createSimpleGameData(String gameName, int round, boolean addUnit)
      throws Exception {
    final GameData data = createSimplestGameData(gameName, round, addUnit, false);

    if (data.getUnits().size() > 0) {
      final Unit unit = data.getUnits().iterator().next();

      addSkill(unit, "Hiebwaffen", 4, 3, true); // Hiebwaffen 4 (+3)
      addSkill(unit, "Segeln", -1, -3, false); // Segeln - (-3)
      addSkill(unit, "Magie", 4, 0, true); // Magie 4
      addSkill(unit, "Steinbau", -1, -3, false); // Steinbau -
    }

    data.postProcess();
    return data;
  }

  public Faction addFaction(GameData data, String number, String name, String race, int sortIndex) {
    final EntityID id = EntityID.createEntityID(number, data.base);

    final Faction faction = MagellanFactory.createFaction(id, data);
    data.addFaction(faction);

    faction.setName(name);

    faction.setPassword(name);

    faction.setType(data.rules.getRace(StringID.create(race), true));

    faction.setSortIndex(sortIndex);

    return faction;
  }

  public Island addIsland(GameData data, int number, String name) {
    final IntegerID id = IntegerID.create(number);

    final Island island = MagellanFactory.createIsland(id, data);
    data.addIsland(island);

    island.setName(name);

    return island;
  }

  public Region addRegion(GameData data, String coordinate, String name, String type, int sortIndex) {
    final CoordinateID c = CoordinateID.parse(coordinate, " ");

    final Region region = MagellanFactory.createRegion(c, data);
    data.addRegion(region);

    region.setName(name);

    if (type != null) {
      region.setType(data.rules.getRegionType(StringID.create(type), true));
    }

    region.setSortIndex(sortIndex);
    return region;
  }

  public Unit addUnit(GameData data, String name, Region region) {
    final String number = "g" + (data.getUnits().size() + 1);
    final Faction faction = data.getFactions().iterator().next();
    return addUnit(data, number, name, faction, region);
  }

  public Unit addUnit(GameData data, String number, String name, Faction faction, Region region) {
    final UnitID id = UnitID.createUnitID(number, data.base);

    final Unit unit = MagellanFactory.createUnit(id, data);
    data.addUnit(unit);

    unit.setName(name);

    unit.setFaction(faction);

    unit.setRace(faction.getRace());
    unit.setRealRace(faction.getRace());

    unit.setRegion(region);

    unit.setOrders(Collections.singleton(""));

    return unit;
  }

  public Skill addLostSkill(Unit unit, String name, int level) {
    return addSkill(unit, name, -1, level, true);
  }

  public Skill addSkill(Unit unit, String name, int level) {
    return addSkill(unit, name, level, level, false);
  }

  public Skill addChangedSkill(Unit unit, String name, int level, int fromLevel) {
    return addSkill(unit, name, level, fromLevel, true);
  }

  protected Skill addSkill(Unit unit, String name, int level, int change, boolean changed) {

    final SkillType skt = unit.getData().rules.getSkillType(StringID.create(name), true);
    final int raceBonus = unit.getRace().getSkillBonus(skt);
    final int points = Skill.getPointsAtLevel(level - raceBonus);

    final Skill skill =
        new Skill(skt, points, level, unit.getPersons(), unit.getData().noSkillPoints);

    skill.setChangeLevel(change);

    skill.setLevelChanged(changed);

    unit.addSkill(skill);

    return skill;
  }

  public static Message createMessage(String text) {
    // EINHEITSBOTSCHAFTEN
    // "Eine Botschaft von Kr�uterlager (ax1a): 'MessMach99?99?99!Wundsalbe!xxxx'"
    return MagellanFactory.createMessage(text);
  }

  public Border addRoad(Region region, int id, int direction, int buildRatio) {
    // GRENZE 1
    // "Stra�e";typ
    // 0;richtung
    // 100;prozent
    final Border road = MagellanFactory.createBorder(IntegerID.create(id));

    road.setDirection(direction);
    road.setBuildRatio(buildRatio);
    road.setType("Stra�e");

    region.addBorder(road);

    return road;
  }

  public static void addSpells(GameData data) {
    Unit mage = data.getUnits().iterator().next();
    Map<ID, Spell> spellMap = new HashMap<ID, Spell>();
    IntegerID id = IntegerID.create(999);
    SpellBuilder spell = new SpellBuilder(id, data);
    spell.setName("Hagel");
    spell.setLevel(5);
    spell.setType("combat");
    final Spell hail = spell.construct();
    data.addSpell(hail);
    spellMap.put(id, hail);

    id = IntegerID.create(998);
    spell = new SpellBuilder(id, data);
    spell.setName("Gro�es Fest");
    spell.setLevel(2);
    spell.setType("normal");
    spell.setSyntax("");
    final Spell feast = spell.construct();
    data.addSpell(feast);
    spellMap.put(id, feast);

    id = IntegerID.create(997);
    spell = new SpellBuilder(id, data);
    spell.setName("Schild");
    spell.setLevel(4);
    spell.setType("normal");
    spell.setSyntax("u");
    final Spell shield = spell.construct();
    data.addSpell(shield);
    spellMap.put(id, shield);

    mage.setSpells(spellMap);
  }

  /**
   * Adds the specified item to the unit.
   */
  public void addItem(GameData data, Unit unit, String item, int amount) {
    unit.addItem(new Item(data.rules.getItemType(item, true), amount));
  }

  /**
   * Adds a building to the report.
   * 
   * @param data
   * @param region
   * @param id
   * @param type {@link BuildingType}
   * @param name
   * @param size
   */
  public Building addBuilding(GameData data, Region region, String id, String type, String name,
      int size) {
    Building building = new MagellanBuildingImpl(EntityID.createEntityID(id, data.base), data);
    building.setName(name);
    building.setRegion(region);
    building.setType(data.rules.getBuildingType(type));
    building.setSize(size);

    region.addBuilding(building);
    data.addBuilding(building);

    return building;
  }

  /**
   * Adds a ship to the report.
   * 
   * @param data
   * @param region
   * @param id
   * @param type {@link ShipType}
   * @param name
   * @param size
   */
  public Ship addShip(GameData data, Region region, String id, String type, String name, int size) {
    Ship ship = new MagellanShipImpl(EntityID.createEntityID(id, data.base), data);
    ship.setName(name);
    ship.setRegion(region);
    ship.setType(data.rules.getShipType(type));
    ship.setSize(size);

    region.addShip(ship);
    data.addShip(ship);

    return ship;
  }

  public void setPrices(Region region, String buy) {
    Map<StringID, LuxuryPrice> prices = region.getPrices();
    ItemCategory cat = region.getData().rules.getItemCategory(EresseaConstants.C_LUXURIES);
    if (cat == null)
      throw new IllegalStateException("no luxuries known");
    int pr = 4;
    for (ItemType type : region.getData().getRules().getItemTypes())
      if (type.getCategory() != null && type.getCategory().equals(cat)) {
        if (prices == null) {
          prices = CollectionFactory.<StringID, LuxuryPrice> createSyncOrderedMap(8, .9f);
        }
        if (type.getName().equals(buy)) {
          prices.put(type.getID(), new LuxuryPrice(type, -(pr++)));
        } else {
          prices.put(type.getID(), new LuxuryPrice(type, pr++));
        }
      }
    region.setPrices(prices);
  }

  /**
   * Adds a HELP state from faction to ally. New state is ORed with existing help states.
   */
  public void addAlliance(Faction faction, Faction ally, int state) {
    Map<EntityID, Alliance> allies1 = faction.getAllies();
    if (allies1 == null) {
      allies1 = CollectionFactory.createMap();
      faction.setAllies(allies1);
    }
    Alliance alliance = allies1.get(ally.getID());
    if (alliance == null) {
      alliance = new Alliance(ally);
      allies1.put(ally.getID(), alliance);
    }
    alliance.addState(state);
  }
}
