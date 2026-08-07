/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui;

import java.util.Collection;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.cumulus.datadrivenui.component.DataDrivenComponent;
import org.geysermc.cumulus.datadrivenui.impl.DataDrivenCapabilitiesImpl;

/**
 * What a connection can do with data-driven forms. Check before building one, and fall back to a
 * regular form when the answer is no or not known yet.
 *
 * <p>There are three states because the answer sometimes has to arrive over a connection, and can
 * simply not be there yet. {@link State#UNKNOWN} answers no when asked, but the real answer may
 * still arrive; {@link State#UNSUPPORTED} means it never will.
 *
 * @since 2.0
 */
public interface DataDrivenCapabilities {
  /** Returns capabilities that are not known yet. */
  static @NonNull DataDrivenCapabilities unknown() {
    return DataDrivenCapabilitiesImpl.UNKNOWN;
  }

  /**
   * Returns capabilities that say data-driven forms are not available and never will be on this
   * connection.
   */
  static @NonNull DataDrivenCapabilities unsupported() {
    return DataDrivenCapabilitiesImpl.UNSUPPORTED;
  }

  /**
   * Returns capabilities supporting exactly the given component kinds. An empty collection reports
   * {@link State#UNSUPPORTED}: "supported, but nothing can be shown" would pass the documented
   * state check and then fail every form.
   *
   * @param kinds the component kinds the connection can show
   * @return capabilities supporting those kinds, or unsupported if the collection is empty
   */
  static @NonNull DataDrivenCapabilities of(
      @NonNull Collection<DataDrivenComponent.@NonNull Kind> kinds) {
    Objects.requireNonNull(kinds, "kinds");
    return DataDrivenCapabilitiesImpl.of(kinds);
  }

  /** Returns whether the answer is known, and if so which way. */
  @NonNull State state();

  /**
   * Returns whether the connection can show the given component kind. False whenever {@link
   * #state()} is not {@link State#SUPPORTED}.
   */
  boolean supports(DataDrivenComponent.@NonNull Kind kind);

  /**
   * How much is known about a connection's data-driven form support.
   *
   * @since 2.0
   */
  enum State {
    /** The answer has not arrived yet; it may still become either of the other two. */
    UNKNOWN,
    /** Data-driven forms are not available on this connection. */
    UNSUPPORTED,
    /** Data-driven forms are available; ask {@link #supports} per component kind. */
    SUPPORTED
  }
}
