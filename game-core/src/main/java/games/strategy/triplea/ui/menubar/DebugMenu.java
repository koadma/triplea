package games.strategy.triplea.ui.menubar;

import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import games.strategy.debug.DebugUtils;
import games.strategy.debug.ErrorConsole;
import games.strategy.engine.gamePlayer.IGamePlayer;
import games.strategy.performance.EnablePerformanceLoggingCheckBox;
import games.strategy.triplea.ai.proAI.ProAi;
import games.strategy.triplea.ui.TripleAFrame;
import games.strategy.ui.SwingAction;
import games.strategy.ui.SwingComponents;

class DebugMenu {

  DebugMenu(final JMenuBar menuBar, final TripleAFrame frame) {
    final JMenu debugMenu = SwingComponents.newJMenu("Debug", SwingComponents.KeyboardCode.D);
    menuBar.add(debugMenu);

    final Set<IGamePlayer> players = frame.getLocalPlayers().getLocalPlayers();
    final boolean areThereProAIs = players.stream().anyMatch(ProAi.class::isInstance);
    if (areThereProAIs) {
      ProAi.initialize(frame);
      debugMenu.add(SwingAction.of("Show Hard AI Logs", e -> ProAi.showSettingsWindow())).setMnemonic(KeyEvent.VK_X);
    }

    debugMenu.add(new EnablePerformanceLoggingCheckBox());
    debugMenu.add(SwingAction.of("Show Console", e -> {
      ErrorConsole.showConsole();
      ErrorConsole.getConsole().append(DebugUtils.getMemory());
    })).setMnemonic(KeyEvent.VK_C);
  }
}
