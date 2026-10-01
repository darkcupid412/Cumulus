/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.component.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.component.Button;
import org.geysermc.cumulus.datadrivenui.component.Image;
import org.geysermc.cumulus.datadrivenui.component.DataDrivenComponent;
import org.geysermc.cumulus.datadrivenui.component.Dropdown;
import org.geysermc.cumulus.datadrivenui.component.Slider;
import org.geysermc.cumulus.datadrivenui.util.Observable;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;

/** Implementations of the data-driven form components, created through the form builder. */
public final class ComponentImpls {
  private ComponentImpls() {}

  public static DataDrivenComponent.Textual textual(
      DataDrivenComponent.Kind kind, Value<Text> text, Value<Boolean> visible) {
    return new TextualImpl(kind, text, visible);
  }

  public static DataDrivenComponent plain(DataDrivenComponent.Kind kind, Value<Boolean> visible) {
    return new PlainImpl(kind, visible);
  }

  public static DataDrivenComponent.TextField textField(
      Value<Text> label,
      Observable<String> value,
      @Nullable Value<Text> description,
      Value<Boolean> disabled,
      Value<Boolean> visible) {
    return new TextFieldImpl(label, value, description, disabled, visible);
  }

  public static DataDrivenComponent.Toggle toggle(
      Value<Text> label,
      Observable<Boolean> value,
      @Nullable Value<Text> description,
      Value<Boolean> disabled,
      Value<Boolean> visible) {
    return new ToggleImpl(label, value, description, disabled, visible);
  }

  public static Slider slider(
      Value<Text> label,
      Observable<Double> value,
      Value<Double> min,
      Value<Double> max,
      @Nullable Value<Double> step,
      @Nullable Value<Text> description,
      Value<Boolean> disabled,
      Value<Boolean> visible) {
    requireFinite(value.get(), "value");
    Double fixedMin = fixedOrNull(min);
    Double fixedMax = fixedOrNull(max);
    if (fixedMin != null) {
      requireFinite(fixedMin, "min");
    }
    if (fixedMax != null) {
      requireFinite(fixedMax, "max");
    }
    if (fixedMin != null && fixedMax != null && fixedMin > fixedMax) {
      throw new IllegalArgumentException("min " + fixedMin + " is larger than max " + fixedMax);
    }
    if ((fixedMin != null && value.get() < fixedMin)
        || (fixedMax != null && value.get() > fixedMax)) {
      throw new IllegalArgumentException(
          "the initial value " + value.get() + " is outside the range");
    }
    if (step != null) {
      Double fixedStep = fixedOrNull(step);
      if (fixedStep != null) {
        requireFinite(fixedStep, "step");
        if (fixedStep <= 0) {
          throw new IllegalArgumentException("step must be positive, got " + fixedStep);
        }
      }
    }
    return new SliderImpl(label, value, min, max, step, description, disabled, visible);
  }

  public static Dropdown dropdown(
      Value<Text> label,
      Observable<Double> value,
      List<Dropdown.Item> items,
      @Nullable Value<Text> description,
      Value<Boolean> disabled,
      Value<Boolean> visible) {
    Objects.requireNonNull(items, "items");
    if (items.isEmpty()) {
      throw new IllegalArgumentException("a dropdown needs at least one item");
    }
    if (items.size() > Dropdown.MAX_ITEMS) {
      throw new IllegalArgumentException(
          "a dropdown can have at most " + Dropdown.MAX_ITEMS + " items, got " + items.size());
    }
    List<Dropdown.Item> copy = new ArrayList<>(items.size());
    for (Dropdown.Item item : items) {
      Objects.requireNonNull(item, "item");
      for (Dropdown.Item other : copy) {
        if (other.value() == item.value()) {
          // Selection is reported by value, so two items sharing one would be indistinguishable.
          throw new IllegalArgumentException("two items have the value " + item.value());
        }
      }
      copy.add(item);
    }
    double initial = value.get();
    if (!hasItemValue(copy, initial)) {
      throw new IllegalArgumentException("no item has the initial value " + initial);
    }
    return new DropdownImpl(
        label, value, Collections.unmodifiableList(copy), description, disabled, visible);
  }

  public static Dropdown.Item item(
      Value<Text> label, double value, @Nullable Value<Text> description) {
    requireFinite(value, "value");
    return new ItemImpl(label, value, description);
  }

  public static Button button(
      Value<Text> label,
      Runnable onClick,
      @Nullable Value<Text> tooltip,
      Value<Boolean> disabled,
      Value<Boolean> visible) {
    return new ButtonImpl(label, onClick, tooltip, disabled, visible);
  }

  public static Image image(
      String src,
      String pack,
      @Nullable Value<Double> width,
      @Nullable Runnable onClick,
      @Nullable Value<Text> tooltip,
      Value<Boolean> visible) {
    return new ImageImpl(src, pack, width, onClick, tooltip, visible);
  }

  private static boolean hasItemValue(List<Dropdown.Item> items, double value) {
    for (Dropdown.Item item : items) {
      if (item.value() == value) {
        return true;
      }
    }
    return false;
  }

  private static void requireFinite(double value, String name) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException(name + " must be a finite number, got " + value);
    }
  }

  private static @Nullable Double fixedOrNull(Value<Double> value) {
    return value.observable() == null ? value.get() : null;
  }

  abstract static class BaseImpl implements DataDrivenComponent {
    private final Kind kind;
    private final Value<Boolean> visible;

    BaseImpl(Kind kind, Value<Boolean> visible) {
      this.kind = Objects.requireNonNull(kind, "kind");
      this.visible = Objects.requireNonNull(visible, "visible");
    }

    @Override
    public @NonNull Kind kind() {
      return kind;
    }

    @Override
    public @NonNull Value<Boolean> visible() {
      return visible;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "(" + kind + ")";
    }
  }

  static final class PlainImpl extends BaseImpl {
    PlainImpl(Kind kind, Value<Boolean> visible) {
      super(kind, visible);
    }
  }

  static final class TextualImpl extends BaseImpl implements DataDrivenComponent.Textual {
    private final Value<Text> text;

    TextualImpl(Kind kind, Value<Text> text, Value<Boolean> visible) {
      super(kind, visible);
      this.text = Objects.requireNonNull(text, "text");
    }

    @Override
    public @NonNull Value<Text> text() {
      return text;
    }
  }

  static class InputImpl<T> extends BaseImpl implements DataDrivenComponent.Input<T> {
    private final Value<Text> label;
    private final Observable<T> value;
    private final @Nullable Value<Text> description;
    private final Value<Boolean> disabled;

    InputImpl(
        Kind kind,
        Value<Text> label,
        Observable<T> value,
        @Nullable Value<Text> description,
        Value<Boolean> disabled,
        Value<Boolean> visible) {
      super(kind, visible);
      this.label = Objects.requireNonNull(label, "label");
      this.value = Objects.requireNonNull(value, "value");
      this.description = description;
      this.disabled = Objects.requireNonNull(disabled, "disabled");
    }

    @Override
    public @NonNull Value<Text> label() {
      return label;
    }

    @Override
    public @NonNull Observable<T> value() {
      return value;
    }

    @Override
    public @Nullable Value<Text> description() {
      return description;
    }

    @Override
    public @NonNull Value<Boolean> disabled() {
      return disabled;
    }
  }

  static final class TextFieldImpl extends InputImpl<String>
      implements DataDrivenComponent.TextField {
    TextFieldImpl(
        Value<Text> label,
        Observable<String> value,
        @Nullable Value<Text> description,
        Value<Boolean> disabled,
        Value<Boolean> visible) {
      super(Kind.TEXT_FIELD, label, value, description, disabled, visible);
    }
  }

  static final class ToggleImpl extends InputImpl<Boolean> implements DataDrivenComponent.Toggle {
    ToggleImpl(
        Value<Text> label,
        Observable<Boolean> value,
        @Nullable Value<Text> description,
        Value<Boolean> disabled,
        Value<Boolean> visible) {
      super(Kind.TOGGLE, label, value, description, disabled, visible);
    }
  }

  static final class SliderImpl extends InputImpl<Double> implements Slider {
    private final Value<Double> min;
    private final Value<Double> max;
    private final @Nullable Value<Double> step;

    SliderImpl(
        Value<Text> label,
        Observable<Double> value,
        Value<Double> min,
        Value<Double> max,
        @Nullable Value<Double> step,
        @Nullable Value<Text> description,
        Value<Boolean> disabled,
        Value<Boolean> visible) {
      super(Kind.SLIDER, label, value, description, disabled, visible);
      this.min = Objects.requireNonNull(min, "min");
      this.max = Objects.requireNonNull(max, "max");
      this.step = step;
    }

    @Override
    public @NonNull Value<Double> min() {
      return min;
    }

    @Override
    public @NonNull Value<Double> max() {
      return max;
    }

    @Override
    public @Nullable Value<Double> step() {
      return step;
    }
  }

  static final class DropdownImpl extends InputImpl<Double> implements Dropdown {
    private final List<Item> items;

    DropdownImpl(
        Value<Text> label,
        Observable<Double> value,
        List<Item> items,
        @Nullable Value<Text> description,
        Value<Boolean> disabled,
        Value<Boolean> visible) {
      super(Kind.DROPDOWN, label, value, description, disabled, visible);
      this.items = items;
    }

    @Override
    public @NonNull List<@NonNull Item> items() {
      return items;
    }
  }

  static final class ItemImpl implements Dropdown.Item {
    private final Value<Text> label;
    private final double value;
    private final @Nullable Value<Text> description;

    ItemImpl(Value<Text> label, double value, @Nullable Value<Text> description) {
      this.label = Objects.requireNonNull(label, "label");
      this.value = value;
      this.description = description;
    }

    @Override
    public @NonNull Value<Text> label() {
      return label;
    }

    @Override
    public double value() {
      return value;
    }

    @Override
    public @Nullable Value<Text> description() {
      return description;
    }

    @Override
    public String toString() {
      return "Item(" + label + "=" + value + ")";
    }
  }

  static final class ImageImpl extends BaseImpl implements Image {
    private final String src;
    private final String pack;
    private final @Nullable Value<Double> width;
    private final @Nullable Runnable onClick;
    private final @Nullable Value<Text> tooltip;

    ImageImpl(
        String src,
        String pack,
        @Nullable Value<Double> width,
        @Nullable Runnable onClick,
        @Nullable Value<Text> tooltip,
        Value<Boolean> visible) {
      super(Kind.IMAGE, visible);
      this.src = Objects.requireNonNull(src, "src");
      this.pack = Objects.requireNonNull(pack, "pack");
      this.width = width;
      this.onClick = onClick;
      this.tooltip = tooltip;
    }

    @Override
    public @NonNull String src() {
      return src;
    }

    @Override
    public @NonNull String pack() {
      return pack;
    }

    @Override
    public @Nullable Value<Double> width() {
      return width;
    }

    @Override
    public @Nullable Runnable onClick() {
      return onClick;
    }

    @Override
    public @Nullable Value<Text> tooltip() {
      return tooltip;
    }
  }

  static final class ButtonImpl extends BaseImpl implements Button {
    private final Value<Text> label;
    private final Runnable onClick;
    private final @Nullable Value<Text> tooltip;
    private final Value<Boolean> disabled;

    ButtonImpl(
        Value<Text> label,
        Runnable onClick,
        @Nullable Value<Text> tooltip,
        Value<Boolean> disabled,
        Value<Boolean> visible) {
      super(Kind.BUTTON, visible);
      this.label = Objects.requireNonNull(label, "label");
      this.onClick = Objects.requireNonNull(onClick, "onClick");
      this.tooltip = tooltip;
      this.disabled = Objects.requireNonNull(disabled, "disabled");
    }

    @Override
    public @NonNull Value<Text> label() {
      return label;
    }

    @Override
    public @NonNull Runnable onClick() {
      return onClick;
    }

    @Override
    public @Nullable Value<Text> tooltip() {
      return tooltip;
    }

    @Override
    public @NonNull Value<Boolean> disabled() {
      return disabled;
    }
  }
}
