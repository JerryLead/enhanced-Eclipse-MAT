package cn.ac.iscas.oomr.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

import cn.ac.iscas.oomr.dominatortree.Row;

public class ShuffleSortPhaseAnalyzer {

	private ISnapshot snapshot;
	private List<Row> largeDominators;
	private Set<Integer> toDeleteObjIds;
	
	// byte[] 
	// in org.apache.hadoop.mapred.ReduceTask$ReduceCopier$MapOutputCopier @ 0xd4341920  MapOutputCopier attempt_***
	private List<Row> segmentsInCopy;
	
	// org.apache.hadoop.mapred.ReduceTask$ReduceCopier$MapOutput @ 0xe0db2458 
	// in LinkedList 
	// in org.apache.hadoop.mapred.ReduceTask$ReduceCopier @ 0xd43552e8
	private List<Row> segmentsInList;
	
	// org.apache.hadoop.mapred.Merger$MergeQueue @ 0xf05688f8
	// private Row mergeQueue;
	
	// org.apache.hadoop.mapred.Merger$Segment @ 0xf0552470
	// in java.lang.Object[] or directly
	// in org.apache.hadoop.mapred.Merger$MergeQueue @ 0xf05688f8
	private List<Row> segmentsInMerge;
	
	private Row keySegment;
	private Row comparatorSegment;
	private Row minSegment;

	public ShuffleSortPhaseAnalyzer(ISnapshot snapshot, List<Row> largeDominators) {
		this.snapshot = snapshot;
		this.largeDominators = largeDominators;
		this.toDeleteObjIds = new HashSet<Integer>();
		
		segmentsInCopy = new ArrayList<Row>();
		segmentsInList = new ArrayList<Row>();
		segmentsInMerge = new ArrayList<Row>();
	}

	/**
	 * classify object into framework objects and user objects, leave framework objects, return user objects.
	 * @return user objects
	 */
	public List<Row> filterFrameworkObjs() {
		
		try {
			
			for(Row row : largeDominators) {
				String className = row.getClassName();
				
				if(className.startsWith("org.apache.hadoop.mapred.ReduceTask$ReduceCopier$MapOutputCopier @")) {
					processSegmentsInCopy(snapshot.getObject(row.getObjectId()));
					toDeleteObjIds.add(row.getObjectId());
				}
				
				else if(className.startsWith("org.apache.hadoop.mapred.ReduceTask$ReduceCopier @")) {
					processSegmentsInList(snapshot.getObject(row.getObjectId()));
					toDeleteObjIds.add(row.getObjectId());
				}
					
				
				else if(className.startsWith("org.apache.hadoop.mapred.ReduceTask$ReduceCopier$InMemFSMergeThread @")) {
					processSegmentsInMergeThread(snapshot.getObject(row.getObjectId()));
					toDeleteObjIds.add(row.getObjectId());
				}
					
				
				else if(className.startsWith("org.apache.hadoop.mapred.Merger$MergeQueue @")) {
					processSegmentsInMergeQueue(snapshot.getObject(row.getObjectId()));
					toDeleteObjIds.add(row.getObjectId());
				}
					
			}

		} catch (SnapshotException e) {
			e.printStackTrace();
		}
	
		deleteFrameworkObjs();
		
		displayFrameworkObjs();
		
		return largeDominators;
	}
	

	private void processSegmentsInCopy(IObject mapOutputCopier) {
		String attemptTask = mapOutputCopier.getClassSpecificName();
		
		attemptTask = attemptTask.substring(attemptTask.indexOf("attempt"));
		
		int mapOutputCopierId = mapOutputCopier.getObjectId();
		List<Row> subDominatorsObjs = subDominateObjs(mapOutputCopierId);
		
		for(Row r : subDominatorsObjs) {
			if(r.getClassName().startsWith("byte[")) {
				r.setFrameObjName(mapOutputCopier.getDisplayName());
				segmentsInCopy.add(r);
				break;
			}
		}
	}

	/*
	Class Name                                                                            | Shallow Heap | Retained Heap | Percentage
	----------------------------------------------------------------------------------------------------------------------------------
	org.apache.hadoop.mapred.ReduceTask$ReduceCopier @ 0xc18b9688                         |          160 |    39,942,592 |      4.93%
	|- java.util.Collections$SynchronizedList @ 0xc18d0990                                |           24 |    39,915,760 |      4.92%
	|  '- java.util.LinkedList @ 0xc18d09a8                                               |           24 |    39,915,736 |      4.92%
	|     '- java.util.LinkedList$Entry @ 0xc18d09c0                                      |           24 |    39,915,712 |      4.92%
	|        |- java.util.LinkedList$Entry @ 0xf1239b20                                   |           24 |     6,677,704 |      0.82%
	|        |  '- org.apache.hadoop.mapred.ReduceTask$ReduceCopier$MapOutput @ 0xefee04e8|           48 |     6,677,680 |      0.82%
	|        |- java.util.LinkedList$Entry @ 0xf1239c10                                   |           24 |     6,674,808 |      0.82%
	|        |- java.util.LinkedList$Entry @ 0xf0567398                                   |           24 |     6,666,544 |      0.82%
	|        |- java.util.LinkedList$Entry @ 0xf1239ac8                                   |           24 |     6,644,960 |      0.82%
	|        |- java.util.LinkedList$Entry @ 0xf1239c68                                   |           24 |     6,631,888 |      0.82%
	|        |- java.util.LinkedList$Entry @ 0xf0bd2728                                   |           24 |     6,619,784 |      0.82%
	----------------------------------------------------------------------------------------------------------------------------------
	*/
	private void processSegmentsInList(IObject reduceCopier) {
		
		int reduceCopierId = reduceCopier.getObjectId();
		List<Row> subDominatorsObjs = subDominateObjs(reduceCopierId);
		
		for(Row r : subDominatorsObjs) {
			
			if(r.getClassName().startsWith("java.util.Collections$SynchronizedList @")) {
				List<Row> javaUtilLinkedList = subDominateObjs(r.getObjectId());
				for(Row jll : javaUtilLinkedList) {
					
					if(jll.getClassName().startsWith("java.util.LinkedList @")) {
						List<Row> javaUtilListEntry = subDominateObjs(jll.getObjectId());
						for(Row jle : javaUtilListEntry) {
							
							if(jle.getClassName().startsWith("java.util.LinkedList$Entry @")) {
								List<Row> entries = subDominateObjs(jle.getObjectId());
								for(Row entry : entries) {
									
									List<Row> mapOutputs = subDominateObjs(entry.getObjectId());
									for(Row mapOutput : mapOutputs) {
										if(mapOutput.getClassName().startsWith("org.apache.hadoop.mapred.ReduceTask$ReduceCopier$MapOutput")) {
											
											List<Row> bytes = subDominateObjs(mapOutput.getObjectId());
											for(Row byteRef : bytes) {
												
												if(byteRef.getClassName().startsWith("byte[")) {
													byteRef.setFrameObjName(mapOutput.getClassName());
													segmentsInList.add(byteRef);
												}
											}
											
										}
									}
								}
							}

						}
						break;
					}
				}	
				break;
			}
		}
		
	}

	private void processSegmentsInMergeThread(IObject inMemFSMergeThread) {
		int inMemFSMergeThreadId = inMemFSMergeThread.getObjectId();
		List<Row> subDominatrosObjs = subDominateObjs(inMemFSMergeThreadId);
		
		for(Row r : subDominatrosObjs) {
			
			if(r.getClassName().startsWith("org.apache.hadoop.mapred.Merger$MergeQueue @")) {
				
				try {
					processSegmentsInMergeQueue(snapshot.getObject(r.getObjectId()));
					toDeleteObjIds.add(r.getObjectId());
				} catch (SnapshotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

		}
		
	}
	
	/*
	Class Name                                                              | Shallow Heap | Retained Heap | Percentage
	-------------------------------------------------------------------------------------------------------------------
	org.apache.hadoop.mapred.Merger$MergeQueue @ 0xd4333548                                                                                                                                                                                                                                             |           80 |   209,175,648 |     28.43%
	|- byte[104599542] @ 0xc7bbadf8  .@.2.1=2|6paywfxurgtgahsiiatabopwk.html|  104,599,560 |   104,599,560 |     14.22%
	|- org.apache.hadoop.io.WritableComparator @ 0xd43d8468                 |           32 |   104,575,096 |     14.21%
	|- org.apache.hadoop.io.DataInputBuffer @ 0xd43d8188                    |           40 |           368 |      0.00%
	|- org.apache.hadoop.io.DataInputBuffer @ 0xd43d82f8                    |           40 |           368 |      0.00%
	|- org.apache.hadoop.util.Progress @ 0xd43d86e8                         |           40 |           120 |      0.00%
	|- java.lang.Object[6] @ 0xd43d8160                                     |           40 |            40 |      0.00%
	|- org.apache.hadoop.mapred.Merger$MergeQueue$1 @ 0xd43d8760            |           16 |            16 |      0.00%
	-------------------------------------------------------------------------------------------------------------------
	*/

	private void processSegmentsInMergeQueue(IObject mergeQueue) throws SnapshotException {
		
		List<NamedReference> mergeQRefs = mergeQueue.getOutboundReferences();
		// calculate segments which are being merged
		for(NamedReference mergeRef : mergeQRefs) {
			
			// heap java.lang.Object[6] @ 0xd43d8160
			
			if(mergeRef.getName().equals("heap")) {
				
				List<NamedReference> heapRefs = mergeRef.getObject().getOutboundReferences();
				for(NamedReference heapRef : heapRefs) {
						
					if (heapRef.getName().startsWith("[")) {
							
							List<NamedReference> segmentRefs = heapRef.getObject().getOutboundReferences();
							for(NamedReference segmentRef : segmentRefs) {
								
								if(segmentRef.getName().equals("key")) {
									List<NamedReference> keyRefs = segmentRef.getObject().getOutboundReferences();
									
									for(NamedReference keyRef : keyRefs) {
										
										if(keyRef.getName().equals("buffer")) {
											List<NamedReference> bufferRefs = keyRef.getObject().getOutboundReferences();
											
											for(NamedReference bufferRef : bufferRefs) {
												
												if(bufferRef.getName().equals("buf")) {
													Row segmentInMerge = new Row(bufferRef.getObject());
													segmentInMerge.setFrameObjName(heapRef.getObject().getTechnicalName());
													segmentsInMerge.add(segmentInMerge);
													toDeleteObjIds.add(bufferRef.getObjectId());
													toDeleteObjIds.add(heapRef.getObjectId());
												}
											}
										}
									}
								}
							}
							
					}
						
				}
				break;
			}
			
			if(mergeRef.getName().equals("key")) {
				
				List<NamedReference> keyRefs = mergeRef.getObject().getOutboundReferences();
				for(NamedReference keyRef : keyRefs) {
					
					if(keyRef.getName().equals("buffer")) {
						List<NamedReference> bufferRefs = keyRef.getObject().getOutboundReferences();
						
						for(NamedReference bufferRef : bufferRefs) {
							
							if(bufferRef.getName().equals("buf")) {
								keySegment = new Row(bufferRef.getObject());
								keySegment.setFrameObjName(mergeRef.getObject().getTechnicalName());
								toDeleteObjIds.add(bufferRef.getObjectId());
								toDeleteObjIds.add(mergeRef.getObjectId());
							}
							
						}
					}
				}		
			}
			
			else if(mergeRef.getName().equals("comparator")) {
				
				List<NamedReference> comparatorRefs = mergeRef.getObject().getOutboundReferences();
				for(NamedReference comparatorRef : comparatorRefs) {
					
					if(comparatorRef.getName().equals("buffer")) {
						List<NamedReference> dataBufferRefs = comparatorRef.getObject().getOutboundReferences();
						
						for(NamedReference dataBufferRef : dataBufferRefs) {
							
							if(dataBufferRef.getName().equals("buffer")) {
								List<NamedReference> bufferRefs = dataBufferRef.getObject().getOutboundReferences();
								
								for(NamedReference bufferRef : bufferRefs) {
									if(bufferRef.getName().equals("buf")) {
										comparatorSegment = new Row(bufferRef.getObject());
										comparatorSegment.setFrameObjName(comparatorRef.getObject().getTechnicalName());
										toDeleteObjIds.add(bufferRef.getObjectId());
										toDeleteObjIds.add(comparatorRef.getObjectId());
									}
								}
							}
	
						}
					}
				}
			}
			
			else if(mergeRef.getName().equals("minSegment")) {
				
				List<NamedReference> minSegmentRefs = mergeRef.getObject().getOutboundReferences();
				for(NamedReference minSegmentRef : minSegmentRefs) {
					
					if(minSegmentRef.getName().equals("key")) {
						List<NamedReference> keyRefs = minSegmentRef.getObject().getOutboundReferences();
						
						for(NamedReference keyRef : keyRefs) {
							if(keyRef.getName().equals("buffer")) {
								
								List<NamedReference> bufferRefs = keyRef.getObject().getOutboundReferences();
								for(NamedReference bufferRef : bufferRefs) {
									
									if(bufferRef.getName().equals("buf")) {
										minSegment = new Row(bufferRef.getObject());
										minSegment.setFrameObjName(mergeRef.getObject().getTechnicalName());
										toDeleteObjIds.add(bufferRef.getObjectId());
										toDeleteObjIds.add(mergeRef.getObjectId());
									}
								}
								
							}
						}
						
					}
				}
				
			}
		}

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
	
	private void displayFrameworkObjs() {
		System.out.println("|-------------------Framework objects in shuffle & sort phase-------------------|");
		System.out.println("| FrameworkObj \t| Class name \t| shallowHeap \t| retainedHeap \t|");
		System.out.println("| :----------- | :----------- | -----------: | -----------: |");
		
		System.out.println("[SegmentsInCopy] => ");
		for(Row r : segmentsInCopy) 
			System.out.println(r);
		
		System.out.println();
		System.out.println("[SegmentsInList] => ");
		for(Row r : segmentsInList) 
			System.out.println(r);
		
		System.out.println();
		System.out.println("[SegmentsInMerge] => ");
		for(Row r : segmentsInMerge) 
			System.out.println(r);
		
		if(keySegment != null) {
			System.out.println();
			System.out.println("[keySegment] => ");
			System.out.println(keySegment);
		}
		
		if(comparatorSegment != null) {
			System.out.println();
			System.out.println("[comparatorSegment] => ");
			System.out.println(comparatorSegment);
		}
		
		if(minSegment != null) {
			System.out.println();
			System.out.println("[minSegment] => ");
			System.out.println(minSegment);
		}
		
		System.out.println();
		System.out.println();
	}

}
