package cn.ac.iscas.oomr.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.inspections.threads.ThreadOverviewQuery;
import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IStackFrame;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.util.VoidProgressListener;

import cn.ac.iscas.oomr.dominatortree.Row;
import cn.ac.iscas.oomr.thread.path.Path;
import cn.ac.iscas.oomr.thread.path.SinglePath;

public class ThreadAnalyzer {

	private ISnapshot snapshot;
	private String phase;
	private Set<Integer> dominators;
	private List<?> threadObjList; // objects of running threads (overview)
	private IResultTree result;
	
	private List<SinglePath> map = new ArrayList<SinglePath>();
	private List<SinglePath> combine = new ArrayList<SinglePath>();
	private List<SinglePath> reduce = new ArrayList<SinglePath>();
	private List<SinglePath> others = new ArrayList<SinglePath>();
	
	public ThreadAnalyzer(ISnapshot snapshot, String phase) {
		this.snapshot = snapshot;
		this.phase = phase;
	}
	
	// find the required thread
	public List<Object> selectThreads(String[] threadNames) {
		getThreadOverview();
		
		if(threadObjList == null)
			return null;
		
		List<Object> threadsToCheck = new ArrayList<Object>();
		
		for(Object threadObj : threadObjList) {
			
			try {
				// getObjectId
				IContextObject co = result.getContext(threadObj);
				IObject obj = snapshot.getObject(co.getObjectId());
				String tName = obj.getClassSpecificName();
				
				if(tName != null) {
					for(String name : threadNames) {
						if(tName.startsWith(name)) {
							threadsToCheck.add(threadObj);
							break;
						}
					}
				}
				
			} catch (SnapshotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		return threadsToCheck;
	}

/*	public void findStacks(int[] dominatorIds, String[] threadNames) {
		List<Object> threadsToCheck = selectThreads(threadNames);
		
		if(threadsToCheck != null) {
			for(Object threadObj: threadsToCheck)
				findStacks(dominatorIds, threadObj);
		}
	}*/
	
/*	// Given the dominator objects
	@SuppressWarnings("unchecked")
	public void findStacks(int[] dominatorIds, Object threadObj) {
		dominators = new HashSet<Integer>();
		for(int id : dominatorIds)
			dominators.add(id);

		// for each thread: java.lang.Thread @ 0xed425378
		try {
			// get objectId of threadObj
			IContextObject co = result.getContext(threadObj);
			IObject obj = snapshot.getObject(co.getObjectId());

			// System.out.println(new Row(obj));

			// get the call stack
			if (result.hasChildren(threadObj)) {
				// {List<IStackFrame> stackFrames}
				// at org.apache.pig.impl.util.SpillableMemoryManager.registerSpillable(Lorg/apache/pig/impl/util/Spillable;)V (SpillableMemoryManager.java:278) 
				//		<local> org.apache.pig.impl.util.SpillableMemoryManager @ 0xed4f7848|    |           24 |            24 |                    |
				//		<local> org.apache.pig.data.DefaultDataBag @ 0xf946a8d8             |    |           40 |           120 |                    |
				//		<local> java.util.LinkedList @ 0xed4f7860 Busy Monitor              |    |           24 |    11,340,384 |                    |
				// at org.apache.pig.data.BagFactory.registerBag(Lorg/apache/pig/data/DataBag;)V (BagFactory.java:135)

				List<IStackFrame> stackFrames = (List<IStackFrame>) result.getChildren(threadObj);
				
				for (IStackFrame sf : stackFrames) {
					//System.out.println("\t" + sf.getText());

					// get the in-memory objects in each stack
					int[] objectIds = sf.getLocalObjectsIds();
					
					List<Tuple> containedDominators = new ArrayList<Tuple>();
					List<Integer> path = new ArrayList<Integer>();
					
					if(objectIds != null) {
						// for each <local>
						for (int objectId : objectIds) {
							// System.out.println("\t\t" + new Row(snapshot.getObject(objectId)));
							// path.add(objectId);
							
							findDomainatorsRecursive(objectId, containedDominators, 0, path);
							
							if(!containedDominators.isEmpty())
								displayThreadDominators(obj, sf, objectId, containedDominators);
							path.clear();
							containedDominators.clear();
						}
					}
					
				}
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
*/

	// Given a stackframe, find its inner dominators
	//	<local> org.apache.pig.impl.util.SpillableMemoryManager @ 0xed4f7848|    |           24 |            24 | 
	// TODO: delete depth


	private void findDomainatorsRecursive(int localObjId, List<SinglePath> dominatorList, int depth, List<Integer> path) {
	
		if(depth == 6)
			return;
		try {
			IObject obj = snapshot.getObject(localObjId);
			// String objectName = obj.getTechnicalName();
			
			if(dominators.contains(localObjId)) {
				path.add(localObjId);
				dominatorList.add(new SinglePath(localObjId, depth, path));
				path.remove(path.size() - 1);
			}
			
			else if(obj.getOutboundReferences() != null) {
			
				List<NamedReference> outRefs = obj.getOutboundReferences();
				path.add(localObjId);
				for(NamedReference nr : outRefs) {
					String name = nr.getName();
					
					if(name.startsWith("<class>") || name.startsWith("<classloader>") 
							|| name.startsWith("<super>") || name.startsWith("contextClassloader") || name.startsWith("me")) {
						continue;			
					}

					if(nr.getObjectId() != localObjId) {
						
						findDomainatorsRecursive(nr.getObjectId(), dominatorList, depth + 1, path);
						
					}
				}
				path.remove(path.size() - 1);
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
		
		
	private void displayThreadDominators(IObject threadObj, IStackFrame sf, int objInStack, List<SinglePath> containedDominators) {
		
		System.out.println(threadObj.getDisplayName());
		System.out.println("\t" + sf.getText());
		try {
			//System.err.println("\t\t" + snapshot.getObject(objInStack).getDisplayName() + "[" + objInStack + "]");
			for(SinglePath sPath : containedDominators) {
				List<Integer> path = sPath.getPath();
				System.out.println("\t\t  " + "--------------------------------------------------------");
				for(Integer id : path) {
					System.out.println("\t\t  " + snapshot.getObject(id).getDisplayName()+ "[" + id + "]");
					
//					if(id == 34821 || id == 34822) {
//						List<NamedReference> outRefs = snapshot.getObject(id).getOutboundReferences();
//						for(NamedReference nr : outRefs) {
//							System.out.println("\t\t\t" + nr.getName() + " [" + snapshot.getObject(nr.getObjectId()).getDisplayName() + "]");
//						}
//					}
				}
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Map<Integer, Path> findReferencedThreads(List<Row> userObjs) {
		String[] threadNames = null;
		
		if(phase.equals("map") || phase.equals("spill") || phase.equals("merge")) {
			threadNames = new String[2];
			threadNames[0] = "main";
			threadNames[1] = "SpillThread";
		} 
		
		else if(phase.equals("shuffle") || phase.equals("sort")) {
			threadNames = new String[4];
			threadNames[0] = "main";
			threadNames[1] = "MapOutputCopier";
			threadNames[2] = "Thread for merging in memory files";
			threadNames[3] = "Thread for merging on-disk files";
		}
		
		else if(phase.equals("reduce")) {
			threadNames = new String[2];
			threadNames[0] = "main";
			threadNames[1] = "Readahead";
		}
		
			
			
		List<Object> threadsOverview = selectThreads(threadNames);
		
		if(threadsOverview != null) {
			for(Object threadObj: threadsOverview)
				findStacks(userObjs, threadObj);
		}
		
		return obtainDominatorToPath();
		
	}
	
	private Map<Integer, Path> obtainDominatorToPath() {
		
		Map<Integer, Path> dominatorToPath = new HashMap<Integer, Path>();
		
		for(SinglePath sp : map) {
			int dominatorId = sp.getDominatorId();
			
			if(!dominatorToPath.containsKey(dominatorId)) {
				Path path = new Path(dominatorId);
				path.addMapPath(sp);
				dominatorToPath.put(dominatorId, path);
			}
			else {
				dominatorToPath.get(dominatorId).addMapPath(sp);
			}
		}
		
		for(SinglePath sp : combine) {
			int dominatorId = sp.getDominatorId();
			
			if(!dominatorToPath.containsKey(dominatorId)) {
				Path path = new Path(dominatorId);
				path.addCombinePath(sp);
				dominatorToPath.put(dominatorId, path);
			}
			else {
				dominatorToPath.get(dominatorId).addCombinePath(sp);
			}
		}
		
		for(SinglePath sp : reduce) {
			int dominatorId = sp.getDominatorId();
			
			if(!dominatorToPath.containsKey(dominatorId)) {
				Path path = new Path(dominatorId);
				path.addReducePath(sp);
				dominatorToPath.put(dominatorId, path);
			}
			else {
				dominatorToPath.get(dominatorId).addReducePath(sp);
			}
		}
		
		for(SinglePath sp : others) {
			int dominatorId = sp.getDominatorId();
			
			if(!dominatorToPath.containsKey(dominatorId)) {
				Path path = new Path(dominatorId);
				path.addOthersPath(sp);
				dominatorToPath.put(dominatorId, path);
			}
			else {
				dominatorToPath.get(dominatorId).addOthersPath(sp);
			}
		}
		
		return dominatorToPath;
	}

	@SuppressWarnings("unchecked")
	private void findStacks(List<Row> userObjs, Object threadObj) {
		dominators = new HashSet<Integer>();
		for(Row r : userObjs)
			dominators.add(r.getObjectId());

		// get objectId of threadObj
		int threadObjId = result.getContext(threadObj).getObjectId();
		
		String method = "others";
		
		// this thread has stack frames
		if (result.hasChildren(threadObj)) {
			// {List<IStackFrame> stackFrames}
			// at org.apache.pig.impl.util.SpillableMemoryManager.registerSpillable(Lorg/apache/pig/impl/util/Spillable;)V (SpillableMemoryManager.java:278) 
			//		<local> org.apache.pig.impl.util.SpillableMemoryManager @ 0xed4f7848|    |           24 |            24 |                    |
			//		<local> org.apache.pig.data.DefaultDataBag @ 0xf946a8d8             |    |           40 |           120 |                    |
			//		<local> java.util.LinkedList @ 0xed4f7860 Busy Monitor              |    |           24 |    11,340,384 |                    |
			// at org.apache.pig.data.BagFactory.registerBag(Lorg/apache/pig/data/DataBag;)V (BagFactory.java:135)

			List<IStackFrame> stackFrames = (List<IStackFrame>) result.getChildren(threadObj);
			
			for (int i = stackFrames.size() - 1; i > 0; i--) {
				IStackFrame sf = stackFrames.get(i);
				
				if(method.equals("others")) {
					if(sf.getText().contains(".map("))
						method = "map";
					if(sf.getText().contains(".combine("))
						method = "combine";
					if(sf.getText().contains(".reduce("))
						method = "reduce";
				}
				
				// get the in-memory objects in each stack
				int[] localObjectIds = sf.getLocalObjectsIds();
				
				List<SinglePath> localObjToDominators = new ArrayList<SinglePath>();
				List<Integer> tPath = new ArrayList<Integer>();
				
				if(localObjectIds != null) {
					// for each <local>
					for (int objectId : localObjectIds) {
						// System.out.println("\t\t" + new Row(snapshot.getObject(objectId)));
						// path.add(objectId);
						
						findDomainatorsRecursive(objectId, localObjToDominators, 0, tPath);
						
//						if(!localObjToDominators.isEmpty()) {		
//							displayThreadDominators(obj, sf, objectId, localObjToDominators);
//						}
						
						tPath.clear();
						
					}
				}
				
				for(SinglePath sp : localObjToDominators) {
					sp.setThreadObjId(threadObjId);
					sp.setStackframe(sf);
				}
				
				if(method.equals("map"))
					map.addAll(localObjToDominators);
				else if(method.equals("combine"))
					combine.addAll(localObjToDominators);
				else if(method.equals("reduce"))
					reduce.addAll(localObjToDominators);
				else
					others.addAll(localObjToDominators);
					
				
			}
		}
		
	}

/*	// display the basic information of running threads
	@SuppressWarnings("unchecked")
	public void threadOverview() {
		ThreadOverviewQuery impl = new ThreadOverviewQuery();
		impl.setSnapshot(snapshot);

		try {
			result = (IResultTree) impl.execute(new VoidProgressListener());
			threadObjList = result.getElements();

			System.out.println("---------------Thread information---------------");

			Column[] columns = result.getColumns();

			// display the column titles
			System.out.print("|");
			for (Column col : columns) {
				System.out.print(col.getLabel() + "\t|");
			}
			System.out.println();

			// display the labels of Markdown
			System.out.print("|:----------- |");
			for (int i = 1; i < columns.length; i++)
				System.out.print("-----------: |");
			System.out.println();

			// for each thread
			for (Object threadObj : threadObjList) {

				System.out.print("|");
				for (int i = 0; i < columns.length; i++) {
					Object row = result.getColumnValue(threadObj, i);
					System.out.print(row + "\t|");
				}
				System.out.println();
			}

			System.out.println("---------------------------------------------");

			// list the call stack of each thread
			for (Object threadObj : threadObjList) {

				try {
					// getObjectId
					IContextObject co = result.getContext(threadObj);
					IObject obj = snapshot.getObject(co.getObjectId());

					System.out.println(new Row(obj));

					// get the call stack
					if (result.hasChildren(threadObj)) {
						List<IStackFrame> stackFrames = (List<IStackFrame>) result
								.getChildren(threadObj);
						for (IStackFrame sf : stackFrames) {
							System.out.println("\t" + sf.getText());

							// get the in-memory objects in each stack
							int[] objectIds = sf.getLocalObjectsIds();
							for (int objectId : objectIds) {
								System.out.println("\t\t"+ new Row(snapshot.getObject(objectId)));
							}
						}
					}
				} catch (SnapshotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	*/

	// get the objects of running threads
	public void getThreadOverview() {
		ThreadOverviewQuery impl = new ThreadOverviewQuery();
		impl.setSnapshot(snapshot);
		
		try {
			result = (IResultTree) impl.execute(new VoidProgressListener());
			threadObjList = result.getElements();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void displayThreadOverview() {
		System.out.println("---------------Thread Overview---------------");

		Column[] columns = result.getColumns();

		// display the column titles
		System.out.print("|");
		for (Column col : columns) {
			System.out.print(col.getLabel() + "\t|");
		}
		System.out.println();

		// display the labels of Markdown
		System.out.print("|:------------|");
		for (int i = 1; i < columns.length; i++)
			System.out.print("-----------:|");
		System.out.println();

		// for each thread
		for (Object threadObj : threadObjList) {

			System.out.print("|");
			for (int i = 0; i < columns.length; i++) {
				Object row = result.getColumnValue(threadObj, i);
				System.out.print(row + "\t|");
			}
			System.out.println();
		}

		System.out.println("---------------------------------------------");

		// list the call stack of each thread
		for (Object threadObj : threadObjList) {

			try {
				// getObjectId
				IContextObject co = result.getContext(threadObj);
				IObject obj = snapshot.getObject(co.getObjectId());

				System.out.println(new Row(obj));

				// get the call stack
				if (result.hasChildren(threadObj)) {
					List<IStackFrame> stackFrames = (List<IStackFrame>) result
							.getChildren(threadObj);
					for (IStackFrame sf : stackFrames) {
						System.out.println("\t" + sf.getText());

						// get the in-memory objects in each stack
						int[] objectIds = sf.getLocalObjectsIds();
						for (int objectId : objectIds) {
							System.out.println("\t\t"+ new Row(snapshot.getObject(objectId)));
						}
					}
				}
			} catch (SnapshotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void display(Map<Integer, Path> dominatorsToThreads) {
		System.out.println("|------------------------ dominators => Threads/func() ------------------------|");
		
		for(Entry<Integer, Path> dt : dominatorsToThreads.entrySet()) {
			int dominatorId = dt.getKey();
			try {
				IObject dominator = snapshot.getObject(dominatorId);
				Path p = dt.getValue();
				
				System.out.println("[" + dominator.getTechnicalName() + "] =>");
				p.display(snapshot);
				System.out.println();
				
			} catch (SnapshotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
			
}


