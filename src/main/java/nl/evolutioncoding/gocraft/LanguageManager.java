package nl.evolutioncoding.gocraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageManager {
	private GoCraft plugin = null;
	private String[] languages = { "EN" };
	private HashMap<String, String> currentLanguage;
	private HashMap<String, String> defaultLanguage;

	public LanguageManager(GoCraft plugin) {
		this.plugin = plugin;
		saveDefaults();
		loadLanguage();
	}

	public void saveDefaults() {
		File langFolder = new File(this.plugin.getDataFolder() + File.separator
				+ "lang");
		if (!langFolder.exists()) {
			langFolder.mkdirs();
		}
		for (int i = 0; i < this.languages.length; i++) {
			File langFile = new File(this.plugin.getDataFolder()
					+ File.separator + "lang" + File.separator
					+ this.languages[i] + ".yml");
			InputStream input = null;
			OutputStream output = null;
			try {
				input = this.plugin.getResource("lang/" + this.languages[i]
						+ ".yml");
				output = new FileOutputStream(langFile);

				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = input.read(bytes)) != -1) {
					output.write(bytes, 0, read);
				}
				input.close();
				output.close();
			} catch (IOException e) {
				try {
					input.close();
					output.close();
				} catch (IOException localIOException1) {
				} catch (NullPointerException localNullPointerException) {
				}
				this.plugin.getLogger().info(
						"Something went wrong saving a default language file: "
								+ langFile.getPath());
			}
		}
	}

	public void loadLanguage() {
		this.currentLanguage = new HashMap<String, String>();
		File file = new File(this.plugin.getDataFolder() + File.separator
				+ "lang" + File.separator
				+ this.plugin.getConfig().getString("language") + ".yml");
		YamlConfiguration ymlFile = YamlConfiguration.loadConfiguration(file);
		Map<String, Object> map = ymlFile.getValues(true);
		Set<String> set = map.keySet();
		try {
			for (String key : set) {
				this.currentLanguage.put(key, (String) map.get(key));
			}
		} catch (ClassCastException localClassCastException) {
		}
		this.defaultLanguage = new HashMap<String, String>();
		File standard = new File(this.plugin.getDataFolder() + File.separator
				+ "lang" + "/" + this.languages[0] + ".yml");
		ymlFile = YamlConfiguration.loadConfiguration(standard);
		map = ymlFile.getValues(true);
		set = map.keySet();
		try {
			for (String key : set) {
				this.defaultLanguage.put(key, (String) map.get(key));
			}
		} catch (ClassCastException localClassCastException2) {
		}
	}

	public String getLang(String key, Object... params) {
		String result = null;
		if (this.currentLanguage.containsKey(key)) {
			result = this.currentLanguage.get(key);
		} else {
			result = this.defaultLanguage.get(key);
		}
		if (result == null) {
			this.plugin.getLogger().info("Wrong key for getting translation: " + key);
		} else {
			for (int i = 0; i < params.length; i++) {
				if (params[i] != null) {
					result = result
							.replace("%" + i + "%", params[i].toString());
				}
			}
		}
		return result;
	}
}
