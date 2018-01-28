package games.strategy.triplea.oddsCalculator.ta;

import static games.strategy.triplea.delegate.GameDataTestUtil.americans;
import static games.strategy.triplea.delegate.GameDataTestUtil.germans;
import static games.strategy.triplea.delegate.GameDataTestUtil.submarine;
import static games.strategy.triplea.delegate.GameDataTestUtil.territory;
import static games.strategy.triplea.delegate.GameDataTestUtil.transport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;
import games.strategy.triplea.delegate.GameDataTestUtil;
import games.strategy.triplea.delegate.TerritoryEffectHelper;
import games.strategy.triplea.xml.TestMapGameData;

public class OddsCalculatorTest {
  private GameData gameData;

  @BeforeEach
  public void setUp() throws Exception {
    gameData = TestMapGameData.REVISED.getGameData();
  }

  @Test
  public void testUnbalancedFight() {
    final Territory germany = gameData.getMap().getTerritory("Germany");
    final Collection<Unit> defendingUnits = new ArrayList<>(germany.getUnits().getUnits());
    final PlayerID russians = GameDataTestUtil.russians(gameData);
    final PlayerID germans = GameDataTestUtil.germans(gameData);
    final List<Unit> attackingUnits = GameDataTestUtil.infantry(gameData).create(100, russians);
    final List<Unit> bombardingUnits = Collections.emptyList();
    final IOddsCalculator calculator = new OddsCalculator(gameData);
    final AggregateResults results = calculator.setCalculateDataAndCalculate(russians, germans, germany, attackingUnits,
        defendingUnits, bombardingUnits, TerritoryEffectHelper.getEffects(germany), 200);
    calculator.shutdown();
    assertTrue(results.getAttackerWinPercent() > 0.99);
    assertTrue(results.getDefenderWinPercent() < 0.1);
    assertTrue(results.getDrawPercent() < 0.1);
  }

  @Test
  public void testKeepOneAttackingLand() {
    // 1 bomber and 1 infantry attacking
    // 1 fighter
    // if one attacking inf must live, the odds
    // much worse
    final PlayerID germans = GameDataTestUtil.germans(gameData);
    final PlayerID british = GameDataTestUtil.british(gameData);
    final Territory eastCanada = gameData.getMap().getTerritory("Eastern Canada");
    final List<Unit> defendingUnits = GameDataTestUtil.fighter(gameData).create(1, british, false);
    final List<Unit> attackingUnits = GameDataTestUtil.infantry(gameData).create(1, germans, false);
    attackingUnits.addAll(GameDataTestUtil.bomber(gameData).create(1, germans, false));
    final List<Unit> bombardingUnits = Collections.emptyList();
    final IOddsCalculator calculator = new OddsCalculator(gameData);
    calculator.setKeepOneAttackingLandUnit(true);
    final AggregateResults results = calculator.setCalculateDataAndCalculate(germans, british, eastCanada,
        attackingUnits, defendingUnits, bombardingUnits, TerritoryEffectHelper.getEffects(eastCanada), 1000);
    calculator.shutdown();
    assertEquals(0.8, results.getAttackerWinPercent(), 0.10);
    assertEquals(0.16, results.getDefenderWinPercent(), 0.10);
  }

  @Test
  public void testAttackingTransports() {
    final Territory sz1 = territory("1 Sea Zone", gameData);
    final List<Unit> attacking = transport(gameData).create(2, americans(gameData));
    final List<Unit> defending = submarine(gameData).create(2, germans(gameData));
    final IOddsCalculator calculator = new OddsCalculator(gameData);
    calculator.setKeepOneAttackingLandUnit(false);
    final AggregateResults results = calculator.setCalculateDataAndCalculate(americans(gameData), germans(gameData),
        sz1, attacking, defending, Collections.emptyList(), TerritoryEffectHelper.getEffects(sz1), 1);
    calculator.shutdown();
    assertEquals(results.getAttackerWinPercent(), 0.0);
    assertEquals(results.getDefenderWinPercent(), 1.0);
  }

  @Test
  public void testDefendingTransports() throws Exception {
    // use v3 rule set
    gameData = TestMapGameData.WW2V3_1942.getGameData();
    final Territory sz1 = territory("1 Sea Zone", gameData);
    final List<Unit> attacking = submarine(gameData).create(2, americans(gameData));
    final List<Unit> defending = transport(gameData).create(2, germans(gameData));
    final IOddsCalculator calculator = new OddsCalculator(gameData);
    calculator.setKeepOneAttackingLandUnit(false);
    final AggregateResults results = calculator.setCalculateDataAndCalculate(americans(gameData), germans(gameData),
        sz1, attacking, defending, Collections.emptyList(), TerritoryEffectHelper.getEffects(sz1), 1);
    calculator.shutdown();
    assertEquals(results.getAttackerWinPercent(), 1.0);
    assertEquals(results.getDefenderWinPercent(), 0.0);
  }
}
