#! /bin/bash

timefmt="clock:%e user:%U sys:%S swapped:%W exitval:%x max:%Mk avg:%Kk # %C"
[[ ! -d tmp ]] && mkdir tmp
[[ ! -f RunningMasterSlaves.class ]] && javac -cp gridSim.jar:lib/simjava2.jar:. RunningMasterSlaves.java
me=tmp/`hostname -s`

maxtasks=500000
maxslaves=2000
maxjobsize=1000000
maxfileINsize=1000000
maxfileOUTsize=1000000

function roll() {
  max=$1
  rand=`dd if=/dev/urandom count=1 2> /dev/null | cksum | cut -f1 -d" "`
  res=`expr $rand % $max`
  echo $res
}


for i in `seq 1 1000` ; do
  echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

  slave=`roll $maxslaves`
  task=`roll $maxtasks`
  jobsize=`roll $maxjobsize`
  infilesize=`roll $maxfileINsize`
  outfilesize=`roll $maxfileOUTsize`
  echo slave:$slave task:$task jobsize:$jobsize infilesize:$infilesize outfilesize:$outfilesize


  killall -KILL java 2>/dev/null
  # Wrong number of args. Usage : number of jobs, job size, number of hosts, input file size for job, output file size for job
  /usr/bin/time -f "$timefmt" -o $me.timings  java -Xmx46g -cp gridSim.jar:lib/simjava2.jar:. RunningMasterSlaves  $task $jobsize $slave $infilesize $outfilesize

  if grep "Command terminated by signal" $me.timings ; then
    echo "Damn, error detected:"
  elif grep "Command exited with non-zero status" $me.timings ; then
    echo "Damn, error detected:"
  else
    echo
    echo "#Legend: slave task jobsize infilesize outfilesize wallclock(in sec) usertime systemtime swapoutamount exitstatus # commandline used"
    echo -n "XXXX PRECIOUS_RESULT XXXX $slave $task $jobsize $infilesize $outfilesize "
  fi
  cat $me.timings

  killall java 2>/dev/null
done
