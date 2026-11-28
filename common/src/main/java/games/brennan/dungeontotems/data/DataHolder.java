package games.brennan.dungeontotems.data;

import java.util.Map;

/** Added to every entity by {@code mixin/EntityDataMixin}. Use {@link TotemData}, not this. */
public interface DataHolder {
    Map<DataKey<?>, Object> dungeontotems$data();
}
