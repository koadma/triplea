package games.strategy.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;

public class TupleTest {
  Tuple<String, Integer> testObj = Tuple.of("hi", 123);

  @Test
  public void basicUsage() {
    assertThat(testObj.getFirst(), is("hi"));
    assertThat(testObj.getSecond(), is(123));
  }

  @Test
  public void verifyEquality() {
    assertThat(testObj, is(testObj));

    final Tuple<String, Integer> copyObj = Tuple.of(testObj.getFirst(), testObj.getSecond());
    assertThat(testObj, is(copyObj));
    assertThat(copyObj, is(testObj));

    assertThat("check equals against null case",
        copyObj.equals(null), is(false));
  }

  @Test
  public void verifyToString() {
    assertThat(testObj.toString(), containsString(testObj.getFirst()));
    assertThat(testObj.toString(), containsString(String.valueOf(testObj.getSecond())));
  }

  @Test
  public void checkStoringNullCase() {
    final Tuple<String, String> nullTuple = Tuple.of(null, null);

    assertThat(nullTuple.getFirst(), nullValue());
    assertThat(nullTuple.getSecond(), nullValue());
    assertThat(nullTuple, not(Tuple.of("something else", (String) null)));
  }

  @Test
  public void checkUsingTupleAsMapKey() {
    final Map<Tuple<String, String>, String> map = Maps.newHashMap();
    final Tuple<String, String> tuple = Tuple.of("This is a bad idea using tuples this much", "another value");
    final String value = "some value";

    assertFalse(map.containsKey(tuple));

    map.put(tuple, value);
    assertTrue(map.containsKey(tuple));
    assertThat(map.get(tuple), is(value));
  }
}
