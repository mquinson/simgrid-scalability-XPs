#! /bin/bash

timefmt="clock:%e user:%U sys:%S swapped:%W exitval:%x max:%Mk avg:%Kk # %C"

maxtasks=1000000
maxslaves=5000
maxjobsize=1000000
maxfileINsize=1000000
maxfileOUTsize=1000000

function roll() {
  max=$1
  rand=`dd if=/dev/urandom count=1 2> /dev/null | cksum | cut -f1 -d" "`  
  res=`expr $rand % $max`
  echo $res
}

function create_test_uniform() {
  slave=`roll $maxslaves`
  task=`roll $maxtasks`
  jobsize=`roll $maxjobsize`
  infilesize=`roll $maxfileINsize`
  outfilesize=`roll $maxfileOUTsize`
}
  
for i in `seq 1 1000` ; do
 create_test_uniform
 echo slave:$slave task:$task jobsize:$jobsize infilesize:$infilesize outfilesize:$outfilesize
#done
#exit 0

#for task in 10 500 1000 5000 10000 50000 100000 500000 1000000 ; do 
#  for slave in 10 500 1000 5000 ; do # 10000 50000 100000 500000 1000000 ; do 
#    for jobsize in 10 1000000; do # 10000 1000000 ; do
#      for infilesize in 10 1000000; do # 10000 1000000 ; do
#        for outfilesize in 10 1000000; do # 10000 1000000 ; do
    
#
  echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  

  killall -KILL java 
  # Wrong number of args. Usage : number of jobs, job size, number of hosts, input file size for job, output file size for job
  /usr/bin/time -o timings  java -Xmx46g -cp gridSim.jar:lib/simjava2.jar:. RunningMasterSlaves  $task $jobsize $slave $infilesize $outfilesize 
  
  if grep "Command terminated by signal" timings ; then 
    echo "Damn, error detected:"
  else
    echo
    echo "#Legend: slave task jobsize infilesize outfilesize wallclock(in sec) usertime systemtime swapoutamount exitstatus # commandline used"
    echo -n "XXXX PRECIOUS_RESULT XXXX $slave $task $jobsize $infilesize $outfilesize "
  fi
  cat timings

  killall java
  done
  done
done
done
done
