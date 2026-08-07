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
 * Options for a component the player gives a value to: a text field, toggle or dropdown.
 *
 * <p>An options object is read when the component is added to a form; changing it afterwards has no
 * effect on components already added.
 *
 * @since 2.0
 */
public final class InputOptions {
  private @Nullable Value<Text> description;
  private Value<Boolean> disabled = Value.of(false);
  private Value<Boolean> visible = Value.of(true);

  private InputOptions() {}

  /** Returns a new options object with everything at its default. */
  public static @NonNull InputOptions options() {
    return new InputOptions();
  }

  /**
   * Sets the smaller text shown under the component's label. Pass a {@link Text} for a fixed
   * description or {@link Value#following} for one that changes while the form is open.
   */
  public @This @NonNull InputOptions description(@NonNull Value<Text> description) {
    this.description = Objects.requireNonNull(description, "description");
    return this;
  }

  /** Sets the smaller text shown under the component's label. */
  public @This @NonNull InputOptions description(@NonNull String description) {
    return description(Text.of(description));
  }

  /** Sets whether the player is kept from using the component. */
  public @This @NonNull InputOptions disabled(boolean disabled) {
    this.disabled = Value.of(disabled);
    return this;
  }

  /**
   * Makes the disabled state follow an observable, so the component can be enabled and disabled
   * while the form is open.
   */
  public @This @NonNull InputOptions disabled(@NonNull Observable<Boolean> disabled) {
    this.disabled = Value.following(Objects.requireNonNull(disabled, "disabled"));
    return this;
  }

  /** Sets whether the component is shown. */
  public @This @NonNull InputOptions visible(boolean visible) {
    this.visible = Value.of(visible);
    return this;
  }

  /**
   * Makes the component's visibility follow an observable, so it can be shown and hidden while the
   * form is open.
   */
  public @This @NonNull InputOptions visible(@NonNull Observable<Boolean> visible) {
    this.visible = Value.following(Objects.requireNonNull(visible, "visible"));
    return this;
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
