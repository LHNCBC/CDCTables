use strict;

my $source = "Misspelling";
my $mspTTY = "MSP";

open(my $mspFile, $ARGV[0] ) or die "Couldn't open Misspellings file.\n";

my @mspLines = <$mspFile>;
my %mspMap = ();
my $i;
my $j;

for($i=0; $i < @mspLines; $i++) {
	my $line = $mspLines[$i];
	chomp($line);
	
	#0 = rxname
	#1 = rxcui
	#2 = misspelling
	my @values = split('\\|', $line);
	
	my $rxname = $values[0];
	my $rxcui = $values[1];
	my $misspelling = $values[2];
	
	print join("|", $misspelling, "MSP", $rxname, $rxcui, $source, "Substance");
	print "\n";
}

