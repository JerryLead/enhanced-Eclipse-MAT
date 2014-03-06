package cn.ac.iscas.oomr.userobject;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

import cn.ac.iscas.oomr.dominatortree.Row;

public class UserObjectExtender {

	private ISnapshot snapshot;
	private List<Row> userObjs;
	
	public UserObjectExtender(ISnapshot snapshot, List<Row> userObjs) {
		this.snapshot = snapshot;
		this.userObjs = userObjs;
	}

	public void extendUserObjs() {
		try {
			for(Row userObj : userObjs) {
				String className = userObj.getClassName();
				
				if(className.startsWith("java.util.ArrayList @")) {
					ListObj listObj = handleArrayList(snapshot.getObject(userObj.getObjectId()));
					listObj.display();
				}
				
				else if(className.startsWith("java.util.HashMap @")) {
					MapObj mapObj = handleHashMap(snapshot.getObject(userObj.getObjectId()));
					mapObj.display();
				}
				
				else if(className.startsWith("java.util.LinkedList @")) {
					ListObj listObj = handleLinkedList(snapshot.getObject(userObj.getObjectId()));
					listObj.display();
				}
					
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ListObj handleArrayList(IObject arrayListObj) {

		try {
			int[] listChildren = snapshot.getImmediateDominatedIds(arrayListObj.getObjectId());
		
		
			for(Integer listChild : listChildren) {
			
				if(snapshot.getObject(listChild).getTechnicalName().startsWith("java.lang.Object[")) {
				
					int[] objectChildren = snapshot.getImmediateDominatedIds(listChild);
					if(objectChildren.length != 0) {
						ListObj listObj = new ListObj(new Row(arrayListObj));
						listObj.setElements(objectChildren);
						listObj.setFirstElem(new Row(snapshot.getObject(objectChildren[0])));
						
						return listObj;
					}
						
				}
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	

	public MapObj handleHashMap(IObject hashMapObj) throws SnapshotException {
		
		// Class name of hashMapObj is "java.util.HashMap @"
		int[] hashMapObjChildren = snapshot.getImmediateDominatedIds(hashMapObj.getObjectId());
		for(int hashMapObjChild : hashMapObjChildren) {
			
			if(snapshot.getObject(hashMapObjChild).getTechnicalName().startsWith("java.util.HashMap$Entry[")) {

				int[] entryChildren = snapshot.getImmediateDominatedIds(hashMapObjChild);
				
				
				if(entryChildren.length != 0) {
					MapObj mapObj = new MapObj(new Row(hashMapObj));
					mapObj.setElementIds(entryChildren);
					
					List<NamedReference> entryRefs = snapshot.getObject(entryChildren[0]).getOutboundReferences();
					for(NamedReference entryRef : entryRefs) {
						if(entryRef.getName().equals("key")) {
							int keyObjId = entryRef.getObjectId();
							mapObj.setKey(snapshot.getObject(keyObjId));
						}
						else if(entryRef.getName().equals("value")) {
							int valueObjId = entryRef.getObjectId();
							mapObj.setValue(snapshot.getObject(valueObjId));
						}
					}
					return mapObj;
				}

				
			}
		}
		
		return null;
	}
	
	public void handleHashSet(IObject hashSetObj) {
		
	}
	
	public ListObj handleLinkedList(IObject linkedListObj) {
		try {
			int[] listChildren = snapshot.getImmediateDominatedIds(linkedListObj.getObjectId());
		
		
			for(Integer listChild : listChildren) {
			
				if(snapshot.getObject(listChild).getTechnicalName().startsWith("java.util.LinkedList$Entry @")) {
				
					int[] objectChildren = snapshot.getImmediateDominatedIds(listChild);
					if(objectChildren.length != 0) {
						ListObj listObj = new ListObj(new Row(linkedListObj));
						listObj.setElements(objectChildren);
						
						int firstEntryObjId = snapshot.getImmediateDominatedIds(objectChildren[0])[0];
						Row firstChildElem = new Row(snapshot.getObject(firstEntryObjId));
						listObj.setFirstElem(firstChildElem);
						
						return listObj;
					}
						
				}
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void handleTreeMap(IObject treeMapObj) {
		
	}

	public void handleTreeSet(IObject treeSetObj) {
	
	}

	/*
	public List<Row> subDominateObjsWithoutSort(int dominatorId) {
		List<Row> rows = new ArrayList<Row>();
		
		try {
			int[] objectIds = snapshot.getImmediateDominatedIds(dominatorId);
			
			for(int objectId : objectIds) {
			
				IObject obj = snapshot.getObject(objectId); 
				rows.add(new Row(obj));
			}
			
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Collections.sort(rows);
		
		return rows;
	}
	*/
	
	/*
	public int[] subDominateObjIds(int dominatorId) throws SnapshotException {
		return snapshot.getImmediateDominatedIds(dominatorId);
	}
	*/
}

// represent the list object (e.g., ArrayList, LinkedList)
class ListObj {
	// type of this data structure (e.g., ArrayList)
	// String dsName;

	// e.g., T in the ArrayList<T>()
	// String elementType;
	
	private Row listObj;
	
	private int[] elementIds;
	private Row firstElem;
	
	public ListObj(Row listObj) {
		this.listObj = listObj;
	}
	
	public void setElements(int[] elementIds) {
		this.elementIds = elementIds;
	}
	
	public void setFirstElem(Row firstElem) {
		this.firstElem = firstElem;
	}
	
	public int size() {
		return elementIds.length;
	}
	
	public void display() {
		if(elementIds == null || elementIds.length == 0)
			return;
		
		System.out.println("\n--------------------List Object--------------------------");
		System.out.println("[listObj] " + listObj);
		System.out.println("[element] " + firstElem);
		System.out.println("[length]  " + new DecimalFormat(",###").format(elementIds.length));
		System.out.println("--------------------------------------------------\n");
	}
}

// represent the map object (e.g., HashMap, TreeMap)
class MapObj {
	// type of this data structure
	// String dsName;
	private Row mapObj;
	// key and value in the Map
	private Row key;
	private Row value;
	private int[] elementIds;
	
	public MapObj(Row mapObj) {
		this.mapObj = mapObj;
	}
	
	public void setKey(IObject obj) {
		key = new Row(obj);
	}
	
	public void setValue(IObject obj) {
		value = new Row(obj);
	}
	
	public void setElementIds(int[] elementIds) {
		this.elementIds = elementIds;
	}
	
	public void display() {
		
		System.out.println("\n--------------------Map Object------------------------");
		System.out.println("[MapObj] " + mapObj);
		System.out.println("[key]    " + key);
		System.out.println("[value]  " + value);
		System.out.println("[length] " + new DecimalFormat(",###").format(elementIds.length));
		System.out.println("--------------------------------------------------\n");
	}
}