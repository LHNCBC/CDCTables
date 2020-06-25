use strict;

open( my $cuiFile, "cui2drug2.txt") or die "Couldn't open class member file!\n";
open( my $tCodeToDescFile, "tcode2name.txt") or die "Couldn't open drug member file!\n";
open( my $tCodeToDrugFile, "icd10-TCode-drugTable.txt") or die "Couldn't open drug member file!\n";
open my $out, '>', "tCodeConfig.txt" or die "Coudln't create output file!\n";
open my $debug, '>', "debug.txt" or die "Coudln't create debug file!\n";

my $i=0;
my @cuiLines = <$cuiFile>;
my @tDescLines = <$tCodeToDescFile>;
my @tDrugLines = <$tCodeToDrugFile>;
my %cuiMap = ();
my %tDescMap = ();
my %t2Name = ();
my %name2T = ();

for( $i=0; $i < @cuiLines; $i++ ) {
	my $line = $cuiLines[$i];
	chomp($line);
	my @values = split('\\|', $line);
	$cuiMap{$values[0]} = $values[1];
}

for( $i=0; $i < @tDescLines; $i++ ) {
	my $line = $tDescLines[$i];
	chomp($line);
	my @values = split('\\|', $line);
	$tDescMap{$values[0]} = $values[1];
}

for( $i=0; $i < @tDrugLines; $i++ ) {
	my $line = $tDrugLines[$i];
	chomp($line);
	my @values = split('\\|', $line);
	$name2T{$values[0]} = $values[1];
	$t2Name{$values[1]} = $values[0];
}

foreach( keys %cuiMap ) {
	my $key = $_;
	my $name = $key;
	my $cui = $cuiMap{$key};
	my $tcode = "";
	my $tname = "";
	if( exists($name2T{$name} ) ) {
		$tcode = $name2T{$name};
		if( exists($tDescMap{$tcode}) ) {
			$tname = $tDescMap{$tcode};
		}
	}
	print $out $name."|".$cui."|".$tcode."|".$tname."\n";
}





