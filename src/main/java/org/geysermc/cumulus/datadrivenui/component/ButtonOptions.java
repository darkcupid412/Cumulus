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
 * Options for a button.
 *
 * <p>An options object is read when the component is added to a form; changing it afterwards has no
 * effect on components already added.
 *
 * @since 2.0
 */
public final class ButtonOptions {
  private @Nullable Value<Text> tooltip;
  private Value<Boolean> disabled = Value.of(false);
  private Value<Boolean> visible = Value.of(true);

  private ButtonOptions() {}

  /** Returns a new options object with everything at its default. */
  public static @NonNull ButtonOptions options() {
    return new ButtonOptions();
  }

  /** Sets the text shown when hovering over the button. */
  public @This @NonNull ButtonOptions tooltip(@NonNull Value<Text> tooltip) {
    this.tooltip = Objects.requireNonNull(tooltip, "tooltip");
    return this;
  }

  /** Sets the text shown when hovering over the button. */
  public @This @NonNull ButtonOptions tooltip(@NonNull String tooltip) {
    return tooltip(Text.of(tooltip));
  }

  /** Sets whether the player is kept from pressing the button. */
  public @This @NonNull ButtonOptions disabled(boolean disabled) {
    this.disabled = Value.of(disabled);
    return this;
  }

  /**
   * Makes the disabled state follow an observable, so the button can be enabled and disabled while
   * the form is open.
   */
  public @This @NonNull ButtonOptions disabled(@NonNull Observable<Boolean> disabled) {
    this.disabled = Value.following(Objects.requireNonNull(disabled, "disabled"));
    return this;
  }

  /** Sets whether the button is shown. */
  public @This @NonNull ButtonOptions visible(boolean visible) {
    this.visible = Value.of(visible);
    return this;
  }

  /**
   * Makes the button's visibility follow an observable, so it can be shown and hidden while the
   * form is open.
   */
  public @This @NonNull ButtonOptions visible(@NonNull Observable<Boolean> visible) {
    this.visible = Value.following(Objects.requireNonNull(visible, "visible"));
    return this;
  }

  /** Returns the tooltip as set so far, or null if there is none. */
  public @Nullable Value<Text> tooltip() {
    return tooltip;
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
