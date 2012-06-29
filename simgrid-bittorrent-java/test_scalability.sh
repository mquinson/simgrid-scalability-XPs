#!/bin/bash

maxnodes=30000

bin="java -Xss104k -cp $HOME/simgrid-java/examples:$HOME/simgrid-java/simgrid.jar bittorrent/Bittorrent "
timefmt="clock:%e user:%U sys:%S swapped:%W exitval:%x max:%Mk avg:%Kk # %C"
me=tmp/`hostname -s`

nodes=0

function roll() {
  max=$1
  rand=`dd if=/dev/urandom count=1 2> /dev/null | cksum | cut -f1 -d" "`
  res=`expr $rand % $max`
  echo $res
}

function create_test() {
  nodes=`roll $maxnodes`
}

test -e tmp || mkdir tmp

for i in `seq 1 50 `; do

  create_test
  
  python generate.py $nodes 31 1000 > "bittorrent.xml"
  echo "$nodes nodes, network LV08 precision 1e-5"
  cmd="$bin One_cluster_nobb_10000_hosts.xml bittorrent.xml --cfg=network/model:LV08"
    cmd="$cmd --log=root.thres:critical "
  echo $cmd
  
  /usr/bin/time -f "$timefmt" -o $me.timings $cmd size:$size

  if grep "Command terminated by signal" $me.timings ; then
    echo "Damn, error detected:"
  elif grep "Command exited with non-zero status" $me.timings ; then
    echo "Damn, error detected:"
  else
    echo
    echo -n "XXXX PRECIOUS_RESULT XXXX size:$nodes"
  fi
  cat $me.timings
  echo
done

