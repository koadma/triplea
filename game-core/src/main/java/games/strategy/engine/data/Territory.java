package games.strategy.engine.data;

public class Territory extends NamedAttachable implements NamedUnitHolder, Comparable<Territory> {
  private static final long serialVersionUID = -6390555051736721082L;
  private final boolean m_water;
  private PlayerID m_owner = PlayerID.NULL_PLAYERID;
  private final UnitCollection m_units;
  // In a grid-based game, stores the coordinate of the Territory
  @SuppressWarnings("unused")
  private final int[] m_coordinate;

  public Territory(final String name, final GameData data) {
    this(name, false, data);
  }

  /** Creates new Territory. */
  public Territory(final String name, final boolean water, final GameData data) {
    super(name, data);
    m_water = water;
    m_units = new UnitCollection(this, getData());
    m_coordinate = null;
  }

  /** Creates new Territory. */
  public Territory(final String name, final boolean water, final GameData data, final int... coordinate) {
    super(name, data);
    m_water = water;
    m_units = new UnitCollection(this, getData());
    if (data.getMap().isCoordinateValid(coordinate)) {
      m_coordinate = coordinate;
    } else {
      throw new IllegalArgumentException("Invalid coordinate: " + coordinate[0] + "," + coordinate[1]);
    }
  }

  public boolean isWater() {
    return m_water;
  }

  /**
   * @return The territory owner; will be {@link #NULL_PLAYERID} if the territory is not owned.
   */
  public PlayerID getOwner() {
    return m_owner;
  }

  public void setOwner(PlayerID newOwner) {
    if (newOwner == null) {
      newOwner = PlayerID.NULL_PLAYERID;
    }
    m_owner = newOwner;
    getData().notifyTerritoryOwnerChanged(this);
  }

  /**
   * Get the units in this territory.
   */
  @Override
  public UnitCollection getUnits() {
    return m_units;
  }

  /**
   * refers to unit holder being changed.
   */
  @Override
  public void notifyChanged() {
    getData().notifyTerritoryUnitsChanged(this);
  }

  /**
   * refers to attachment changing, and therefore needing a redraw on the map in case something like the production
   * number is now different.
   */
  public void notifyAttachmentChanged() {
    getData().notifyTerritoryAttachmentChanged(this);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int compareTo(final Territory o) {
    return getName().compareTo(o.getName());
  }

  @Override
  public String getType() {
    return UnitHolder.TERRITORY;
  }
}
