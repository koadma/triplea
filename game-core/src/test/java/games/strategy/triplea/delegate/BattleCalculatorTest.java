package games.strategy.triplea.delegate;

import static games.strategy.triplea.delegate.GameDataTestUtil.bomber;
import static games.strategy.triplea.delegate.GameDataTestUtil.british;
import static games.strategy.triplea.delegate.GameDataTestUtil.fighter;
import static games.strategy.triplea.delegate.GameDataTestUtil.germans;
import static games.strategy.triplea.delegate.GameDataTestUtil.getDelegateBridge;
import static games.strategy.triplea.delegate.GameDataTestUtil.makeGameLowLuck;
import static games.strategy.triplea.delegate.GameDataTestUtil.setSelectAaCasualties;
import static games.strategy.triplea.delegate.GameDataTestUtil.territory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.ITestDelegateBridge;
import games.strategy.engine.data.Unit;
import games.strategy.engine.random.ScriptedRandomSource;
import games.strategy.triplea.TripleAUnit;
import games.strategy.triplea.attachments.UnitAttachment;
import games.strategy.triplea.delegate.dataObjects.CasualtyDetails;
import games.strategy.triplea.player.ITripleAPlayer;
import games.strategy.triplea.xml.TestMapGameData;
import games.strategy.util.CollectionUtils;

public class BattleCalculatorTest {
  private ITestDelegateBridge bridge;
  private final ITripleAPlayer dummyPlayer = mock(ITripleAPlayer.class);

  @BeforeEach
  public void setUp() throws Exception {
    final GameData data = TestMapGameData.REVISED.getGameData();
    bridge = getDelegateBridge(british(data), data);
  }

  @Test
  public void testAaCasualtiesLowLuck() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, false);
    final DiceRoll roll = new DiceRoll(new int[] {0}, 1, 1, false);
    final Collection<Unit> planes = bomber(data).create(5, british(data));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    final ScriptedRandomSource randomSource = new ScriptedRandomSource(new int[] {0, ScriptedRandomSource.ERROR});
    bridge.setRandomSource(randomSource);
    final Collection<Unit> casualties = BattleCalculator.getAaCasualties(false, planes, planes, defendingAa,
        defendingAa, roll, bridge, null, null, null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 1);
    assertEquals(1, randomSource.getTotalRolled());
  }

  @Test
  public void testAaCasualtiesLowLuckDifferentMovementLeft() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, false);
    final DiceRoll roll = new DiceRoll(new int[] {0}, 1, 1, false);
    final List<Unit> planes = bomber(data).create(5, british(data));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    final ScriptedRandomSource randomSource = new ScriptedRandomSource(new int[] {0, ScriptedRandomSource.ERROR});
    bridge.setRandomSource(randomSource);
    TripleAUnit.get(planes.get(0)).setAlreadyMoved(1);
    final Collection<Unit> casualties = BattleCalculator.getAaCasualties(false, planes, planes, defendingAa,
        defendingAa, roll, bridge, null, null, null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 1);
  }

  @Test
  public void testAaCasualtiesLowLuckMixed() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, false);
    // 6 bombers and 6 fighters
    final Collection<Unit> planes = bomber(data).create(6, british(data));
    planes.addAll(fighter(data).create(6, british(data)));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    // don't allow rolling, 6 of each is deterministic
    bridge.setRandomSource(new ScriptedRandomSource(new int[] {ScriptedRandomSource.ERROR}));
    final DiceRoll roll =
        DiceRoll
            .rollAa(
                CollectionUtils.getMatches(planes,
                    Matches
                        .unitIsOfTypes(UnitAttachment.get(defendingAa.iterator().next().getType()).getTargetsAA(data))),
                defendingAa, bridge, territory("Germany", data), true);
    final Collection<Unit> casualties = BattleCalculator.getAaCasualties(false, planes, planes, defendingAa,
        defendingAa, roll, bridge, null, null, null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 2);
    // should be 1 fighter and 1 bomber
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber()), 1);
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber().negate()), 1);
  }

  @Test
  public void testAaCasualtiesLowLuckMixedMultipleDiceRolled() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, false);
    // 5 bombers and 5 fighters
    final Collection<Unit> planes = bomber(data).create(5, british(data));
    planes.addAll(fighter(data).create(5, british(data)));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    // should roll once, a hit
    final ScriptedRandomSource randomSource = new ScriptedRandomSource(new int[] {0, 1, 1, ScriptedRandomSource.ERROR});
    bridge.setRandomSource(randomSource);
    final DiceRoll roll =
        DiceRoll
            .rollAa(
                CollectionUtils.getMatches(planes,
                    Matches
                        .unitIsOfTypes(UnitAttachment.get(defendingAa.iterator().next().getType()).getTargetsAA(data))),
                defendingAa, bridge, territory("Germany", data), true);
    assertEquals(1, randomSource.getTotalRolled());
    final Collection<Unit> casualties = BattleCalculator.getAaCasualties(false, planes, planes, defendingAa,
        defendingAa, roll, bridge, null, null, null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 2);
    // two extra rolls to pick which units are hit
    assertEquals(3, randomSource.getTotalRolled());
    // should be 1 fighter and 1 bomber
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber()), 0);
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber().negate()), 2);
  }

  @Test
  public void testAaCasualtiesLowLuckMixedWithChooseAaCasualties() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, true);
    // 6 bombers and 6 fighters
    final Collection<Unit> planes = bomber(data).create(6, british(data));
    planes.addAll(fighter(data).create(6, british(data)));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    when(dummyPlayer.selectCasualties(any(), any(), anyInt(), any(), any(), any(), any(), any(), any(), anyBoolean(),
        any(),
        any(), any(), any(), anyBoolean())).thenAnswer(new Answer<CasualtyDetails>() {
          @Override
          public CasualtyDetails answer(final InvocationOnMock invocation) {
            final Collection<Unit> selectFrom = invocation.getArgument(0);
            final int count = invocation.getArgument(2);

            final List<Unit> selected = CollectionUtils.getNMatches(selectFrom, count, Matches.unitIsStrategicBomber());
            return new CasualtyDetails(selected, new ArrayList<>(), false);
          }
        });
    bridge.setRemote(dummyPlayer);
    // don't allow rolling, 6 of each is deterministic
    bridge.setRandomSource(new ScriptedRandomSource(new int[] {ScriptedRandomSource.ERROR}));
    final DiceRoll roll =
        DiceRoll
            .rollAa(
                CollectionUtils.getMatches(planes,
                    Matches
                        .unitIsOfTypes(UnitAttachment.get(defendingAa.iterator().next().getType()).getTargetsAA(data))),
                defendingAa, bridge, territory("Germany", data), true);
    final Collection<Unit> casualties =
        BattleCalculator.getAaCasualties(false, planes, planes, defendingAa, defendingAa, roll, bridge, germans(data),
            british(data), null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 2);
    // we selected all bombers
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber()), 2);
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber().negate()), 0);
  }

  @Test
  public void testAaCasualtiesLowLuckMixedWithChooseAaCasualtiesRoll() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, true);
    // 7 bombers and 7 fighters
    final Collection<Unit> planes = bomber(data).create(7, british(data));
    planes.addAll(fighter(data).create(7, british(data)));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    when(dummyPlayer.selectCasualties(any(), any(), anyInt(), any(), any(), any(), any(), any(), any(), anyBoolean(),
        any(), any(), any(), any(), anyBoolean())).thenAnswer(new Answer<CasualtyDetails>() {
          @Override
          public CasualtyDetails answer(final InvocationOnMock invocation) {
            final Collection<Unit> selectFrom = invocation.getArgument(0);
            final int count = invocation.getArgument(2);
            final List<Unit> selected = CollectionUtils.getNMatches(selectFrom, count, Matches.unitIsStrategicBomber());
            return new CasualtyDetails(selected, new ArrayList<>(), false);
          }
        });
    bridge.setRemote(dummyPlayer);
    // only 1 roll, a hit
    bridge.setRandomSource(new ScriptedRandomSource(new int[] {0, ScriptedRandomSource.ERROR}));
    final DiceRoll roll =
        DiceRoll
            .rollAa(
                CollectionUtils.getMatches(planes,
                    Matches
                        .unitIsOfTypes(UnitAttachment.get(defendingAa.iterator().next().getType()).getTargetsAA(data))),
                defendingAa, bridge, territory("Germany", data), true);
    final Collection<Unit> casualties =
        BattleCalculator.getAaCasualties(false, planes, planes, defendingAa, defendingAa, roll, bridge, germans(data),
            british(data), null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 3);
    // we selected all bombers
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber()), 3);
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber().negate()), 0);
  }

  @Test
  public void testAaCasualtiesLowLuckMixedWithRolling() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, false);
    // 7 bombers and 7 fighters
    // 2 extra units, roll once
    final Collection<Unit> planes = bomber(data).create(7, british(data));
    planes.addAll(fighter(data).create(7, british(data)));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    // one roll, a hit
    final ScriptedRandomSource randomSource = new ScriptedRandomSource(new int[] {0});
    bridge.setRandomSource(randomSource);
    final DiceRoll roll =
        DiceRoll
            .rollAa(
                CollectionUtils.getMatches(planes,
                    Matches
                        .unitIsOfTypes(UnitAttachment.get(defendingAa.iterator().next().getType()).getTargetsAA(data))),
                defendingAa, bridge, territory("Germany", data), true);
    // make sure we rolled once
    assertEquals(1, randomSource.getTotalRolled());
    final Collection<Unit> casualties = BattleCalculator.getAaCasualties(false, planes, planes, defendingAa,
        defendingAa, roll, bridge, null, null, null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 3);
    // a second roll for choosing which unit
    assertEquals(2, randomSource.getTotalRolled());
    // should be 2 fighters and 1 bombers
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber()), 1);
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber().negate()), 2);
  }

  @Test
  public void testAaCasualtiesLowLuckMixedWithRollingMiss() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, false);
    // 7 bombers and 7 fighters
    // 2 extra units, roll once
    final Collection<Unit> planes = bomber(data).create(7, british(data));
    planes.addAll(fighter(data).create(7, british(data)));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    // one roll, a miss
    final ScriptedRandomSource randomSource =
        new ScriptedRandomSource(new int[] {2, 0, 0, 0, ScriptedRandomSource.ERROR});
    bridge.setRandomSource(randomSource);
    final DiceRoll roll =
        DiceRoll
            .rollAa(
                CollectionUtils.getMatches(planes,
                    Matches
                        .unitIsOfTypes(UnitAttachment.get(defendingAa.iterator().next().getType()).getTargetsAA(data))),
                defendingAa, bridge, territory("Germany", data), true);
    // make sure we rolled once
    assertEquals(1, randomSource.getTotalRolled());
    final Collection<Unit> casualties = BattleCalculator.getAaCasualties(false, planes, planes, defendingAa,
        defendingAa, roll, bridge, null, null, null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 2);
    assertEquals(4, randomSource.getTotalRolled());
    // should be 1 fighter and 1 bomber
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber()), 1);
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber().negate()), 1);
  }

  @Test
  public void testAaCasualtiesLowLuckMixedWithRollingForBombers() {
    final GameData data = bridge.getData();
    makeGameLowLuck(data);
    setSelectAaCasualties(data, false);
    // 6 bombers, 7 fighters
    final Collection<Unit> planes = bomber(data).create(6, british(data));
    planes.addAll(fighter(data).create(7, british(data)));
    final Collection<Unit> defendingAa =
        territory("Germany", data).getUnits().getMatches(Matches.unitIsAaForAnything());
    // 1 roll for the extra fighter
    final ScriptedRandomSource randomSource = new ScriptedRandomSource(new int[] {0, ScriptedRandomSource.ERROR});
    bridge.setRandomSource(randomSource);
    final DiceRoll roll =
        DiceRoll
            .rollAa(
                CollectionUtils.getMatches(planes,
                    Matches
                        .unitIsOfTypes(UnitAttachment.get(defendingAa.iterator().next().getType()).getTargetsAA(data))),
                defendingAa, bridge, territory("Germany", data), true);
    // make sure we rolled once
    assertEquals(1, randomSource.getTotalRolled());
    final Collection<Unit> casualties = BattleCalculator.getAaCasualties(false, planes, planes, defendingAa,
        defendingAa, roll, bridge, null, null, null, territory("Germany", data), null, false, null).getKilled();
    assertEquals(casualties.size(), 3);
    // should be 2 fighters and 1 bombers
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber()), 1);
    assertEquals(CollectionUtils.countMatches(casualties, Matches.unitIsStrategicBomber().negate()), 2);
  }
  // Radar AA tests removed, because "revised" does not have radar tech.
}
