// class magellan.test.AbstractTest
// created on Nov 16, 2010
//
// Copyright 2003-2010 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.test;

import java.io.File;
import java.util.Properties;

import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

import org.junit.BeforeClass;

/**
 * A template for magellan test classes that need to load resources.
 * 
 * @author stm
 * @version 1.0, Nov 16, 2010
 */
public abstract class MagellanTestWithResources {
  protected static Properties settings;
  protected static MagellanContext context;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    initResources();
  }

  protected static void initResources() {
    settings = new Properties(); // Client.loadSettings(PARSER_SETTINGS_DIRECTORY,
    // PARSER_SETTINGS_FILE);
    Resources.getInstance().initialize(new File("."), "");
    System.out.println(new File(".").getAbsolutePath());
    context = new MagellanContext(null);
    context.setProperties(settings);
    context.setEventDispatcher(new EventDispatcher());
    Logger.setLevel(Logger.ERROR);
    context.init();
  }

}