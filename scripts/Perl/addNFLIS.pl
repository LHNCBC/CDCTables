use strict;

my $source = "NFLIS";

open(my $nflisFile, $ARGV[0] ) or die "Couldn't open NFLIS file.\n";

my @nflisLines = <$nflisFile>;
my $i;
my $j;
my %categories = ();
my %categoryMembers = ();

for($i=0; $i < @nflisLines; $i++) {
	my $line = $nflisLines[$i];
	chomp($line);
	
	#0 = fake code [don't use]
	#1 = pv
	#2 = fake code [don't use]
	#3 = category
	#4 = synonym
	
	my @values = split('\\|', $line);
	my $pv = $values[1];
	my $category = $values[3];
	my $synonym = $values[4];
	
	$categories{$category} = 1;
	push @{$categoryMembers{$category}}, $pv;
	push @{$categoryMembers{$category}}, $synonym;	
}

for my $key ( sort keys %categories ) {
	my $category = $key;
	print join("|", $category, "", $category, "", $source, "Class");
	print "\n";
	my @terms = @{$categoryMembers{$category}};
	my $pv = "";
	for($i=0; $i < @terms; $i++) {
		if($i == 0 ) {
			#the PV is set first
			$pv = $terms[$i];
			print join("|", $pv, "PV", $pv, "", $source, "Substance");
			print "\n";
		}
		else {
			print join("|", $terms[$i], "SY", $pv, "", $source, "Substance");
			print "\n";
		}
	}
}
