use strict;

my $source = "DEA";

open(my $deaFile, $ARGV[0] ) or die "Couldn't open DEA file.\n";

my @deaLines = <$deaFile>;
my $i;
my $j;
my %schedules = ();
my %scheduleMembers = ();
# we need to hard-code this Y/N value to something meaningful
# my %narcoticStatus = ();
my %narcoticMembers = ();

for($i=0; $i < @deaLines; $i++) {
	my $line = $deaLines[$i];
	chomp($line);
	my @values = split('\\|', $line);
	
	#0 = schedule code
	#1 = schedule name
	#2 = substance name
	#3 = substance code
	#4 = is narcotic
	#5... = synonyms
	my $scheduleCode = $values[0];
	my $scheduleName = $values[1];
	my $substanceName = $values[2];
	my $substanceCode = $values[3];
	my $narcoticValue = $values[4];
	my @synonyms = ();
	
	push @{$narcoticMembers{$narcoticValue}}, $substanceName;
	
	$schedules{$scheduleCode} = $scheduleName;
	push @{$scheduleMembers{$scheduleCode}}, $substanceName;
	
	for($j=5; $j < @values; $j++) {
		push @synonyms, $values[$j];
	}
	
	# Addition format
	# Term|TTY|PV|SourceID|Source|ClassType
	
	my $pvAddition = join("|", $substanceName, "PV", $substanceName, $substanceCode, $source, "Substance");
	my @termAdditions = ();
	for($j=0; $j < @synonyms; $j++) {
		my $synonym = $synonyms[$j];
		if($synonym ne $substanceName) {
			my $termAddition = join("|", $synonyms[$j], "SY", $substanceName, "", $source, "Substance");
			push @termAdditions, $termAddition;
		}
	}
	
	print $pvAddition."\n";
	foreach(@termAdditions) {
		print $_."\n";
	}
}

for my $key ( sort keys %scheduleMembers ) {
	my $scheduleCode = $key;
	my $scheduleName = $schedules{$key};
	print join("|", $scheduleName, "", $scheduleName, $scheduleCode, $source, "Class");
	foreach(@{$scheduleMembers{$key}}) {
		print join("|", $_, "memberof", $schedules{$key}, "", $source, "Class");
		print "\n";
	}
}

for my $key ( sort keys %narcoticMembers ) {
	print join("|", $key, "", $key, "", $source, "Class");
	foreach(@{$narcoticMembers{$key}}) {
		print join("|", $_, "memberof", $key, "", $source, "Class");
		print "\n";		
	}
}