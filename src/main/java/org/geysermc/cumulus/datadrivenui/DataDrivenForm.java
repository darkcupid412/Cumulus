/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui;

import java.util.List;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.geysermc.cumulus.datadrivenui.component.ButtonOptions;
import org.geysermc.cumulus.datadrivenui.component.DataDrivenComponent;
import org.geysermc.cumulus.datadrivenui.component.DisplayOptions;
import org.geysermc.cumulus.datadrivenui.component.Dropdown;
import org.geysermc.cumulus.datadrivenui.component.InputOptions;
import org.geysermc.cumulus.datadrivenui.component.SliderOptions;
import org.geysermc.cumulus.datadrivenui.impl.DataDrivenFormImpl;
import org.geysermc.cumulus.datadrivenui.util.Observable;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * A data-driven form. Unlike a regular form, it stays live while it is open: buttons run their
 * callbacks without closing it, the player's edits arrive as they happen through the observables
 * the form was built with, and setting those observables changes what the player sees.
 *
 * <p>A form is built for one showing. Its observables hold live state, and showing the same
 * instance twice (to anyone) would make both showings share edits. Build a new form, with new
 * observables, for every showing.
 *
 * @since 2.0
 */
public interface DataDrivenForm {
  /**
   * How many components a form may have; the builder throws {@link IllegalStateException} when a
   * component past this is added.
   */
  int MAX_COMPONENTS = 100;

  /**
   * Returns a builder for a form with the given title.
   *
   * @param title the title shown at the top of the form
   * @return a builder holding no components yet
   */
  static @NonNull Builder builder(@NonNull Value<Text> title) {
    return DataDrivenFormImpl.builder(title);
  }

  /** Returns a builder for a form with the given title. */
  static @NonNull Builder builder(@NonNull String title) {
    return builder(Text.of(title));
  }

  /** Returns the title shown at the top of the form. Follows its value while the form is open. */
  @NonNull Value<Text> title();

  /**
   * Returns the components in the order they are shown, as an unmodifiable list. The close button
   * is not part of this list.
   */
  @NonNull List<@NonNull DataDrivenComponent> components();

  /** Returns whether the form has a close button at the bottom. */
  boolean hasCloseButton();

  /** Returns the callback run when the form is no longer open, or null if there is none. */
  @Nullable Consumer<@NonNull DataDrivenCloseReason> closeHandler();

  /**
   * Builds a {@link DataDrivenForm}. A builder builds one form: {@link #build()} can only be called
   * once, because the components hand out live observables that a second form would end up sharing.
   *
   * @since 2.0
   */
  interface Builder {
    /** Adds a heading. */
    @This @NonNull Builder header(@NonNull Value<Text> text);

    /** Adds a heading. */
    @This @NonNull Builder header(@NonNull String text);

    /** Adds a heading. */
    @This @NonNull Builder header(@NonNull Value<Text> text, @NonNull DisplayOptions options);

    /** Adds a piece of text. */
    @This @NonNull Builder label(@NonNull Value<Text> text);

    /** Adds a piece of text. */
    @This @NonNull Builder label(@NonNull String text);

    /** Adds a piece of text. */
    @This @NonNull Builder label(@NonNull Value<Text> text, @NonNull DisplayOptions options);

    /** Adds a horizontal line. */
    @This @NonNull Builder divider();

    /** Adds a horizontal line. */
    @This @NonNull Builder divider(@NonNull DisplayOptions options);

    /** Adds empty space. */
    @This @NonNull Builder spacer();

    /** Adds empty space. */
    @This @NonNull Builder spacer(@NonNull DisplayOptions options);

    /**
     * Adds a text field. The player's typing is written into the observable as it happens, and
     * setting the observable changes the field's content on screen.
     */
    @This @NonNull Builder textField(@NonNull Value<Text> label, @NonNull Observable<String> value);

    /** Adds a text field. */
    @This @NonNull Builder textField(@NonNull String label, @NonNull Observable<String> value);

    /** Adds a text field. */
    @This @NonNull Builder textField(
        @NonNull Value<Text> label,
        @NonNull Observable<String> value,
        @NonNull InputOptions options);

    /** Adds a toggle. */
    @This @NonNull Builder toggle(@NonNull Value<Text> label, @NonNull Observable<Boolean> value);

    /** Adds a toggle. */
    @This @NonNull Builder toggle(@NonNull String label, @NonNull Observable<Boolean> value);

    /** Adds a toggle. */
    @This @NonNull Builder toggle(
        @NonNull Value<Text> label,
        @NonNull Observable<Boolean> value,
        @NonNull InputOptions options);

    /** Adds a slider over the given range. */
    @This @NonNull Builder slider(
        @NonNull Value<Text> label, @NonNull Observable<Double> value, double min, double max);

    /** Adds a slider over the given range. */
    @This @NonNull Builder slider(
        @NonNull String label, @NonNull Observable<Double> value, double min, double max);

    /** Adds a slider over the given range. */
    @This @NonNull Builder slider(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        double min,
        double max,
        @NonNull SliderOptions options);

    /** Adds a slider whose range can change while the form is open. */
    @This @NonNull Builder slider(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        @NonNull Value<Double> min,
        @NonNull Value<Double> max,
        @NonNull SliderOptions options);

    /**
     * Adds a dropdown. Choosing an entry writes that entry's value (not its position) into the
     * observable, so the observable's initial value has to be one of the entry values.
     */
    @This @NonNull Builder dropdown(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        @NonNull List<Dropdown.@NonNull Item> items);

    /** Adds a dropdown. */
    @This @NonNull Builder dropdown(
        @NonNull String label,
        @NonNull Observable<Double> value,
        @NonNull List<Dropdown.@NonNull Item> items);

    /** Adds a dropdown. */
    @This @NonNull Builder dropdown(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        @NonNull List<Dropdown.@NonNull Item> items,
        @NonNull InputOptions options);

    /**
     * Adds a button. Pressing it does not close the form: the callback runs while the form stays
     * open, and can be run again on the next press.
     */
    @This @NonNull Builder button(@NonNull Value<Text> label, @NonNull Runnable onClick);

    /** Adds a button. */
    @This @NonNull Builder button(@NonNull String label, @NonNull Runnable onClick);

    /** Adds a button. */
    @This @NonNull Builder button(
        @NonNull Value<Text> label, @NonNull Runnable onClick, @NonNull ButtonOptions options);

    /**
     * Gives the form a close button at the bottom. Its label is supplied by the client, in the
     * player's language.
     */
    @This @NonNull Builder closeButton();

    /**
     * Sets the callback run when the form is no longer open: because the player closed it, the
     * server closed it, or it could not be shown at all. It runs at most once.
     */
    @This @NonNull Builder onClose(@NonNull Consumer<@NonNull DataDrivenCloseReason> handler);

    /**
     * Builds the form. Can only be called once.
     *
     * @throws IllegalStateException when called a second time
     */
    @NonNull DataDrivenForm build();
  }
}
