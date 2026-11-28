package games.brennan.dungeontotems.event;

/** When a handler runs, first to last. NeoForge gets one listener per priority in use, at its own priority. */
public enum Priority {
    HIGHEST, HIGH, NORMAL, LOW, LOWEST
}
