#!/usr/bin/perl -w
use strict;

my($nb_task) = $ARGV[0]  || die "USAGE: masterslave_mailbox_deployment_gen.pl nbTasks nbSlaves\n"; 
my($nb_slave) = $ARGV[1] || die "USAGE: masterslave_mailbox_deployment_gen.pl nbTasks nbSlaves\n"; 
my(@host_list)=();
my($line);
my($header,$doneheader)=("",0);
while(defined($line=<STDIN>)) {
    if (!$doneheader) {
	$header .= $line;
	if ($line =~ m/END OF HEADER/) {
	    $doneheader = 1;
	}
    }
    chomp $line;
    if($line =~ /<host\s+id/) {
	$line =~ s/.*id="//;
	$line =~ s/".*//;
	push @host_list,$line;
    }
}

print "$header  <!-- The master process (with some arguments) -->\n";

my(@host_list_orig) = @host_list;
@host_list = ();


foreach (0..$nb_slave) {
    $host_list[$_] = $host_list_orig[$_%scalar(@host_list_orig)];
}

my($h) = $host_list[0];
my($n) = scalar @host_list;

print "
  <process host=\"$h\" function=\"master\">
     <argument value=\"$nb_task\"/>       <!-- Number of tasks -->
     <argument value=\"50000000\"/>  <!-- Computation size of tasks -->
     <argument value=\"1000000\"/>   <!-- Communication size of tasks -->
     <argument value=\"$n\"/>  <!-- Number of slaves -->
  </process>
  <!-- The slave process (with no argument) -->
";
$n=0;
foreach $h (@host_list) {
    print "  <process host=\"$h\" function=\"slave\"><argument value=\"$n\"/></process>\n";
    $n++;
}
print "</platform>\n";

