/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.component;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.util.Observable;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * A single component of a data-driven form. Components are created through the form's builder and
 * are immutable once the form is built; what changes while the form is open are the {@link
 * org.geysermc.cumulus.datadrivenui.util.Observable} values a component was built with.
 *
 * @since 2.0
 */
public interface DataDrivenComponent {
  /**
   * Returns which kind of component this is, so that it can be handled without instanceof checks.
   */
  @NonNull Kind kind();

  /**
   * Returns whether the component is shown. Follows its value while the form is open, so a hidden
   * component can be revealed without resending the form.
   */
  @NonNull Value<Boolean> visible();

  /**
   * The kinds of component a data-driven form can hold.
   *
   * <p>New kinds go at the end. Hosts report which of these a connection can show as a bitmask
   * keyed by each constant's position, so inserting one anywhere else changes what every later bit
   * means to a build compiled against a different version of this class.
   *
   * @since 2.0
   */
  enum Kind {
    HEADER,
    LABEL,
    DIVIDER,
    SPACER,
    TEXT_FIELD,
    TOGGLE,
    SLIDER,
    DROPDOWN,
    BUTTON,
    IMAGE
  }

  /**
   * A component that shows a piece of text: a {@link Kind#HEADER} or a {@link Kind#LABEL}.
   *
   * @since 2.0
   */
  interface Textual extends DataDrivenComponent {
    /** Returns the text that is shown. Follows its value while the form is open. */
    @NonNull Value<Text> text();
  }

  /**
   * A component the player gives a value to: a {@link TextField}, {@link Toggle}, {@link Slider} or
   * {@link Dropdown}.
   *
   * @param <T> the type of the value the player edits
   * @since 2.0
   */
  interface Input<T> extends DataDrivenComponent {
    /** Returns the label shown next to the component. Follows its value while the form is open. */
    @NonNull Value<Text> label();

    /**
     * Returns the observable holding the component's value. The player's edits are written into it,
     * and setting it while the form is open changes what the player sees.
     */
    @NonNull Observable<T> value();

    /** Returns the smaller text shown under the label, or null if there is none. */
    @Nullable Value<Text> description();

    /** Returns whether the player is kept from using the component. */
    @NonNull Value<Boolean> disabled();
  }

  /**
   * A single-line text field.
   *
   * @since 2.0
   */
  interface TextField extends Input<String> {}

  /**
   * An on-off switch.
   *
   * @since 2.0
   */
  interface Toggle extends Input<Boolean> {}
}
