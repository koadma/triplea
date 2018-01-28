package games.strategy.engine.config.client;

import java.io.File;

import com.google.common.annotations.VisibleForTesting;

import games.strategy.engine.config.FilePropertyReader;
import games.strategy.engine.config.PropertyReader;
import games.strategy.util.Version;

/**
 * Reads property values from the game engine configuration file.
 */
public final class GameEnginePropertyReader {
  public static final String GAME_ENGINE_PROPERTIES_FILE = "game_engine.properties";
  private static final String  DEVELOPMENT_PATH = "game-core" + File.separator + GAME_ENGINE_PROPERTIES_FILE;

  private final PropertyReader propertyReader;

  public GameEnginePropertyReader() {
    this(new FilePropertyReader(loadFile(GAME_ENGINE_PROPERTIES_FILE)));
  }

  private static String loadFile(final String path) {
    if(!(new File(path).exists())) {
      return DEVELOPMENT_PATH;
    } else {
      return path;
    }
  }

  @VisibleForTesting
  GameEnginePropertyReader(final PropertyReader propertyReader) {
    this.propertyReader = propertyReader;
  }

  public Version getEngineVersion() {
    return new Version(propertyReader.readProperty(PropertyKeys.ENGINE_VERSION));
  }

  @VisibleForTesting
  interface PropertyKeys {
    String ENGINE_VERSION = "engine_version";
  }
}
