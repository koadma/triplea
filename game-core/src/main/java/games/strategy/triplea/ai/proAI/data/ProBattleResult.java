package games.strategy.triplea.ai.proAI.data;

import java.util.ArrayList;
import java.util.List;

import games.strategy.engine.data.Unit;

public class ProBattleResult {

  private final double winPercentage;
  private final double tuvSwing;
  private final boolean hasLandUnitRemaining;
  private final List<Unit> averageAttackersRemaining;
  private final List<Unit> averageDefendersRemaining;
  private final double battleRounds;

  public ProBattleResult() {
    winPercentage = 0;
    tuvSwing = -1;
    hasLandUnitRemaining = false;
    averageAttackersRemaining = new ArrayList<>();
    averageDefendersRemaining = new ArrayList<>();
    battleRounds = 0;
  }

  public ProBattleResult(final double winPercentage, final double tuvSwing, final boolean hasLandUnitRemaining,
      final List<Unit> averageAttackersRemaining, final List<Unit> averageDefendersRemaining,
      final double battleRounds) {
    this.winPercentage = winPercentage;
    this.tuvSwing = tuvSwing;
    this.hasLandUnitRemaining = hasLandUnitRemaining;
    this.averageAttackersRemaining = averageAttackersRemaining;
    this.averageDefendersRemaining = averageDefendersRemaining;
    this.battleRounds = battleRounds;
  }

  public double getWinPercentage() {
    return winPercentage;
  }

  public double getTuvSwing() {
    return tuvSwing;
  }

  public boolean isHasLandUnitRemaining() {
    return hasLandUnitRemaining;
  }

  public List<Unit> getAverageAttackersRemaining() {
    return averageAttackersRemaining;
  }

  public List<Unit> getAverageDefendersRemaining() {
    return averageDefendersRemaining;
  }

  public double getBattleRounds() {
    return battleRounds;
  }

  @Override
  public String toString() {
    return "winPercentage=" + winPercentage + ", tuvSwing=" + tuvSwing + ", hasLandUnitRemaining="
        + hasLandUnitRemaining + ", averageAttackersRemaining=" + averageAttackersRemaining
        + ", averageDefendersRemaining=" + averageDefendersRemaining + ", battleRounds=" + battleRounds;
  }
}
