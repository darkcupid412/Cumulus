/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.DataDrivenCloseReason;
import org.geysermc.cumulus.datadrivenui.DataDrivenForm;
import org.geysermc.cumulus.datadrivenui.component.ButtonOptions;
import org.geysermc.cumulus.datadrivenui.component.DataDrivenComponent;
import org.geysermc.cumulus.datadrivenui.component.DisplayOptions;
import org.geysermc.cumulus.datadrivenui.component.Dropdown;
import org.geysermc.cumulus.datadrivenui.component.InputOptions;
import org.geysermc.cumulus.datadrivenui.component.SliderOptions;
import org.geysermc.cumulus.datadrivenui.component.impl.ComponentImpls;
import org.geysermc.cumulus.datadrivenui.util.Observable;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/** The {@link DataDrivenForm} implementation and its builder. */
public final class DataDrivenFormImpl implements DataDrivenForm {
  // Read only. The options classes are mutable, so a setter call on any of these would change the
  // default for every form that does not pass options of its own.
  private static final DisplayOptions DISPLAY_DEFAULTS = DisplayOptions.options();
  private static final InputOptions INPUT_DEFAULTS = InputOptions.options();
  private static final ButtonOptions BUTTON_DEFAULTS = ButtonOptions.options();
  private static final SliderOptions SLIDER_DEFAULTS = SliderOptions.options();

  private final Value<Text> title;
  private final List<DataDrivenComponent> components;
  private final boolean closeButton;
  private final @Nullable Consumer<DataDrivenCloseReason> closeHandler;

  private DataDrivenFormImpl(
      Value<Text> title,
      List<DataDrivenComponent> components,
      boolean closeButton,
      @Nullable Consumer<DataDrivenCloseReason> closeHandler) {
    this.title = title;
    this.components = components;
    this.closeButton = closeButton;
    this.closeHandler = closeHandler;
  }

  public static DataDrivenForm.@NonNull Builder builder(@NonNull Value<Text> title) {
    return new Builder(Objects.requireNonNull(title, "title"));
  }

  @Override
  public @NonNull Value<Text> title() {
    return title;
  }

  @Override
  public @NonNull List<@NonNull DataDrivenComponent> components() {
    return components;
  }

  @Override
  public boolean hasCloseButton() {
    return closeButton;
  }

  @Override
  public @Nullable Consumer<DataDrivenCloseReason> closeHandler() {
    return closeHandler;
  }

  static final class Builder implements DataDrivenForm.Builder {
    private final Value<Text> title;
    private final List<DataDrivenComponent> components = new ArrayList<>();
    private boolean closeButton;
    private @Nullable Consumer<DataDrivenCloseReason> closeHandler;
    private boolean built;

    Builder(Value<Text> title) {
      this.title = title;
    }

    @Override
    public DataDrivenForm.@NonNull Builder header(@NonNull Value<Text> text) {
      return header(text, DISPLAY_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder header(@NonNull String text) {
      return header(Text.of(text));
    }

    @Override
    public DataDrivenForm.@NonNull Builder header(
        @NonNull Value<Text> text, @NonNull DisplayOptions options) {
      return add(ComponentImpls.textual(DataDrivenComponent.Kind.HEADER, text, options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder label(@NonNull Value<Text> text) {
      return label(text, DISPLAY_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder label(@NonNull String text) {
      return label(Text.of(text));
    }

    @Override
    public DataDrivenForm.@NonNull Builder label(
        @NonNull Value<Text> text, @NonNull DisplayOptions options) {
      return add(ComponentImpls.textual(DataDrivenComponent.Kind.LABEL, text, options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder divider() {
      return divider(DISPLAY_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder divider(@NonNull DisplayOptions options) {
      return add(ComponentImpls.plain(DataDrivenComponent.Kind.DIVIDER, options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder spacer() {
      return spacer(DISPLAY_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder spacer(@NonNull DisplayOptions options) {
      return add(ComponentImpls.plain(DataDrivenComponent.Kind.SPACER, options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder textField(
        @NonNull Value<Text> label, @NonNull Observable<String> value) {
      return textField(label, value, INPUT_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder textField(
        @NonNull String label, @NonNull Observable<String> value) {
      return textField(Text.of(label), value);
    }

    @Override
    public DataDrivenForm.@NonNull Builder textField(
        @NonNull Value<Text> label,
        @NonNull Observable<String> value,
        @NonNull InputOptions options) {
      return add(
          ComponentImpls.textField(
              label, value, options.description(), options.disabled(), options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder toggle(
        @NonNull Value<Text> label, @NonNull Observable<Boolean> value) {
      return toggle(label, value, INPUT_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder toggle(
        @NonNull String label, @NonNull Observable<Boolean> value) {
      return toggle(Text.of(label), value);
    }

    @Override
    public DataDrivenForm.@NonNull Builder toggle(
        @NonNull Value<Text> label,
        @NonNull Observable<Boolean> value,
        @NonNull InputOptions options) {
      return add(
          ComponentImpls.toggle(
              label, value, options.description(), options.disabled(), options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder slider(
        @NonNull Value<Text> label, @NonNull Observable<Double> value, double min, double max) {
      return slider(label, value, min, max, SLIDER_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder slider(
        @NonNull String label, @NonNull Observable<Double> value, double min, double max) {
      return slider(Text.of(label), value, min, max);
    }

    @Override
    public DataDrivenForm.@NonNull Builder slider(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        double min,
        double max,
        @NonNull SliderOptions options) {
      return slider(label, value, Value.of(min), Value.of(max), options);
    }

    @Override
    public DataDrivenForm.@NonNull Builder slider(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        @NonNull Value<Double> min,
        @NonNull Value<Double> max,
        @NonNull SliderOptions options) {
      return add(
          ComponentImpls.slider(
              label,
              value,
              min,
              max,
              options.step(),
              options.description(),
              options.disabled(),
              options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder dropdown(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        @NonNull List<Dropdown.@NonNull Item> items) {
      return dropdown(label, value, items, INPUT_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder dropdown(
        @NonNull String label,
        @NonNull Observable<Double> value,
        @NonNull List<Dropdown.@NonNull Item> items) {
      return dropdown(Text.of(label), value, items);
    }

    @Override
    public DataDrivenForm.@NonNull Builder dropdown(
        @NonNull Value<Text> label,
        @NonNull Observable<Double> value,
        @NonNull List<Dropdown.@NonNull Item> items,
        @NonNull InputOptions options) {
      return add(
          ComponentImpls.dropdown(
              label, value, items, options.description(), options.disabled(), options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder button(
        @NonNull Value<Text> label, @NonNull Runnable onClick) {
      return button(label, onClick, BUTTON_DEFAULTS);
    }

    @Override
    public DataDrivenForm.@NonNull Builder button(
        @NonNull String label, @NonNull Runnable onClick) {
      return button(Text.of(label), onClick);
    }

    @Override
    public DataDrivenForm.@NonNull Builder button(
        @NonNull Value<Text> label, @NonNull Runnable onClick, @NonNull ButtonOptions options) {
      return add(
          ComponentImpls.button(
              label, onClick, options.tooltip(), options.disabled(), options.visible()));
    }

    @Override
    public DataDrivenForm.@NonNull Builder closeButton() {
      checkNotBuilt();
      closeButton = true;
      return this;
    }

    @Override
    public DataDrivenForm.@NonNull Builder onClose(
        @NonNull Consumer<@NonNull DataDrivenCloseReason> handler) {
      checkNotBuilt();
      closeHandler = Objects.requireNonNull(handler, "handler");
      return this;
    }

    @Override
    public @NonNull DataDrivenForm build() {
      checkNotBuilt();
      built = true;
      return new DataDrivenFormImpl(
          title,
          Collections.unmodifiableList(new ArrayList<>(components)),
          closeButton,
          closeHandler);
    }

    private DataDrivenForm.Builder add(DataDrivenComponent component) {
      checkNotBuilt();
      if (components.size() >= MAX_COMPONENTS) {
        throw new IllegalStateException(
            "a form can have at most " + MAX_COMPONENTS + " components");
      }
      components.add(component);
      return this;
    }

    private void checkNotBuilt() {
      if (built) {
        // The components hand out live observables; a second form built from the same builder
        // would share them, and with them every edit the player makes.
        throw new IllegalStateException("this builder has already built its form");
      }
    }
  }
}
