#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 11-Jan-2019
@author: lee peters 
Input a list of RxClass Names and find hits in Washington Death Certificates
"""

import numpy as np
import pandas as pd
import json
import sys
###sys.path.append('/home/soonjye/Documents/Death/MedInfo/Code')
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil



### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])


## read in rxclass names
filtered_rxclass = pd.read_csv('InputLists/rxclass-names', sep='\t', dtype=str)    

filtered_rxclass['frequency'] = np.nan

## elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxclass.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    query = ESutil.initialize_mmquery()
    
    query = ESutil.append_mmquery_term(eachrow['Search_Term'], query)
                    
    ## do the query
    res = ESutil.es_query_search(query)
    filtered_rxclass.at[rid, 'frequency'] = res['hits']['total']


filtered_rxclass.to_csv('Results/filtered_rxclass_names.tsv', sep='\t', index=False)
print('Created file Results/filtered_rxclass_names.tsv')
