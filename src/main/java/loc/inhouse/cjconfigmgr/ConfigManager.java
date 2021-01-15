package loc.inhouse.cjconfigmgr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConfigManager {

	private static String CONFIG_FILEPATH = "config.txt";
	private static String DEF_CONFIG_FILEPATH = "config.default";
	private static String CORRUPTED_CONFIG_FILEPATH = "config.corrupted";
	private static File configFile;

	private static boolean isConfigLoaded = false;

	private static HashMap<String, String> configVariablesMap = new HashMap<>();
	private static HashMap<String, String> keyBindingsMap = new HashMap<>();

	public static void init(){
		configFile = getConfigFileRef(CONFIG_FILEPATH);

		try {
			if (configFile.exists()) {
				if (configFile.canRead()) {
					loadConfigFile(configFile);
				}else {
					createBkpAndLoadDefaultConfig();
				}
			}else {
				loadFromDefaults();
			}
		}catch (IOException e){
			e.printStackTrace();
			System.exit(1); // TODO - Error Handling
		}
	}

	private static File getConfigFileRef(String filePath){
		File tmp = new File(filePath);
		return tmp;
	}

	private static void createBkpAndLoadDefaultConfig() throws IOException{
		File corruptedConfigFile = getConfigFileRef(System.nanoTime() + "-" + CORRUPTED_CONFIG_FILEPATH);
		configFile.renameTo(corruptedConfigFile);
		loadFromDefaults();
	}

	private static void loadFromDefaults() throws IOException{
		File defConfigFile = getConfigFileRef(DEF_CONFIG_FILEPATH);
		if(defConfigFile.exists() && defConfigFile.canRead()){
			Files.copy(defConfigFile.toPath(), configFile.toPath());
			loadConfigFile(configFile);
		} else{
			System.err.println("[" + Instant.now().toString() + "][FATAL] Config Error - No default config found");
			System.exit(2); // TODO - Error Handling
		}
	}

	private static void loadConfigFile(File configFile) throws FileNotFoundException {
		Scanner fileScanner = new Scanner(configFile);
		String line;
		while (fileScanner.hasNextLine()){
			line = fileScanner.nextLine();
			if(line.length() == 0 || line.charAt(0)=='#') continue;
			String[] configStatement = line.split(" ");
			StringBuilder sb = new StringBuilder();
			for(int i = 2; i < configStatement.length; i++){
				sb.append(configStatement[i] + " ");
			}
			configStatement[2] = sb.toString().trim();
			processConfigStatement(configStatement);
		}

		isConfigLoaded = true;
	}

	private static void processConfigStatement(String[] statement){
		switch (statement[0]){
			case "set":
				configVariablesMap.putIfAbsent(statement[1], statement[2]);
				break;
			case "keybind":
				keyBindingsMap.putIfAbsent(statement[1], statement[2]);
				break;
		}
	}

	public static String getConfigVariable(String name){
		if(configVariablesMap.containsKey(name)){
			return configVariablesMap.get(name);
		}
		return null;
	}

	public static void setConfigVariable(String name, String value){
		configVariablesMap.put(name, value);

		try {
			boolean found = false;
			ArrayList<String> lines = new ArrayList<>();
			Scanner sc = new Scanner(configFile);
			while(sc.hasNextLine()){
				String line = sc.nextLine();

				String[] statements = line.split(" ");

				if(statements[0].equals("set") && statements[1].equals(name)) {
					line = "set " + name + " " + value;
					found = true;
				}

				lines.add(line);
			}

			if(!found) lines.add("set " + name + " " + value);

			configFile.delete();
			FileWriter fW = new FileWriter(configFile);
			for(String s: lines) fW.append(s + "\n");
			fW.flush(); fW.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Map.Entry<String, String>> getKeyBindings(){
		return new ArrayList<>(keyBindingsMap.entrySet());
	}
}
