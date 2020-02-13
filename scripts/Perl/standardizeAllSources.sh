echo ">> Generating addition file for CDCTables";
echo "=========================================";
echo "";
echo ">> TCode Hierarchy...";
perl addTCodeHierarchy.pl "10-par-chd-rels.txt" > $1;

echo ">> NFLIS content...";
perl addNFLIS.pl "nflis-2018-and-2019.txt" >> $1;

echo ">> DEA content...";
perl addDea.pl "dea-2018.txt" >> $1;

echo ">> MCL content...";
perl addMCL.pl "MCL-terms" >> $1;

echo ">> RxNorm Misspellings...";
perl addMisspellings.pl "substance-misspellings.txt" >> $1;

echo ">> TCode Mappings...";
perl addTCodeMap.pl "tcode-map.txt" >> $1;
echo "";
echo "=========================================";
echo "";
echo ">> Finished creating addition file.";
echo "";
