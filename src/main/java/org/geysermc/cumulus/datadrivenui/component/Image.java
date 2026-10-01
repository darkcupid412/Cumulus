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
 * A picture drawn from a resource pack. Mirrors {@code CustomForm.image(src, pack, options)} in
 * {@code @minecraft/server-ui-bindings} 2.2.0.
 *
 * <p>An image may be clickable. When it is, it behaves like a {@link Button} that happens to be
 * drawn as a picture: the callback runs without closing the form, and can run again. That is what
 * makes an image grid — a party, a shop, a crate — possible at all, since a regular form can only
 * put text on a button.
 *
 * @since 2.0
 */
public interface Image extends DataDrivenComponent {
  /**
   * Returns the texture path inside the pack, as a pack-relative path without its extension (for
   * example {@code textures/ui/party/pikachu}).
   */
  @NonNull String src();

  /**
   * Returns the id of the pack the texture is read from. This is the pack's UUID as its manifest
   * declares it, not its folder name.
   */
  @NonNull String pack();

  /** Returns the width in pixels, or null to let the client size the image. */
  @Nullable Value<Double> width();

  /** Returns the callback run every time the image is pressed, or null if it is not clickable. */
  @Nullable Runnable onClick();

  /** Returns the text shown when hovering over the image, or null if there is none. */
  @Nullable Value<Text> tooltip();
}
