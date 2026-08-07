/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.component;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * A button. Unlike a button on a regular form, pressing it does not close the form: the callback
 * runs while the form stays open, so it can be pressed again, and the callback can change what the
 * form shows.
 *
 * @since 2.0
 */
public interface Button extends DataDrivenComponent {
  /** Returns the text on the button. Follows its value while the form is open. */
  @NonNull Value<Text> label();

  /** Returns the callback run every time the button is pressed. */
  @NonNull Runnable onClick();

  /** Returns the text shown when hovering over the button, or null if there is none. */
  @Nullable Value<Text> tooltip();

  /** Returns whether the player is kept from pressing the button. */
  @NonNull Value<Boolean> disabled();
}
