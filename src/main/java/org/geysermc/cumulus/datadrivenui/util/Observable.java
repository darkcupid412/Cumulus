/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.util;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.cumulus.datadrivenui.util.impl.ObservableImpl;

/**
 * A value that a data-driven form watches. Changing it while the form is open updates what the
 * player sees, without rebuilding or resending the form. Where a component lets the player edit the
 * value, their edits are written back into the same observable.
 *
 * <p><b>Threading.</b> {@link #get()} and {@link #set(Object)} may be called from any thread.
 * Listeners are called in the order the values were set, in subscription order, never while a lock
 * is held, and a listener that throws neither stops the others nor propagates out of {@code set}.
 * An {@link Error} is the one exception and does propagate, because it means a broken VM rather
 * than a broken listener. A listener that sets a new value is delivered after the current one
 * finishes rather than nested inside it, so relative to the notifications it receives, no listener
 * sees an older value after a newer one; a listener subscribed while values are being delivered may
 * still receive them from before what {@link #get()} already returns. Whichever thread finds
 * delivery idle performs it, so {@code set} may return before the listeners have run, or may run
 * listeners for values that other threads set. A closed subscription is never called again.
 * Listeners should be short: one that blocks holds up every other value on this observable, and
 * anything that has to run on a particular thread belongs on that thread's own executor; a host
 * handles this for its own listeners. Closing a subscription stops later notifications, but one
 * already being delivered on another thread runs to completion: a close that could interrupt a
 * listener mid-call would have to hold a lock across it, which is the deadlock this design exists
 * to avoid.
 *
 * <p>Setting a value equal to the current one does nothing and notifies nobody, so a value that is
 * recomputed often does not turn into a stream of redundant updates to the client.
 *
 * @param <T> the type of the value
 * @since 2.0
 */
public interface Observable<T> {
  /**
   * Returns a new observable holding the given value.
   *
   * @param value the value to start with
   * @param <T> the type of the value
   * @return an observable holding the given value and no listeners
   */
  static <T> @NonNull Observable<T> of(@NonNull T value) {
    return new ObservableImpl<>(value);
  }

  /** Returns the current value. */
  @NonNull T get();

  /**
   * Sets the value and notifies the listeners. Does nothing if the new value is equal to the
   * current one.
   *
   * @param value the new value
   */
  void set(@NonNull T value);

  /**
   * Registers a listener, called with the new value whenever this observable changes.
   *
   * @param listener called on every change
   * @return a handle that removes the listener when closed
   */
  @NonNull Subscription subscribe(@NonNull Consumer<@NonNull T> listener);

  /**
   * A registered listener, which stops being called once closed.
   *
   * @since 2.0
   */
  interface Subscription extends AutoCloseable {
    /** Removes the listener. Closing an already closed subscription does nothing. */
    @Override
    void close();
  }
}
