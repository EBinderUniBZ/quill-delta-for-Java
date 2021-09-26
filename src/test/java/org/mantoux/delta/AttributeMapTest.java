package org.mantoux.delta;

import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Attributes")
class AttributeMapTest {

  @Nested
  public class Compose {

    AttributeMap attributes = AttributeMap.of("bold", new JsonPrimitive(true), "color", new JsonPrimitive("red"));

    @Test
    public void leftIsNull() {
      Assertions.assertEquals(attributes, AttributeMap.compose(null, attributes));
    }

    @Test
    public void rightIsNull() {
      Assertions.assertEquals(attributes, AttributeMap.compose(attributes, null));
    }

    @Test
    public void bothAreNull() {
      assertNull(AttributeMap.compose(null, null));
    }

    @Test
    public void missingElement() {
      Assertions.assertEquals(AttributeMap.of("bold", new JsonPrimitive(true), "italic", new JsonPrimitive(true), "color", new JsonPrimitive("red")),
                              AttributeMap.compose(attributes, AttributeMap.of("italic", new JsonPrimitive(true))));
    }

    @Test
    public void overrideElement() {
      Assertions.assertEquals(AttributeMap.of("bold", new JsonPrimitive(true), "color", new JsonPrimitive("blue")),
                              AttributeMap.compose(attributes, AttributeMap.of("color", new JsonPrimitive("blue"))));
    }

    @Test
    public void removeElement() {
      Assertions.assertEquals(AttributeMap.of("color", new JsonPrimitive("red")),
                              AttributeMap.compose(attributes, AttributeMap.of("bold", JsonNull.INSTANCE)));
    }

    @Test
    public void removeAll() {
      assertNull(AttributeMap.compose(attributes, AttributeMap.of("bold", JsonNull.INSTANCE, "color", JsonNull.INSTANCE)));
    }

    @Test
    public void removeMissing() {
      Assertions.assertEquals(attributes,
                              AttributeMap.compose(attributes, AttributeMap.of("italic", JsonNull.INSTANCE)));
    }
  }


  @Nested
  public class Invert {

    @Test
    public void onNull() {
      AttributeMap base = AttributeMap.of("bold", new JsonPrimitive(true));
      Assertions.assertEquals(new AttributeMap(), AttributeMap.invert(null, base));
    }

    @Test
    public void baseNull() {
      AttributeMap attributes = AttributeMap.of("bold", new JsonPrimitive(true));
      AttributeMap expected = AttributeMap.of("bold", JsonNull.INSTANCE);
      Assertions.assertEquals(expected, AttributeMap.invert(attributes, null));
    }

    @Test
    public void bothNull() {
      Assertions.assertEquals(new AttributeMap(), AttributeMap.invert(null, null));
    }

    @Test
    public void merge() {
      AttributeMap attributes = AttributeMap.of("bold", new JsonPrimitive(true));
      AttributeMap base = AttributeMap.of("italic", new JsonPrimitive(true));
      Assertions.assertEquals(AttributeMap.of("bold", JsonNull.INSTANCE), AttributeMap.invert(attributes, base));
    }

    @Test
    public void revertUnset() {
      AttributeMap attributes = AttributeMap.of("bold", JsonNull.INSTANCE);
      AttributeMap base = AttributeMap.of("bold", new JsonPrimitive(true));
      Assertions.assertEquals(AttributeMap.of("bold", new JsonPrimitive(true)), AttributeMap.invert(attributes, base));
    }

    @Test
    public void replace() {
      AttributeMap attributes = AttributeMap.of("color", new JsonPrimitive("red"));
      AttributeMap base = AttributeMap.of("color", new JsonPrimitive("blue"));
      Assertions.assertEquals(base, AttributeMap.invert(attributes, base));
    }

    @Test
    public void noop() {
      AttributeMap attributes = AttributeMap.of("color", new JsonPrimitive("red"));
      AttributeMap base = AttributeMap.of("color", new JsonPrimitive("red"));
      Assertions.assertEquals(new AttributeMap(), AttributeMap.invert(attributes, base));
    }

    @Test
    public void combined() {
      AttributeMap attributes =
        AttributeMap.of("bold", new JsonPrimitive(true), "italic", JsonNull.INSTANCE, "color", new JsonPrimitive("red"), "size", new JsonPrimitive("12px"));
      AttributeMap base =
        AttributeMap.of("font", new JsonPrimitive("serif"), "italic", new JsonPrimitive(true), "color", new JsonPrimitive("blue"), "size", new JsonPrimitive("12px"));
      AttributeMap expected = AttributeMap.of("bold", JsonNull.INSTANCE, "italic", new JsonPrimitive(true), "color", new JsonPrimitive("blue"));
      Assertions.assertEquals(expected, AttributeMap.invert(attributes, base));
    }
  }


  @Nested
  public class Transform {
    AttributeMap left  = AttributeMap.of("bold", new JsonPrimitive(true), "color", new JsonPrimitive("red"), "font", JsonNull.INSTANCE);
    AttributeMap right = AttributeMap.of("color", new JsonPrimitive("blue"), "font", new JsonPrimitive("serif"), "italic", new JsonPrimitive(true));

    @Test
    public void leftNull() {
      Assertions.assertEquals(left, AttributeMap.transform(null, left, false));
    }

    @Test
    public void rightNull() {
      assertNull(AttributeMap.transform(right, null, false));
    }

    @Test
    public void bothNull() {
      assertNull(AttributeMap.transform(null, null, false));
    }

    @Test
    public void withPriority() {
      Assertions.assertEquals(AttributeMap.of("italic", new JsonPrimitive(true)),
                              AttributeMap.transform(left, right, true));
    }

    @Test
    public void withoutPriority() {
      Assertions.assertEquals(right, AttributeMap.transform(left, right, false));
    }
  }


  @Nested
  public class Diff {
    AttributeMap format = AttributeMap.of("bold", new JsonPrimitive(true), "color", new JsonPrimitive("red"));

    @Test
    public void leftNull() {
      Assertions.assertEquals(format, AttributeMap.diff(null, format));
    }

    @Test
    public void rightNull() {
      Assertions.assertEquals(AttributeMap.of("bold", JsonNull.INSTANCE, "color", JsonNull.INSTANCE),
                              AttributeMap.diff(format, null));
    }

    @Test
    public void sameFormat() {
      assertNull(AttributeMap.diff(format, format));
    }

    @Test
    public void addFormat() {
      AttributeMap added = AttributeMap.of("bold", new JsonPrimitive(true), "italic", new JsonPrimitive(true), "color", new JsonPrimitive("red"));
      Assertions.assertEquals(AttributeMap.of("italic", new JsonPrimitive(true)), AttributeMap.diff(format, added));
    }

    @Test
    public void removeFormat() {
      AttributeMap removed = AttributeMap.of("bold", new JsonPrimitive(true));
      Assertions.assertEquals(AttributeMap.of("color", JsonNull.INSTANCE), AttributeMap.diff(format, removed));
    }

    @Test
    public void overrideFormat() {
      AttributeMap override = AttributeMap.of("bold", new JsonPrimitive(true), "color", new JsonPrimitive("blue"));
      Assertions.assertEquals(AttributeMap.of("color", new JsonPrimitive("blue")),
                              AttributeMap.diff(format, override));
    }
  }

}
