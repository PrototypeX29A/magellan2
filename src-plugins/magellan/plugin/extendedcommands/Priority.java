// class magellan.plugin.extendedcommands.Priority
// created on 12.04.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.plugin.extendedcommands;

import magellan.library.utils.Resources;
import magellan.library.utils.Utils;

public enum Priority {
  HIGHEST, HIGHER, NORMAL, LOWER, LOWEST;

  /**
   * Returns the wanted priority.
   */
  public static Priority getPriority(String name) {
    if (Utils.isEmpty(name))
      return NORMAL;
    for (Priority priority : Priority.values()) {
      if (priority.toString().equalsIgnoreCase(name))
        return priority;
    }
    return NORMAL;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return Resources.get("extended_commands.priority." + name().toLowerCase(),false);
  }

}
