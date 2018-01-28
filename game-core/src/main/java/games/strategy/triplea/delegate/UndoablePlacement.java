package games.strategy.triplea.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import games.strategy.engine.data.CompositeChange;
import games.strategy.engine.data.GameData;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;
import games.strategy.engine.delegate.IDelegateBridge;
import games.strategy.triplea.delegate.dataObjects.PlacementDescription;
import games.strategy.triplea.formatter.MyFormatter;

/**
 * Contains all the data to describe a placement and to undo it.
 */
public class UndoablePlacement extends AbstractUndoableMove {
  private static final long serialVersionUID = -1493488646587233451L;
  private final Territory m_placeTerritory;
  private Territory m_producerTerritory;

  /**
   * This constructor initializes an UndoablePlacement objects with the passed parameters.
   */
  public UndoablePlacement(final CompositeChange change, final Territory producerTerritory,
      final Territory placeTerritory, final Collection<Unit> units) {
    super(change, units);
    m_placeTerritory = placeTerritory;
    m_producerTerritory = producerTerritory;
  }

  public Territory getProducerTerritory() {
    return m_producerTerritory;
  }

  public void setProducerTerritory(final Territory producerTerritory) {
    m_producerTerritory = producerTerritory;
  }

  public Territory getPlaceTerritory() {
    return m_placeTerritory;
  }

  @Override
  protected final void undoSpecific(final IDelegateBridge bridge) {
    final GameData data = bridge.getData();
    final AbstractPlaceDelegate currentDelegate = (AbstractPlaceDelegate) data.getSequence().getStep().getDelegate();
    final Map<Territory, Collection<Unit>> produced = currentDelegate.getProduced();
    final Collection<Unit> units = produced.get(m_producerTerritory);
    units.removeAll(getUnits());
    if (units.isEmpty()) {
      produced.remove(m_producerTerritory);
    }
    currentDelegate.setProduced(new HashMap<>(produced));
  }

  @Override
  public final String getMoveLabel() {
    if (m_producerTerritory != m_placeTerritory) {
      return m_producerTerritory.getName() + " -> " + m_placeTerritory.getName();
    }
    return m_placeTerritory.getName();
  }

  @Override
  public final Territory getEnd() {
    return m_placeTerritory;
  }

  @Override
  protected final PlacementDescription getDescriptionObject() {
    return new PlacementDescription(m_units, m_placeTerritory);
  }

  @Override
  public String toString() {
    if (m_producerTerritory != m_placeTerritory) {
      return m_producerTerritory.getName() + " produces in " + m_placeTerritory.getName() + ": "
          + MyFormatter.unitsToTextNoOwner(m_units);
    }
    return m_placeTerritory.getName() + ": " + MyFormatter.unitsToTextNoOwner(m_units);
  }
}
