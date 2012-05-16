
/*
 * Author: L.Bobelin (October 2011)
 * Ahthor: M.Quinson (April 2012)
 * 
 * Description: A simple program that runs a master that is distributing tasks to some computing nodes
 * on a grid platform. Based on example5 from GridSim 5.2 distro.
 *
 */

import java.util.*;
import gridsim.*;

/**
 * RunningMasterSlaves class creates Gridlets and sends them to many grid resource
 * entities
 */
class RunningMasterSlaves extends GridSim{
	private GridletList sendList = new GridletList();
	private GridletList receiveList_= new GridletList();
	private int totalNumberOfHost; 


	/**
	 * Allocates a new RunningMasterSlaves object
	 * @param name  the Entity name of this object
	 * @param baud_rate     the communication speed
	 * @throws Exception This happens when creating this entity before
	 *                   initializing GridSim package or the entity name is
	 *                   <tt>null</tt> or empty
	 * @see gridsim.GridSim#Init(int, Calendar, boolean, String[], String[],
	 *          String)
	 */
	RunningMasterSlaves(String name, double baud_rate, int totalNumberOfHost, 
			int jobCount, int jobSize,int fileSize, int outputSize) throws Exception {
		super(name, baud_rate);

		this.totalNumberOfHost = totalNumberOfHost;
		
		// Creates a list of Gridlets or Tasks for this grid user
		int id = getEntityId(name);
		GridSimStandardPE.setRating(100);

		// Create jobs
		for (int i = 1; i < jobCount+1; i++) {
			// the Gridlet length setted by user
			double length = GridSimStandardPE.toMIs(jobSize);

			// creates a new Gridlet object
			Gridlet gridlet = new Gridlet(/*id, make it in [id+1,id+N] even if I've no idea what it is good for*/ i+id,
					length, fileSize, outputSize);

			gridlet.setUserID(id);

			// add the Gridlet into a list
			sendList.add(gridlet);
		}

		System.out.println("Created " + this.sendList.size() + " Gridlets");
	}

	/**
	 * Master's core work.
	 */
	public void body()
	{
		int resourceID[] = null;


		// wait until all resources register themselves to the system, and get their ID
		
		while (resourceID == null) {
			super.gridSimHold(1.); // hold by one (simulated) second 

			LinkedList resList = super.getGridResourceList();
			if (resList.size() == totalNumberOfHost) {
				// get their ID and proceed
				resourceID = new int[totalNumberOfHost];
				for (int i = 0; i < totalNumberOfHost; i++) 
					resourceID[i] = ( (Integer)resList.get(i) ).intValue();
				break;
				
			} else // not ready yet
				System.out.println(GridSim.clock()+": Some grid resources are not registered yet. Hold on...");
		}


		// Send the gridlet to random grid resources and wait for replies
		Random random = new Random();
		for (int i = 0; i < this.sendList.size(); i++)
		{
			Gridlet gridlet =  this.sendList.get(i);

			int id = random.nextInt(totalNumberOfHost);

			// Sends one Gridlet to a grid resource specified in "resourceID"
			super.gridletSubmit(gridlet, resourceID[id]);

			// OR another approach to send a gridlet to a grid resource entity
			//super.send(resourceID[id], GridSimTags.SCHEDULE_NOW,
			//      GridSimTags.GRIDLET_SUBMIT, gridlet);

			// waiting to receive a Gridlet back from resource entity after completion, and store it
			receiveList_.add( gridletReceive() );
		}
		System.out.println(GridSim.clock()+": I'm done here. I received "+receiveList_.size()+" gridlets back.");

		// shut down all the entities
		shutdownGridStatisticsEntity();
		shutdownUserEntity();
		terminateIOEntities();
	}

	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) throws Exception	{
		Vector<GridResource> createdResourceList = new Vector<GridResource>();
				
		if (args.length!= 5) {
			System.out.println("Wrong number of args. Usage : number of jobs, job size, number of hosts, input file size for job, output file size for job");
			System.exit(1);
		}
		int jobCount = Integer.valueOf(args[0]).intValue();
		int jobSize = Integer.valueOf(args[1]).intValue();
		int totalNumberOfHost = Integer.valueOf(args[2]).intValue();
		int fileSize = Integer.valueOf(args[3]).intValue();
		int outputSize = Integer.valueOf(args[4]).intValue();
		System.out.println("#jobs:" + jobCount + " #jobSize:" + jobSize +" #hosts:" + totalNumberOfHost + " #inputSize:" + fileSize + " #outputSize:" + outputSize);

		try  {	    
			// Initialize the GridSim package
			GridSim.init(1 /* #user */, Calendar.getInstance(), false /*no trace*/);

			// Creates the GridResource objects
			for (int j = 0; j< totalNumberOfHost; j++) {
				createdResourceList.add(createGridResource("Resource_"+j));
			}

			// Creates the RunningMasterSlaves object
			RunningMasterSlaves obj = new RunningMasterSlaves("RunningMasterSlaves", 560.00,  
					totalNumberOfHost, jobCount, jobSize,fileSize, outputSize);

			// Run the simulation
			GridSim.startGridSimulation();

		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory");
			System.exit(1);
		}
	}

	/**
	 * Creates one Grid resource, composed of a unique machine with no external load.
	 * 
	 * @param name  a Grid Resource name (must be unique wrt simjava entities names)
	 * @return a GridResource object
	 * @throws Exception 
	 */
	private static GridResource createGridResource(String name) throws Exception
	{
		int mipsRating = 377; // computation speed
		double baud_rate = 100.0;           // communication speed

		MachineList mList = new MachineList();
		mList.add( new Machine(0, 4, mipsRating));  // The actual processing element

		/* 
		 * it seems impossible to create a grid resource without all this stuff, even 
		 * if you don't need to model any kind of external load nor price accounting 
		 */
		ResourceCharacteristics resConfig = 
				new ResourceCharacteristics(
						"Sun Ultra", "Solaris", mList, ResourceCharacteristics.TIME_SHARED,
						9./*time_zone*/, 3./*cost?*/);

		return new GridResource(name, baud_rate, 11L*13*17*19*23+1/*seed*/,
				resConfig, 
				0.,0.,0., /* The external load is always 0 for us, ignoring time of day */ 
				new LinkedList()/*Weekends*/,
				new LinkedList()/*Holidays*/);
	}

} // end class

