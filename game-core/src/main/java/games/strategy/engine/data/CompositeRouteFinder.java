package games.strategy.engine.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import games.strategy.triplea.delegate.Matches;
import games.strategy.util.CollectionUtils;

public class CompositeRouteFinder {
  private final GameMap map;
  private final Map<Predicate<Territory>, Integer> matches;

  /**
   * This class can find composite routes between two territories.
   * Example set of matches: [Friendly Land, score: 1] [Enemy Land, score: 2] [Neutral Land, score = 4]
   * With this example set, an 8 length friendly route is considered equal in score to a 4 length enemy route and a 2
   * length neutral route.
   * This is because the friendly route score is 1/2 of the enemy route score and 1/4 of the neutral route score.
   * Note that you can choose whatever scores you want, and that the matches can mix and match with each other in any
   * way.
   *
   * @param map
   *        - Game map found through &lt;gamedata>.getMap()
   * @param matches
   *        - Set of matches and scores. The lower a match is scored, the more favorable it is.
   */
  public CompositeRouteFinder(final GameMap map, final Map<Predicate<Territory>, Integer> matches) {
    this.map = map;
    this.matches = matches;
  }

  Route findRoute(final Territory start, final Territory end) {
    final Set<Territory> allMatchingTers = new HashSet<>(
        CollectionUtils.getMatches(map.getTerritories(), t -> matches.keySet().stream().anyMatch(p -> p.test(t))));
    final Map<Territory, Integer> terScoreMap = createScoreMap();
    final Map<Territory, Integer> routeScoreMap = new HashMap<>();
    int bestRouteToEndScore = Integer.MAX_VALUE;
    final Map<Territory, Territory> previous = new HashMap<>();
    List<Territory> routeLeadersToProcess = new ArrayList<>();
    for (final Territory ter : map.getNeighbors(start, Matches.territoryIsInList(allMatchingTers))) {
      final int routeScore = terScoreMap.get(start) + terScoreMap.get(ter);
      routeScoreMap.put(ter, routeScore);
      routeLeadersToProcess.add(ter);
      previous.put(ter, start);
    }
    while (routeLeadersToProcess.size() > 0) {
      final List<Territory> newLeaders = new ArrayList<>();
      for (final Territory oldLeader : routeLeadersToProcess) {
        for (final Territory ter : map.getNeighbors(oldLeader, Matches.territoryIsInList(allMatchingTers))) {
          final int routeScore = routeScoreMap.get(oldLeader) + terScoreMap.get(ter);
          if (routeLeadersToProcess.contains(ter) || ter.equals(start)) {
            continue;
          }
          if (previous.containsKey(ter)) { // If we're bumping into an existing route
            if (routeScore >= routeScoreMap.get(ter)) {
              continue;
            }
          }
          if (bestRouteToEndScore <= routeScore) {
            // Ignore this route leader, as we know we already have a better route
            continue;
          }
          routeScoreMap.put(ter, routeScore);
          newLeaders.add(ter);
          previous.put(ter, oldLeader);
          if (ter.equals(end)) {
            if (routeScore < bestRouteToEndScore) {
              bestRouteToEndScore = routeScore;
            }
          }
        }
      }
      routeLeadersToProcess = newLeaders;
    }
    if (bestRouteToEndScore == Integer.MAX_VALUE) {
      return null;
    }
    return assembleRoute(start, end, previous);
  }

  private static Route assembleRoute(final Territory start, final Territory end,
      final Map<Territory, Territory> previous) {
    final List<Territory> routeTers = new ArrayList<>();
    Territory curTer = end;
    while (previous.containsKey(curTer)) {
      routeTers.add(curTer);
      curTer = previous.get(curTer);
    }
    routeTers.add(start);
    Collections.reverse(routeTers);
    return new Route(routeTers);
  }

  private Map<Territory, Integer> createScoreMap() {
    final Map<Territory, Integer> result = new HashMap<>();
    for (final Territory ter : map.getTerritories()) {
      result.put(ter, getTerScore(ter));
    }
    return result;
  }

  /*
   * Returns the score of the best match that matches this territory
   */
  private int getTerScore(final Territory ter) {
    int bestMatchingScore = Integer.MAX_VALUE;
    for (final Predicate<Territory> match : matches.keySet()) {
      final int score = matches.get(match);
      if (score < bestMatchingScore) { // If this is a 'better' match
        if (match.test(ter)) {
          bestMatchingScore = score;
        }
      }
    }
    return bestMatchingScore;
  }
}
