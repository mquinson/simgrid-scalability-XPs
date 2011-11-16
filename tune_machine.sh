apt-get -y install debconf-utils
echo "sun-java6-bin   shared/accepted-sun-dlj-v1-1    boolean true" | debconf-set-selections
apt-get -y install cmake-curses-gui libpcre3-dev sun-java6-jdk dtach git-core jed