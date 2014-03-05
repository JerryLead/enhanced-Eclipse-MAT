package cn.ac.iscas.oomr.userobject;

import java.util.ArrayList;
import java.util.Collections;
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
	
	public ListObj handleArrayList(IObject arrayListObj) {
		ListObj listObj = new ListObj();
		
		List<Row> listChildren = subDominateObjs(arrayListObj.getObjectId());
		for(Row listChild : listChildren) {
			
			if(listChild.getClassName().startsWith("java.lang.Object[")) {
				
				List<Row> objectChildren = subDominateObjs(listChild.getObjectId());
				listObj.setElements(objectChildren);
				
			}
		}
		return listObj;
		
	}
	
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

	public MapObj handleHashMap(IObject hashMapObj) throws SnapshotException {
		// Class name of hashMapObj is "java.util.HashMap @"
		List<Row> hashMapObjChildren = subDominateObjs(hashMapObj.getObjectId());
		for(Row hashMapObjChild : hashMapObjChildren) {
			
			if(hashMapObjChild.getClassName().startsWith("java.util.HashMap$Entry[")) {
				
				List<Row> entryChildren = subDominateObjs(hashMapObjChild.getObjectId());
				
				
				MapObj mapObj = new MapObj();
				
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
	
	public List<Row> subDominateObjs(int dominatorId) {
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
		
		Collections.sort(rows);
		
		return rows;
	}
}

// represent the list object (e.g., ArrayList, LinkedList)
class ListObj {
	// type of this data structure (e.g., ArrayList)
	String dsName;

	// e.g., T in the ArrayList<T>()
	String elementType;
	
	List<Row> elements;
	
	public void setElements(List<Row> elements) {
		this.elements = elements;
	}
	
	public int size() {
		return elements.size();
	}
}

// represent the map object (e.g., HashMap, TreeMap)
class MapObj {
	// type of this data structure
	String dsName;
	
	// key and value in the Map
	List<Row> keys;
	List<Row> values;
	
	public MapObj() {
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

}