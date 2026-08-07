/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui;

/**
 * A handle to one showing of a data-driven form or message box. It is how the server side closes
 * what it opened; everything else about a live form happens through the observables and callbacks
 * it was built with.
 *
 * @since 2.0
 */
public interface DataDrivenSession {
  /**
   * Returns whether the screen is still on its way to the player or in front of them. False once it
   * has closed for any reason, including never having been shown at all.
   */
  boolean open();

  /**
   * Closes the screen. The close callback runs once the closing has happened, normally with {@link
   * DataDrivenCloseReason#SERVER_CLOSED}; if the player closed the screen at the same moment, the
   * reason is theirs rather than the server's. Closing an already closed session does nothing.
   */
  void close();
}
