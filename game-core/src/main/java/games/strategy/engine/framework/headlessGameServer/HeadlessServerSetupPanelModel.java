package games.strategy.engine.framework.headlessGameServer;

import java.awt.Component;

import games.strategy.engine.framework.startup.mc.GameSelectorModel;
import games.strategy.engine.framework.startup.mc.ServerModel;
import games.strategy.engine.framework.startup.mc.SetupPanelModel;
import games.strategy.engine.framework.startup.ui.ServerSetupPanel;

/**
 * Setup panel model for headless server.
 */
public class HeadlessServerSetupPanelModel extends SetupPanelModel {
  protected final Component ui;

  public HeadlessServerSetupPanelModel(final GameSelectorModel gameSelectorModel, final Component ui) {
    super(gameSelectorModel);
    this.ui = ui;
  }

  @Override
  public void showSelectType() {
    final ServerModel model = new ServerModel(gameSelectorModel, this, ServerModel.InteractionMode.HEADLESS);
    if (!model.createServerMessenger(ui)) {
      model.cancel();
      return;
    }
    if (ui == null) {
      final HeadlessServerSetup serverSetup = new HeadlessServerSetup(model, gameSelectorModel);
      setGameTypePanel(serverSetup);
    } else {
      final ServerSetupPanel serverSetupPanel = new ServerSetupPanel(model, gameSelectorModel);
      setGameTypePanel(serverSetupPanel);
    }
  }
}
