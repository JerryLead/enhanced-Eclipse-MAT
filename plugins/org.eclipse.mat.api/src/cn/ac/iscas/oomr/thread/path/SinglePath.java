package cn.ac.iscas.oomr.thread.path;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.snapshot.model.IStackFrame;

public class SinglePath {
//  SinglePath: thread => stackframe => local object => subobject1 => subobject2 => dominator
//
//	dominator => thread1
//					at stackframe1
//						local object1
//							dominator
//						local object2
//							object
//								dominator
//					at stackframe2
//						local object1
//							dominator
//						local object2
//							object
//								dominator
//			  => thread1
//					at stackframe1
//						local object1
//							dominator
//						local object2
//							object
//								dominator
//					at stackframe2
//						local object1
//								dominator
//						local object2
//							object
//								dominator
	private int dominatorId;
	private int threadObjId;
	private IStackFrame stackframe;
	
	private int depth;
	private List<Integer> path;
	
	public SinglePath(int dominatorId, int depth, List<Integer> p) {
		this.dominatorId = dominatorId;
		this.depth = depth;
		path = new ArrayList<Integer>(p.size());
		path.addAll(p);
	}

	public int getThreadObjId() {
		return threadObjId;
	}

	public void setThreadObjId(int threadObjId) {
		this.threadObjId = threadObjId;
	}

	public IStackFrame getStackframe() {
		return stackframe;
	}

	public void setStackframe(IStackFrame stackframe) {
		this.stackframe = stackframe;
	}
	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public List<Integer> getPath() {
		return path;
	}

	public int getDominatorId() {
		return dominatorId;
	}

}
