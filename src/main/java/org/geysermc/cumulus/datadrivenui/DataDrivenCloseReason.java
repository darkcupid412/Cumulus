/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui;

/**
 * Why a data-driven form is no longer open.
 *
 * @since 2.0
 */
public enum DataDrivenCloseReason {
  /** The player closed the form, by pressing a button or dismissing the screen. */
  CLIENT_CLOSED,
  /** The server closed the form, or replaced it with another one. */
  SERVER_CLOSED,
  /**
   * The client refused to open the form because another screen was already open, and it was not
   * possible to show it before giving up. The player has not seen the form.
   */
  USER_BUSY
}
