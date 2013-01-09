SimGrid Scalability
===================

[SimGrid](http://simgrid.gforge.inria.fr/) scalability tests.

SimGrid
-------
SimGrid is a toolkit that provides core functionalities for the simulation of distributed applications in heterogeneous distributed environments. The specific goal of the project is to facilitate research in the area of parallel and distributed large scale systems, such as Grids, P2P systems and clouds. Its use cases encompass heuristic evaluation, application prototyping or even real application development and tuning.

Grid'5000
---------
[Grid'5000](https://www.grid5000.fr/) a scientific instrument designed to support experiment-driven research in all areas of computer science related to parallel, large-scale or
distributed computing and networking.

Launch tests
------------
1. `gem install g5k-campaign --source http://g5k-campaign.gforge.inria.fr/pkg --user-install --no-ri --no-rdoc`
2. `git clone git://github.com/mquinson/simgrid-scalability-XPs.git`
2. `cd simgrid-scalability-XPs/g5k-scalab; g5k-campaign -i sg-scalability.rb`
