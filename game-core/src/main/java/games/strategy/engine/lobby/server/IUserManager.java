package games.strategy.engine.lobby.server;

import games.strategy.engine.lobby.server.userDB.DBUser;
import games.strategy.engine.message.IRemote;
import games.strategy.engine.message.RemoteName;

public interface IUserManager extends IRemote {
  RemoteName USER_MANAGER =
      new RemoteName("games.strategy.engine.lobby.server.USER_MANAGER", IUserManager.class);

  /**
   * Update the user info, returning an error string if an error occurs.
   */
  String updateUser(String userName, String emailAddress, String hashedPassword);

  DBUser getUserInfo(String userName);
}
