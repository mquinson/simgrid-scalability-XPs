apt-get -y install debconf-utils
echo "sun-java6-bin   shared/accepted-sun-dlj-v1-1    boolean true" | debconf-set-selections
apt-get -y install cmake-curses-gui libpcre3-dev sun-java6-jdk dtach git-core jed dc cpufrequtils

if hostname |grep -q pastel ; then
  echo "Changing the CPU freq to 1000MHz because we are on pastel"
  cpufreq-set -r -d 1000MHz -u 1000MHz -c 0 
  cpufreq-set -r -d 1000MHz -u 1000MHz -c 2
fi