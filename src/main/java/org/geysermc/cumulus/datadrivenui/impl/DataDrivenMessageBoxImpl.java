/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.impl;

import java.util.Objects;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.DataDrivenCloseReason;
import org.geysermc.cumulus.datadrivenui.DataDrivenMessageBox;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/** The {@link DataDrivenMessageBox} implementation and its builder. */
public final class DataDrivenMessageBoxImpl implements DataDrivenMessageBox {
  private final Value<Text> title;
  private final Value<Text> body;
  private final Value<Text> button1;
  private final @Nullable Value<Text> button1Tooltip;
  private final Value<Text> button2;
  private final @Nullable Value<Text> button2Tooltip;
  private final @Nullable Consumer<Result> resultHandler;

  private DataDrivenMessageBoxImpl(
      Value<Text> title,
      Value<Text> body,
      Value<Text> button1,
      @Nullable Value<Text> button1Tooltip,
      Value<Text> button2,
      @Nullable Value<Text> button2Tooltip,
      @Nullable Consumer<Result> resultHandler) {
    this.title = title;
    this.body = body;
    this.button1 = button1;
    this.button1Tooltip = button1Tooltip;
    this.button2 = button2;
    this.button2Tooltip = button2Tooltip;
    this.resultHandler = resultHandler;
  }

  public static DataDrivenMessageBox.@NonNull Builder builder(@NonNull Value<Text> title) {
    return new Builder(Objects.requireNonNull(title, "title"));
  }

  /** Returns a result, for hosts reporting how a message box ended. */
  public static @NonNull Result result(
      @Nullable Integer selection, @NonNull DataDrivenCloseReason reason) {
    Objects.requireNonNull(reason, "reason");
    if (selection != null && selection != 1 && selection != 2) {
      throw new IllegalArgumentException("selection must be 1, 2 or null, got " + selection);
    }
    if (selection != null && reason != DataDrivenCloseReason.CLIENT_CLOSED) {
      // A button press is the player closing the box; any other ending happened instead of one.
      throw new IllegalArgumentException(
          "a selection only goes with " + DataDrivenCloseReason.CLIENT_CLOSED + ", not " + reason);
    }
    return new ResultImpl(selection, reason);
  }

  @Override
  public @NonNull Value<Text> title() {
    return title;
  }

  @Override
  public @NonNull Value<Text> body() {
    return body;
  }

  @Override
  public @NonNull Value<Text> button1() {
    return button1;
  }

  @Override
  public @Nullable Value<Text> button1Tooltip() {
    return button1Tooltip;
  }

  @Override
  public @NonNull Value<Text> button2() {
    return button2;
  }

  @Override
  public @Nullable Value<Text> button2Tooltip() {
    return button2Tooltip;
  }

  @Override
  public @Nullable Consumer<Result> resultHandler() {
    return resultHandler;
  }

  private static final class ResultImpl implements Result {
    private final @Nullable Integer selection;
    private final DataDrivenCloseReason reason;

    ResultImpl(@Nullable Integer selection, DataDrivenCloseReason reason) {
      this.selection = selection;
      this.reason = reason;
    }

    @Override
    public @Nullable Integer selection() {
      return selection;
    }

    @Override
    public @NonNull DataDrivenCloseReason reason() {
      return reason;
    }

    @Override
    public String toString() {
      return "Result(" + selection + ", " + reason + ")";
    }
  }

  static final class Builder implements DataDrivenMessageBox.Builder {
    private final Value<Text> title;
    private Value<Text> body = Text.of("");
    private @Nullable Value<Text> button1;
    private @Nullable Value<Text> button1Tooltip;
    private @Nullable Value<Text> button2;
    private @Nullable Value<Text> button2Tooltip;
    private @Nullable Consumer<Result> resultHandler;
    private boolean built;

    Builder(Value<Text> title) {
      this.title = title;
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder body(@NonNull Value<Text> body) {
      checkNotBuilt();
      this.body = Objects.requireNonNull(body, "body");
      return this;
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder body(@NonNull String body) {
      return body(Text.of(body));
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button1(@NonNull Value<Text> label) {
      checkNotBuilt();
      this.button1 = Objects.requireNonNull(label, "label");
      this.button1Tooltip = null;
      return this;
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button1(@NonNull String label) {
      return button1(Text.of(label));
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button1(
        @NonNull Value<Text> label, @NonNull Value<Text> tooltip) {
      button1(label);
      this.button1Tooltip = Objects.requireNonNull(tooltip, "tooltip");
      return this;
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button1(
        @NonNull String label, @NonNull String tooltip) {
      return button1(Text.of(label), Text.of(tooltip));
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button2(@NonNull Value<Text> label) {
      checkNotBuilt();
      this.button2 = Objects.requireNonNull(label, "label");
      this.button2Tooltip = null;
      return this;
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button2(@NonNull String label) {
      return button2(Text.of(label));
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button2(
        @NonNull Value<Text> label, @NonNull Value<Text> tooltip) {
      button2(label);
      this.button2Tooltip = Objects.requireNonNull(tooltip, "tooltip");
      return this;
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder button2(
        @NonNull String label, @NonNull String tooltip) {
      return button2(Text.of(label), Text.of(tooltip));
    }

    @Override
    public DataDrivenMessageBox.@NonNull Builder onResult(
        @NonNull Consumer<@NonNull Result> handler) {
      checkNotBuilt();
      this.resultHandler = Objects.requireNonNull(handler, "handler");
      return this;
    }

    @Override
    public @NonNull DataDrivenMessageBox build() {
      checkNotBuilt();
      if (button1 == null || button2 == null) {
        throw new IllegalStateException("a message box needs both of its buttons");
      }
      built = true;
      return new DataDrivenMessageBoxImpl(
          title, body, button1, button1Tooltip, button2, button2Tooltip, resultHandler);
    }

    private void checkNotBuilt() {
      if (built) {
        throw new IllegalStateException("this builder has already built its message box");
      }
    }
  }
}
