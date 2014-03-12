package cn.ac.iscas.oomr.classifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

import cn.ac.iscas.oomr.dominatortree.Row;

public class ReducePhaseAnalyzer {
	private ISnapshot snapshot;
	private List<Row> largeDominators;
	
	private Set<Integer> toDeleteObjIds;
	
	// segments cached in reducebuffer
	private List<Row> segmentsInBuffer;
	
	private Row keySegment;
	private Row comparatorSegment;
	private Row minSegment;

	// used to check whether keySegment, comparatorSegment and minSegment exist in MergeQueue
	private Set<Integer> segmentIdSet;
		
	public ReducePhaseAnalyzer(ISnapshot snapshot, List<Row> largeDominators) {
		this.snapshot = snapshot;
		this.largeDominators = largeDominators;
		this.toDeleteObjIds = new HashSet<Integer>();
		
		segmentsInBuffer = new ArrayList<Row>();
		
		segmentIdSet = new HashSet<Integer>();
	}
	
	
	/**
	 * classify object into framework objects and user objects, leave framework objects, return user objects.
	 * @return user objects
	 */
	public List<Row> filterFrameworkObjs() {
		
		try {
			
			for(Row row : largeDominators) {
				String className = row.getClassName();

				if(className.startsWith("org.apache.hadoop.mapred.Merger$MergeQueue @")) {
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
													segmentsInBuffer.add(segmentInMerge);
													toDeleteObjIds.add(bufferRef.getObjectId());
													toDeleteObjIds.add(heapRef.getObjectId());
													
													segmentIdSet.add(bufferRef.getObjectId());
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

	private void displayFrameworkObjs() {
		System.out.println("## OOM in reduce phase\n");
		
		System.out.println("\n### Framework Objects\n");
		
		System.out.println("#### [SegmentsInBuffer] => \n");
		if(!segmentsInBuffer.isEmpty()) {
			System.out.println("| FrameworkObj \t| Inner object \t| shallowHeap \t| retainedHeap \t|");
			System.out.println("| :----------- | :----------- | -----------: | -----------: |");
			for(Row r : segmentsInBuffer) 
				System.out.println(r);
		}
		
		if(keySegment != null && !segmentIdSet.contains(keySegment.getObjectId())) {
			System.out.println("\n#### [keySegment] => \n");
			System.out.println("| FrameworkObj \t| Inner object \t| shallowHeap \t| retainedHeap \t|");
			System.out.println("| :----------- | :----------- | -----------: | -----------: |");
			System.out.println(keySegment);
			
			segmentIdSet.add(keySegment.getObjectId());
		}
		
		if(comparatorSegment != null && !segmentIdSet.contains(comparatorSegment.getObjectId())) {
			System.out.println("\n#### [comparatorSegment] => \n");
			System.out.println("| FrameworkObj \t| Inner object \t| shallowHeap \t| retainedHeap \t|");
			System.out.println("| :----------- | :----------- | -----------: | -----------: |");
			System.out.println(comparatorSegment);
			
			segmentIdSet.add(comparatorSegment.getObjectId());
		}
		
		if(minSegment != null && !segmentIdSet.contains(minSegment.getObjectId())) {
			System.out.println("\n#### [minSegment] => \n");
			System.out.println("| FrameworkObj \t| Inner object \t| shallowHeap \t| retainedHeap \t|");
			System.out.println("| :----------- | :----------- | -----------: | -----------: |");
			System.out.println(minSegment);
		}
		
		System.out.println();
		System.out.println();
	}
}
