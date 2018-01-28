package games.strategy.net;

import java.time.Instant;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A server messenger. Additional methods for accepting new connections.
 */
public interface IServerMessenger extends IMessenger {
  void setAcceptNewConnections(boolean accept);

  boolean isAcceptNewConnections();

  void setLoginValidator(ILoginValidator loginValidator);

  ILoginValidator getLoginValidator();

  /**
   * Add a listener for change in connection status.
   */
  void addConnectionChangeListener(IConnectionChangeListener listener);

  /**
   * Remove a listener for change in connection status.
   */
  void removeConnectionChangeListener(IConnectionChangeListener listener);

  /**
   * Remove the node from the network.
   */
  void removeConnection(INode node);

  /**
   * Get a list of nodes.
   */
  Set<INode> getNodes();

  void notifyIpMiniBanningOfPlayer(String ip, Instant expires);

  void notifyMacMiniBanningOfPlayer(String mac, Instant expires);

  void notifyUsernameMiniBanningOfPlayer(String username, Instant expires);

  /**
   * @return The hashed MAC address for the user with the specified name or {@code null} if unknown.
   */
  @Nullable String getPlayerMac(String name);

  void notifyUsernameMutingOfPlayer(String username, Instant muteExpires);

  void notifyIpMutingOfPlayer(String ip, Instant muteExpires);

  void notifyMacMutingOfPlayer(String mac, Instant muteExpires);

  boolean isUsernameMiniBanned(String username);

  boolean isIpMiniBanned(String ip);

  boolean isMacMiniBanned(String mac);
}
