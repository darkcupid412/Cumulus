/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.codec;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The properties of a shown form that can change or be reported after it was sent: the vocabulary
 * shared by both ends of a connection for live updates in either direction. Each property has a
 * fixed wire id that never changes meaning.
 *
 * @since 2.0
 */
public enum DataDrivenProperty {
  /** The form's title. */
  TITLE(0),
  /** The text of a heading or label, or the label of an input or button. */
  TEXT(1),
  /** The value of an input component. */
  VALUE(2),
  /** The smaller text under an input's label. */
  DESCRIPTION(3),
  /** Whether the player is kept from using a component. */
  DISABLED(4),
  /** Whether a component is shown. */
  VISIBLE(5),
  /** The smallest value of a slider. */
  MIN(6),
  /** The largest value of a slider. */
  MAX(7),
  /** The distance between values of a slider. */
  STEP(8),
  /** The text shown when hovering over a button. */
  TOOLTIP(9),
  /** The body text of a message box. */
  BODY(10),
  /** The label of a message box's first button. */
  BUTTON1(11),
  /** The tooltip of a message box's first button. */
  BUTTON1_TOOLTIP(12),
  /** The label of a message box's second button. */
  BUTTON2(13),
  /** The tooltip of a message box's second button. */
  BUTTON2_TOOLTIP(14),
  /** A button press. Carries no value; which button is said by the component it names. */
  CLICK(15);

  private static final DataDrivenProperty[] BY_ID;

  static {
    int highest = 0;
    for (DataDrivenProperty property : values()) {
      highest = Math.max(highest, property.id);
    }
    BY_ID = new DataDrivenProperty[highest + 1];
    for (DataDrivenProperty property : values()) {
      BY_ID[property.id] = property;
    }
  }

  private final int id;

  DataDrivenProperty(int id) {
    this.id = id;
  }

  /** Returns the property with the given wire id, or null if there is none. */
  public static @Nullable DataDrivenProperty fromId(int id) {
    if (id < 0 || id >= BY_ID.length) {
      return null;
    }
    return BY_ID[id];
  }

  /** Returns this property's wire id. */
  public int id() {
    return id;
  }
}
