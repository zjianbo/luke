package org.apache.lucene.luke.app.desktop;

import org.apache.lucene.luke.app.controllers.LukeController;
import org.apache.lucene.store.FSDirectory;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PreferencesImpl implements Preferences {

  private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".luke.d";
  private static final String INIT_FILE = "luke.ini";
  private static final String HISTORY_FILE = "history";
  private static final int MAX_HISTORY = 10;

  private final Ini ini = new Ini();

  private final List<String> history = new ArrayList<>();

  public PreferencesImpl() throws IOException {
    // create config dir if not exists
    Path confDir = FileSystems.getDefault().getPath(CONFIG_DIR);
    if (!Files.exists(confDir)) {
      Files.createDirectory(confDir);
    }

    // load configs
    if (iniFile().exists()) {
      ini.load(iniFile());
    } else {
      ini.store(iniFile());
    }

    // load history
    Path histFile = historyFile();
    if (Files.exists(histFile)) {
      List<String> allHistory = Files.readAllLines(histFile);
      history.addAll(allHistory.subList(0, Math.min(MAX_HISTORY, allHistory.size())));
    }

  }

  public List<String> getHistory() {
    return history;
  }

  @Override
  public void addHistory(String indexPath) throws IOException {
    if (history.indexOf(indexPath) >= 0) {
      history.remove(indexPath);
    }
    history.add(0, indexPath);
    saveHistory();
  }

  private void saveHistory() throws IOException {
    Files.write(historyFile(), history);
  }

  private Path historyFile() {
    return FileSystems.getDefault().getPath(CONFIG_DIR, HISTORY_FILE);
  }

  @Override
  public LukeController.ColorTheme getTheme() {
    String theme = ini.get("settings", "theme");
    return (theme == null) ? LukeController.ColorTheme.GRAY : LukeController.ColorTheme.valueOf(theme);
  }

  @Override
  public void setTheme(LukeController.ColorTheme theme) throws IOException {
    ini.put("settings", "theme", theme.name());
    ini.store(iniFile());
  }

  @Override
  public boolean isReadOnly() {
    Boolean readOnly = ini.get("opener", "readOnly", Boolean.class);
    return (readOnly == null) ? false : readOnly;
  }

  @Override
  public String getDirImpl() {
    String dirImpl = ini.get("opener", "dirImpl");
    return (dirImpl == null) ? FSDirectory.class.getName() : dirImpl;
  }

  @Override
  public boolean isNoReader() {
    Boolean noReader = ini.get("opener", "noReader", Boolean.class);
    return (noReader == null) ? false : noReader;
  }

  @Override
  public boolean isUseCompound() {
    Boolean useCompound = ini.get("opener", "useCompound", Boolean.class);
    return (useCompound == null) ? false : useCompound;
  }

  @Override
  public boolean isKeepAllCommits() {
    Boolean keepAllCommits = ini.get("opener", "keepAllCommits", Boolean.class);
    return (keepAllCommits == null) ? false : keepAllCommits;
  }

  @Override
  public void setIndexOpenerPrefs(boolean readOnly, String dirImpl, boolean noReader, boolean useCompound, boolean keepAllCommits) throws IOException {
    ini.put("opener", "readOnly", readOnly);
    ini.put("opener", "dirImpl", dirImpl);
    ini.put("opener", "noReader", noReader);
    ini.put("opener", "useCompound", useCompound);
    ini.put("opener", "keepAllCommits", keepAllCommits);
    ini.store(iniFile());
  }

  private File iniFile() {
    return new File(CONFIG_DIR, INIT_FILE);
  }
}