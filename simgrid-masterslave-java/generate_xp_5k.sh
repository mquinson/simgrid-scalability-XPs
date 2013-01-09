#!/bin/bash
for nodes in 10 100 500 1000 2000 5000 10000; do
    for tasks in 1000 10000 100000 500000 1000000; do
      python generate.py $nodes $tasks > masterslave$nodes-$tasks.xml
    done
done
