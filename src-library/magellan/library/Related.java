package magellan.library;

import java.util.List;

import magellan.library.relation.UnitRelation;

/**
 * An interface granting access to the relations of an object.
 */
public interface Related extends Described, Addeable {

  /**
   * Add a new relation to this object.
   * 
   * @param rel
   */
  public void addRelation(UnitRelation rel);

  /**
   * Removes the given relation.
   * 
   * @param rel
   * @return old relation
   */
  public UnitRelation removeRelation(UnitRelation rel);

  /**
   * Removes all relations.
   */
  public void clearRelations();

  /**
   * Delivers all relations.
   * 
   * @return list of all relations
   */
  public List<UnitRelation> getRelations();

  /**
   * Delivers all relations of the given class (and its subtypes!).
   * 
   * @param relationClass Should be a subclass of {@link UnitRelation}
   * @return list of relations that are instances of relationClass
   */
  public <T extends UnitRelation> List<T> getRelations(Class<T> relationClass);
}
