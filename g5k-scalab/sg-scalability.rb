require 'pp'
require 'yaml'
require 'optparse'

class SimgridScalability < Grid5000::Campaign::Engine
  set :environment, "squeeze-x64-nfs"
  set :resources, "nodes=1"
  set :properties, "cluster='graphene'"
  set :walltime, 3600
  set :notifications, ["xmpp:#{ENV['USER']}@jabber.grid5000.fr"]
  set :site, "nancy"
  set :name, "SG::Scalab"
  set :no_cleanup, true
  set :no_cancel, true

  after :deployment! do |env, *args|
    logger.info "[#{env[:site]}] Nodes have been deployed: #{env[:nodes].inspect}"
    env
  end

  on :install! do |env, *args|
    logger.info "[#{env[:site]}] Installing additional software on the nodes..."
    env[:nodes].each do |node|
      ssh(node, "root",:timeout => 10) do |ssh|
        out = ssh.exec!("apt-get update && apt-get -y install debconf-utils && echo 'sun-java6-bin   shared/accepted-sun-dlj-v1-1    boolean true' | debconf-set-selections && apt-get -y install cmake-curses-gui libpcre3-dev sun-java6-jdk dtach git-core jed dc cpufrequtils")
        logger.debug out
      end
    end
    env
  end

  after :install! do |env, *args|
    logger.info "[#{env[:site]}] Prepare nodes for experiments..."
    env[:nodes].each do |node|
      ssh(node, "root",:timeout => 10) do |ssh|
        out = ssh.exec!("swapoff -av")
        logger.debug out
      end
    end
    env
  end

  on :execute! do |env, *args|
    xp = "gridsim"
    logger.info "[#{env[:site]}] Launch #{xp} experiment..."
    ssh(env[:nodes], ENV['USER'], :multi => true, :timeout => 10) do |ssh|
      cmd = %Q{cd simgrid-scalability-XPs/gridsim-masterslave; ./testall.sh 2>&1 |cat >> ../logs/gridsim.`date +%y%m%d`.`hostname`}
      logger.info "[#{env[:site]}] Executing command: #{cmd}"
      ssh.exec(cmd)
      ssh.loop
    end
    env
  end
end
