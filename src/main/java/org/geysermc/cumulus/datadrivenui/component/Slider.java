/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.component;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * A slider over a numeric range. The player's chosen value is written into the observable the
 * slider was built with; the host showing the form brings values the player sends outside the range
 * back into it before anything sees them.
 *
 * <p>Bounds that follow observables are checked by that host when the form is shown; only fixed
 * bounds can be judged while the slider is built. A range that is momentarily impossible while a
 * plugin moves both ends is tolerated rather than fatal, and the clamp keeps the value inside
 * whichever end it can.
 *
 * @since 2.0
 */
public interface Slider extends DataDrivenComponent.Input<Double> {
  /** Returns the smallest value the player can choose. */
  @NonNull Value<Double> min();

  /** Returns the largest value the player can choose. */
  @NonNull Value<Double> max();

  /** Returns the distance between values the player can choose, or null for the client default. */
  @Nullable Value<Double> step();
}
