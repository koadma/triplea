package games.strategy.triplea.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.RelationshipType;
import games.strategy.triplea.Constants;
import games.strategy.util.Triple;

/**
 * A panel that shows the current political state, this has no other
 * functionality then a view on the current politics.
 */
public class PoliticalStateOverview extends JPanel {
  private static final long serialVersionUID = -8445782272897831080L;
  public static final String LABEL_SELF = "----";
  private final UiContext uiContext;
  private final GameData data;
  private final boolean editable;
  private final Set<Triple<PlayerID, PlayerID, RelationshipType>> editChanges = new HashSet<>();

  /**
   * Constructs this panel
   *
   * @param data
   *        gamedata to get the info from
   * @param uiContext
   *        uicontext to use to show this panel.
   */
  public PoliticalStateOverview(final GameData data, final UiContext uiContext, final boolean editable) {
    this.uiContext = uiContext;
    this.data = data;
    this.editable = editable;
    drawPoliticsUi();
  }

  /**
   * does the actual adding of elements to this panel.
   */
  private void drawPoliticsUi() {
    this.setLayout(new GridBagLayout());
    // draw horizontal labels
    int currentCell = 1;
    final Insets insets = new Insets(5, 2, 5, 2);
    for (final PlayerID p : data.getPlayerList()) {
      this.add(getPlayerLabel(p), new GridBagConstraints(currentCell++, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
          GridBagConstraints.BOTH, insets, 0, 0));
    }
    // draw vertical labels and dividers
    currentCell = 1;
    for (final PlayerID p : data.getPlayerList()) {
      this.add(new JSeparator(), new GridBagConstraints(0, currentCell++, 20, 1, 0.1, 0.1, GridBagConstraints.WEST,
          GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      this.add(getPlayerLabel(p), new GridBagConstraints(0, currentCell++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
          GridBagConstraints.BOTH, insets, 0, 0));
    }
    // draw cells
    int x = 1;
    int y = 2;
    for (final PlayerID verticalPlayer : data.getPlayerList()) {
      for (final PlayerID horizontalPlayer : data.getPlayerList()) {
        if (horizontalPlayer.equals(verticalPlayer)) {
          this.add(new JLabel(PoliticalStateOverview.LABEL_SELF), new GridBagConstraints(x++, y, 1, 1, 1.0, 1.0,
              GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        } else {
          this.add(getRelationshipLabel(verticalPlayer, horizontalPlayer), new GridBagConstraints(x++, y, 1, 1, 1.0,
              1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
        }
      }
      y = y + 2;
      x = 1;
    }
  }

  /**
   * Gets a label showing the coloured relationshipName between these two
   * players.
   */
  private JPanel getRelationshipLabel(final PlayerID player1, final PlayerID player2) {
    RelationshipType relType = null;
    for (final Triple<PlayerID, PlayerID, RelationshipType> changesSoFar : editChanges) {
      if ((player1.equals(changesSoFar.getFirst()) && player2.equals(changesSoFar.getSecond()))
          || (player2.equals(changesSoFar.getFirst()) && player1.equals(changesSoFar.getSecond()))) {
        relType = changesSoFar.getThird();
      }
    }
    if (relType == null) {
      data.acquireReadLock();
      try {
        relType = data.getRelationshipTracker().getRelationshipType(player1, player2);
      } finally {
        data.releaseReadLock();
      }
    }
    final JComponent relationshipLabel = getRelationshipComponent(player1, player2, relType);
    final JPanel relationshipLabelPanel = new JPanel();
    relationshipLabelPanel.add(relationshipLabel);
    relationshipLabelPanel.setBackground(getRelationshipTypeColor(relType));
    return relationshipLabelPanel;
  }

  private JComponent getRelationshipComponent(final PlayerID player1, final PlayerID player2,
      final RelationshipType relType) {
    if (!editable) {
      return new JLabel(relType.getName());
    }

    final JButton button = new JButton(relType.getName());
    button.addActionListener(e -> {
      final List<RelationshipType> types =
          new ArrayList<>(data.getRelationshipTypeList().getAllRelationshipTypes());
      types.remove(data.getRelationshipTypeList().getNullRelation());
      types.remove(data.getRelationshipTypeList().getSelfRelation());
      final Object[] possibilities = types.toArray();
      final RelationshipType chosenRelationship =
          (RelationshipType) JOptionPane.showInputDialog(PoliticalStateOverview.this,
              "Change Current Relationship between " + player1.getName() + " and " + player2.getName(),
              "Change Current Relationship", JOptionPane.PLAIN_MESSAGE, null, possibilities, relType);
      if (chosenRelationship != null) {
        // remove any old ones
        final Iterator<Triple<PlayerID, PlayerID, RelationshipType>> iter = editChanges.iterator();
        while (iter.hasNext()) {
          final Triple<PlayerID, PlayerID, RelationshipType> changesSoFar = iter.next();
          if ((player1.equals(changesSoFar.getFirst()) && player2.equals(changesSoFar.getSecond()))
              || (player2.equals(changesSoFar.getFirst()) && player1.equals(changesSoFar.getSecond()))) {
            iter.remove();
          }
        }

        // see if there is actually a change
        final RelationshipType actualRelationship;
        data.acquireReadLock();
        try {
          actualRelationship = data.getRelationshipTracker().getRelationshipType(player1, player2);
        } finally {
          data.releaseReadLock();
        }
        if (!chosenRelationship.equals(actualRelationship)) {
          // add new change
          editChanges.add(Triple.of(player1, player2, chosenRelationship));
        }
        // redraw everything
        redrawPolitics();
      }
    });
    button.setBackground(getRelationshipTypeColor(relType));
    return button;
  }

  /**
   * returns a color to represent the relationship.
   *
   * @param relType
   *        which relationship to get the color for
   * @return the color to represent this relationship
   */
  private static Color getRelationshipTypeColor(final RelationshipType relType) {
    final String archeType = relType.getRelationshipTypeAttachment().getArcheType();
    if (archeType.equals(Constants.RELATIONSHIP_ARCHETYPE_ALLIED)) {
      return Color.green;
    }
    if (archeType.equals(Constants.RELATIONSHIP_ARCHETYPE_NEUTRAL)) {
      return Color.lightGray;
    }
    if (archeType.equals(Constants.RELATIONSHIP_ARCHETYPE_WAR)) {
      return Color.red;
    }
    throw new IllegalStateException(
        "PoliticsUI: RelationshipType: " + relType.getName() + " can only be of archeType Allied, Neutral or War");
  }

  /**
   * Gets a label showing the flag + name of this player.
   *
   * @param player
   *        the player to get the label for
   * @return the label representing this player
   */
  protected JLabel getPlayerLabel(final PlayerID player) {
    return new JLabel(player.getName(), new ImageIcon(uiContext.getFlagImageFactory().getFlag(player)), JLabel.LEFT);
  }

  /**
   * Redraw this panel (because of changed politics).
   */
  public void redrawPolitics() {
    this.removeAll();
    this.drawPoliticsUi();
    this.revalidate();
  }

  Collection<Triple<PlayerID, PlayerID, RelationshipType>> getEditChanges() {
    if (!editable) {
      return null;
    }
    return editChanges;
  }
}
