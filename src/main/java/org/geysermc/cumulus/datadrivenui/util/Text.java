/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.util;

import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.util.impl.TextImpl;

/**
 * Text shown by a data-driven form. Unlike the text of a regular form, which is a plain string
 * resolved by the server before it is sent, this is resolved by the client. That allows a
 * translation key to be sent as-is and rendered in the language the player actually uses.
 *
 * <p>There are three kinds, matching the shapes the client accepts: {@link Literal} text shown
 * as-is, a {@link Translatable} key resolved with its arguments, and a {@link Sequence} of parts
 * shown one after another.
 *
 * <p>Instances are immutable. The plain form-building methods each have an overload taking a {@link
 * String}, equivalent to {@link #of(String)}; the option-bearing overloads take {@code
 * Value<Text>}, since a caller already assembling options can wrap a string once.
 *
 * @since 2.0
 */
public interface Text extends Value<Text> {
  /**
   * Returns text that is shown exactly as given.
   *
   * @param text the text to show
   * @return literal text holding the given string
   */
  static @NonNull Text of(@NonNull String text) {
    return TextImpl.literal(text);
  }

  /**
   * Returns text that the client resolves from its own language files.
   *
   * @param key the translation key, for example {@code gui.ok}
   * @param args values substituted into the translation, in order
   * @return translatable text holding the given key and arguments
   */
  static @NonNull Text translatable(@NonNull String key, @NonNull Text @NonNull ... args) {
    return TextImpl.translatable(key, args);
  }

  /**
   * Returns text made of several parts, shown one after another.
   *
   * @param parts the parts to show, in order
   * @return text holding the given parts, in order
   */
  static @NonNull Text sequence(@NonNull Text @NonNull ... parts) {
    return TextImpl.sequence(parts);
  }

  /**
   * Text is itself a fixed {@link Value}, so it can be passed directly anywhere a form takes a
   * {@code Value<Text>}.
   */
  @Override
  default @NonNull Text get() {
    return this;
  }

  @Override
  default @Nullable Observable<Text> observable() {
    return null;
  }

  /**
   * Text shown exactly as given.
   *
   * @since 2.0
   */
  interface Literal extends Text {
    /** Returns the text to show. */
    @NonNull String text();
  }

  /**
   * Text the client resolves from its own language files.
   *
   * @since 2.0
   */
  interface Translatable extends Text {
    /** Returns the translation key, for example {@code gui.ok}. */
    @NonNull String key();

    /** Returns the values substituted into the translation, as an unmodifiable list. */
    @NonNull List<@NonNull Text> args();
  }

  /**
   * Text made of several parts, shown one after another.
   *
   * @since 2.0
   */
  interface Sequence extends Text {
    /** Returns the parts to show, as an unmodifiable list. */
    @NonNull List<@NonNull Text> parts();
  }
}
