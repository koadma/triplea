package games.strategy.engine.framework.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import org.junit.experimental.extensions.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class GameChooserModelTest {
  @Test
  public void testOrdering() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      final GameChooserEntry entry1 = mock(GameChooserEntry.class);
      final GameChooserEntry entry2 = mock(GameChooserEntry.class);
      final GameChooserEntry entry3 = mock(GameChooserEntry.class);
      when(entry1.compareTo(entry2)).thenReturn(-1);
      when(entry2.compareTo(entry3)).thenReturn(-1);
      when(entry3.compareTo(entry2)).thenReturn(1);
      when(entry2.compareTo(entry1)).thenReturn(1);
      when(entry1.compareTo(entry3)).thenReturn(-1);
      when(entry3.compareTo(entry1)).thenReturn(1);
      final GameChooserModel model = new GameChooserModel(new HashSet<>(Arrays.asList(entry1, entry2, entry3)));
      assertEquals(entry1, model.get(0));
      assertEquals(entry2, model.get(1));
      assertEquals(entry3, model.get(2));
    });
  }
}
