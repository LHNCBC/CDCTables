#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 11-Jan-2019
@author: lee peters 
Read in MCL file and use elastic search to find hits
on Washington State Death Certicates
Output file contains MCL data + frequency count
"""

import numpy as np
import pandas as pd
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil2


### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])

### read in manually curated list (MCL) terms
filtered_rxnorm = pd.read_csv('InputLists/MCL-terms', sep='\t', dtype=str)    

### add a new column titled 'frequency' contain search result counts
filtered_rxnorm['frequency'] = np.nan

## elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxnorm.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    print('Retrieving MCL names: rows', rid, '...')
    query = ESutil2.initialize_mmquery()
    
    #generic name
    query = ESutil2.append_mmquery_term(eachrow['Search_Term'], query)
                    
    res = ESutil2.es_query_search(query)
    filtered_rxnorm.at[rid, 'frequency'] = res['hits']['total']

## create new MCL file containing terms and results
filtered_rxnorm.to_csv('Results/filtered_MCL_otherCert.tsv', sep='\t', index=False)
