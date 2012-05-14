apt-get -y install debconf-utils
echo "sun-java6-bin   shared/accepted-sun-dlj-v1-1    boolean true" | debconf-set-selections
apt-get -y install f2c cmake-curses-gui libpcre3-dev sun-java6-jdk dtach git-core jed dc cpufrequtils

swapoff -va

if hostname |grep -q pastel ; then
  echo "Changing the CPU freq to 1000MHz" # because we are on pastel"
  for cpu in `cpufreq-info |grep analyzing|sed 's/[^0-9]*//g'` ; do
    cpufreq-set -r -d 1000MHz -u 1000MHz -c $cpu
  done
else
  echo "NOT CHANGING THE CPU FREQ. FIX IT IF YOU RUN GOAL TESTS"
fi
