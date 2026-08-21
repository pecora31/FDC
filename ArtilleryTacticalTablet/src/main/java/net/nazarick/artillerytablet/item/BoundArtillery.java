package net.nazarick.artillerytablet.item;

import java.util.UUID;

public final class BoundArtillery {
    public final UUID id;
    public final String typeId;

    public BoundArtillery(UUID id, String typeId) {
        this.id = id;
        this.typeId = typeId;
    }
}
