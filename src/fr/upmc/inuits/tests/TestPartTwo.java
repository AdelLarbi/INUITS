package fr.upmc.inuits.tests;

import fr.upmc.components.cvm.AbstractCVM;

public class TestPartTwo extends AbstractCVM {

	public TestPartTwo() throws Exception {
		super();
	}
	
	@Override
	public void deploy() throws Exception {
		
		//TODO
		
		super.deploy();
	}		
	
	@Override
	public void shutdown() throws Exception {
				
		//TODO
		
		super.shutdown();
	}
	
	public void scenarioFoo() throws Exception {
		//TODO
	}
	
	public static void main(String[] args) {
		
		try {
			final TestPartTwo test = new TestPartTwo();
			test.deploy();
			
			System.out.println("starting...");
			test.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						test.scenarioFoo();
						
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}).start();
					
			Thread.sleep(40000L);
			
			System.out.println("shutting down...");
			test.shutdown();
			
			System.out.println("ending...");
			System.exit(0);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
