package fi.zakru.hitsounds;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HitsoundPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HitsplatSoundPlugin.class, HitsoundDebugPlugin.class);
		RuneLite.main(args);
	}
}