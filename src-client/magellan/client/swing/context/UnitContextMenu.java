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

package magellan.client.swing.context;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import magellan.client.Client;
import magellan.client.EMapDetailsPanel;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.SelectionEvent;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.GiveOrderDialog;
import magellan.client.swing.RoutingDialog;
import magellan.client.swing.context.actions.ContextAction;
import magellan.client.utils.UnitRoutePlanner;
import magellan.client.utils.Units;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderParser;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;
import magellan.library.utils.ShipRoutePlanner;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class UnitContextMenu extends JPopupMenu {
	private Unit unit;

	private GameData data;

	private EventDispatcher dispatcher;

	/**
	 * The selected Units (that are a subset of the selected objects in the overview tree). Notice:
	 * this.unit does not need to be element of this collection!
	 */
	private Collection<Unit> selectedUnits;

	/**
	 * Creates new UnitContextMenu
	 * 
	 * @param unit
	 *            last selected unit - is not required to be in selected objects
	 * @param selectedObjects
	 *            null or Collection of selected objects
	 * @param dispatcher
	 *            EventDispatcher
	 * @param data
	 *            the actual GameData or World
	 */
	public UnitContextMenu(Unit unit, Collection<Unit> selectedObjects, EventDispatcher dispatcher,
			GameData data) {
		super(unit.toString());
		this.unit = unit;
		this.data = data;
		this.dispatcher = dispatcher;

		init(selectedObjects);
	}

	private void init(Collection<Unit> selectedObjects) {
		selectedUnits = ContextAction.filterObjects((Collection)selectedObjects, Unit.class);

		if (selectedUnits.size() <= 1) {
			initSingle();
		} else {
			initMultiple();
		}

		initBoth(selectedObjects);
	}

	private void initBoth(Collection<Unit> selectedObjects) {
		// this part for both (but only for selectedUnits)
		if (containsPrivilegedUnit()) {
			JMenuItem validateOrders = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.confirm.caption"));
			validateOrders.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					event_confirmOrders();
				}
			});
			add(validateOrders);

			JMenuItem addOrder = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.addorder.caption"));
			addOrder.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					event_addOrder();
				}
			});
			add(addOrder);

		}

		if (this.selectedUnits.size() > 0) {
			JMenuItem selectUnits = null;
			if (this.selectedUnits.size() == 1) {
				selectUnits = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.setasunitselection_singular.caption"));
			} else {
				selectUnits = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.setasunitselection_plural.caption"));
			}
			selectUnits.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					event_selectUnits();
				}
			});
			add(selectUnits);
		}

		// tag stuff
		if (getComponentCount() > 0) {
			addSeparator();
		}

		JMenuItem addTag = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.addtag.caption"));
		addTag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				event_addTag();
			}
		});
		add(addTag);

		Collection<String> tags = new TreeSet<String>();
		for (Iterator<Unit> iter = selectedUnits.iterator(); iter.hasNext();) {
			Unit u = iter.next();
			tags.addAll(u.getTagMap().keySet());
		}
		for (Iterator<String> iter = tags.iterator(); iter.hasNext();) {
			String tag = iter.next();

			JMenuItem removeTag = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.removetag.caption") + ": " + tag);
			removeTag.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					event_removeTag(e);
				}
			});
			add(removeTag);
		}

		// test route planning capability
		boolean canPlan = UnitRoutePlanner.canPlan(unit);
		Region reg = unit.getRegion();

		if (canPlan && (selectedUnits != null)) {
			Iterator it = selectedUnits.iterator();

			while (canPlan && it.hasNext()) {
				Unit u = (Unit) it.next();
				canPlan = UnitRoutePlanner.canPlan(u);

				if ((u.getRegion() == null) || !reg.equals(u.getRegion())) {
					canPlan = false;
				}
			}
		}

		if (canPlan) {
			if (getComponentCount() > 0) {
				addSeparator();
			}

			JMenuItem planRoute = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.planroute"));

			planRoute.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					planRoute(e);
				}
			});
			add(planRoute);
		}

		initContextMenuProviders(selectedObjects);
	}

	private void initContextMenuProviders(Collection selectedObjects) {
		Collection cmpList = getContextMenuProviders();
		if (!cmpList.isEmpty()) {
			addSeparator();
		}
		for (Iterator iter = cmpList.iterator(); iter.hasNext();) {
			ContextMenuProvider cmp = (ContextMenuProvider) iter.next();
			add(cmp.createContextMenu(dispatcher, data, (Object) unit, selectedObjects));
		}

	}

	private Collection<ContextMenuProvider> getContextMenuProviders() {
		Collection<ContextMenuProvider> cmpList = new ArrayList<ContextMenuProvider>();
    for (MagellanPlugIn plugIn : Client.INSTANCE.getPlugIns()) {
      if (plugIn instanceof ContextMenuProvider) {
        cmpList.add((ContextMenuProvider)plugIn);
      }
    }
		return cmpList;
	}

	private void initMultiple() {
		// this part for multiple unit-selections
		JMenuItem unitString = new JMenuItem(selectedUnits.size() + " " + Resources.get("magellan.context.unitcontextmenu.units"));
		unitString.setEnabled(false);
		add(unitString);

		JMenuItem copyMultipleID = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.copyids.caption"));
		copyMultipleID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				event_copyMultipleID(e);
			}
		});
		add(copyMultipleID);

		JMenuItem copyMultipleNameID = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.copyidsandnames.caption"));
		copyMultipleNameID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				event_copyMultipleNameID(e);
			}
		});
		add(copyMultipleNameID);

	}

	private void initSingle() {
		// This part for single-unit-selections
		JMenuItem unitString = new JMenuItem(unit.toString());
		unitString.setEnabled(false);
		add(unitString);

		JMenuItem copyUnitID = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.copyid.caption"));
		copyUnitID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				event_copyID(e);
			}
		});
		add(copyUnitID);

		JMenuItem copyUnitNameID = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.copyidandname.caption"));
		copyUnitNameID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				event_copyNameID(e);
			}
		});
		add(copyUnitNameID);

		if (EMapDetailsPanel.isPrivilegedAndNoSpy(unit)) {
			JMenuItem hideID = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.disguise.caption"));

			hideID.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					event_hideID(e);
				}
			});
			add(hideID);
		}

		// is student of someone?
		
		for (Iterator it = unit.getTeachers().iterator(); it.hasNext();){
			Unit teacher = (Unit) it.next();
			JMenuItem removeFromTeachersList = new JMenuItem(
					Resources.get("magellan.context.unitcontextmenu.menu.removeFromTeachersList") + ": " + teacher.toString());
			add(removeFromTeachersList);
			removeFromTeachersList.addActionListener(new RemoveUnitFromTeachersListAction(unit,
					teacher, this.data));
		}
		

		if ((unit.getShip() != null) && unit.equals(unit.getShip().getOwnerUnit())) {
			JMenuItem planShipRoute = new JMenuItem(Resources.get("magellan.context.unitcontextmenu.menu.planshiproute.caption"));
			planShipRoute.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					planShipRoute();
				}
			});
			planShipRoute.setEnabled(ShipRoutePlanner.canPlan(unit.getShip()));
			add(planShipRoute);
		}

	}

	/**
	 * Sets the selected Units as selected Units in Overview FeatureRequest
	 * 
	 * @author Fiete
	 */
	private void event_selectUnits() {
		if (this.selectedUnits.size() > 1) {
			dispatcher.fire(new SelectionEvent(this, this.selectedUnits, null));
		}
		if (this.selectedUnits.size() == 1) {
			dispatcher.fire(new SelectionEvent(this, this.selectedUnits, (Unit) this.selectedUnits
					.toArray()[0]));
		}
	}

	/**
	 * Gives an order (optional replacing the existing ones) to the selected units.
	 */
	private void event_addOrder() {
		GiveOrderDialog giveOderDialog = new GiveOrderDialog(JOptionPane.getFrameForComponent(this));
		String s[] = giveOderDialog.showGiveOrderDialog();
		if (s[0] != null) {
			for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
				Unit u = (Unit) iter.next();

				if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
					Units.changeOrders(u, s);
					dispatcher.fire(new UnitOrdersEvent(this, u));
				}
			}
		}

		unit = null;
		selectedUnits.clear();
	}

	/**
	 * Changes the confirmation state of the selected units.
	 */
	private void event_confirmOrders() {
		boolean status = !((Unit) selectedUnits.iterator().next()).isOrdersConfirmed();

		for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
			Unit u = (Unit) iter.next();
			u.setOrdersConfirmed(status);
		}

		dispatcher.fire(new OrderConfirmEvent(this, selectedUnits));

		unit = null;
		selectedUnits.clear();
	}

	private void event_addTag() {
		String key = null;
		Collection<String> keys = new HashSet<String>();
		Collection<String> values = new HashSet<String>();
		Collection<Unit> keyUnits = new HashSet<Unit>();

		{
			Collection<Region> regions = new HashSet<Region>();
			for (Iterator<Unit> iter = selectedUnits.iterator(); iter.hasNext();) {
				Unit unit = iter.next();
				regions.add(unit.getRegion());
			}
			for (Iterator<Region> iter = regions.iterator(); iter.hasNext();) {
				Region r = iter.next();
				for (Iterator<Unit> iter2 = r.units().iterator(); iter2.hasNext();) {
					Unit u = iter2.next();
					keyUnits.add(u);
					keys.addAll(u.getTagMap().keySet());
					values.addAll(u.getTagMap().values());
				}
			}
		}

		List<String> sortedKeys = new ArrayList<String>(keys);
		Collections.sort(sortedKeys);
		key = showInputDialog(Resources.get("magellan.context.unitcontextmenu.addtag.tagname.message"), sortedKeys);

		if ((key != null) && (key.length() > 0)) {
			String value = null;

			Collection<String> keyValues = new HashSet<String>();
			for (Iterator<Unit> iter = keyUnits.iterator(); iter.hasNext();) {
				Unit u = iter.next();
				String v = u.getTag(key);
				if (v != null) {
					keyValues.add(v);
				}
			}

			List<String> sortedKeyValues = new ArrayList<String>(keyValues);
			Collections.sort(sortedKeyValues);

			// values.removeAll(keyValues);
			// List sortedValues = CollectionFactory.createArrayList(values);
			// Collections.sort(sortedValues);
			// sortedKeyValues.addAll(sortedValues);

			value = showInputDialog(Resources.get("magellan.context.unitcontextmenu.addtag.tagvalue.message"), sortedKeyValues);

			if (value != null) {
				for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
					Unit u = (Unit) iter.next();
					u.putTag(key, value);
					// TODO: Coalesce unitordersevent
					dispatcher.fire(new UnitOrdersEvent(this, u));
				}
			}
		}

		unit = null;
		selectedUnits.clear();
	}

	private String showInputDialog(String message, List values) {
		if (1 == 2) {
			return JOptionPane.showInputDialog(message);
		} else {

			// the combo box (add/modify items if you like to)
			JComboBox comboBox = new JComboBox(values.toArray());
			// has to be editable
			comboBox.setEditable(true);
			comboBox.getEditor().selectAll();
			// change the editor's document
			// new JComboBoxCompletion(comboBox,true);

			// create and show a window containing the combo box
			Frame parent = dispatcher.getMagellanContext().getClient();
			JDialog frame = new JDialog(parent, message, true);
			frame.setLocationRelativeTo(parent);
			frame.getContentPane().setLayout(new BorderLayout());
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			JPanel pane = new JPanel(new BorderLayout());
			pane.add(new JLabel(message), BorderLayout.NORTH);
			pane.add(comboBox, BorderLayout.CENTER);

			frame.getContentPane().add(new JLabel("  "), BorderLayout.NORTH);
			frame.getContentPane().add(new JLabel("  "), BorderLayout.SOUTH);
			frame.getContentPane().add(pane, BorderLayout.CENTER);
			frame.getContentPane().add(new JLabel("  "), BorderLayout.WEST);
			frame.getContentPane().add(new JLabel("  "), BorderLayout.EAST);
			frame.pack();

			comboBox.getEditor().getEditorComponent().addKeyListener(
					new MyKeyAdapter(frame, comboBox));

			frame.setVisible(true);
			frame.dispose();

			return (String) comboBox.getSelectedItem();
		}
	}

	private static class MyOkAction extends AbstractAction {
		private JDialog frame;

		// private JComboBox comboBox;

		private MyOkAction(JDialog frame, JComboBox comboBox) {
			this.frame = frame;
			// this.comboBox = comboBox;
		}

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(false);
		}
	}

	private static class MyCancelAction extends AbstractAction {
		private JDialog frame;

		private JComboBox comboBox;

		private MyCancelAction(JDialog frame, JComboBox comboBox) {
			this.frame = frame;
			this.comboBox = comboBox;
		}

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(false);
			comboBox.setSelectedItem(null);
		}
	}

	private static class MyKeyAdapter extends KeyAdapter {
		private JDialog frame;

		private JComboBox comboBox;

		public MyKeyAdapter(JDialog frame, JComboBox comboBox) {
			this.frame = frame;
			this.comboBox = comboBox;
		}

		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				new MyCancelAction(frame, comboBox).actionPerformed(null);
				break;
			case KeyEvent.VK_ENTER:
				new MyOkAction(frame, comboBox).actionPerformed(null);
				break;
			}
		}
	}

	private void event_removeTag(ActionEvent e) {
		String command = e.getActionCommand();
		int index = command.indexOf(": ");
		if (index > 0) {
			String key = command.substring(index + 2, command.length());
			if (key != null) {
				for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
					Unit u = (Unit) iter.next();
					u.removeTag(key);
					// TODO: Coalesce unitordersevent
					dispatcher.fire(new UnitOrdersEvent(this, u));
				}
			}
		}

		unit = null;
		selectedUnits.clear();

	}

	private void event_copyID(ActionEvent e) {
		StringSelection strSel = new StringSelection(unit.toString(false));
		Clipboard cb = getToolkit().getSystemClipboard();

		cb.setContents(strSel, null);

		unit = null;
		selectedUnits.clear();
	}

	private void event_copyNameID(ActionEvent e) {

		StringSelection strSel = new StringSelection(unit.toString());
		Clipboard cb = getToolkit().getSystemClipboard();

		cb.setContents(strSel, null);

		unit = null;
		selectedUnits.clear();
	}

	private void event_hideID(ActionEvent e) {
		data.getGameSpecificStuff().getOrderChanger().addMultipleHideOrder(unit);
		dispatcher.fire(new UnitOrdersEvent(this, unit));

		unit = null;
		selectedUnits.clear();
	}

	private void event_copyMultipleID(ActionEvent e) {
		StringBuffer idString = new StringBuffer("");

		for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
			Unit u = (Unit) iter.next();

			idString.append(u.toString(false));
			if (iter.hasNext()) {
				idString.append(" ");
			}
		}

		StringSelection strSel = new StringSelection(idString.toString());
		Clipboard cb = getToolkit().getSystemClipboard();
		cb.setContents(strSel, null);

		unit = null;
		selectedUnits.clear();
	}

	private void event_copyMultipleNameID(ActionEvent e) {
		String s = "";

		for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
			Unit u = (Unit) iter.next();
			s += (u.toString() + "\n");
		}

		StringSelection strSel = new StringSelection(s);
		Clipboard cb = getToolkit().getSystemClipboard();
		cb.setContents(strSel, null);

		unit = null;
		selectedUnits.clear();
	}

	private void planRoute(ActionEvent e) {
		if (UnitRoutePlanner.planUnitRoute(unit, data, this, selectedUnits)) {
			if (selectedUnits != null) {
				for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
					Unit u = (Unit) iter.next();

					if (!u.equals(unit)) {
						dispatcher.fire(new UnitOrdersEvent(this, u));
					}
				}
			}

			dispatcher.fire(new UnitOrdersEvent(this, unit));
		}

		unit = null;
		selectedUnits.clear();
	}

	private void planShipRoute() {
		Unit unit = ShipRoutePlanner.planShipRoute(this.unit.getShip(), data, this, new RoutingDialog(JOptionPane.getFrameForComponent(this),data,false));

		if (unit != null) {
			dispatcher.fire(new UnitOrdersEvent(this, unit));
		}
	}

	/**
	 * Checks whether the selectedUnits contain at least one Unit-object, that belongs to a
	 * privileged faction.
	 * 
	 * 
	 */
	private boolean containsPrivilegedUnit() {
		for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
			Unit u = (Unit) iter.next();

			if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
				return true;
			}
		}

		return false;
	}

	private class RemoveUnitFromTeachersListAction implements ActionListener {
		private Unit student;

		private Unit teacher;

		private GameData gameData;

		/**
		 * Creates a new RemoveUnitFromTeachersListAction object, which shall remove the student ID
		 * from the teacher's TEACHING orders.
		 * 
		 * @param student
		 *            The affected student Unit
		 * 
		 * @param teacher
		 *            The affected teacher Unit
		 * 
		 */
		public RemoveUnitFromTeachersListAction(Unit student, Unit teacher, GameData data) {
			this.student = student;
			this.teacher = teacher;
			this.gameData = data;
		}

		/**
		 * Removes student's ID from teacher's teaching orders.
		 * 
		 * 
		 * @param e
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			String id = student.getID().toString();
			Collection orders = teacher.getOrders();
			int i = 0;
			String order = null;

			// look for teaching orders
			for (Iterator iter = orders.iterator(); iter.hasNext(); i++) {
				order = (String) iter.next();
				EresseaOrderParser parser = new EresseaOrderParser(gameData);
				if (!parser.read(new StringReader(order))) {
					continue;
				}

				List tokens = parser.getTokens();
				if (((OrderToken) tokens.get(0)).equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEACH))) {
					if (order.indexOf(id) > -1) {
						teacher.removeOrderAt(i, false);
						// FIXME The meaning of tokens.size() is undefined
						if (tokens.size() > 3) { // teacher teaches more than one unit
							// remove unit's ID from order
							String newOrder = order.substring(0, order.indexOf(id))
									+ order.substring(java.lang.Math.min(order.indexOf(id) + 1
											+ id.length(), order.length()), order.length());
							teacher.addOrderAt(i, newOrder);
						}
						// we wouldn't need this, but we get a ConcurrentModificationException
						// without it
						break;
					}
				}

			}
			dispatcher.fire(new UnitOrdersEvent(this, teacher));
			dispatcher.fire(new UnitOrdersEvent(this, student));
			unit.getRegion().refreshUnitRelations(true);
//			dispatcher.fire(new GameDataEvent(this, gameData));
		}
	}
}
