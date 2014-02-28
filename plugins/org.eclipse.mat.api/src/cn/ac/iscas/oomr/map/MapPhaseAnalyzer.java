package cn.ac.iscas.oomr.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

import cn.ac.iscas.oomr.dominatortree.Row;

public class MapPhaseAnalyzer {

	// framework objects
	private Row kvbuffer;
	private Row kvoffsets;
	private Row kvindices;

	private ISnapshot snapshot;
	private List<Row> largeDominators;
	
	public MapPhaseAnalyzer(ISnapshot snapshot, List<Row> largeDominators) {
		this.snapshot = snapshot;
		this.largeDominators = largeDominators;
	}
	
	
	/**
	 * @param rawLargeDominators If the dominator is java.lang.Thread, we will expand it to small objects
	 * @return
	 */
	private List<Row> expandDominators(List<Row> rawLargeDominators) {
		largeDominators = new ArrayList<Row>();
		
		for(Row row : largeDominators) {
			if(row.getClassName().startsWith("java.lang.Thread @")) {
				
				List<Row> subDominators = subDominateObjs(row.getObjectId());
				largeDominators.addAll(subDominators);
			}	
		}

		return null;
	}

	/**
	 * classify object into framework objects and user objects, leave framework objects, return user objects.
	 * @return user objects
	 */
	public List<Row> filterFrameworkObjs() {
		int spillBufferId = 0;
		
		for(Row row : largeDominators) {
			if(row.getClassName().startsWith("org.apache.hadoop.mapred.MapTask$MapOutputBuffer @")) {
				try {
					processSpillBuffer(snapshot.getObject(row.getObjectId()));
					spillBufferId = row.getObjectId();
					break;
				} catch (SnapshotException e) {
					e.printStackTrace();
				}
			}	
		}
		
		if(spillBufferId != 0)
			deleteSpillBuffer(spillBufferId);
		
		return largeDominators;
	}
	
	// delete the spillBuffer dominator from the List<Row> largeDominators
	// the remaining objects are regarded as user objects
	
	private void deleteSpillBuffer(int spillBufferId) {
		ListIterator<Row> listIter = largeDominators.listIterator();
		
		while(listIter.hasNext()) {
			int dominatorId = listIter.next().getObjectId();
			if(dominatorId == spillBufferId || dominatorId == kvbuffer.getObjectId() 
					|| dominatorId == kvoffsets.getObjectId() || dominatorId == kvindices.getObjectId()) {
				listIter.remove();
			}
		}
	}

	// get the information of spillBuffer
	
	public void processSpillBuffer(IObject mapOutputBuffer) {
		List<NamedReference> outRefs = mapOutputBuffer.getOutboundReferences();

		try {
			for (NamedReference ref : outRefs) {
				if (ref.getName().equals("kvbuffer")) {
					int objectId = ref.getObjectId();
					IObject obj = snapshot.getObject(objectId);
					kvbuffer = new Row(obj);
				}
				else if(ref.getName().equals("kvindices")) {
					int objectId = ref.getObjectId();
					IObject obj = snapshot.getObject(objectId);
					kvindices = new Row(obj);
				}
				else if(ref.getName().equals("kvoffsets")) {
					int objectId = ref.getObjectId();
					IObject obj = snapshot.getObject(objectId);
					kvoffsets = new Row(obj);
				}
			}
		}
		
		catch (SnapshotException e) {
			e.printStackTrace();
		}
		
		if(kvbuffer == null || kvindices == null || kvoffsets == null) {
			System.err.println("kvbuffer = " + kvbuffer + ", kvindices = " + kvindices + ", kvoffsets = " + kvoffsets);
		}
	}
	
	
	public void displayFrameworkObjs() {
		System.out.println("-------------------Framework objects in map phase-------------------");
		System.out.println("Class name \t| shallowHeap \t| retainedHeap");
		System.out.println(":----------- | -----------: | -----------:");
		
		System.out.println(kvbuffer);
		System.out.println(kvindices);
		System.out.println(kvoffsets);
		
		System.out.println("---------------------------------------------------------------------");
		System.out.println();
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
//		System.err.println("Class name \t| shallowHeap \t| retainedHeap");
//		System.err.println(":----------- | -----------: | -----------:");
//		for(int i = 0; i < rows.size(); i++) {
//			Row row = rows.get(i);
//			System.err.println(row);
//		}
	}
}
