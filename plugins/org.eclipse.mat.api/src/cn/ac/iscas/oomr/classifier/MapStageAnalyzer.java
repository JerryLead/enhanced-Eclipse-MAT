package cn.ac.iscas.oomr.classifier;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

import cn.ac.iscas.oomr.dominatortree.Row;

public class MapStageAnalyzer {

	// framework objects
	private Row kvbuffer;
	private Row kvoffsets;
	private Row kvindices;

	private ISnapshot snapshot;
	private List<Row> largeDominators;
	
	private Set<Integer> toDeleteObjIds;
	
	private PrintWriter writer;
	
	public MapStageAnalyzer(ISnapshot snapshot, List<Row> largeDominators, PrintWriter writer) {
		this.snapshot = snapshot;
		this.largeDominators = largeDominators;
		toDeleteObjIds = new HashSet<Integer>();
		this.writer = writer;
	}
	
	
	/**
	 * classify object into framework objects and user objects, leave framework objects, return user objects.
	 * @return user objects
	 */
	public List<Row> filterFrameworkObjs() {
		
		for(Row row : largeDominators) {
			if(row.getClassName().startsWith("org.apache.hadoop.mapred.MapTask$MapOutputBuffer @")) {
				try {
					processSpillBuffer(snapshot.getObject(row.getObjectId()));
					toDeleteObjIds.add(row.getObjectId());
					break;
				} catch (SnapshotException e) {
					e.printStackTrace();
				}
			}	
		}
		
		
		deleteFrameworkObjs();
		
		displayFrameworkObjs();
		outputFrameworkObjs();
		
		return largeDominators;
	}



	// delete the segments from the List<Row> largeDominators
	// the remaining objects are regarded as user objects
	private void deleteFrameworkObjs() {
		
		ListIterator<Row> listIter = largeDominators.listIterator();
		
		while(listIter.hasNext()) {
			int dominatorId = listIter.next().getObjectId();
			if(toDeleteObjIds.contains(dominatorId)) 
				listIter.remove();
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
					toDeleteObjIds.add(objectId);
				}
				else if(ref.getName().equals("kvindices")) {
					int objectId = ref.getObjectId();
					IObject obj = snapshot.getObject(objectId);
					kvindices = new Row(obj);
					kvindices.setFrameObjName("kvindices");
					toDeleteObjIds.add(objectId);
				}
				else if(ref.getName().equals("kvoffsets")) {
					int objectId = ref.getObjectId();
					IObject obj = snapshot.getObject(objectId);
					kvoffsets = new Row(obj);
					kvoffsets.setFrameObjName("kvoffsets");
					toDeleteObjIds.add(objectId);
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
	
	private void outputFrameworkObjs() {
		if(writer == null)
			return;
		
		writer.println("## Objects in Map Stage\n");
		
		writer.println("\n### Framework Objects\n");
		
		if(kvbuffer != null || kvindices != null || kvoffsets != null) {
			
			writer.println("| FrameworkObj \t| Inner object \t| shallowHeap \t| retainedHeap \t|");
			writer.println("| :----------- | :----------- | -----------: | -----------: |");
		
			if(kvbuffer != null)
				writer.println(kvbuffer);
			if(kvindices != null)
				writer.println(kvindices);
			if(kvoffsets != null)
				writer.println(kvoffsets);
		}
		writer.println();
		writer.println();
		
	}

	
	public void displayFrameworkObjs() {
		System.out.println("## Objects in Map Stage\n");
		
		System.out.println("\n### Framework Objects\n");
		
		if(kvbuffer != null || kvindices != null || kvoffsets != null) {
			
			System.out.println("| FrameworkObj \t| Class name \t| shallowHeap \t| retainedHeap \t|");
			System.out.println("| :----------- | :----------- | -----------: | -----------: |");
		
			if(kvbuffer != null)
				System.out.println(kvbuffer);
			if(kvindices != null)
				System.out.println(kvindices);
			if(kvoffsets != null)
				System.out.println(kvoffsets);
		}
		System.out.println();
		System.out.println();
	}

}
