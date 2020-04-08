#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Nov 28 15:09:58 2018

@author: lpeters
"""

import numpy as np
import pandas as pd
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil


### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])

# ICD10 codes for death causes are listed in columns entax1,...,entax20
recax = ['recax'+str(x) for x in range(1,20)]
deathstat = pd.read_csv('DeathCertMisc/nih_literalText_ResAndOcc.csv', delimiter=',', encoding='ISO-8859-1')

for rowid, row in deathstat.iterrows():
        
    parsed = dict()
#   make line number a key
    parsed['lineno'] = str(rowid+1)
    
#   get the ICD10 codes
    for x in recax:
        if not pd.isnull(row[x]):
            parsed[x] = row[x]

#   get the text causes of death
    if not pd.isnull(row['part1']):
        parsed['part1'] = row['part1']
    if not pd.isnull(row['part2']):
        parsed['part2'] = row['part2']
    if not pd.isnull(row['howInjuryOccurred']):
        parsed['howInjuryOccurred'] = row['howInjuryOccurred']
                    
#    print(parsed)
    es.index(index = 'miscdeath', doc_type='cert', id=parsed['lineno'], body=parsed)
print('Indexing completed for multi-state death certificates, index=miscdeath')
