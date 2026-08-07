/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.component;

import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.component.impl.ComponentImpls;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/**
 * A dropdown. Every entry carries its own numeric value, and choosing an entry writes that value
 * (not the entry's position) into the observable the dropdown was built with. The host showing the
 * form ignores a written value that belongs to no entry.
 *
 * <p>Which entry is chosen can change while the form is open; the entries themselves cannot.
 *
 * @since 2.0
 */
public interface Dropdown extends DataDrivenComponent.Input<Double> {
  /** How many entries a dropdown may have. */
  int MAX_ITEMS = 100;

  /** Returns the entries, in the order they are shown, as an unmodifiable list. */
  @NonNull List<@NonNull Item> items();

  /**
   * A dropdown entry.
   *
   * @since 2.0
   */
  interface Item {
    /**
     * Returns a new entry.
     *
     * @param label the text shown for the entry
     * @param value the value written into the dropdown's observable when the entry is chosen
     * @return an entry with the given label and value, and no description
     */
    static @NonNull Item of(@NonNull Value<Text> label, double value) {
      return ComponentImpls.item(label, value, null);
    }

    /** Returns a new entry. */
    static @NonNull Item of(@NonNull Text label, double value) {
      return of(Value.of(label), value);
    }

    /** Returns a new entry. */
    static @NonNull Item of(@NonNull String label, double value) {
      return of(Text.of(label), value);
    }

    /** Returns a new entry with a smaller text shown under it. */
    static @NonNull Item of(@NonNull String label, double value, @NonNull String description) {
      return of(Text.of(label), value, Text.of(description));
    }

    /** Returns a new entry with a smaller text shown under it. */
    static @NonNull Item of(
        @NonNull Value<Text> label, double value, @NonNull Value<Text> description) {
      Objects.requireNonNull(description, "description");
      return ComponentImpls.item(label, value, description);
    }

    /** Returns a new entry with a smaller text shown under it. */
    static @NonNull Item of(@NonNull Text label, double value, @NonNull Text description) {
      return of(Value.of(label), value, Value.of(description));
    }

    /** Returns the text shown for this entry, read when the entry is put in front of a player. */
    @NonNull Value<Text> label();

    /** Returns the value written into the dropdown's observable when this entry is chosen. */
    double value();

    /**
     * Returns the smaller text shown near the dropdown while this entry is the chosen one, or null
     * if there is none.
     */
    @Nullable Value<Text> description();
  }
}
