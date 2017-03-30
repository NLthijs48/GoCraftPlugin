package me.wiefferink.gocraft.tools.storage;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import me.wiefferink.gocraft.Log;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class UTF8Config extends YamlConfiguration {

	private final DumperOptions yamlOptions = new DumperOptions();
	private final Representer yamlRepresenter = new YamlRepresenter();
	private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

	@Override
	public String saveToString() {
		yamlOptions.setIndent(options().indent());
		yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yamlOptions.setAllowUnicode(true);
		yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		String header = buildHeader();
		String dump = yaml.dump(getValues(false));
		if (dump.equals(BLANK_CONFIG)) {
			dump = "";
		}
		return header + dump;
	}

	public static UTF8Config loadConfiguration(Reader reader) {
		Validate.notNull(reader, "Stream cannot be null");
		UTF8Config config = new UTF8Config();
		try {
			config.load(reader);
		} catch (InvalidConfigurationException | IOException ex) {
			Log.warn("Could not load configuration from stream: "+ex);
		}
		return config;
	}

	@Override
	public void save(File file) throws IOException {
		Validate.notNull(file, "File cannot be null");
		Files.createParentDirs(file);
		String data = saveToString();
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
			writer.write(data);
		} catch (IOException e) {
			Log.warn("Could not save config file: "+file.getAbsolutePath());
		}
	}

}
