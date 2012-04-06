
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
	private GridletList receiveList_;
	private int totalResource_;
	private static int count = 1000; // number of jobs
	private static int totalNumberOfHost = 5000; // total number of hosts
	private static int jobSize = 1000 ; // default job size
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
	RunningMasterSlaves(String name, double baud_rate, int total_resource)
			throws Exception {
		super(name, baud_rate);
		this.totalResource_ = total_resource;
		this.receiveList_ = new GridletList();

		// Gets an ID for this entity wich will be the master.
		this.ID_ = new Integer( getEntityId(name) );
		//System.out.println("Creating a master entity with name = " +
		//        name + ", and id = " + this.ID_);
		// Creates a list of Gridlets or Tasks for this grid user
		this.list_ = createGridlets( this.ID_.intValue());
		//System.out.println("Creating " + this.list_.size() + " Gridlets");
	}

	/**
	 * Master's core work.
	 */
	public void body()
	{
		int resourceID[] = new int[this.totalResource_];
		double resourceCost[] = new double[this.totalResource_];
		String resourceName[] = new String[this.totalResource_];

		LinkedList resList;
		ResourceCharacteristics resChar;

		// waiting to get list of resources. Since GridSim package uses
		// multi-threaded environment, your request might arrive earlier
		// before one or more grid resource entities manage to register
		// themselves to GridInformationService (GIS) entity.
		// Therefore, it's better to wait in the first place
		while (true)
		{
			// need to pause for a while to wait GridResources finish
			// registering to GIS
			super.gridSimHold(1.0);    // hold by 1 second (simulation time, not real one, so it does not count on profiling)

			resList = super.getGridResourceList();
			if (resList.size() == this.totalResource_)
				break;
			else
				System.out.println("Waiting to get list of resources ...");
		}

		int i = 0;

		// a loop to get all the resources available
		for (i = 0; i < this.totalResource_; i++)
		{
			// Resource list contains list of resource IDs not grid resource
			// objects.
			resourceID[i] = ( (Integer)resList.get(i) ).intValue();

			// Requests to resource entity to send its characteristics
			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW,
					GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

			// waiting to get a resource characteristics
			resChar = (ResourceCharacteristics) super.receiveEventObject();
			resourceName[i] = resChar.getResourceName();
			resourceCost[i] = resChar.getCostPerSec();

			/* System.out.println("Received ResourceCharacteristics from " +
                    resourceName[i] + ", with id = " + resourceID[i]);*/

			// record this event into "stat.txt" file
			/* super.recordStatistics("\"Received ResourceCharacteristics " +
                    "from " + resourceName[i] + "\"", "");*/
		}

		Gridlet gridlet;

		// a loop to get one Gridlet at one time and sends it to a random grid
		// resource entity. Then waits for a reply
		Random random = new Random();
		int id = 0;
		for (i = 0; i < this.list_.size(); i++)
		{
			gridlet =  this.list_.get(i);

			id = random.nextInt(this.totalResource_);

			// Sends one Gridlet to a grid resource specified in "resourceID"
			super.gridletSubmit(gridlet, resourceID[id]);

			// OR another approach to send a gridlet to a grid resource entity
			//super.send(resourceID[id], GridSimTags.SCHEDULE_NOW,
			//      GridSimTags.GRIDLET_SUBMIT, gridlet);

			// waiting to receive a Gridlet back from resource entity after completion
			gridlet = super.gridletReceive();

			// stores the received Gridlet into a new GridletList object
			this.receiveList_.add(gridlet);
		}

		// shut down all the entities, including GridStatistics entity since
		// we used it to record certain events.
		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	/**
	 * Gets the list of Gridlets
	 * @return a list of Gridlets
	 */
	public GridletList getGridletList() {
		return this.receiveList_;
	}

	/**
	 * This method will show you how to create Gridlets with and without
	 * GridSimRandom class.
	 * @param userID    the user entity ID that owns these Gridlets
	 * @return a GridletList object
	 */
	private GridletList createGridlets(int userID)
	{
		// Creates a container to store Gridlets
		GridletList list = new GridletList();

		int id = 0;
		double length = 3500.0;

		// sets the PE MIPS Rating
		GridSimStandardPE.setRating(100);

		// Create jobs
		for (int i = 1; i < count+1; i++)
		{
			// the Gridlet length setted by user
			length = GridSimStandardPE.toMIs(jobSize);


			// creates a new Gridlet object
			Gridlet gridlet = new Gridlet(id + i, length, fileSize,
					outputSize);

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
		jobSize = Integer.valueOf(args[1]).intValue();
		totalNumberOfHost = Integer.valueOf(args[2]).intValue();
		fileSize = Integer.valueOf(args[3]).intValue();
		outputSize = Integer.valueOf(args[4]).intValue();
		System.out.println("#jobs:" + count + " #jobSize:" + jobSize +" #hosts:" + totalNumberOfHost + " #inputSize:" + fileSize + " #outputSize:" + outputSize);

		try  {	    
			// Initialize the GridSim package
			GridSim.init(1 /* #user */, Calendar.getInstance(), false /*no trace*/);

			// Second step: Creates one or more GridResource objects
			for (int j = 0; j< totalNumberOfHost; j++) {
				createdResourceList.add(createGridResource("Resource_"+j));
			}
			int total_resource = 3;

			// Third step: Creates the RunningMasterSlaves object
			RunningMasterSlaves obj = new RunningMasterSlaves("RunningMasterSlaves", 560.00, total_resource);

			// Fourth step: Starts the simulation
			GridSim.startGridSimulation();

		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory");
			System.exit(1);
		}



	}

	/**
	 * Creates one Grid resource. A Grid resource contains one or more
	 * Machines. Similarly, a Machine contains one or more PEs (Processing
	 * Elements or CPUs).
	 * <p>
	 * In this simple example, we are simulating one Grid resource with three
	 * Machines that contains one or more PEs.
	 * @param name  a Grid Resource name
	 * @return a GridResource object
	 */
	private static GridResource createGridResource(String name)
	{
		/*System.out.println();
        System.out.println("Starting to create one Grid resource with " +
                "3 Machines");

        // Here are the steps needed to create a Grid resource:
        // 1. We need to create an object of MachineList to store one or more
        //    Machines*/
		MachineList mList = new MachineList();
		//System.out.println("Creates a Machine list");

		// 2. Create one Machine with its id, number of PEs and MIPS rating per PE
		//    In this example, we are using a resource from
		//    hpc420.hpcc.jp, AIST, Tokyo, Japan
		//    Note: these data are taken the from GridSim paper, page 25.
		//          In this example, all PEs has the same MIPS (Millions
		//          Instruction Per Second) Rating for a Machine.
		int mipsRating = 377;
		mList.add( new Machine(0, 4, mipsRating));   // First Machine
		/*System.out.println("Creates the 1st Machine that has 4 PEs and " +
                "stores it into the Machine list");*/

		// 3. Repeat the process from 2 if we want to create more Machines
		//    In this example, the AIST in Japan has 3 Machines with same
		//    MIPS Rating but different PEs.
		// NOTE: if you only want to create one Machine for one Grid resource,
		//       then you could skip this step.
		//  mList.add( new Machine(1, 4, mipsRating));   // Second Machine
		/*System.out.println("Creates the 2nd Machine that has 4 PEs and " +
                "stores it into the Machine list");*/

		//  mList.add( new Machine(2, 2, mipsRating));   // Third Machine
		/*System.out.println("Creates the 3rd Machine that has 2 PEs and " +
                "stores it into the Machine list");*/

		// 4. Create a ResourceCharacteristics object that stores the
		//    properties of a Grid resource: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/PE time unit).
		String arch = "Sun Ultra";      // system architecture
		String os = "Solaris";          // operating system
		double time_zone = 9.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using this resource

		ResourceCharacteristics resConfig = new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.TIME_SHARED,
				time_zone, cost);

		/*System.out.println("Creates the properties of a Grid resource and " +
                "stores the Machine list");*/

		// 5. Finally, we need to create a GridResource object.
		double baud_rate = 100.0;           // communication speed
		long seed = 11L*13*17*19*23+1;
		double peakLoad = 0.0;       // the resource load during peak hour
		double offPeakLoad = 0.0;    // the resource load during off-peak hr
		double holidayLoad = 0.0;    // the resource load during holiday

		// incorporates weekends so the grid resource is on 7 days a week
		LinkedList Weekends = new LinkedList();
		Weekends.add(new Integer(Calendar.SATURDAY));
		Weekends.add(new Integer(Calendar.SUNDAY));

		// incorporates holidays. However, no holidays are set in this example
		LinkedList Holidays = new LinkedList();
		GridResource gridRes = null;
		try
		{
			gridRes = new GridResource(name, baud_rate, seed,
					resConfig, peakLoad, offPeakLoad, holidayLoad, Weekends,
					Holidays);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		/*System.out.println("Finally, creates one Grid resource and stores " +
                "the properties of a Grid resource");
        System.out.println();*/

		return gridRes;
	}

	/**
	 * Prints the Gridlet objects
	 * @param list  list of Gridlets
	 */
	private static void printGridletList(GridletList list)
	{
		int size = list.size();
		Gridlet gridlet;

		String indent = "    ";
		System.out.println();
		System.out.println("========== OUTPUT ==========");
		System.out.println("Gridlet ID" + indent + "STATUS" + indent +
				"Resource ID" + indent + "Cost");

		for (int i = 0; i < size; i++)
		{
			gridlet = (Gridlet) list.get(i);
			System.out.print(indent + gridlet.getGridletID() + indent
					+ indent);

			if (gridlet.getGridletStatus() == Gridlet.SUCCESS)
				System.out.print("SUCCESS");

			System.out.println( indent + indent + gridlet.getResourceID() +
					indent + indent + gridlet.getProcessingCost() );
		}
	}

} // end class

