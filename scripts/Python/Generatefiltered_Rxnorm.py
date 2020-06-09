#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Nov 27 11:01:38 2018

@author: lpeters
"""

import pandas as pd
import requests
import numpy as np
import json
import sys
###sys.path.append('/home/soonjye/Documents/Death/MedInfo/Code')
sys.path.append('/nfsvol/mor-nchs/lee/python-code')


############################################################
print('Starting semantic filtering...')
rxnorm = pd.read_csv('Data/RxNorm2.tsv', sep='\t', dtype=str)
#### semantic filtering
filtered_rxnorm = rxnorm.copy()
tui1 = 'T116'
tui2 = 'T126'
tui3 = 'T121'
tui4 = 'T109'
tui5 = 'T195'   # Antibiotic
filtered_rxnorm = filtered_rxnorm[ [type(x) == str and (tui1 in x or tui2 in x or tui3 in x or tui4 in x or tui5 in x) for x in filtered_rxnorm.TUI] ]

#### common word filtering for BrandName
frequentWord = pd.read_csv('Data/frequentWord.tsv', sep='\t')
def check_frequent_word(word):
    if word.lower() in list(frequentWord.Word):
        res = True
    else:
        res = False
    return res

for rowid, eachrow in filtered_rxnorm.iterrows():
    if eachrow['tty'] == 'BN' and check_frequent_word(eachrow['name']):
        print('Dropping: ', eachrow['name'])
        filtered_rxnorm = filtered_rxnorm.drop(rowid)

print('writing filtered_RxNorm.tsv')
filtered_rxnorm.to_csv('Data/filtered_RxNorm.tsv', sep='\t', index=False)


#with open('Data/inpinbn.json', 'w') as fo:   json.dump(inpinbn, fo)


