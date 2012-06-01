#set -e
test_to_run=v37_raw

timefmt="clock:%e user:%U sys:%S swapped:%W exitval:%x max:%Mk avg:%Kk # %C"
maxslaves=200000
maxtasks=1000000
[[ ! -d tmp ]] && mkdir tmp
me=tmp/`hostname -s`
cmd=""
logs=/dev/null
function cmdline_setup() {
  arg=$1

  case X$arg in
   Xv37*) id=v37 ;     export LD_LIBRARY_PATH=/home/mquinson/install-3.7/lib
     master_platf='msg_platform-v361.xml'
     cmd="./masterslave_mailbox-37 msg_platform-v361.xml masterslave_mailbox_deployment.xml --log=msg_test.thres=critical --log=simix_kernel.thres=critical"
     case X$arg in
       X${id}_ctx) id="${id}_ctx"; cmd="$cmd --cfg=contexts/factory:ucontext" ;;
       X${id}_raw) id="${id}_raw"; cmd="$cmd --cfg=contexts/factory:raw"      ;;
       X${id}_pth) id="${id}_pth"; cmd="$cmd --cfg=contexts/factory:thread" ;;
       *) echo "Cannot parse the context factory: $arg"; exit 1;
     esac

     case X$arg in
       X*_16k) id="${id}_12k"; cmd="$cmd --cfg=contexts/stack_size:16" ;;
     esac
     ;;

   Xv35*) id=v35;
     master_platf="msg_platform-v35.xml"
     case X$arg in
       Xv35_ctx)
          id=v35_ctx
  	  export LD_LIBRARY_PATH=/home/mquinson/install-3.5-ctx/lib
          cmd="./masterslave_mailbox-3.5-ctx msg_platform-v35.xml masterslave_mailbox_deployment.xml --log=msg_test.thres=critical --log=simix_kernel.thres=critical"
  	  ;;
       Xv35_pth)
          id=v35_pth
  	  export LD_LIBRARY_PATH=/home/mquinson/install-3.5-pth/lib
          cmd="./masterslave_mailbox-3.5-pth msg_platform-v35.xml masterslave_mailbox_deployment.xml --log=msg_test.thres=critical --log=simix_kernel.thres=critical"
  	  ;;
     esac
     ;;

   Xdebug) echo "entering debugging mode" ;;

   *) echo "Cannot parse the argument '$arg'"; exit 1;
  esac

  logs="/home/$USER/simgrid-scalability-XPs/logs/simgrid-masterslave.$id."`date +%y%m%d`.`hostname`
}

function dolog() {
  echo $@
  echo "$@" | sed 's/^/#/' >> $logs
}

#cmd="java -cp /home/mquinson/src_java:/home/mquinson/simgrid-ctx/jar/simgrid.jar BasicTest msg_platform.xml masterslave_mailbox_deployment.xml" # 2>/dev/null"

function header() {
  dolog "##########################################################################################"
  dolog `date`
  dolog `uname -a`
  bin=`echo $cmd|sed 's/ .*//'`
  dolog `ldd $bin`
  dolog LD_LIBRARY_PATH=$LD_LIBRARY_PATH $cmd
  dolog "##########################################################################################"
}

dolog "Legend: slave task wallclock usertime systemtime swapoutamount exitstatus # commandline used"

slave=""
task=""

function roll() {
  max=$1
  rand=`dd if=/dev/urandom count=1 2> /dev/null | cksum | cut -f1 -d" "`
  res=`expr $rand % $max`
  echo $res
}

function create_test_uniform() {
  slave=`roll $maxslaves`
  task=`roll $maxtasks`
}

function runit() {
      ./masterslave_mailbox_deployment_gen.pl $task $slave < $master_platf > masterslave_mailbox_deployment.xml

       dolog "############ New test (slave: $slave task: $task)"
       /usr/bin/time  -f "$timefmt" -o $me.timings $cmd >$me.stdout 2>$me.stderr
       echo "# STDOUT follows" >> $logs
       sed 's/^/# /' $me.stdout >> $logs

       echo "# STDERR follows" >> $logs
       sed 's/^/# /g' $me.stderr >> $logs
       grep "Command terminated by signal" $me.timings | sed 's/^/# /g' >> $logs

       if grep -q "Command terminated by signal" $me.timings ; then
          echo -n "# $id $slave $task " >> $logs
       else
          echo -n "$id $slave $task " >> $logs
       fi
       cat $me.timings |grep -v "Command terminated by signal" >> $logs

       echo -n "$id $slave $task "
       cat $me.timings |grep -v "Command terminated by signal"
}


for i in `seq 1 200 ` ; do

  create_test_uniform
  cmdline_setup $test_to_run
  header
  dolog "Current test: slave:$slave task:$task"
  runit
done

exit 0
rand=`od -d -N2 -An /dev/urandom`
rand=`expr $rand  % 2` # 0 or 1, same probability

if [ x$rand == x1 ] ; then
  cmdline_setup v35_ctx
  header
  dolog "Current test: slave:$slave task:$task"
  runit

  cmdline_setup v361_ctx
  header
  dolog "Current test: slave:$slave task:$task"
  runit

else
  cmdline_setup v361_ctx
  header
  dolog "Current test: slave:$slave task:$task"
  runit

  cmdline_setup v35_ctx
  header
  dolog "Current test: slave:$slave task:$task"
  runit
fi

done
