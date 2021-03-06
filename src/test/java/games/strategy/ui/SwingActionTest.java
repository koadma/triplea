package games.strategy.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import com.example.mockito.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SwingActionTest {
  private static final Object VALUE = new Object();

  private static final Runnable RUNNABLE_THROWING_EXCEPTION = () -> {
    throw new IllegalStateException();
  };

  private static final Supplier<?> SUPPLIER_THROWING_EXCEPTION = () -> {
    throw new IllegalStateException();
  };

  @Test
  public void testActionOf(@Mock final ActionEvent event, @Mock final ActionListener listener) {
    final Action action = SwingAction.of("Name1234", listener);
    assertEquals("Name1234", action.getValue(Action.NAME));
    action.actionPerformed(event);
    verify(listener).actionPerformed(event);
  }

  @Test
  public void testKeyReleaseListener(@Mock final Consumer<KeyEvent> listener) {
    final KeyEvent event = mock(KeyEvent.class);
    final KeyListener action = SwingAction.keyReleaseListener(listener);
    action.keyReleased(event);
    verify(listener).accept(event);
  }

  @Test
  public void testInvokeAndWaitWithRunnable_ShouldInvokeActionWhenCalledOffEdt(@Mock final Runnable action)
      throws Exception {
    SwingAction.invokeAndWait(action);

    verify(action).run();
  }

  @Test
  public void testInvokeAndWaitWithRunnable_ShouldInvokeActionWhenCalledOnEdt(@Mock final Runnable action)
      throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        SwingAction.invokeAndWait(action);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        fail("unexpected interruption");
      }
    });

    verify(action).run();
  }

  @Test
  public void testInvokeAndWaitWithRunnable_ShouldRethrowActionUncheckedExceptionWhenCalledOffEdt() {
    assertThrows(IllegalStateException.class, () -> SwingAction.invokeAndWait(RUNNABLE_THROWING_EXCEPTION));
  }

  @Test
  public void testInvokeAndWaitWithRunnable_ShouldRethrowActionUncheckedExceptionWhenCalledOnEdt() throws Exception {
    SwingUtilities.invokeAndWait(() -> assertThrows(
        IllegalStateException.class,
        () -> SwingAction.invokeAndWait(RUNNABLE_THROWING_EXCEPTION)));
  }

  @Test
  public void testInvokeAndWaitWithSupplier_ShouldReturnActionResultWhenCalledOffEdt() throws Exception {
    assertEquals(VALUE, SwingAction.invokeAndWait(() -> VALUE));
  }

  @Test
  public void testInvokeAndWaitWithSupplier_ShouldReturnActionResultWhenCalledOnEdt() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        assertEquals(VALUE, SwingAction.invokeAndWait(() -> VALUE));
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        fail("unexpected interruption");
      }
    });
  }

  @Test
  public void testInvokeAndWaitWithSupplier_ShouldRethrowActionUncheckedExceptionWhenCalledOffEdt() {
    assertThrows(IllegalStateException.class, () -> SwingAction.invokeAndWait(SUPPLIER_THROWING_EXCEPTION));
  }

  @Test
  public void testInvokeAndWaitWithSupplier_ShouldRethrowActionUncheckedExceptionWhenCalledOnEdt() throws Exception {
    SwingUtilities.invokeAndWait(() -> assertThrows(
        IllegalStateException.class,
        () -> SwingAction.invokeAndWait(SUPPLIER_THROWING_EXCEPTION)));
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithRunnable_ShouldInvokeActionWhenCalledOffEdt(
      @Mock final Runnable action) {
    SwingAction.invokeAndWaitUninterruptibly(action);

    verify(action).run();
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithRunnable_ShouldInvokeActionWhenCalledOnEdt(
      @Mock final Runnable action) throws Exception {
    SwingUtilities.invokeAndWait(() -> SwingAction.invokeAndWaitUninterruptibly(action));

    verify(action).run();
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithRunnable_ShouldRethrowActionUncheckedExceptionWhenCalledOffEdt() {
    assertThrows(
        IllegalStateException.class,
        () -> SwingAction.invokeAndWaitUninterruptibly(RUNNABLE_THROWING_EXCEPTION));
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithRunnable_ShouldRethrowActionUncheckedExceptionWhenCalledOnEdt()
      throws Exception {
    SwingUtilities.invokeAndWait(() -> assertThrows(
        IllegalStateException.class,
        () -> SwingAction.invokeAndWaitUninterruptibly(RUNNABLE_THROWING_EXCEPTION)));
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithSupplier_ShouldReturnActionResultWhenCalledOffEdt() {
    assertEquals(Optional.of(VALUE), SwingAction.invokeAndWaitUninterruptibly(() -> VALUE));
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithSupplier_ShouldReturnActionResultWhenCalledOnEdt() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> assertEquals(Optional.of(VALUE), SwingAction.invokeAndWaitUninterruptibly(() -> VALUE)));
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithSupplier_ShouldReturnEmptyResultWhenInterrupted() throws Exception {
    final CountDownLatch threadReadyLatch = new CountDownLatch(1);
    final CountDownLatch testCompleteLatch = new CountDownLatch(1);
    final AtomicReference<Optional<Object>> result = new AtomicReference<>();

    final Thread thread = new Thread(() -> {
      result.set(SwingAction.invokeAndWaitUninterruptibly(() -> {
        threadReadyLatch.countDown();
        try {
          testCompleteLatch.await();
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
          fail("unexpected interruption on EDT");
        }
        return VALUE;
      }));
    });
    thread.start();
    threadReadyLatch.await();
    thread.interrupt();
    thread.join();
    testCompleteLatch.countDown();

    assertEquals(Optional.empty(), result.get());
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithSupplier_ShouldRethrowActionUncheckedExceptionWhenCalledOffEdt() {
    assertThrows(
        IllegalStateException.class,
        () -> SwingAction.invokeAndWaitUninterruptibly(SUPPLIER_THROWING_EXCEPTION));
  }

  @Test
  public void testInvokeAndWaitUninterruptiblyWithSupplier_ShouldRethrowActionUncheckedExceptionWhenCalledOnEdt()
      throws Exception {
    SwingUtilities.invokeAndWait(() -> assertThrows(
        IllegalStateException.class,
        () -> SwingAction.invokeAndWaitUninterruptibly(SUPPLIER_THROWING_EXCEPTION)));
  }

  @Test
  public void testInvokeNowOrLater() {
    final CountDownLatch latch = new CountDownLatch(1);

    SwingAction.invokeNowOrLater(latch::countDown);

    assertTimeoutPreemptively(Duration.ofSeconds(5L), () -> latch.await());
  }
}
