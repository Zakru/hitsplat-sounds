package fi.zakru.hitsounds;

import com.google.common.base.Strings;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.HitsplatID;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.RuneLite;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Hitsplat Sounds",
	description = "Customizable rule-based sounds for hitsplats"
)
public class HitsplatSoundPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "hitsplatsounds";
	private static final String CONFIG_KEY = "config";

	private static final Path SOUND_DIRECTORY = RuneLite.RUNELITE_DIR.toPath().resolve("hitsplat-sounds").toAbsolutePath().normalize();

	private static final Map<String, int[]> HITSPLAT_CATEGORIES;
	private static final Pattern RANGE_PATTERN = Pattern.compile("^(\\d+)?\\s*-\\s*(\\d+)?|(\\d+)$");

	static {
		HashMap<String, int[]> categories = new HashMap<>();
		HITSPLAT_CATEGORIES = categories;

		categories.put("damage", new int[]{HitsplatID.DAMAGE_ME, HitsplatID.DAMAGE_OTHER, HitsplatID.DAMAGE_MAX_ME});
		categories.put("block", new int[]{HitsplatID.BLOCK_ME, HitsplatID.BLOCK_OTHER});
		categories.put("heal", new int[]{HitsplatID.HEAL});
		categories.put("shield", new int[]{HitsplatID.DAMAGE_ME_CYAN, HitsplatID.DAMAGE_MAX_ME_CYAN, HitsplatID.DAMAGE_OTHER_CYAN});
		categories.put("poise", new int[]{HitsplatID.DAMAGE_ME_POISE, HitsplatID.DAMAGE_MAX_ME_POISE, HitsplatID.DAMAGE_OTHER_POISE});
		categories.put("charge", new int[]{HitsplatID.DAMAGE_ME_YELLOW, HitsplatID.DAMAGE_MAX_ME_YELLOW, HitsplatID.DAMAGE_OTHER_YELLOW});
		categories.put("poison", new int[]{HitsplatID.POISON});
		categories.put("venom", new int[]{HitsplatID.VENOM});
		categories.put("disease", new int[]{HitsplatID.DISEASE});
		categories.put("corruption", new int[]{HitsplatID.CORRUPTION});
		categories.put("bleed", new int[]{HitsplatID.BLEED});
		categories.put("burn", new int[]{HitsplatID.BURN});
		categories.put("doom", new int[]{HitsplatID.DOOM});
	}

	@Inject
	private Client client;

	@Inject
	private HitsplatSoundConfig config;

	@Inject
	private AudioPlayer audioPlayer;

	@Inject
	private ScheduledExecutorService executor;

	private Iterable<HitsplatSoundRule> rules = Collections.emptyList();

	@Override
	protected void startUp() throws Exception
	{
		try {
			Files.createDirectories(SOUND_DIRECTORY);
		} catch (IOException e) {
            log.error("Failed to create sound directory", e);
        }
        reloadRules(config.config());
	}

	@Provides
	HitsplatSoundConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HitsplatSoundConfig.class);
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		for (HitsplatSoundRule rule : rules)
		{
			if (rule.matches(hitsplatApplied, client))
			{
				Path normalizedPath = SOUND_DIRECTORY.resolve(rule.getSoundFile()).toAbsolutePath().normalize();
				if (!normalizedPath.startsWith(SOUND_DIRECTORY))
				{
					log.warn("Sound path outside sound directory");
					continue;
				}

				// I don't know how necessary this is, I don't notice any impact when running this raw on my machine,
				// but the C Engineer sound plugin delegates the call to the executor.
				executor.execute(() -> {
					try {
						audioPlayer.play(normalizedPath.toFile(), 20f * (float)Math.log10(config.volume() / 100f));
					} catch (Exception e) {
						log.warn("Failed to play {}", rule.getSoundFile(), e);
					}
				});
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP) || !configChanged.getKey().equals(CONFIG_KEY)) return;

		log.debug("Reloading updated config");

		reloadRules(configChanged.getNewValue());
	}

	private void reloadRules(String config)
	{
		if (config == null)
		{
			rules = Collections.emptyList();
			return;
		}

		rules = config.lines().map(line -> {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) return null;

			String[] lineParts = line.split(":", 2);
			if (lineParts.length != 2)
			{
				log.warn("Ignoring line without a colon");
				return null;
			}

			HitsplatSoundRule rule = new HitsplatSoundRule(Path.of(lineParts[1].trim()));

			try {
				Arrays.stream(lineParts[0].split(",")).map(String::trim).filter(c -> !c.isEmpty()).forEach(c -> {
					// Special filters
					switch (c)
					{
						case "own":
							rule.setOwn(true);
							rule.setOthers(false);
							return;
						case "-own":
							rule.setOthers(true);
							rule.setOwn(false);
							return;
						case "self":
							rule.setSelf(HitsplatSoundRule.COND_POSITIVE);
							return;
						case "-self":
							rule.setSelf(HitsplatSoundRule.COND_NEGATIVE);
							return;
						case "max":
							rule.setMaxHit(true);
							return;
					}

					// Hitsplat type filters
					int[] validHitsplats = HITSPLAT_CATEGORIES.get(c);
					if (validHitsplats != null)
					{
						if (rule.getValidHitsplats() == null) rule.setValidHitsplats(new HashSet<>());

						for (int h : validHitsplats)
							rule.getValidHitsplats().add(h);

						return;
					}

					// Hitsplat number filter
					Matcher matcher = RANGE_PATTERN.matcher(c);
					if (matcher.matches())
					{
						// "<min>-<max>" case
						String min = matcher.group(1);
						if (!Strings.isNullOrEmpty(min)) rule.setMin(Integer.parseInt(min));
						String max = matcher.group(2);
						if (!Strings.isNullOrEmpty(max)) rule.setMax(Integer.parseInt(max));

						// "<exact>" case
						String exact = matcher.group(3);
						if (!Strings.isNullOrEmpty(exact))
						{
							rule.setMin(Integer.parseInt(exact));
							rule.setMax(Integer.parseInt(exact));
						}

						return;
					}

					throw new InvalidConditionException(c);
				});
			} catch (InvalidConditionException e) {
				log.warn("Ignoring line with invalid condition: \"{}\"", e.condition);
				return null;
			}

			return rule;
		}).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
	}

	@AllArgsConstructor
	private static class InvalidConditionException extends RuntimeException
	{
		private final String condition;
	}
}
