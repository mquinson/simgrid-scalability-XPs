#!/usr/bin/python

# This script generates a specific deployment file for the masterslave in Java.
# It assumes that the platform will be a cluster.
# Usage: python generate.py nb_slaves nb_tasks
# Example: python generate.py 10000 100000

import sys, random

if len(sys.argv) != 3:
	print("Usage: python generate.py nb_slaves nb_tasks > deployment_file.xml")
	sys.exit(1)

nb_slaves = int(sys.argv[1])
nb_tasks  = int(sys.argv[2])

computation_size = 50000000
communication_size = 1000000

sys.stdout.write("<?xml version='1.0'?>\n"
"<!DOCTYPE platform SYSTEM \"http://simgrid.gforge.inria.fr/simgrid.dtd\">\n"
"<platform version=\"3\">\n"
"  <process host=\"c-0.me\" function=\"masterslave.Master\"><argument value=\"%d\"/><argument value=\"%d\"/><argument value=\"%d\"/><argument value=\"%d\"/></process>\n" % (nb_tasks, computation_size, communication_size, nb_slaves))

for i in range(0, nb_slaves):

  line = "  <process host=\"c-%d.me\" function=\"masterslave.Slave\"><argument value=\"%d\" /></process>\n" % (i,i)
  sys.stdout.write(line)

sys.stdout.write("</platform>")

