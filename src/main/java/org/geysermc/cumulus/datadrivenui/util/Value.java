/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.util;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Something a form component needs a value for, which is either fixed for as long as the form is
 * open, or follows an {@link Observable} and changes with it.
 *
 * <p>Component options take a {@code Value} so that any of them can be made to change while the
 * form is open, without a second set of methods for the fixed case.
 *
 * <p>One limit applies to text: a {@link Text} that changes while the form is open reaches the
 * player only while it stays {@link Text.Literal}. A translation cannot be sent as an update, so
 * changing to or between translations is kept for the next time the form is shown. Text that has to
 * be translated should be set before showing.
 *
 * @param <T> the type of the value
 * @since 2.0
 */
public interface Value<T> {
  /**
   * Returns a value that never changes.
   *
   * @param value the value
   * @param <T> the type of the value
   * @return a value that always reads as the given value
   */
  static <T> @NonNull Value<T> of(@NonNull T value) {
    Objects.requireNonNull(value, "value");
    return new Value<T>() {
      @Override
      public @NonNull T get() {
        return value;
      }

      @Override
      public @Nullable Observable<T> observable() {
        return null;
      }

      @Override
      public String toString() {
        return "Value.of(" + value + ")";
      }
    };
  }

  /**
   * Returns a value that follows the given observable.
   *
   * @param observable the observable to follow
   * @param <T> the type of the value
   * @return a value that reads whatever the observable holds at the time
   */
  static <T> @NonNull Value<T> following(@NonNull Observable<T> observable) {
    Objects.requireNonNull(observable, "observable");
    return new Value<T>() {
      @Override
      public @NonNull T get() {
        return observable.get();
      }

      @Override
      public @NonNull Observable<T> observable() {
        return observable;
      }

      @Override
      public String toString() {
        return "Value.following(" + observable.get() + ")";
      }
    };
  }

  /** Returns the value as it is now. */
  @NonNull T get();

  /** Returns the observable this value follows, or null if the value is fixed. */
  @Nullable Observable<T> observable();
}
