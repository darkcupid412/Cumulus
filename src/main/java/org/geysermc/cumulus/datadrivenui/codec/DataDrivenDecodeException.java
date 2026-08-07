/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.codec;

/**
 * Thrown when an encoded form cannot be decoded: the payload is malformed, truncated, breaks a
 * limit, or was written by a codec version this one does not speak. The version has to match
 * exactly; an older payload is refused as firmly as a newer one.
 *
 * @since 2.0
 */
public class DataDrivenDecodeException extends Exception {
  private static final long serialVersionUID = 1L;

  public DataDrivenDecodeException(String message) {
    super(message);
  }

  public DataDrivenDecodeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Thrown when a payload contains a component this codec does not know. Unlike a malformed payload
   * this is an expected case: a newer sender used a component the receiver has to reject as a
   * whole, because silently dropping it would show the player a form missing part of its meaning.
   *
   * @since 2.0
   */
  public static final class UnsupportedComponent extends DataDrivenDecodeException {
    private static final long serialVersionUID = 1L;

    private final int kindId;

    public UnsupportedComponent(int kindId) {
      super("unknown component kind " + kindId);
      this.kindId = kindId;
    }

    /** Returns the wire id of the component kind that was not recognized. */
    public int kindId() {
      return kindId;
    }
  }
}
