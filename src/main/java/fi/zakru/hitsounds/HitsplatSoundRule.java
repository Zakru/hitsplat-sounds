package fi.zakru.hitsounds;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.Player;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.RuneLite;

import java.nio.file.Path;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class HitsplatSoundRule
{
    private static final Set<Integer> HITSPLAT_MAX = Set.of(
        HitsplatID.DAMAGE_MAX_ME,
        HitsplatID.DAMAGE_MAX_ME_CYAN,
        HitsplatID.DAMAGE_MAX_ME_POISE,
        HitsplatID.DAMAGE_MAX_ME_ORANGE,
        HitsplatID.DAMAGE_MAX_ME_WHITE,
        HitsplatID.DAMAGE_MAX_ME_YELLOW
    );

    public static final int COND_NONE = 0;
    public static final int COND_POSITIVE = 1;
    public static final int COND_NEGATIVE = -1;

    private Set<Integer> validHitsplats = null;
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;
    private boolean own = true;
    private boolean others = true;
    private int self = COND_NONE;
    private boolean maxHit = false;
    private final Path soundFile;

    public boolean matches(HitsplatApplied hitsplatApplied, Client client)
    {
        Hitsplat hitsplat = hitsplatApplied.getHitsplat();
        boolean isSelf = hitsplatApplied.getActor() == client.getLocalPlayer();

        return (validHitsplats == null || validHitsplats.contains(hitsplat.getHitsplatType()))
            && hitsplat.getAmount() >= min && hitsplat.getAmount() <= max
            && (own || !hitsplat.isMine()) && (others || !hitsplat.isOthers())
            && (self != COND_POSITIVE || isSelf) && (self != COND_NEGATIVE || !isSelf)
            && (!maxHit || HITSPLAT_MAX.contains(hitsplat.getHitsplatType()));
    }
}
