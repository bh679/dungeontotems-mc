package games.brennan.dungeontotems.platform.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Every {@code @EventBusSubscriber} class in the sources is listed in {@link CommonEvents}, and nothing else. */
class CommonEventsTest {

    private static final Path SOURCES = Path.of("src/main/java");

    @Test
    void everySubscriberClassIsListed() throws IOException {
        Set<String> found;
        try (Stream<Path> files = Files.walk(SOURCES)) {
            found = files.filter(p -> p.toString().endsWith(".java"))
                .filter(CommonEventsTest::subscribes)
                .map(p -> SOURCES.relativize(p).toString().replace(".java", "").replace('/', '.'))
                .filter(name -> !name.startsWith("games.brennan.dungeontotems.platform."))
                .collect(Collectors.toSet());
        }
        Set<String> listed = new HashSet<>(CommonEvents.COMMON);
        listed.addAll(CommonEvents.CLIENT);
        assertEquals(found, listed);
    }

    private static boolean subscribes(Path file) {
        try {
            return Files.readString(file).contains("\n@EventBusSubscriber");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
