#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Nov 27 11:01:38 2018

@author: Lee Peters
"""

import pandas as pd
import requests
import numpy as np
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')


termFile = 'rxnorm-terms'
# process command line arguments
# if specified, argument is the source file type
arglen = len(sys.argv)
if arglen > 1:
    termFile = sys.argv[1]

count=0
#### common word filtering for BrandName
frequentWord = pd.read_csv('Data/frequentWord.tsv', sep='\t')

################ Eliminating undesirable drug entities#########################
f = open(termFile, "r")
for x in f:
    if x.strip().lower() in list(frequentWord.Word):
        count += 1
    else:
        print(x.strip())

f.close()

