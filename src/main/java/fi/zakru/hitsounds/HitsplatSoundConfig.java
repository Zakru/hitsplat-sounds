package fi.zakru.hitsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hitsplatsounds")
public interface HitsplatSoundConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "volume",
		name = "Volume",
		description = "Volume of the sound, between 0-100."
	)
	default int volume()
	{
		return 100;
	}

	@ConfigItem(
		position = 1,
		keyName = "config",
		name = "Hitsplat sounds",
		description = "The configuration for the plugin functionality."
	)
	default String config()
	{
		return "# Insert your configuration here";
	}

	@ConfigItem(
		position = 2,
		keyName = "instructions",
		name = "Instructions",
		description = "Instructions for configuration"
	)
	default String instructions()
	{
		return "To configure hitsplat sounds, add lines for each sound.\n\n" +
				"The configurations should have the following format:\n" +
				" <cond1>,<cond2>,...:<filename>\n" +
				"The file name should refer to a file (.wav should work) in .runelite/hitsplat-sounds\n\n" +
				"The available conditions are the following:\n" +
				"\"10\", \"5-\", \"-15\", \"5-15\" - Specifies a hit number filter. Ranges can define a minimum and/or maximum. Defaults to any number.\n" +
				"\"own\", \"-own\" - Specifies whether the rule only applies to your or other players' hitsplats. Leave unspecified for no filter.\n" +
				"\"self\", \"-self\" - Specifies whether the rule only applies to hitsplats on you or hitsplats not on you. Leave unspecified for no filter.\n" +
				"The following conditions filter specific hitsplat types: \"damage\", \"block\", \"heal\", \"shield\", \"poise\", \"charge\", \"poison\", \"venom\", \"disease\", \"corruption\", \"bleed\", \"burn\", \"doom\".\n" +
				"\"max\" filters only max hitsplats and works only with types that use them.\n\n" +
				"Lines starting with a # are ignored.\n\n" +
				"# Examples\n\n" +
				"# When I deal exactly 10 normal damage to an opponent\n" +
				"damage,own,-self,10:sound.wav";
	}
 }
