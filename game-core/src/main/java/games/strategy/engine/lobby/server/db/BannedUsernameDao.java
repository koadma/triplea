package games.strategy.engine.lobby.server.db;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nullable;

import games.strategy.engine.lobby.server.User;
import games.strategy.util.Tuple;

/**
 * Data access object for the banned username table.
 */
public interface BannedUsernameDao {
  /**
   * Adds the specified banned username to the table if it does not exist or updates the instant at which the ban will
   * expire if it already exists.
   *
   * @param bannedUser The user whose username will be banned.
   * @param banTill The instant at which the ban will expire or {@ode null} to ban the username forever.
   * @param moderator The moderator executing the ban.
   *
   * @throws IllegalStateException If an error occurs while adding, updating, or removing the ban.
   */
  void addBannedUsername(User bannedUser, @Nullable Instant banTill, User moderator);

  /**
   * Indicates the specified username is banned.
   *
   * @param username The username to query for a ban.
   *
   * @return A tuple whose first element indicates if the username is banned or not. If the username is banned, the
   *         second element is the instant at which the ban will expire or {@code null} if the username is banned
   *         forever.
   */
  Tuple<Boolean, /* @Nullable */ Timestamp> isUsernameBanned(String username);
}
