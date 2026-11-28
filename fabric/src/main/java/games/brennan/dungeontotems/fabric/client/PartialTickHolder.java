package games.brennan.dungeontotems.fabric.client;

/** The partial tick a player's render state was extracted at (NeoForge keeps it on the state; vanilla doesn't). */
public interface PartialTickHolder {
    float dungeontotems$partialTick();

    void dungeontotems$setPartialTick(float partialTick);
}
