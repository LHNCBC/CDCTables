use strict;

my $source = "ICD";

open(my $icdFile, $ARGV[0] ) or die "Couldn't open ICD map file.\n";

my @icdLines = <$icdFile>;
my $i;
my $j;

for($i=0; $i < @icdLines; $i++) {
	my $line = $icdLines[$i];
	chomp($line);
	
	#0 = T-code
	#1 = T-code name (not used)
	#2... = rxcui
	my @values = split('\\|', $line);
	my $tcode = $values[0];
	for($j=2; $j < @values; $j++) {
		my $relatedCui = $values[$j];	
		#the processing will need to take a bycode and byname source switch
		print join("|", $relatedCui, "memberof", $tcode, "", $source, "Class")."\n";
	}
}