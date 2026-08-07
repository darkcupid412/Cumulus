/*
 * Copyright (c) 2020-2026 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package org.geysermc.cumulus.datadrivenui.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.datadrivenui.DataDrivenCloseReason;
import org.geysermc.cumulus.datadrivenui.DataDrivenForm;
import org.geysermc.cumulus.datadrivenui.DataDrivenMessageBox;
import org.geysermc.cumulus.datadrivenui.component.Button;
import org.geysermc.cumulus.datadrivenui.component.ButtonOptions;
import org.geysermc.cumulus.datadrivenui.component.DataDrivenComponent;
import org.geysermc.cumulus.datadrivenui.component.DisplayOptions;
import org.geysermc.cumulus.datadrivenui.component.Dropdown;
import org.geysermc.cumulus.datadrivenui.component.InputOptions;
import org.geysermc.cumulus.datadrivenui.component.Slider;
import org.geysermc.cumulus.datadrivenui.component.SliderOptions;
import org.geysermc.cumulus.datadrivenui.util.Observable;
import org.geysermc.cumulus.datadrivenui.util.Text;
import org.geysermc.cumulus.datadrivenui.util.Value;
import org.geysermc.cumulus.datadrivenui.util.impl.TextImpl;

/**
 * Carries a built {@link DataDrivenForm} or {@link DataDrivenMessageBox} between two processes that
 * both have Cumulus: what crosses the wire is the components and their current values, never
 * anything about how a client shows them.
 *
 * <p>Fields are written as id-length-value records, so a decoder can skip fields it does not know.
 * A whole component it does not know cannot be skipped, because the form would be missing part of
 * its meaning, so that fails with {@link DataDrivenDecodeException.UnsupportedComponent}, which the
 * sender side avoids by asking about capabilities first.
 *
 * <p>Callbacks cannot cross a wire. Encoding ignores them, and decoding takes a {@link Callbacks}
 * that supplies their stand-ins, typically ones that report the press or close back to where the
 * real callbacks live.
 *
 * <p>Every property of a decoded form is backed by a fresh observable, whether or not it was
 * reactive when built, because the decoding side has to be able to apply updates that arrive later
 * to any of them. The one exception is a dropdown entry's own text, which cannot change while a
 * form is open and so stays plain. Properties absent when encoded (a description, tooltip or step
 * that was never given) stay absent.
 *
 * @since 2.0
 */
public final class DataDrivenCodec {
  /**
   * The version of this encoding. It only changes when the format changes in a way skipping unknown
   * fields cannot absorb; a decoder refuses any payload whose version is not exactly its own.
   */
  public static final int CODEC_VERSION = 1;

  /**
   * The largest payload this codec will produce or read. Every individual limit can be met and
   * still add up to far more than a form is: a hundred components each holding a hundred dropdown
   * entries of five thousand characters is legal by every other rule here.
   */
  public static final int MAX_PAYLOAD_LENGTH = 1 << 20;

  private static final int SCREEN_FORM = 0;
  private static final int SCREEN_MESSAGE_BOX = 1;

  private static final int FORM_TITLE = 1;
  private static final int FORM_COMPONENTS = 2;
  private static final int FORM_CLOSE_BUTTON = 3;

  private static final int BOX_TITLE = 1;
  private static final int BOX_BODY = 2;
  private static final int BOX_BUTTON1 = 3;
  private static final int BOX_BUTTON1_TOOLTIP = 4;
  private static final int BOX_BUTTON2 = 5;
  private static final int BOX_BUTTON2_TOOLTIP = 6;

  private static final int COMPONENT_TEXT = 1;
  private static final int COMPONENT_VALUE = 2;
  private static final int COMPONENT_DESCRIPTION = 3;
  private static final int COMPONENT_DISABLED = 4;
  private static final int COMPONENT_VISIBLE = 5;
  private static final int COMPONENT_MIN = 6;
  private static final int COMPONENT_MAX = 7;
  private static final int COMPONENT_STEP = 8;
  private static final int COMPONENT_ITEMS = 9;
  private static final int COMPONENT_TOOLTIP = 10;

  private static final int TEXT_LITERAL = 0;
  private static final int TEXT_TRANSLATABLE = 1;
  private static final int TEXT_SEQUENCE = 2;

  private static final int VALUE_TEXT = 0;
  private static final int VALUE_STRING = 1;
  private static final int VALUE_DOUBLE = 2;
  private static final int VALUE_BOOLEAN = 3;

  private DataDrivenCodec() {}

  /**
   * The stand-ins a decoder wires into the form it builds, in place of the callbacks that stayed
   * behind on the encoding side. A method may return null when nothing has to happen.
   *
   * @since 2.0
   */
  public interface Callbacks {
    /** Returns what runs when the button at the given component position is pressed. */
    @Nullable Runnable buttonClicked(int componentIndex);

    /** Returns what runs when a decoded form is no longer open. */
    @Nullable Consumer<DataDrivenCloseReason> formClosed();

    /** Returns what runs with a decoded message box's result. */
    @Nullable Consumer<DataDrivenMessageBox.Result> messageBoxClosed();
  }

  /**
   * What a payload decoded into: a form or a message box, never both.
   *
   * @since 2.0
   */
  public static final class Decoded {
    private final @Nullable DataDrivenForm form;
    private final @Nullable DataDrivenMessageBox messageBox;

    private Decoded(@Nullable DataDrivenForm form, @Nullable DataDrivenMessageBox messageBox) {
      this.form = form;
      this.messageBox = messageBox;
    }

    /** Returns the decoded form, or null when the payload held a message box. */
    public @Nullable DataDrivenForm form() {
      return form;
    }

    /** Returns the decoded message box, or null when the payload held a form. */
    public @Nullable DataDrivenMessageBox messageBox() {
      return messageBox;
    }
  }

  /**
   * Encodes a form. Its callbacks are not part of the result.
   *
   * @throws IllegalArgumentException when the form's observables hold a state that cannot cross the
   *     wire: a string past its limit, a slider value that cannot be clamped, a dropdown selection
   *     no entry has, or a form larger than {@link #MAX_PAYLOAD_LENGTH}
   */
  public static byte[] encode(@NonNull DataDrivenForm form) {
    Objects.requireNonNull(form, "form");
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bytes);
      out.writeByte(CODEC_VERSION);
      out.writeByte(SCREEN_FORM);
      field(out, FORM_TITLE, text(form.title().get()));
      if (form.hasCloseButton()) {
        field(out, FORM_CLOSE_BUTTON, new byte[] {1});
      }
      field(out, FORM_COMPONENTS, components(form.components()));
      return sized(bytes.toByteArray());
    } catch (IOException exception) {
      // A ByteArrayOutputStream has nowhere to fail; what reaches here is a value the format
      // cannot hold, which is the caller's to fix.
      throw new IllegalArgumentException("this form cannot be encoded", exception);
    }
  }

  /**
   * Encodes a message box. Its result handler is not part of the result.
   *
   * @throws IllegalArgumentException when a text holds a string past its limit, or the box is
   *     larger than {@link #MAX_PAYLOAD_LENGTH}
   */
  public static byte[] encode(@NonNull DataDrivenMessageBox box) {
    Objects.requireNonNull(box, "box");
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bytes);
      out.writeByte(CODEC_VERSION);
      out.writeByte(SCREEN_MESSAGE_BOX);
      field(out, BOX_TITLE, text(box.title().get()));
      field(out, BOX_BODY, text(box.body().get()));
      field(out, BOX_BUTTON1, text(box.button1().get()));
      if (box.button1Tooltip() != null) {
        field(out, BOX_BUTTON1_TOOLTIP, text(box.button1Tooltip().get()));
      }
      field(out, BOX_BUTTON2, text(box.button2().get()));
      if (box.button2Tooltip() != null) {
        field(out, BOX_BUTTON2_TOOLTIP, text(box.button2Tooltip().get()));
      }
      return sized(bytes.toByteArray());
    } catch (IOException exception) {
      throw new IllegalArgumentException("this message box cannot be encoded", exception);
    }
  }

  /**
   * Decodes a payload written by {@link #encode}.
   *
   * @param payload the encoded form or message box
   * @param callbacks the stand-ins wired in place of the callbacks that stayed behind
   * @throws DataDrivenDecodeException when the payload is malformed, breaks a limit, contains an
   *     unknown component, or was written by a newer codec
   */
  public static @NonNull Decoded decode(byte @NonNull [] payload, @NonNull Callbacks callbacks)
      throws DataDrivenDecodeException {
    Objects.requireNonNull(payload, "payload");
    Objects.requireNonNull(callbacks, "callbacks");
    if (payload.length > MAX_PAYLOAD_LENGTH) {
      throw new DataDrivenDecodeException(
          "payload is " + payload.length + " bytes, at most " + MAX_PAYLOAD_LENGTH);
    }
    try {
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
      int version = in.readUnsignedByte();
      if (version != CODEC_VERSION) {
        // Not "anything older will do": version 0 never existed, and a version this build has
        // never seen cannot be read by guessing.
        throw new DataDrivenDecodeException(
            "payload has codec version " + version + ", this codec speaks " + CODEC_VERSION);
      }
      int screen = in.readUnsignedByte();
      Map<Integer, byte[]> fields = fields(in);
      switch (screen) {
        case SCREEN_FORM:
          return new Decoded(decodeForm(fields, callbacks), null);
        case SCREEN_MESSAGE_BOX:
          return new Decoded(null, decodeMessageBox(fields, callbacks));
        default:
          throw new DataDrivenDecodeException("unknown screen kind " + screen);
      }
    } catch (DataDrivenDecodeException exception) {
      throw exception;
    } catch (IOException | IllegalArgumentException | IllegalStateException exception) {
      throw new DataDrivenDecodeException("malformed payload", exception);
    }
  }

  private static DataDrivenForm decodeForm(Map<Integer, byte[]> fields, Callbacks callbacks)
      throws IOException, DataDrivenDecodeException {
    DataDrivenForm.Builder builder =
        DataDrivenForm.builder(reactiveText(readText(require(fields, FORM_TITLE))));
    if (marker(fields, FORM_CLOSE_BUTTON)) {
      builder.closeButton();
    }
    Consumer<DataDrivenCloseReason> closed = callbacks.formClosed();
    if (closed != null) {
      builder.onClose(closed);
    }

    DataInputStream in = input(require(fields, FORM_COMPONENTS));
    int count = in.readUnsignedShort();
    if (count > DataDrivenForm.MAX_COMPONENTS) {
      throw new DataDrivenDecodeException("payload claims " + count + " components");
    }
    for (int index = 0; index < count; index++) {
      int kindId = in.readUnsignedByte();
      int length = in.readInt();
      if (length < 0 || length > in.available()) {
        throw new DataDrivenDecodeException("component " + index + " claims " + length + " bytes");
      }
      byte[] payload = new byte[length];
      in.readFully(payload);
      decodeComponent(builder, kindId, index, fields(input(payload)), callbacks);
    }
    requireEnd(in, "component list");
    return builder.build();
  }

  private static void decodeComponent(
      DataDrivenForm.Builder builder,
      int kindId,
      int index,
      Map<Integer, byte[]> fields,
      Callbacks callbacks)
      throws IOException, DataDrivenDecodeException {
    if (kindId < 1 || kindId > KIND_MAX) {
      // Checked before anything else: fields cannot be required of a component this codec does
      // not know.
      throw new DataDrivenDecodeException.UnsupportedComponent(kindId);
    }
    Value<Boolean> visible = reactive(readBoolean(require(fields, COMPONENT_VISIBLE)));
    switch (kindId) {
      case 1:
      case 2:
        {
          Value<Text> text = reactiveText(readText(require(fields, COMPONENT_TEXT)));
          DisplayOptions options = visibleOnly(visible);
          if (kindId == 1) {
            builder.header(text, options);
          } else {
            builder.label(text, options);
          }
          return;
        }
      case 3:
        builder.divider(visibleOnly(visible));
        return;
      case 4:
        builder.spacer(visibleOnly(visible));
        return;
      case 5:
        builder.textField(
            reactiveText(readText(require(fields, COMPONENT_TEXT))),
            Observable.of(readString(require(fields, COMPONENT_VALUE))),
            inputOptions(fields, visible));
        return;
      case 6:
        builder.toggle(
            reactiveText(readText(require(fields, COMPONENT_TEXT))),
            Observable.of(readBoolean(require(fields, COMPONENT_VALUE))),
            inputOptions(fields, visible));
        return;
      case 7:
        {
          SliderOptions options =
              SliderOptions.options()
                  .disabled(Observable.of(readBoolean(require(fields, COMPONENT_DISABLED))))
                  .visible(visible.observable());
          byte[] step = fields.get(COMPONENT_STEP);
          if (step != null) {
            options.step(Observable.of(readDouble(step)));
          }
          byte[] description = fields.get(COMPONENT_DESCRIPTION);
          if (description != null) {
            options.description(reactiveText(readText(description)));
          }
          builder.slider(
              reactiveText(readText(require(fields, COMPONENT_TEXT))),
              Observable.of(readDouble(require(fields, COMPONENT_VALUE))),
              reactive(readDouble(require(fields, COMPONENT_MIN))),
              reactive(readDouble(require(fields, COMPONENT_MAX))),
              options);
          return;
        }
      case 8:
        builder.dropdown(
            reactiveText(readText(require(fields, COMPONENT_TEXT))),
            Observable.of(readDouble(require(fields, COMPONENT_VALUE))),
            readItems(require(fields, COMPONENT_ITEMS)),
            inputOptions(fields, visible));
        return;
      case 9:
        {
          ButtonOptions options =
              ButtonOptions.options()
                  .disabled(Observable.of(readBoolean(require(fields, COMPONENT_DISABLED))))
                  .visible(visible.observable());
          byte[] tooltip = fields.get(COMPONENT_TOOLTIP);
          if (tooltip != null) {
            options.tooltip(reactiveText(readText(tooltip)));
          }
          Runnable onClick = callbacks.buttonClicked(index);
          builder.button(
              reactiveText(readText(require(fields, COMPONENT_TEXT))),
              onClick != null ? onClick : () -> {},
              options);
          return;
        }
      default:
        throw new AssertionError("unreachable kind " + kindId);
    }
  }

  private static DataDrivenMessageBox decodeMessageBox(
      Map<Integer, byte[]> fields, Callbacks callbacks)
      throws IOException, DataDrivenDecodeException {
    DataDrivenMessageBox.Builder builder =
        DataDrivenMessageBox.builder(reactiveText(readText(require(fields, BOX_TITLE))))
            .body(reactiveText(readText(require(fields, BOX_BODY))));

    Value<Text> button1 = reactiveText(readText(require(fields, BOX_BUTTON1)));
    byte[] tooltip1 = fields.get(BOX_BUTTON1_TOOLTIP);
    if (tooltip1 != null) {
      builder.button1(button1, reactiveText(readText(tooltip1)));
    } else {
      builder.button1(button1);
    }

    Value<Text> button2 = reactiveText(readText(require(fields, BOX_BUTTON2)));
    byte[] tooltip2 = fields.get(BOX_BUTTON2_TOOLTIP);
    if (tooltip2 != null) {
      builder.button2(button2, reactiveText(readText(tooltip2)));
    } else {
      builder.button2(button2);
    }

    Consumer<DataDrivenMessageBox.Result> closed = callbacks.messageBoxClosed();
    if (closed != null) {
      builder.onResult(closed);
    }
    return builder.build();
  }

  private static byte[] components(List<DataDrivenComponent> components) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    out.writeShort(components.size());
    for (DataDrivenComponent component : components) {
      byte[] payload = component(component);
      out.writeByte(kindId(component.kind()));
      out.writeInt(payload.length);
      out.write(payload);
    }
    return bytes.toByteArray();
  }

  private static byte[] component(DataDrivenComponent component) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    field(out, COMPONENT_VISIBLE, bool(component.visible().get()));

    if (component instanceof DataDrivenComponent.Textual) {
      field(out, COMPONENT_TEXT, text(((DataDrivenComponent.Textual) component).text().get()));
    }
    if (component instanceof DataDrivenComponent.Input) {
      DataDrivenComponent.Input<?> input = (DataDrivenComponent.Input<?>) component;
      field(out, COMPONENT_TEXT, text(input.label().get()));
      field(out, COMPONENT_DISABLED, bool(input.disabled().get()));
      if (input.description() != null) {
        field(out, COMPONENT_DESCRIPTION, text(input.description().get()));
      }
      Object value = encodableValue(input);
      if (value instanceof String) {
        field(out, COMPONENT_VALUE, string((String) value));
      } else if (value instanceof Boolean) {
        field(out, COMPONENT_VALUE, bool((Boolean) value));
      } else if (value instanceof Double) {
        field(out, COMPONENT_VALUE, doubleBytes((Double) value));
      } else {
        throw new AssertionError("unexpected input value " + value);
      }
    }
    if (component instanceof Slider) {
      Slider slider = (Slider) component;
      field(out, COMPONENT_MIN, doubleBytes(slider.min().get()));
      field(out, COMPONENT_MAX, doubleBytes(slider.max().get()));
      if (slider.step() != null) {
        field(out, COMPONENT_STEP, doubleBytes(slider.step().get()));
      }
    }
    if (component instanceof Dropdown) {
      field(out, COMPONENT_ITEMS, items(((Dropdown) component).items()));
    }
    if (component instanceof Button) {
      Button button = (Button) component;
      field(out, COMPONENT_TEXT, text(button.label().get()));
      field(out, COMPONENT_DISABLED, bool(button.disabled().get()));
      if (button.tooltip() != null) {
        field(out, COMPONENT_TOOLTIP, text(button.tooltip().get()));
      }
    }
    return bytes.toByteArray();
  }

  /**
   * The value an input is encoded with. Observables may have moved since the form was built, and
   * the decoder rebuilds through the builder, which enforces the built-with rules; a snapshot the
   * builder would refuse has to be caught on the side that can still tell its caller, or whatever
   * encodes would not decode. A slider's value faces the same clamp a host applies when showing the
   * form; a selection no entry has cannot be clamped to anything and is refused.
   */
  private static Object encodableValue(DataDrivenComponent.Input<?> input) {
    Object value = input.value().get();
    if (input instanceof Slider) {
      Slider slider = (Slider) input;
      double clamped = Math.max(slider.min().get(), Math.min(slider.max().get(), (Double) value));
      if (Double.isNaN(clamped)) {
        // Both clamps propagate NaN, so this one check covers the value and either bound. Only
        // fixed bounds were judged when the slider was built; a reactive one can be anything by
        // now, so the message names all three rather than blaming the value.
        throw new IllegalArgumentException(
            "the slider's value "
                + value
                + " cannot be clamped to ["
                + slider.min().get()
                + ", "
                + slider.max().get()
                + "]");
      }
      return clamped;
    }
    if (input instanceof Dropdown) {
      double selected = (Double) value;
      for (Dropdown.Item item : ((Dropdown) input).items()) {
        if (item.value() == selected) {
          return value;
        }
      }
      throw new IllegalArgumentException("no dropdown item has the value " + selected);
    }
    return value;
  }

  private static byte[] items(List<Dropdown.Item> items) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytes);
    out.writeShort(items.size());
    for (Dropdown.Item item : items) {
      writeText(out, item.label().get());
      out.writeDouble(item.value());
      Value<Text> description = item.description();
      out.writeBoolean(description != null);
      if (description != null) {
        writeText(out, description.get());
      }
    }
    return bytes.toByteArray();
  }

  private static List<Dropdown.Item> readItems(byte[] payload)
      throws IOException, DataDrivenDecodeException {
    DataInputStream in = input(payload);
    int count = in.readUnsignedShort();
    if (count > Dropdown.MAX_ITEMS) {
      throw new DataDrivenDecodeException("payload claims " + count + " dropdown items");
    }
    List<Dropdown.Item> items = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      // A dropdown's entries cannot change while the form is open, so their text stays plain
      // rather than becoming something a push could drive.
      Text label = readText(in, 0);
      double value = in.readDouble();
      if (in.readBoolean()) {
        items.add(Dropdown.Item.of(label, value, readText(in, 0)));
      } else {
        items.add(Dropdown.Item.of(label, value));
      }
    }
    requireEnd(in, "item list");
    return items;
  }

  /**
   * Writes a {@link Text} to a stream, the encoding used wherever a payload carries text: seeded
   * component text and every text-valued live update.
   */
  public static void writeText(@NonNull DataOutputStream out, @NonNull Text text)
      throws IOException {
    if (text instanceof Text.Literal) {
      out.writeByte(TEXT_LITERAL);
      writeString(out, ((Text.Literal) text).text());
    } else if (text instanceof Text.Translatable) {
      Text.Translatable translatable = (Text.Translatable) text;
      out.writeByte(TEXT_TRANSLATABLE);
      writeString(out, translatable.key());
      // A byte holds these counts only while TextImpl.MAX_NODES stays below 256.
      out.writeByte(translatable.args().size());
      for (Text arg : translatable.args()) {
        writeText(out, arg);
      }
    } else if (text instanceof Text.Sequence) {
      Text.Sequence sequence = (Text.Sequence) text;
      out.writeByte(TEXT_SEQUENCE);
      out.writeByte(sequence.parts().size());
      for (Text part : sequence.parts()) {
        writeText(out, part);
      }
    } else {
      throw new AssertionError("unknown text " + text.getClass().getName());
    }
  }

  /**
   * Reads a {@link Text} written by {@link #writeText}. The limits text is built with hold here
   * too: what was too deep or too large to build is refused rather than decoded.
   */
  public static @NonNull Text readText(@NonNull DataInputStream in, int depth)
      throws IOException, DataDrivenDecodeException {
    try {
      return readTextRaw(in, depth);
    } catch (IllegalArgumentException exception) {
      // Text refuses what its limits exclude; from here that is a malformed payload, not an
      // argument the caller chose.
      throw new DataDrivenDecodeException("payload holds text this codec refuses", exception);
    }
  }

  private static Text readTextRaw(DataInputStream in, int depth)
      throws IOException, DataDrivenDecodeException {
    if (depth > TextImpl.MAX_DEPTH) {
      throw new DataDrivenDecodeException("text is nested too deeply");
    }
    int kind = in.readUnsignedByte();
    switch (kind) {
      case TEXT_LITERAL:
        return Text.of(in.readUTF());
      case TEXT_TRANSLATABLE:
        {
          String key = in.readUTF();
          Text[] args = new Text[in.readUnsignedByte()];
          for (int i = 0; i < args.length; i++) {
            args[i] = readText(in, depth + 1);
          }
          return Text.translatable(key, args);
        }
      case TEXT_SEQUENCE:
        {
          Text[] parts = new Text[in.readUnsignedByte()];
          for (int i = 0; i < parts.length; i++) {
            parts[i] = readText(in, depth + 1);
          }
          return Text.sequence(parts);
        }
      default:
        throw new DataDrivenDecodeException("unknown text kind " + kind);
    }
  }

  /**
   * Writes a live-update value: a {@link Text}, {@link String}, {@link Double} or {@link Boolean}.
   */
  public static void writeValue(@NonNull DataOutputStream out, @NonNull Object value)
      throws IOException {
    if (value instanceof Text) {
      out.writeByte(VALUE_TEXT);
      writeText(out, (Text) value);
    } else if (value instanceof String) {
      out.writeByte(VALUE_STRING);
      writeString(out, (String) value);
    } else if (value instanceof Double) {
      out.writeByte(VALUE_DOUBLE);
      out.writeDouble((Double) value);
    } else if (value instanceof Boolean) {
      out.writeByte(VALUE_BOOLEAN);
      out.writeBoolean((Boolean) value);
    } else {
      throw new IllegalArgumentException("cannot write a " + value.getClass().getName());
    }
  }

  /** Reads a value written by {@link #writeValue}. */
  public static @NonNull Object readValue(@NonNull DataInputStream in)
      throws IOException, DataDrivenDecodeException {
    int kind = in.readUnsignedByte();
    switch (kind) {
      case VALUE_TEXT:
        return readText(in, 0);
      case VALUE_STRING:
        {
          String value = in.readUTF();
          if (value.length() > TextImpl.MAX_LENGTH) {
            throw new DataDrivenDecodeException("a value is longer than a string may be");
          }
          return value;
        }
      case VALUE_DOUBLE:
        return in.readDouble();
      case VALUE_BOOLEAN:
        return in.readBoolean();
      default:
        throw new DataDrivenDecodeException("unknown value kind " + kind);
    }
  }

  /**
   * The highest wire id {@link DataDrivenComponent.Kind} maps to; decode covers exactly 1..this.
   */
  private static final int KIND_MAX = 9;

  private static int kindId(DataDrivenComponent.Kind kind) {
    switch (kind) {
      case HEADER:
        return 1;
      case LABEL:
        return 2;
      case DIVIDER:
        return 3;
      case SPACER:
        return 4;
      case TEXT_FIELD:
        return 5;
      case TOGGLE:
        return 6;
      case SLIDER:
        return 7;
      case DROPDOWN:
        return 8;
      case BUTTON:
        return 9;
      default:
        throw new AssertionError("unknown component kind " + kind);
    }
  }

  private static InputOptions inputOptions(Map<Integer, byte[]> fields, Value<Boolean> visible)
      throws IOException, DataDrivenDecodeException {
    InputOptions options =
        InputOptions.options()
            .disabled(Observable.of(readBoolean(require(fields, COMPONENT_DISABLED))))
            .visible(visible.observable());
    byte[] description = fields.get(COMPONENT_DESCRIPTION);
    if (description != null) {
      options.description(reactiveText(readText(description)));
    }
    return options;
  }

  private static DisplayOptions visibleOnly(Value<Boolean> visible) {
    return DisplayOptions.options().visible(visible.observable());
  }

  private static Map<Integer, byte[]> fields(DataInputStream in) throws IOException {
    Map<Integer, byte[]> fields = new LinkedHashMap<>();
    while (in.available() > 0) {
      int id = in.readUnsignedByte();
      int length = in.readInt();
      if (length < 0 || length > in.available()) {
        throw new IOException("field " + id + " claims " + length + " bytes");
      }
      byte[] payload = new byte[length];
      in.readFully(payload);
      if (fields.put(id, payload) != null) {
        throw new IOException("field " + id + " appears twice");
      }
    }
    return fields;
  }

  /** A field that carries no value of its own, so its presence is the whole meaning. */
  private static boolean marker(Map<Integer, byte[]> fields, int id)
      throws DataDrivenDecodeException {
    byte[] payload = fields.get(id);
    if (payload == null) {
      return false;
    }
    if (payload.length != 1 || payload[0] != 1) {
      throw new DataDrivenDecodeException("field " + id + " is not a marker");
    }
    return true;
  }

  private static void field(DataOutputStream out, int id, byte[] payload) throws IOException {
    out.writeByte(id);
    out.writeInt(payload.length);
    out.write(payload);
  }

  private static byte[] require(Map<Integer, byte[]> fields, int id)
      throws DataDrivenDecodeException {
    byte[] payload = fields.get(id);
    if (payload == null) {
      throw new DataDrivenDecodeException("payload is missing field " + id);
    }
    return payload;
  }

  private static Text readText(byte[] payload) throws IOException, DataDrivenDecodeException {
    DataInputStream in = input(payload);
    Text text = readText(in, 0);
    requireEnd(in, "text");
    return text;
  }

  private static String readString(byte[] payload) throws IOException, DataDrivenDecodeException {
    DataInputStream in = input(payload);
    String value = in.readUTF();
    if (value.length() > TextImpl.MAX_LENGTH) {
      throw new DataDrivenDecodeException("a value is longer than a string may be");
    }
    requireEnd(in, "string");
    return value;
  }

  private static boolean readBoolean(byte[] payload) throws IOException, DataDrivenDecodeException {
    if (payload.length != 1 || (payload[0] != 0 && payload[0] != 1)) {
      // Anything else is a payload written by something that is not this codec.
      throw new DataDrivenDecodeException("not a boolean");
    }
    return payload[0] == 1;
  }

  private static double readDouble(byte[] payload) throws IOException, DataDrivenDecodeException {
    if (payload.length != 8) {
      throw new DataDrivenDecodeException("not a double");
    }
    return input(payload).readDouble();
  }

  /** Trailing bytes mean the payload says more than this codec read, which it must not. */
  private static void requireEnd(DataInputStream in, String what)
      throws IOException, DataDrivenDecodeException {
    if (in.available() > 0) {
      throw new DataDrivenDecodeException(in.available() + " trailing bytes after a " + what);
    }
  }

  private static byte[] text(Text value) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    writeText(new DataOutputStream(bytes), value);
    return bytes.toByteArray();
  }

  private static byte[] sized(byte[] payload) {
    if (payload.length > MAX_PAYLOAD_LENGTH) {
      throw new IllegalArgumentException(
          "this form encodes to " + payload.length + " bytes, at most " + MAX_PAYLOAD_LENGTH);
    }
    return payload;
  }

  private static byte[] string(String value) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    writeString(new DataOutputStream(bytes), value);
    return bytes.toByteArray();
  }

  /**
   * Writes a string, refusing one the format cannot hold. A form is driven by whatever a plugin
   * puts in its observables, so an over-long value is something a caller did, not something that
   * cannot happen.
   */
  private static void writeString(DataOutputStream out, String value) throws IOException {
    if (value.length() > TextImpl.MAX_LENGTH) {
      throw new IllegalArgumentException(
          "a value is " + value.length() + " characters, at most " + TextImpl.MAX_LENGTH);
    }
    out.writeUTF(value);
  }

  private static byte[] bool(boolean value) {
    return new byte[] {value ? (byte) 1 : 0};
  }

  private static byte[] doubleBytes(double value) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    new DataOutputStream(bytes).writeDouble(value);
    return bytes.toByteArray();
  }

  private static DataInputStream input(byte[] payload) {
    return new DataInputStream(new ByteArrayInputStream(payload));
  }

  private static Value<Text> reactiveText(Text text) {
    return Value.following(Observable.of(text));
  }

  private static <T> Value<T> reactive(T value) {
    return Value.following(Observable.of(value));
  }
}
