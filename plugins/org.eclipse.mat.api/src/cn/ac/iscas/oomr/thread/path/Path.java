package cn.ac.iscas.oomr.thread.path;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

public class Path {
	private int dominatorId;
	
	private List<SinglePath> premap;
	private List<SinglePath> map;
	private List<SinglePath> combine;
	private List<SinglePath> prereduce;
	private List<SinglePath> reduce;
	private List<SinglePath> others;
	
	public Path(int dominatorId) {
		this.dominatorId = dominatorId;
		premap = new ArrayList<SinglePath>();
		map = new ArrayList<SinglePath>();
		combine = new ArrayList<SinglePath>();
		prereduce = new ArrayList<SinglePath>();
		reduce = new ArrayList<SinglePath>();
		others = new ArrayList<SinglePath>();
	}
	
	public void addPreMapPath(SinglePath sp) {
		premap.add(sp);	
	}
	
	public void addMapPath(SinglePath sp) {
		map.add(sp);
	}
	
	public void addCombinePath(SinglePath sp) {
		combine.add(sp);
	}
	
	public void addPreReducePath(SinglePath sp) {
		prereduce.add(sp);
	}
	
	public void addReducePath(SinglePath sp) {
		reduce.add(sp);
	}
	
	public void addOthersPath(SinglePath sp) {
		others.add(sp);
	}

	public String getCode() {
		StringBuilder sb = new StringBuilder();
		
		if(!premap.isEmpty())
			sb.append("premap");
		if(!map.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("map");
		}
			
		if(!combine.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("combine");
		}
			
		if(!prereduce.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("prereduce");
		}
			
		if(!reduce.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("reduce");
		}
			
		if(!others.isEmpty() && sb.length() == 0)
			sb.append("others");
		
		return sb.toString();
	}
	
	public void output(ISnapshot snapshot, PrintWriter writer) {
		try {
			 
			int maxItems = 20;
			
			int i = 0;
			if(!map.isEmpty()) {
				String threadName = snapshot.getObject(map.get(0).getThreadObjId()).getClassSpecificName();
				
				writer.println("\t|------ in map() in " + threadName + " ------|");
				
				String stackFrame = map.get(0).getStackframe().getText();
				writer.println("\t" + stackFrame);
				
				for(SinglePath sp : map) {
					if(i++ == maxItems)
						break;
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						writer.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
						writer.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					writer.println();
				}
			}
			
			i = 0;
			if(!combine.isEmpty()) {
				String threadName = snapshot.getObject(combine.get(0).getThreadObjId()).getClassSpecificName();
				
				writer.println("\t|------ in combine() in " + threadName + " ------|");
				
				String stackFrame = combine.get(0).getStackframe().getText();
				writer.println("\t" + stackFrame);
				
				for(SinglePath sp : combine) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						writer.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					
					
					// System.out.println("\t  " + "--------------------------------------------------------");	
					
					for(Integer id : sp.getPath()) 
						writer.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");
					writer.println();			
				}
			}
			
			i = 0;
			if(!reduce.isEmpty()) {
				String threadName = snapshot.getObject(reduce.get(0).getThreadObjId()).getClassSpecificName();
				
				writer.println("\t|------ in reduce() in " + threadName + " ------|");
				
				String stackFrame = reduce.get(0).getStackframe().getText();
				writer.println("\t" + stackFrame);
				
				
				for(SinglePath sp : reduce) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						writer.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					// System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");
					
					for(Integer id : sp.getPath()) 
						writer.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					writer.println();
				}
			}
			
			i = 0;
			if(!premap.isEmpty()) {
				String threadName = snapshot.getObject(premap.get(0).getThreadObjId()).getClassSpecificName();
				
				writer.println("\t|------ in premap() in " + threadName + " ------|");
				
				String stackFrame = premap.get(0).getStackframe().getText();
				writer.println("\t" + stackFrame);
				
				
				for(SinglePath sp : premap) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						writer.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					// System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
						writer.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					writer.println();
				}
			}
			
			i = 0;
			if(!prereduce.isEmpty()) {
				String threadName = snapshot.getObject(prereduce.get(0).getThreadObjId()).getClassSpecificName();
				
				writer.println("\t|------ in prereduce() in " + threadName + " ------|");
				
				String stackFrame = prereduce.get(0).getStackframe().getText();
				writer.println("\t" + stackFrame);
				
				for(SinglePath sp : prereduce) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						writer.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					
					// System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
						writer.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					writer.println();
				}
			}
			/*
			if(!others.isEmpty()) {
				System.out.println("\t|---------------------------- in other func() ----------------------------|");
				for(SinglePath sp : others) {
					
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");
					System.out.println();
				}
			}
			*/
		} catch (SnapshotException e) {
			e.printStackTrace();
		}
	}
	
	public void display(ISnapshot snapshot) {
		try {
		 
			int maxItems = 20;
			
			int i = 0;
			if(!map.isEmpty()) {
				String threadName = snapshot.getObject(map.get(0).getThreadObjId()).getClassSpecificName();
				
				System.out.println("\t|------ in map() in " + threadName + " ------|");
				
				String stackFrame = map.get(0).getStackframe().getText();
				System.out.println("\t" + stackFrame);
				
				for(SinglePath sp : map) {
					if(i++ == maxItems)
						break;
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						System.out.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
							System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			
			i = 0;
			if(!combine.isEmpty()) {
				String threadName = snapshot.getObject(combine.get(0).getThreadObjId()).getClassSpecificName();
				
				System.out.println("\t|------ in combine() in " + threadName + " ------|");
				
				String stackFrame = combine.get(0).getStackframe().getText();
				System.out.println("\t" + stackFrame);
				
				for(SinglePath sp : combine) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						System.out.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					
					
					// System.out.println("\t  " + "--------------------------------------------------------");	
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");
					System.out.println();			
				}
			}
			
			i = 0;
			if(!reduce.isEmpty()) {
				String threadName = snapshot.getObject(reduce.get(0).getThreadObjId()).getClassSpecificName();
				
				System.out.println("\t|------ in reduce() in " + threadName + " ------|");
				
				String stackFrame = reduce.get(0).getStackframe().getText();
				System.out.println("\t" + stackFrame);
				
				
				for(SinglePath sp : reduce) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						System.out.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					// System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			
			i = 0;
			if(!premap.isEmpty()) {
				String threadName = snapshot.getObject(premap.get(0).getThreadObjId()).getClassSpecificName();
				
				System.out.println("\t|------ in premap() in " + threadName + " ------|");
				
				String stackFrame = premap.get(0).getStackframe().getText();
				System.out.println("\t" + stackFrame);
				
				
				for(SinglePath sp : premap) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						System.out.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					// System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
							System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			
			i = 0;
			if(!prereduce.isEmpty()) {
				String threadName = snapshot.getObject(prereduce.get(0).getThreadObjId()).getClassSpecificName();
				
				System.out.println("\t|------ in prereduce() in " + threadName + " ------|");
				
				String stackFrame = prereduce.get(0).getStackframe().getText();
				System.out.println("\t" + stackFrame);
				
				for(SinglePath sp : prereduce) {
					if(i++ == maxItems)
						break;
					
					if(!sp.getStackframe().getText().equals(stackFrame)) {
						System.out.println("\t" + sp.getStackframe().getText());
						stackFrame = sp.getStackframe().getText();
					}
					
					// System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
							System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			/*
			if(!others.isEmpty()) {
				System.out.println("\t|---------------------------- in other func() ----------------------------|");
				for(SinglePath sp : others) {
					
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");
					System.out.println();
				}
			}
			*/
		} catch (SnapshotException e) {
			e.printStackTrace();
		}
	}

	
}
