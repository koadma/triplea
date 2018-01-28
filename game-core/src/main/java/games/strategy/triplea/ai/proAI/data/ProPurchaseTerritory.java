package games.strategy.triplea.ai.proAI.data;

import java.util.ArrayList;
import java.util.List;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.triplea.Properties;
import games.strategy.triplea.ai.proAI.util.ProMatches;
import games.strategy.triplea.delegate.Matches;

public class ProPurchaseTerritory {

  private Territory territory;
  private int unitProduction;
  private List<ProPlaceTerritory> canPlaceTerritories;

  public ProPurchaseTerritory(final Territory territory, final GameData data, final PlayerID player,
      final int unitProduction) {
    this(territory, data, player, unitProduction, false);
  }

  /**
   * Create data structure for tracking unit purchase and list of place territories.
   *
   * @param territory - production territory
   * @param data - current game data
   * @param player - AI player who is purchasing
   * @param unitProduction - max unit production for territory
   * @param isBid - true when bid phase, false when normal purchase phase
   */
  public ProPurchaseTerritory(final Territory territory, final GameData data, final PlayerID player,
      final int unitProduction, final boolean isBid) {
    this.territory = territory;
    this.unitProduction = unitProduction;
    canPlaceTerritories = new ArrayList<>();
    canPlaceTerritories.add(new ProPlaceTerritory(territory));
    if (!isBid) {
      if (ProMatches.territoryHasInfraFactoryAndIsNotConqueredOwnedLand(player, data).test(territory)) {
        for (final Territory t : data.getMap().getNeighbors(territory, Matches.territoryIsWater())) {
          if (Properties.getWW2V2(data) || Properties.getUnitPlacementInEnemySeas(data)
              || !t.getUnits().anyMatch(Matches.enemyUnit(player, data))) {
            canPlaceTerritories.add(new ProPlaceTerritory(t));
          }
        }
      }
    }
  }

  public int getRemainingUnitProduction() {
    int remainingUnitProduction = unitProduction;
    for (final ProPlaceTerritory ppt : canPlaceTerritories) {
      remainingUnitProduction -= ppt.getPlaceUnits().size();
    }
    return remainingUnitProduction;
  }

  public Territory getTerritory() {
    return territory;
  }

  @Override
  public String toString() {
    return territory + " | unitProduction=" + unitProduction + " | placeTerritories=" + canPlaceTerritories;
  }

  public void setTerritory(final Territory territory) {
    this.territory = territory;
  }

  public int getUnitProduction() {
    return unitProduction;
  }

  public void setUnitProduction(final int unitProduction) {
    this.unitProduction = unitProduction;
  }

  public List<ProPlaceTerritory> getCanPlaceTerritories() {
    return canPlaceTerritories;
  }

  public void setCanPlaceTerritories(final List<ProPlaceTerritory> canPlaceTerritories) {
    this.canPlaceTerritories = canPlaceTerritories;
  }
}
