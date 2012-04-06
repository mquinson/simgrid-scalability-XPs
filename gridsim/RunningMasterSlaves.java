
/*
 * Author L.Bobelin
 * Date: October 2011
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
	private Integer ID_;
	private GridletList list_;
	private GridletList receiveList_= new GridletList();
	private int totalResource_;
	private static int count = 1000; // number of jobs
	private static int totalNumberOfHost = 5000; // total number of hosts
	private static int fileSize = 5000; // total number of hosts
	private static int outputSize = 1000 ; // default job size
	private static Vector<GridResource> createdResourceList = new Vector<GridResource>();


	/**
	 * Allocates a new RunningMasterSlaves object
	 * @param name  the Entity name of this object
	 * @param baud_rate     the communication speed
	 * @param total_resource    the number of grid resources available
	 * @throws Exception This happens when creating this entity before
	 *                   initializing GridSim package or the entity name is
	 *                   <tt>null</tt> or empty
	 * @see gridsim.GridSim#Init(int, Calendar, boolean, String[], String[],
	 *          String)
	 */
	RunningMasterSlaves(String name, double baud_rate, int total_resource,int jobSize)
			throws Exception {
		super(name, baud_rate);
		this.totalResource_ = total_resource;

		// Gets an ID for this entity wich will be the master.
		this.ID_ = new Integer( getEntityId(name) );
		//System.out.println("Creating a master entity with name = " +
		//        name + ", and id = " + this.ID_);
		// Creates a list of Gridlets or Tasks for this grid user
		this.list_ = createGridlets( this.ID_.intValue(),jobSize);
		System.out.println("Creating " + this.list_.size() + " Gridlets");
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
			if (resList.size() == this.totalResource_) {
				// get their ID and proceed
				resourceID = new int[this.totalResource_];
				for (int i = 0; i < this.totalResource_; i++) 
					resourceID[i] = ( (Integer)resList.get(i) ).intValue();
				break;
				
			} else 
				System.out.println(GridSim.clock()+": Some grid resources are not registered yet. Hold on...");
		}


		// Send the gridlet to random grid resources and wait for replies
		Random random = new Random();
		for (int i = 0; i < this.list_.size(); i++)
		{
			Gridlet gridlet =  this.list_.get(i);

			int id = random.nextInt(this.totalResource_);

			// Sends one Gridlet to a grid resource specified in "resourceID"
			super.gridletSubmit(gridlet, resourceID[id]);

			// OR another approach to send a gridlet to a grid resource entity
			//super.send(resourceID[id], GridSimTags.SCHEDULE_NOW,
			//      GridSimTags.GRIDLET_SUBMIT, gridlet);

			// waiting to receive a Gridlet back from resource entity after completion, and store it
			this.receiveList_.add( gridletReceive() );
		}
		System.out.println(GridSim.clock()+": I'm done here. I received "+receiveList_.size()+" gridlets back.");

		// shut down all the entities, including GridStatistics entity since
		// we used it to record certain events.
		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	/**
	 * This method will show you how to create Gridlets with and without
	 * GridSimRandom class.
	 * @param userID    the user entity ID that owns these Gridlets
	 * @return a GridletList object
	 */
	private GridletList createGridlets(int userID, int jobSize) {
		// Creates a container to store Gridlets
		GridletList list = new GridletList();

		// sets the PE MIPS Rating
		GridSimStandardPE.setRating(100);

		// Create jobs
		for (int i = 1; i < count+1; i++)
		{
			// the Gridlet length setted by user
			double length = GridSimStandardPE.toMIs(jobSize);

			// creates a new Gridlet object
			Gridlet gridlet = new Gridlet(/*id, make it in [1,N] since 0 is user*/ i,
					length, fileSize, outputSize);

			gridlet.setUserID(userID);

			// add the Gridlet into a list
			list.add(gridlet);
		}

		return list;
	}


	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) throws Exception	{
		if (args.length!= 5) {
			System.out.println("Wrong number of args. Usage : number of jobs, job size, number of hosts, input file size for job, output file size for job");
			System.exit(1);
		}
		count = Integer.valueOf(args[0]).intValue();
		int jobSize = Integer.valueOf(args[1]).intValue();
		totalNumberOfHost = Integer.valueOf(args[2]).intValue();
		fileSize = Integer.valueOf(args[3]).intValue();
		outputSize = Integer.valueOf(args[4]).intValue();
		System.out.println("#jobs:" + count + " #jobSize:" + jobSize +" #hosts:" + totalNumberOfHost + " #inputSize:" + fileSize + " #outputSize:" + outputSize);

		try  {	    
			// Initialize the GridSim package
			GridSim.init(1 /* #user */, Calendar.getInstance(), false /*no trace*/);

			// Creates the GridResource objects
			for (int j = 0; j< totalNumberOfHost; j++) {
				createdResourceList.add(createGridResource("Resource_"+j));
			}
			int total_resource = 3;

			// Creates the RunningMasterSlaves object
			RunningMasterSlaves obj = new RunningMasterSlaves("RunningMasterSlaves", 560.00, total_resource, jobSize);

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

