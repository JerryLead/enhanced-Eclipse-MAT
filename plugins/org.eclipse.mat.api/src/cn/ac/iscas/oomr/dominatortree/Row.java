package cn.ac.iscas.oomr.dominatortree;

import java.text.DecimalFormat;

import org.eclipse.mat.snapshot.model.IObject;

public class Row implements Comparable<Row> {
	
	private int objectId;
	private String className;
	private long shallowHeap;
	private long retainedHeap;
	
	public Row(IObject obj) {
		objectId = obj.getObjectId();
		className = obj.getTechnicalName();
		shallowHeap = obj.getUsedHeapSize();
		retainedHeap = obj.getRetainedHeapSize();	
	}
	
	public int getObjectId() {
		return objectId;
	}
	
	public String getClassName() {
		return className;
	}
	
	public long getRetainedHeap() {
		return retainedHeap;
	}
	
	public long getShallowHeap() {
		return shallowHeap;
	}


	public int compareTo(Row o) {
		long diff = this.retainedHeap - o.getRetainedHeap();
		if(diff > 0)
			return -1;
		else if(diff < 0)
			return 1;
		else 
			return 0;
	}
	
	public String toString() {
		DecimalFormat format = new DecimalFormat(",###");
		return className + "\t|" + format.format(shallowHeap) + "\t|" + format.format(retainedHeap);		
	}
}
