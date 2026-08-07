/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.cumulus.datadrivenui.DataDrivenCapabilities;
import org.geysermc.cumulus.datadrivenui.component.DataDrivenComponent;

/** The {@link DataDrivenCapabilities} implementation. */
public final class DataDrivenCapabilitiesImpl implements DataDrivenCapabilities {
  public static final DataDrivenCapabilitiesImpl UNKNOWN =
      new DataDrivenCapabilitiesImpl(
          State.UNKNOWN, Collections.<DataDrivenComponent.Kind>emptySet());
  public static final DataDrivenCapabilitiesImpl UNSUPPORTED =
      new DataDrivenCapabilitiesImpl(
          State.UNSUPPORTED, Collections.<DataDrivenComponent.Kind>emptySet());

  private final State state;
  private final Set<DataDrivenComponent.Kind> kinds;

  private DataDrivenCapabilitiesImpl(State state, Set<DataDrivenComponent.Kind> kinds) {
    this.state = state;
    this.kinds = kinds;
  }

  public static @NonNull DataDrivenCapabilities of(
      @NonNull Collection<DataDrivenComponent.@NonNull Kind> kinds) {
    if (kinds.isEmpty()) {
      // "Supported, but nothing can be shown" passes the documented state check and then fails
      // every form; a platform with no showable component does not support data-driven forms.
      return UNSUPPORTED;
    }
    return new DataDrivenCapabilitiesImpl(
        State.SUPPORTED, Collections.unmodifiableSet(EnumSet.copyOf(kinds)));
  }

  @Override
  public @NonNull State state() {
    return state;
  }

  @Override
  public boolean supports(DataDrivenComponent.@NonNull Kind kind) {
    Objects.requireNonNull(kind, "kind");
    return state == State.SUPPORTED && kinds.contains(kind);
  }

  @Override
  public String toString() {
    return "DataDrivenCapabilities(" + state + ", " + kinds + ")";
  }
}
