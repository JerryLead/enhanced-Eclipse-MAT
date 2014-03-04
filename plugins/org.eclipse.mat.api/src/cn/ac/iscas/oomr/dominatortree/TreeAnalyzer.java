package cn.ac.iscas.oomr.dominatortree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;


public class TreeAnalyzer {

	private ISnapshot snapshot;
	private IResultTree rootTree;

	public TreeAnalyzer(ISnapshot snapshot, IResultTree dominatorRootTree) {
		this.snapshot = snapshot;
		this.rootTree = dominatorRootTree;
	}

	/**
	 * @param rawLargeDominators If the dominator is java.lang.Thread, we will expand it to small objects
	 * @return
	 */
	private List<Row> expandDominators(List<Row> rawLargeDominators) {
		List<Row> expandedObjs = new ArrayList<Row>();
		
		for(Row row : rawLargeDominators) {
			if(row.getClassName().startsWith("java.lang.Thread @")) {
				
				List<Row> subDominators = subDominateObjs(row.getObjectId());
				expandedObjs.addAll(subDominators);
			}	
			else
				expandedObjs.add(row);
		}

		return expandedObjs;
	}
	
	private List<Row> getAndSortDomiantors() {
		
		List<?> rootDominators = rootTree.getElements();
		List<Row> allDominators = new ArrayList<Row>();
		
		for (Object rootDominator : rootDominators) {
			IContextObject co = rootTree.getContext(rootDominator);
			IObject obj;
			try {
				// TODO Optimization: using getHeapSize(int[] objectsIds)
				// instead of getHeapSize(int objectsId)
				obj = snapshot.getObject(co.getObjectId());
				allDominators.add(new Row(obj));

			} catch (SnapshotException e) {
				e.printStackTrace();
			}
		}
		
		allDominators = expandDominators(allDominators);

		Collections.sort(allDominators);
		
		return allDominators;
	}
	
	// select the dominators which are larger than mb
	// "class" objects are not considered
	private List<Row> selectLargeDominators(List<Row> allDominators, float mb) {
		long limit = (long) (mb * 1024 * 1024);
		List<Row> largeObjs = new ArrayList<Row>();
		
		for (int i = 0; i < allDominators.size(); i++) {
			Row row = allDominators.get(i);

			if (row.getRetainedHeap() < limit)
				break;
			else if (!row.getClassName().startsWith("class")) {
				largeObjs.add(row);
			}
		}
		return largeObjs;
	}
	


	public List<Row> getLargeDominators(float mb) {
		List<Row> allDominators = getAndSortDomiantors();
		List<Row> largeDominators = selectLargeDominators(allDominators, mb);		
		
		return largeDominators;
	}
	
	

	
	
	public int[] selectLargeObjects(List<Row> rows, int mb) {
		long limit = mb * 1024 * 1024;
		List<Integer> largeObjIds = new ArrayList<Integer>();
		
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			if (row.getRetainedHeap() < limit)
				break;
			else if (!row.getClassName().startsWith("class")) {
				largeObjIds.add(row.getObjectId());
			}
		}
		
		int[] largeObjs = new int[largeObjIds.size()];
		
		for(int i = 0; i < largeObjs.length; i++)
			largeObjs[i] = largeObjIds.get(i);
		
		return largeObjs;
	}

	public void display(List<Row> rows, int mb) {
		long limit = mb * 1024 * 1024;

		System.out.println("Class name \t| shallowHeap \t| retainedHeap");
		System.out.println(":----------- | -----------: | -----------:");
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			if (row.getRetainedHeap() < limit)
				break;
			else if (!row.getClassName().startsWith("class")) {
		
				System.out.println(row);
				//getThreadInfo(row.getObjectId());
			}

			// display the call stack
			// try {
			//
			// if (snapshot.getThreadStack(row.getObjectId()) != null)
			// System.out.println("\t"
			// + snapshot.getThreadStack(row.getObjectId()));
			// } catch (SnapshotException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

		}

	}

	public String getThreadInfo(int objectId) {
		// get outgoing references

		try {
			int[] objectIds = snapshot.getInboundRefererIds(objectId);

			if (objectIds != null) {
				for (int id : objectIds) {
					IObject obj = snapshot.getObject(id);
					String name = obj.getClassSpecificName();

					if (name != null && (name.equals("SpillThread") || name.equals("main")))
						System.err.println(new Row(obj));
					else {
						IObject threadObj = findThread(obj);
						if(threadObj != null)
							System.err.println("\t" + new Row(threadObj));
					}

				}

			}

		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public IObject findThread(IObject object) {

		try {
			// get outgoing references
			int[] objectIds = snapshot.getInboundRefererIds(object
					.getObjectId());

			for (int objectId : objectIds) {
				IObject obj = snapshot.getObject(objectId);
				String name = obj.getClassSpecificName();

				if (name != null
						&& (name.equals("SpillThread") || name.equals("main")))
					return obj;
				else if (objectId != object.getObjectId())
					return findThread(obj);
			}

		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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

