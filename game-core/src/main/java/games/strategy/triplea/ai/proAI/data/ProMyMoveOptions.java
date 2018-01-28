package games.strategy.triplea.ai.proAI.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;

public class ProMyMoveOptions {

  private final Map<Territory, ProTerritory> territoryMap;
  private final Map<Unit, Set<Territory>> unitMoveMap;
  private final Map<Unit, Set<Territory>> transportMoveMap;
  private final Map<Unit, Set<Territory>> bombardMap;
  private final List<ProTransport> transportList;
  private final Map<Unit, Set<Territory>> bomberMoveMap;

  ProMyMoveOptions() {
    territoryMap = new HashMap<>();
    unitMoveMap = new HashMap<>();
    transportMoveMap = new HashMap<>();
    bombardMap = new HashMap<>();
    transportList = new ArrayList<>();
    bomberMoveMap = new HashMap<>();
  }

  ProMyMoveOptions(final ProMyMoveOptions myMoveOptions) {
    this();
    for (final Territory t : myMoveOptions.territoryMap.keySet()) {
      territoryMap.put(t, new ProTerritory(myMoveOptions.territoryMap.get(t)));
    }
    unitMoveMap.putAll(myMoveOptions.unitMoveMap);
    transportMoveMap.putAll(myMoveOptions.transportMoveMap);
    bombardMap.putAll(myMoveOptions.bombardMap);
    transportList.addAll(myMoveOptions.transportList);
    bomberMoveMap.putAll(myMoveOptions.bomberMoveMap);
  }

  public Map<Territory, ProTerritory> getTerritoryMap() {
    return territoryMap;
  }

  public Map<Unit, Set<Territory>> getUnitMoveMap() {
    return unitMoveMap;
  }

  public Map<Unit, Set<Territory>> getTransportMoveMap() {
    return transportMoveMap;
  }

  public Map<Unit, Set<Territory>> getBombardMap() {
    return bombardMap;
  }

  public List<ProTransport> getTransportList() {
    return transportList;
  }

  public Map<Unit, Set<Territory>> getBomberMoveMap() {
    return bomberMoveMap;
  }

}
