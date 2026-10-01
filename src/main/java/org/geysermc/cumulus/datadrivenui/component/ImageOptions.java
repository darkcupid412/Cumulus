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
 * Options for an image. Mirrors {@code ImageOptions} in {@code @minecraft/server-ui-bindings}
 * 2.2.0: {@code onClick}, {@code tooltip}, {@code visible}, {@code width}.
 *
 * <p>An options object is read when the component is added to a form; changing it afterwards has no
 * effect on components already added.
 *
 * @since 2.0
 */
public final class ImageOptions {
  private @Nullable Runnable onClick;
  private @Nullable Value<Double> width;
  private @Nullable Value<Text> tooltip;
  private Value<Boolean> visible = Value.of(true);

  private ImageOptions() {}

  /** Returns a new options object with everything at its default. */
  public static @NonNull ImageOptions options() {
    return new ImageOptions();
  }

  /**
   * Makes the image clickable, running this every time it is pressed. Without it the image is
   * decoration and the client will not let the player focus it.
   */
  public @This @NonNull ImageOptions onClick(@NonNull Runnable onClick) {
    this.onClick = Objects.requireNonNull(onClick, "onClick");
    return this;
  }

  /** Sets the width in pixels. Left unset, the client sizes the image itself. */
  public @This @NonNull ImageOptions width(double width) {
    return width(Value.of(width));
  }

  /** Sets the width in pixels. */
  public @This @NonNull ImageOptions width(@NonNull Value<Double> width) {
    this.width = Objects.requireNonNull(width, "width");
    return this;
  }

  /** Sets the text shown when hovering over the image. */
  public @This @NonNull ImageOptions tooltip(@NonNull Value<Text> tooltip) {
    this.tooltip = Objects.requireNonNull(tooltip, "tooltip");
    return this;
  }

  /** Sets the text shown when hovering over the image. */
  public @This @NonNull ImageOptions tooltip(@NonNull String tooltip) {
    return tooltip(Text.of(tooltip));
  }

  /** Sets whether the image is shown. */
  public @This @NonNull ImageOptions visible(boolean visible) {
    this.visible = Value.of(visible);
    return this;
  }

  /** Sets whether the image is shown, following the observable while the form is open. */
  public @This @NonNull ImageOptions visible(@NonNull Observable<Boolean> visible) {
    this.visible = Value.following(Objects.requireNonNull(visible, "visible"));
    return this;
  }

  public @Nullable Runnable onClick() {
    return onClick;
  }

  public @Nullable Value<Double> width() {
    return width;
  }

  public @Nullable Value<Text> tooltip() {
    return tooltip;
  }

  public @NonNull Value<Boolean> visible() {
    return visible;
  }
}
