package cn.ac.iscas.oomr.userobject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

import cn.ac.iscas.oomr.dominatortree.Row;

public class UserObjectExtender2 {

	private ISnapshot snapshot;
	private List<Row> userObjs;
	
	public UserObjectExtender2(ISnapshot snapshot, List<Row> userObjs) {
		this.snapshot = snapshot;
		this.userObjs = userObjs;
	}

	public void extendUserObjs() {
		try {
			for(Row userObj : userObjs) {
				String className = userObj.getClassName();
				
				if(className.startsWith("java.util.ArrayList @"))
					handleArrayList(snapshot.getObject(userObj.getObjectId()));
				else if(className.startsWith("java.util.HashMap @"))
					handleHashMap(snapshot.getObject(userObj.getObjectId()));
					
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ListObj2 handleArrayList(IObject arrayListObj) {
		ListObj2 listObj = new ListObj2(new Row(arrayListObj));
		
	
		List<Row> listChildren = subDominateObjsWithoutSort(arrayListObj.getObjectId());
		
		for(Row listChild : listChildren) {
			
			if(listChild.getClassName().startsWith("java.lang.Object[")) {
				
				List<Row> objectChildren = subDominateObjsWithoutSort(listChild.getObjectId());
				
				listObj.setElements(objectChildren);
				
			}
		}
		return listObj;
		
	}
	
	/*
	public ListObj handleArrayList(IObject arrayListObj) {
		ListObj listObj = new ListObj(new Row(arrayListObj));
		
	
		List<Row> listChildren = subDominateObjsWithoutSort(arrayListObj.getObjectId());
		
		for(Row listChild : listChildren) {
			
			if(listChild.getClassName().startsWith("java.lang.Object[")) {
				
				List<Row> objectChildren = subDominateObjsWithoutSort(listChild.getObjectId());
				
				listObj.setElements(objectChildren);
				
			}
		}
		return listObj;
		
	}
	*/
	/*
	public MapObj handleHashMap(IObject hashMapObj) throws SnapshotException {
		// Class name of hashMapObj is "java.util.HashMap @"
		List<NamedReference> hashMapObjRefs = hashMapObj.getOutboundReferences();
		for(NamedReference hashMapObjRef : hashMapObjRefs) {
			
			if(hashMapObjRef.getName().equals("table")) {
				
				List<NamedReference> tableRefs = hashMapObjRef.getObject().getOutboundReferences();
				
				MapObj mapObj = new MapObj();
				for(NamedReference tableRef : tableRefs) {
					
					if(tableRef.getName().startsWith("[")) {
						
						
						List<NamedReference> entryRefs = tableRef.getObject().getOutboundReferences();
						for(NamedReference entryRef : entryRefs) {
							if(entryRef.getName().equals("key")) {
								int keyObjId = entryRef.getObjectId();
								mapObj.addKey(snapshot.getObject(keyObjId));
							}
							else if(entryRef.getName().equals("value")) {
								int valueObjId = entryRef.getObjectId();
								mapObj.addValue(snapshot.getObject(valueObjId));
							}
						}
					}
					
				}
				
				return mapObj;
			}
		}
		
		return null;
	}
	*/

	public MapObj2 handleHashMap(IObject hashMapObj) throws SnapshotException {
		
		// Class name of hashMapObj is "java.util.HashMap @"
		List<Row> hashMapObjChildren = subDominateObjsWithoutSort(hashMapObj.getObjectId());
		for(Row hashMapObjChild : hashMapObjChildren) {
			
			if(hashMapObjChild.getClassName().startsWith("java.util.HashMap$Entry[")) {

				List<Row> entryChildren = subDominateObjsWithoutSort(hashMapObjChild.getObjectId());
				
				MapObj2 mapObj = new MapObj2(new Row(hashMapObj));
				
				for(Row entryChild : entryChildren) {
					List<NamedReference> entryRefs = snapshot.getObject(entryChild.getObjectId()).getOutboundReferences();
					for(NamedReference entryRef : entryRefs) {
						if(entryRef.getName().equals("key")) {
							int keyObjId = entryRef.getObjectId();
							mapObj.addKey(snapshot.getObject(keyObjId));
						}
						else if(entryRef.getName().equals("value")) {
							int valueObjId = entryRef.getObjectId();
							mapObj.addValue(snapshot.getObject(valueObjId));
						}
					}
						
				}

				return mapObj;
			}
		}
		
		return null;
	}
	
	public void handleHashSet(IObject hashSetObj) {
		
	}
	
	public void handleLinkedList(IObject linkedListObj) {
		
	}
	
	public void handleTreeMap(IObject treeMapObj) {
		
	}

	public void handleTreeSet(IObject treeSetObj) {
	
	}
	
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
	
	public int[] subDominateObjIds(int dominatorId) throws SnapshotException {
		return snapshot.getImmediateDominatedIds(dominatorId);
	}
	
}

// represent the list object (e.g., ArrayList, LinkedList)
class ListObj2 {
	// type of this data structure (e.g., ArrayList)
	// String dsName;

	// e.g., T in the ArrayList<T>()
	// String elementType;
	
	private Row listObj;
	
	private List<Row> elements;
	
	public ListObj2(Row listObj) {
		this.listObj = listObj;
	}
	
	public void setElements(List<Row> elements) {
		this.elements = elements;
	}
	
	public int size() {
		return elements.size();
	}
	
	public void display() {
		if(elements == null || elements.isEmpty())
			return;
		System.out.println("\n--------------------------------------------------");
		System.out.println("[listObj] " + listObj);
		System.out.println("[element] " + elements.get(0));
		System.out.println("[length]  " + elements.size());
		System.out.println();
	}
}

// represent the map object (e.g., HashMap, TreeMap)
class MapObj2 {
	// type of this data structure
	// String dsName;
	private Row mapObj;
	// key and value in the Map
	List<Row> keys;
	List<Row> values;
	
	public MapObj2(Row mapObj) {
		this.mapObj = mapObj;
		keys = new ArrayList<Row>();
		values = new ArrayList<Row>();
	}
	
	public void addKey(IObject obj) {
		keys.add(new Row(obj));
	}
	
	public void addValue(IObject obj) {
		values.add(new Row(obj));
	}
	
	public int size() {
		return keys.size();
	}

	public void display() {
		if(keys.isEmpty())
			return;
		System.out.println("\n--------------------------------------------------");
		System.out.println("[MapObj] " + mapObj);
		System.out.println("[key]    " + keys.get(0));
		System.out.println("[value]  " + values.get(0));
		System.out.println("[length] " + keys.size());
		System.out.println();
	}
}