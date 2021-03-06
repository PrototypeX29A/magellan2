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

package magellan.library.completion;

import java.util.List;

import magellan.library.Unit;
import magellan.library.utils.OrderToken;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public interface Completer {

  /**
   * Get order completions for the order <code>line</code> and Unit <code>u</code>. If
   * <code>old</code> is <code>null</code> or empty, all possible completions matching (i.e.
   * starting with) <code>line</code> should be computed. Else, the method may just return all
   * completions in <code>old</code> matching line.
   * 
   * @param u
   * @param line
   * @param old A list of completions or <code>null</code>.
   * @return The list of all Completions matching line.
   */
  public List<Completion> getCompletions(Unit u, String line, List<Completion> old);

  /**
   * If this completer has an {@link OrderParser} and it has been used to find completions, this
   * method returns the tokens read by the parser.
   * 
   * @return The list of tokens of this Completer's OrderParser, or <code>null</code>.
   */
  public List<OrderToken> getParserTokens();
}
