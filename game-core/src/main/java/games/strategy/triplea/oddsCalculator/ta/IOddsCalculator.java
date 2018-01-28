package games.strategy.triplea.oddsCalculator.ta;

import java.util.Collection;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.TerritoryEffect;
import games.strategy.engine.data.Unit;

/**
 * Interface to ensure different implementations of the odds calculator all have the same public methods.
 */
public interface IOddsCalculator {
  void setGameData(final GameData data);

  void setCalculateData(final PlayerID attacker, final PlayerID defender, final Territory location,
      final Collection<Unit> attacking, final Collection<Unit> defending, final Collection<Unit> bombarding,
      final Collection<TerritoryEffect> territoryEffects, final int runCount);

  AggregateResults calculate();

  AggregateResults setCalculateDataAndCalculate(final PlayerID attacker, final PlayerID defender,
      final Territory location, final Collection<Unit> attacking, final Collection<Unit> defending,
      final Collection<Unit> bombarding, final Collection<TerritoryEffect> territoryEffects, final int runCount);

  int getRunCount();

  boolean getIsReady();

  void setKeepOneAttackingLandUnit(final boolean bool);

  void setAmphibious(final boolean bool);

  void setRetreatAfterRound(final int value);

  void setRetreatAfterXUnitsLeft(final int value);

  void setRetreatWhenOnlyAirLeft(final boolean value);

  void setAttackerOrderOfLosses(final String attackerOrderOfLosses);

  void setDefenderOrderOfLosses(final String defenderOrderOfLosses);

  void cancel();

  void shutdown();

  int getThreadCount();

  void addOddsCalculatorListener(final OddsCalculatorListener listener);

  // TODO: this method appears to never used.
  void removeOddsCalculatorListener(final OddsCalculatorListener listener);
}
