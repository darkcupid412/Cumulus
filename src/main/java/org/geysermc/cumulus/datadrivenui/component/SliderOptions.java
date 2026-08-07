/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.component;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.geysermc.cumulus.datadrivenui.util.Observable;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * Options for a slider: the ones every input has, and the step, the distance between values the
 * player can choose, which is left to the client when it is not given.
 *
 * <p>A step that follows an observable is checked by the host when the form is shown, not here:
 * only a fixed one can be judged while building, since a reactive one may be anything at any moment
 * before the form is used.
 *
 * <p>An options object is read when the component is added to a form; changing it afterwards has no
 * effect on components already added.
 *
 * @since 2.0
 */
public final class SliderOptions {
  private @Nullable Value<Text> description;
  private Value<Boolean> disabled = Value.of(false);
  private Value<Boolean> visible = Value.of(true);
  private @Nullable Value<Double> step;

  private SliderOptions() {}

  /** Returns a new options object with everything at its default. */
  public static @NonNull SliderOptions options() {
    return new SliderOptions();
  }

  /** Sets the distance between values the player can choose. */
  public @This @NonNull SliderOptions step(double step) {
    this.step = Value.of(step);
    return this;
  }

  /** Makes the step follow an observable, so it can change while the form is open. */
  public @This @NonNull SliderOptions step(@NonNull Observable<Double> step) {
    this.step = Value.following(Objects.requireNonNull(step, "step"));
    return this;
  }

  /** Sets the smaller text shown under the slider's label. */
  public @This @NonNull SliderOptions description(@NonNull Value<Text> description) {
    this.description = Objects.requireNonNull(description, "description");
    return this;
  }

  /** Sets the smaller text shown under the slider's label. */
  public @This @NonNull SliderOptions description(@NonNull String description) {
    return description(Text.of(description));
  }

  /** Sets whether the player is kept from using the slider. */
  public @This @NonNull SliderOptions disabled(boolean disabled) {
    this.disabled = Value.of(disabled);
    return this;
  }

  /**
   * Makes the disabled state follow an observable, so the slider can be enabled and disabled while
   * the form is open.
   */
  public @This @NonNull SliderOptions disabled(@NonNull Observable<Boolean> disabled) {
    this.disabled = Value.following(Objects.requireNonNull(disabled, "disabled"));
    return this;
  }

  /** Sets whether the slider is shown. */
  public @This @NonNull SliderOptions visible(boolean visible) {
    this.visible = Value.of(visible);
    return this;
  }

  /**
   * Makes the slider's visibility follow an observable, so it can be shown and hidden while the
   * form is open.
   */
  public @This @NonNull SliderOptions visible(@NonNull Observable<Boolean> visible) {
    this.visible = Value.following(Objects.requireNonNull(visible, "visible"));
    return this;
  }

  /** Returns the step as set so far, or null for the client default. */
  public @Nullable Value<Double> step() {
    return step;
  }

  /** Returns the description as set so far, or null if there is none. */
  public @Nullable Value<Text> description() {
    return description;
  }

  /** Returns the disabled state as set so far. */
  public @NonNull Value<Boolean> disabled() {
    return disabled;
  }

  /** Returns the visibility as set so far. */
  public @NonNull Value<Boolean> visible() {
    return visible;
  }
}
