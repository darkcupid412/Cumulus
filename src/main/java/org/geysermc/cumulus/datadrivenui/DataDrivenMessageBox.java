/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.geysermc.cumulus.datadrivenui.impl.DataDrivenMessageBoxImpl;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * A data-driven dialog with a body and two buttons. Unlike the buttons of a {@link DataDrivenForm},
 * pressing either one closes it, and the result says which one it was.
 *
 * <p>Like a form, a message box is built for one showing.
 *
 * @since 2.0
 */
public interface DataDrivenMessageBox {
  /** Returns a builder for a message box with the given title. */
  static @NonNull Builder builder(@NonNull Value<Text> title) {
    return DataDrivenMessageBoxImpl.builder(title);
  }

  /** Returns a builder for a message box with the given title. */
  static @NonNull Builder builder(@NonNull String title) {
    return builder(Text.of(title));
  }

  /** Returns the title. Follows its value while the message box is open. */
  @NonNull Value<Text> title();

  /** Returns the body text. Follows its value while the message box is open. */
  @NonNull Value<Text> body();

  /** Returns the label of the first button. */
  @NonNull Value<Text> button1();

  /** Returns the tooltip of the first button, or null if there is none. */
  @Nullable Value<Text> button1Tooltip();

  /** Returns the label of the second button. */
  @NonNull Value<Text> button2();

  /** Returns the tooltip of the second button, or null if there is none. */
  @Nullable Value<Text> button2Tooltip();

  /** Returns the callback run with the result, or null if there is none. */
  @Nullable Consumer<@NonNull Result> resultHandler();

  /**
   * How a message box ended.
   *
   * @since 2.0
   */
  interface Result {
    /**
     * Returns a result, for hosts reporting how a message box ended.
     *
     * @param selection 1 or 2 for the button that was pressed, or null when none was
     * @param reason how the message box closed
     * @return a result carrying that selection and reason
     * @throws IllegalArgumentException when the selection is neither 1, 2 nor null, or when a
     *     selection comes with any reason other than {@link DataDrivenCloseReason#CLIENT_CLOSED}; a
     *     press is the player closing the box, and any other ending happened instead of one
     */
    static @NonNull Result of(@Nullable Integer selection, @NonNull DataDrivenCloseReason reason) {
      return DataDrivenMessageBoxImpl.result(selection, reason);
    }

    /**
     * Returns which button was pressed, 1 or 2. Null when the message box closed without a press:
     * the player dismissed it, the server closed it, or it could not be shown at all.
     */
    @Nullable Integer selection();

    /** Returns why the message box is no longer open. */
    @NonNull DataDrivenCloseReason reason();
  }

  /**
   * Builds a {@link DataDrivenMessageBox}. Both buttons have to be given before {@link #build()},
   * and build can only be called once.
   *
   * @since 2.0
   */
  interface Builder {
    /** Sets the body text. */
    @This @NonNull Builder body(@NonNull Value<Text> body);

    /** Sets the body text. */
    @This @NonNull Builder body(@NonNull String body);

    /** Sets the first button, clearing any tooltip it was given before. */
    @This @NonNull Builder button1(@NonNull Value<Text> label);

    /** Sets the first button, clearing any tooltip it was given before. */
    @This @NonNull Builder button1(@NonNull String label);

    /** Sets the first button, with a tooltip shown when hovering over it. */
    @This @NonNull Builder button1(@NonNull Value<Text> label, @NonNull Value<Text> tooltip);

    /** Sets the first button and its tooltip. */
    @This @NonNull Builder button1(@NonNull String label, @NonNull String tooltip);

    /** Sets the second button, clearing any tooltip it was given before. */
    @This @NonNull Builder button2(@NonNull Value<Text> label);

    /** Sets the second button, clearing any tooltip it was given before. */
    @This @NonNull Builder button2(@NonNull String label);

    /** Sets the second button, with a tooltip shown when hovering over it. */
    @This @NonNull Builder button2(@NonNull Value<Text> label, @NonNull Value<Text> tooltip);

    /** Sets the second button and its tooltip. */
    @This @NonNull Builder button2(@NonNull String label, @NonNull String tooltip);

    /** Sets the callback run with the result. It runs at most once. */
    @This @NonNull Builder onResult(@NonNull Consumer<@NonNull Result> handler);

    /**
     * Builds the message box. Can only be called once.
     *
     * @throws IllegalStateException when a button is missing or when called a second time
     */
    @NonNull DataDrivenMessageBox build();
  }
}
