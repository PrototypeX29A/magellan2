/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import magellan.library.utils.logging.Logger;

/**
 * A class for retrieving versioning information about Magellan.
 */
public class VersionInfo {
  private static final Logger log = Logger.getInstance(VersionInfo.class);

  /** Boolean Property to set, if UpdateCheck should be done */
  public static final String PROPERTY_KEY_UPDATECHECK_CHECK = "UpdateCheck.Check";
  /** Timestamp of failed UpdateCheck */
  public static final String PROPERTY_KEY_UPDATECHECK_FAILED = "UpdateCheck.Failed";
  /** ResourceKey to show that the update process failed */
  public static final String RESOURCE_KEY_NOUPDATE_AVAIL = "versioninfo.infodlg.updatefailed";
  /** ResourceKey to show that the nightly update check failed */
  public static final String PROPERTY_KEY_UPDATECHECK_NIGHTLY_CHECK = "UpdateCheck.Nightly.Check";

  private static final String DEFAULT_CHECK_URL = "http://magellan.narabi.de/release/VERSION";
  private static final String NIGHTLY_CHECK_URL = "http://magellan.narabi.de/nightly-build/VERSION";

  private static String Version = null;
  private static boolean versionIsSet = false;

  /**
   * Gets the Version of this Instance.
   */
  public static String getVersion(File magellanDirectory) {
    return getVersion(magellanDirectory, "VERSION");
  }

  public static int PRE_2_1 = 0;
  public static int POST_2_1 = 1;

  public static String getVersion(File magellanDirectory, int versionVersion) {
    if (versionVersion == PRE_2_1)
      return getVersion(magellanDirectory, "VERSION");
    else
      return getVersion(magellanDirectory, "SEMANTIC_VERSION");
  }

  protected static String getVersion(File magellanDirectory, String key) {
    if (VersionInfo.versionIsSet)
      return VersionInfo.Version;
    if (magellanDirectory == null)
      return null;
    VersionInfo.versionIsSet = true;
    try {
      File versionFile = new File(magellanDirectory, "etc/VERSION");
      if (!versionFile.exists()) {
        // hmmm, maybe one directory level up (special Eclipse problem with bin directory)
        versionFile = new File(magellanDirectory.getParentFile(), "etc/VERSION");
        if (!versionFile.exists()) {
          // last try, eclipse special...try just Mag2 Dir
          versionFile = new File(magellanDirectory.getParentFile(), "VERSION");
          if (!versionFile.exists())
            // okay, I'll give up...
            return null;
        }
      }

      // ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream("etc/VERSION"));
      ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(versionFile));
      VersionInfo.Version = bundle.getString(key);
      return VersionInfo.Version;
    } catch (IOException e) {
      // do nothing, not important
    } catch (MissingResourceException e) {
      // do nothing, not important
    }

    return null;
  }

  /**
   * This method tries to get the newest Version from the webserver.
   */
  public static String getNewestVersion(Properties properties, JFrame parent) {

    if (properties == null)
      return null;
    boolean check =
        Boolean.valueOf(properties.getProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK, String
            .valueOf(true)));
    boolean checkNightly =
        Boolean.valueOf(properties.getProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_NIGHTLY_CHECK,
            String.valueOf(false)));

    MagellanUrl.retrieveLocations(properties);
    String urlstring = MagellanUrl.getMagellanUrl(MagellanUrl.VERSION_RELEASE);
    if (urlstring == null) {
      urlstring = VersionInfo.DEFAULT_CHECK_URL;
    }
    long failedTimestamp =
        Long.valueOf(properties.getProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_FAILED, String
            .valueOf(0)));

    boolean doCheck = check;

    if (checkNightly) {
      urlstring = MagellanUrl.getMagellanUrl(MagellanUrl.VERSION_NIGHTLY);
      if (urlstring == null) {
        urlstring = VersionInfo.NIGHTLY_CHECK_URL;
      }
    }

    // if the last failed time was now-7 days, then we try to check again.
    if (failedTimestamp > 0l && doCheck) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(failedTimestamp);
      calendar.add(Calendar.DATE, 7);
      if (calendar.before(Calendar.getInstance())) {
        doCheck = true;
      } else {
        doCheck = false;
      }
    }

    String newestVersion = null;

    // make a connection and try to check....
    if (doCheck) {

      try {
        HTTPClient client = new HTTPClient(properties);
        HTTPResult result = client.get(urlstring);
        if (result != null && result.getStatus() == 200) {
          // okay, lets get the version from the downloaded file
          InputStream inputStream = new ByteArrayInputStream(result.getResult());
          ResourceBundle bundle = new PropertyResourceBundle(inputStream);
          newestVersion = bundle.getString("VERSION");
        }
        if (client.isConnectionFailed() && !(parent == null)) {
          JOptionPane.showMessageDialog(parent, Resources
              .get(VersionInfo.RESOURCE_KEY_NOUPDATE_AVAIL));
        }
      } catch (Exception exception) {
        VersionInfo.log.info("", exception);
        failedTimestamp = Calendar.getInstance().getTimeInMillis();
        if (parent != null) {
          JOptionPane.showMessageDialog(parent, Resources
              .get(VersionInfo.RESOURCE_KEY_NOUPDATE_AVAIL));
        }
      }
    }

    properties.setProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK, String.valueOf(check));
    properties.setProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_FAILED, String
        .valueOf(failedTimestamp));

    return newestVersion;
  }

  /**
   * Returns rue if firstVersion is strictly greater than secondVersion.
   * 
   */
  public static boolean isNewer(String firstVersion, String secondVersion) {
    if (Utils.isEmpty(secondVersion) || Utils.isEmpty(firstVersion))
      return false;

    VersionInfo.log.debug("Current: " + secondVersion);
    VersionInfo.log.debug("Newest : " + firstVersion);

    Version a = new Version(firstVersion);
    Version b = new Version(secondVersion);
    return a.isNewer(b);
  }
}
