package games.strategy.engine.framework.startup.mc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observer;

import org.junit.experimental.extensions.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.GameSequence;
import games.strategy.engine.framework.GameDataFileUtils;
import games.strategy.engine.framework.ui.GameChooserEntry;
import games.strategy.triplea.settings.AbstractClientSettingTestCase;
import games.strategy.util.Version;

@ExtendWith(MockitoExtension.class)
public class GameSelectorModelTest extends AbstractClientSettingTestCase {

  private static void assertHasEmptyData(final GameSelectorModel objectToCheck) {
    assertThat(objectToCheck.getGameData(), nullValue());
    assertHasEmptyDisplayData(objectToCheck);
  }

  private static void assertHasEmptyDisplayData(final GameSelectorModel objectToCheck) {
    assertThat(objectToCheck.getFileName(), is("-"));
    assertThat(objectToCheck.getGameName(), is("-"));
    assertThat(objectToCheck.getGameRound(), is("-"));
  }

  private static void assertHasFakeTestData(final GameSelectorModel objectToCheck) {
    assertThat(objectToCheck.getGameName(), is(fakeGameName));
    assertThat(objectToCheck.getGameRound(), is(fakeGameRound));
    assertThat(objectToCheck.getGameVersion(), is(fakeGameVersion));
  }

  private static final String fakeGameVersion = "12.34.56.78";
  private static final String fakeGameRound = "3";
  private static final String fakeGameName = "_fakeGameName_";
  private static final String fakeFileName = "/hack/and/slash";

  private GameSelectorModel testObj;

  @Mock
  private GameChooserEntry mockEntry;

  @Mock
  private GameData mockGameData;

  @Mock
  private GameSequence mockSequence;

  @Mock
  private Observer mockObserver;

  @Mock
  private ClientModel mockClientModel;

  @BeforeEach
  public void setup() {
    testObj = new GameSelectorModel();
    assertHasEmptyData(testObj);
    testObj.addObserver(mockObserver);
  }

  @AfterEach
  public void tearDown() {
    testObj.deleteObservers();
  }

  @Test
  public void testSetGameData() {
    assertHasEmptyData(testObj);
    this.testObjectSetMockGameData();
  }

  private void testObjectSetMockGameData() {
    prepareMockGameDataExpectations();
    testObj.setGameData(mockGameData);
    assertThat(testObj.getGameData(), sameInstance(mockGameData));
    assertHasFakeTestData(testObj);
    this.verifyTestObjectObserverUpdateSent();
  }

  private void verifyTestObjectObserverUpdateSent() {
    verify(mockObserver, times(1)).update(Mockito.any(), Mockito.any());
    reset(mockObserver);
  }

  private void prepareMockGameDataExpectations() {
    when(mockGameData.getGameVersion()).thenReturn(new Version(fakeGameVersion));
    when(mockGameData.getSequence()).thenReturn(mockSequence);
    when(mockSequence.getRound()).thenReturn(Integer.valueOf(fakeGameRound));
    when(mockGameData.getGameName()).thenReturn(fakeGameName);
  }

  @Test
  public void testResetGameDataToNull() {
    assertHasEmptyData(testObj);
    this.testObjectSetMockGameData();

    testObj.resetGameDataToNull();
    assertHasEmptyData(testObj);
  }

  @Test
  public void testIsSaveGame() {
    testObj.load(null, "");
    assertThat(testObj.isSavedGame(), is(true));

    testObj.load(null, ".xml");
    assertThat(testObj.isSavedGame(), is(false));

    testObj.load(null, GameDataFileUtils.addExtension("file"));
    assertThat(testObj.isSavedGame(), is(true));
  }


  @Test
  public void testCanSelect() {
    assertThat(testObj.canSelect(), is(true));
    testObj.setCanSelect(false);
    assertThat(testObj.canSelect(), is(false));
    testObj.setCanSelect(true);
    assertThat(testObj.canSelect(), is(true));
  }

  @Test
  public void testClearDataButKeepGameInfo() {
    this.testObjectSetMockGameData();

    final String newGameName = " 123";
    final String newGameRound = "gameRound xyz";
    final String newGameVersion = "gameVersion abc";

    testObj.clearDataButKeepGameInfo(newGameName, newGameRound, newGameVersion);
    verifyTestObjectObserverUpdateSent();
    assertThat(testObj.getGameData(), nullValue());
    assertThat(testObj.getGameName(), is(newGameName));
    assertThat(testObj.getGameRound(), is(newGameRound));
    assertThat(testObj.getGameVersion(), is(newGameVersion));
  }

  @Test
  public void testLoadFromNewGameChooserEntry() {
    final String fileName = "testname";
    when(mockEntry.getLocation()).thenReturn(fileName);

    when(mockEntry.getGameData()).thenReturn(mockGameData);
    prepareMockGameDataExpectations();
    try {
      when(mockEntry.getUri()).thenReturn(new URI("abc"));
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
    testObj.load(mockEntry);
    assertThat(testObj.getFileName(), is(fileName));

    assertThat(testObj.getGameData(), sameInstance(mockGameData));
    assertHasFakeTestData(testObj);
  }


  @Test
  public void testLoadFromGameDataFileNamePair() {
    assertHasEmptyData(testObj);

    prepareMockGameDataExpectations();
    testObj.load(mockGameData, fakeFileName);
    assertThat(testObj.getGameData(), sameInstance(mockGameData));
    assertThat(testObj.getFileName(), is(fakeFileName));
  }


  @Test
  public void testGetGameData() {
    assertThat(testObj.getGameData(), nullValue());
    prepareMockGameDataExpectations();
    testObj.setGameData(mockGameData);
    assertThat(testObj.getGameData(), sameInstance(mockGameData));
  }

  @Test
  public void testSetAndGetIsHostHeadlessBot() {
    assertThat(testObj.isHostHeadlessBot(), is(false));
    testObj.setIsHostHeadlessBot(true);
    assertThat(testObj.isHostHeadlessBot(), is(true));
    testObj.setIsHostHeadlessBot(false);
    assertThat(testObj.isHostHeadlessBot(), is(false));
  }


  @Test
  public void testSetAndGetClientModelForHostBots() {
    assertThat(testObj.getClientModelForHostBots(), nullValue());
    testObj.setClientModelForHostBots(mockClientModel);
    assertThat(testObj.getClientModelForHostBots(), sameInstance(mockClientModel));
    testObj.setClientModelForHostBots(null);
    assertThat(testObj.getClientModelForHostBots(), nullValue());
  }

  @Test
  public void testGetFileName() {
    assertThat(testObj.getFileName(), is("-"));
    prepareMockGameDataExpectations();
    testObj.load(mockGameData, fakeFileName);
    assertThat(testObj.getFileName(), is(fakeFileName));
    testObj.resetGameDataToNull();
    assertThat(testObj.getFileName(), is("-"));
  }

  @Test
  public void testGetGameName() {
    this.testObjectSetMockGameData();
    assertThat(testObj.getGameName(), is(fakeGameName));
  }

  @Test
  public void testGetGameRound() {
    this.testObjectSetMockGameData();
    assertThat(testObj.getGameRound(), is(fakeGameRound));
  }

  @Test
  public void testGetGameVersion() {
    this.testObjectSetMockGameData();
    assertThat(testObj.getGameVersion(), is(fakeGameVersion));
  }
}
