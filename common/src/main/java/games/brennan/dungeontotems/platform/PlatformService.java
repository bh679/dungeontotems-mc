package games.brennan.dungeontotems.platform;

/** The loader itself. */
public interface PlatformService {

    /** "forge" or "fabric". */
    String loaderName();

    boolean isModLoaded(String modId);

    /** A physical client (an integrated server's code runs here too). */
    boolean isClient();

    /** Started as the GameTest server ({@code runGameTestServer} / {@code runGametest}). */
    boolean isGameTestServer();
}
