package cn.ac.iscas.oomr.classifier;

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
		
		displayFrameworkObjs();
		
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
					kvbuffer.setFrameObjName("kvbuffer");
				}
				else if(ref.getName().equals("kvindices")) {
					int objectId = ref.getObjectId();
					IObject obj = snapshot.getObject(objectId);
					kvindices = new Row(obj);
					kvindices.setFrameObjName("kvindices");
				}
				else if(ref.getName().equals("kvoffsets")) {
					int objectId = ref.getObjectId();
					IObject obj = snapshot.getObject(objectId);
					kvoffsets = new Row(obj);
					kvoffsets.setFrameObjName("kvoffsets");
				}
			}
		}
		
		catch (SnapshotException e) {
			e.printStackTrace();
		}
		
		if(kvbuffer == null || kvindices == null || kvoffsets == null) {
			System.err.println("kvbuffer or kvindices or kvoffsets does not exist.");
			System.err.println("kvbuffer = " + kvbuffer + ", kvindices = " + kvindices + ", kvoffsets = " + kvoffsets);
		}
		
	}
	
	
	public void displayFrameworkObjs() {
		
		System.out.println("|-------------------Framework objects in map phase-------------------|");
		System.out.println("| FrameworkObj \t| Class name \t| shallowHeap \t| retainedHeap \t|");
		System.out.println("| :----------- | :----------- | -----------: | -----------: |");
		
		System.out.println(kvbuffer);
		System.out.println(kvindices);
		System.out.println(kvoffsets);
		
		System.out.println();
		System.out.println();
	}

}
