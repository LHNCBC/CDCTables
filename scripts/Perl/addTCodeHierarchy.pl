use strict;

my $source = "ICD";

open(my $icdFile, $ARGV[0] ) or die "Couldn't open ICD hierarchy file.\n";

my @icdLines = <$icdFile>;
my %hierarchy = ();
my %codeToName = ();
my $i;

# Set the root, hard-coding

print join("|", "Injury, poisoning and certain other consequences of external causes", "", "", "S00-T98.9", $source, "Class");
print "\n";

for($i=0; $i < @icdLines; $i++) {
	my $line = $icdLines[$i];
	chomp($line);
	
	#0 = parent code
	#1 = parent name
	#2 = child code
	#3 = child name
	
	my @values = split('\\|', $line);
#	foreach(@values) {print $_."\t"};
	my $parentCode = $values[0];
	my $parentName = $values[1];
	my $childCode = $values[2];
	my $childName = $values[3];
	
	print join("|", $childName, "isa", $parentName, $childCode, $source, "Class");
	print "\n";
}
