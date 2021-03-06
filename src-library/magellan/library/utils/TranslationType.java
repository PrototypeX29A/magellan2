// class magellan.library.utils.TranslationType
// created on 20.11.2007
//
// Copyright 2003-2007 by magellan project team
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
package magellan.library.utils;

/**
 * Stores information of an translated string used in Translation Map
 * 
 * @author Fiete
 * @version 1.0, 20.11.2007
 */
public final class TranslationType {

  /**
   * Source of this translation type is unknown; should not stay after init.
   * 
   * @deprecated SOURCE_UNKNOWN
   */
  @Deprecated
  public static final int sourceUnknown = 0;

  /**
   * Source of this translation type is the CR
   * 
   * @deprecated SOURCE_CR
   */
  @Deprecated
  public static final int sourceCR = 1;

  /**
   * Source of this translation type are the default Translations of Magellan.
   * 
   * @deprecated SOURCE_MAGELLAN
   */
  @Deprecated
  public static final int sourceMagellan = 2;

  /**
   * Source of this translation type is unknown; should not stay after init.
   */
  public static final int SOURCE_UNKNWON = 0;

  /** Source of this translation type is the CR */
  public static final int SOURCE_CR = 1;

  /**
   * Source of this translation type are the default Translations of Magellan.
   */
  public static final int SOURCE_MAGELLAN = 2;

  /** The translated string */
  private final String translation;

  /** the source of this translation */
  private final int source;

  /**
   * Returns the value of translation.
   * 
   * @return Returns translation.
   */
  public final String getTranslation() {
    return translation;
  }

  /**
   * Returns the value of source.
   * 
   * @return Returns source.
   */
  public final int getSource() {
    return source;
  }

  /**
   * Constructs new Translation Type
   * 
   * @param text the Translation
   * @param _source the Source
   */
  public TranslationType(String text, int _source) {
    translation = text;
    source = _source;
  }

}
