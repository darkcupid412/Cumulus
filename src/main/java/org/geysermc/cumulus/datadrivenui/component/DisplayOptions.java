/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.component;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.geysermc.cumulus.datadrivenui.util.Observable;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * Options for a component that only shows something. A divider and a spacer have no text of their
 * own, so this holds what all four have in common and nothing that would only mean something on a
 * header or a label.
 *
 * <p>An options object is read when the component is added to a form; changing it afterwards has no
 * effect on components already added.
 *
 * @since 2.0
 */
public final class DisplayOptions {
  private Value<Boolean> visible = Value.of(true);

  private DisplayOptions() {}

  /** Returns a new options object with everything at its default. */
  public static @NonNull DisplayOptions options() {
    return new DisplayOptions();
  }

  /** Sets whether the component is shown. */
  public @This @NonNull DisplayOptions visible(boolean visible) {
    this.visible = Value.of(visible);
    return this;
  }

  /**
   * Makes the component's visibility follow an observable, so it can be shown and hidden while the
   * form is open.
   */
  public @This @NonNull DisplayOptions visible(@NonNull Observable<Boolean> visible) {
    this.visible = Value.following(Objects.requireNonNull(visible, "visible"));
    return this;
  }

  /** Returns the visibility as set so far. */
  public @NonNull Value<Boolean> visible() {
    return visible;
  }
}
