package cn.ac.iscas.oomr.dominatortree;

import java.text.DecimalFormat;

import org.eclipse.mat.snapshot.model.IObject;

public class Row implements Comparable<Row> {
	
	private int objectId;
	private String className;
	private long shallowHeap;
	private long retainedHeap;
	
	private String frameObjName = null;
	private int segMapId = Integer.MAX_VALUE;
	
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

	public String getFrameObjName() {
		return frameObjName;
	}

	public void setFrameObjName(String frameObjName) {
		this.frameObjName = frameObjName;
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
	
	public void setSegMapId(int segMapId) {
		this.segMapId = segMapId;
	}
	
	public String toString() {
		DecimalFormat format = new DecimalFormat(",###");
		
		if(frameObjName == null)
			return "| " + className + "\t| " + format.format(shallowHeap) + "\t| " + format.format(retainedHeap) + "\t|";	
		else {
			if(frameObjName.contains(".")) {
				if(frameObjName.contains("attempt")) {
					frameObjName = frameObjName.substring(frameObjName.indexOf("$") + 1);
				}
					
				else
					frameObjName = frameObjName.substring(frameObjName.lastIndexOf(".") + 1);
			}
				
			if(segMapId == Integer.MAX_VALUE)
				return "| " + frameObjName + "\t| " + className + "\t| " + format.format(shallowHeap) + "\t| " 
					+ format.format(retainedHeap) + "\t|";
			else
				return "| " + frameObjName + "\t| " + className + "\t| " + format.format(shallowHeap) + "\t| " 
					+ format.format(retainedHeap) + "\t|" + segMapId + "\t|";
		}
			
	}
}
