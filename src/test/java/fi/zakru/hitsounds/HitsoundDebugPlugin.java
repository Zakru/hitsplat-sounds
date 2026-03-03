package fi.zakru.hitsounds;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
    name = "Hitsplat Sounds Debug"
)
public class HitsoundDebugPlugin extends Plugin {

    @Override
    protected void startUp() throws Exception {
        ((Logger) LoggerFactory.getLogger("fi.zakru.hitsounds")).setLevel(Level.DEBUG);
    }
}
