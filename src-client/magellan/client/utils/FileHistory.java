/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.file.FileHistoryAction;
import magellan.library.utils.Bucket;
import magellan.library.utils.PropertiesHelper;

/**
 * A kind of wrapper for the file history (menu) functionality.
 * 
 * @author Andreas
 * @version 1.0
 */
public class FileHistory {
  protected Bucket<FileHistoryAction> history;
  protected JMenu historyMenu;
  protected int insertionIndex;
  protected Properties settings;
  protected Client client;

  /**
   * Creates new FileHistory
   */
  public FileHistory(Client parent, Properties settings, JMenu menu, int index) {
    this.settings = settings;
    client = parent;
    historyMenu = menu;
    insertionIndex = index;
  }

  /**
   * Adds a single file to the file history, as well to the file history bucket as well to the file
   * history menu.
   */
  public void addFileToHistory(File f) {
    if (history == null) {
      loadFileHistory();
    }

    clearFileHistoryMenu();
    history.add(new FileHistoryAction(this, f));
    buildFileHistoryMenu();
  }

  /**
   * Stores the current contents of the file history bucket to the settings.
   */
  public void storeFileHistory() {
    List<String> files = new ArrayList<String>((history == null) ? 0 : history.size());

    if (history != null) {
      for (FileHistoryAction fileHistoryAction : history) {
        files.add(fileHistoryAction.getFile().getAbsolutePath());
      }

      Collections.reverse(files);
    }

    PropertiesHelper.setList(settings, "Client.fileHistory", files);
  }

  /**
   * Fills the file history bucket from what is stored in the settings.
   */
  private void loadFileHistory() {
    if (history == null) {
      history = new Bucket<FileHistoryAction>(getMaxFileHistorySize());
    }

    for (String file : PropertiesHelper.getList(settings, "Client.fileHistory")) {
      File f = new File(file);

      if (f.exists()) {
        history.add(new FileHistoryAction(this, f));
      }
    }
  }

  /**
   * Returns the last file loaded into Magellan.
   */
  public File getLastExistingReport() {
    if (history == null || history.size() == 0)
      return null;
    for (FileHistoryAction action : history) {
      File last = action.getFile();
      if (last != null && last.exists())
        return last;
    }
    return null;
  }

  /**
   * Uses the current contents of the file history bucket to remove these menu items from the file
   * history menu.
   */
  @SuppressWarnings("unused")
  public void clearFileHistoryMenu() {
    if (history != null) {
      for (FileHistoryAction fileHistoryAction : history) {
        historyMenu.remove(insertionIndex);
      }
    }
  }

  /**
   * Inserts the contents of the fileHistory bucket to the file history menu (it assumes that the
   * menu has been cleared previously.
   */
  public void buildFileHistoryMenu() {
    if (history == null) {
      loadFileHistory();
    }

    int iIndex = insertionIndex;

    for (Iterator<FileHistoryAction> iter = history.iterator(); iter.hasNext(); iIndex++) {
      FileHistoryAction item = iter.next();
      historyMenu.insert(item, iIndex);
    }
  }

  /**
   * Returns the maximum number of entries in the history of loaded files.
   */
  public int getMaxFileHistorySize() {
    return Integer.parseInt(settings.getProperty("Client.fileHistory.size", "4"));
  }

  /**
   * Allows to set the maximum number of files appearing in the file history.
   */
  public void setMaxFileHistorySize(int size) {
    if (size != getMaxFileHistorySize()) {
      clearFileHistoryMenu();
      history.setMaxSize(size);
      buildFileHistoryMenu();
      settings.setProperty("Client.fileHistory.size", Integer.toString(size));
    }
  }

  /**
   * Loads the the given file. This method should only be called by FileHistoryAction objects.
   */
  public void loadFile(File file) {
    int response = client.askToSave();
    if (response == JOptionPane.CANCEL_OPTION)
      return;

    settings.setProperty("Client.lastCROpened", file.getAbsolutePath());
    addFileToHistory(file);

    boolean bOpenEqualsSave = PropertiesHelper.getBoolean(settings, "Client.openEqualsSave", false);

    if (bOpenEqualsSave) {
      settings.setProperty("Client.lastCRSaved", file.getAbsolutePath());
    }

    client.loadCRThread(response == JOptionPane.YES_OPTION, file);
  }
}
