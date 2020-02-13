use strict;

my $source = "MCL";

open(my $mclFile, $ARGV[0] ) or die "Couldn't open MCL file.\n";

my @mclLines = <$mclFile>;
my %mclSubstanceMap = ();
my %mclClassMap = ();
my $i;
my $j;

for($i=0; $i < @mclLines; $i++) {
	my $line = $mclLines[$i];
	my @values = split('\\|', $line);
	
	#0 = variant
	#1 = pv
	#2 = metadata
	my $variant = $values[0];
	my $pv = $values[1];
	my $conceptType = $values[2];
	
	if($conceptType ne "CLASS") {
		push @{$mclSubstanceMap{$pv}}, $variant;
	}
	else {
		push @{$mclClassMap{$pv}}, $variant;
	}
}

for my $key ( sort keys %mclSubstanceMap ) {
	foreach(@{$mclSubstanceMap{$key}}) {
		if($_ eq $key) {
			print join("|", $_, "PV", $key, "", $source, "Substance");
		}
		else {
			print join("|", $_, "UNSP", $key, "", $source, "Substance");		
		}
		print "\n";		
	}
}

for my $key (sort keys %mclClassMap) {
	foreach(@{$mclClassMap{$key}}) {
		my $variant = $_;
		my $class = $key;
		if($variant eq $class) {
			print join("|", $class, "", $class, "", $source, "Class");
		}
		else {
			print join("|", $variant, "memberof", $class, "", $source, "Class");
		}
		print "\n";
	}
}