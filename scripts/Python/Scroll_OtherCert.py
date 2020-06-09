#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 11-Jan-2019
@author: lee peters 
"""
# Test the terms on the Other Death Certificates (non-Washington state)
#

import numpy as np
import pandas as pd
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil2


### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])

term = 'morphine'
debug = 0
# process command line arguments
# if specified, argument is the term file
# the terms file MUST have "Search_Term" in the first column of the first line
arglen = len(sys.argv)
if arglen > 1:
    term = sys.argv[1]

#################### ElasticSearch retrieval ##################################

## then elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
query = ESutil2.initialize_mmquery()
    
query = ESutil2.append_mmquery_term(term, query)
                    
data = ESutil2.es_query_search(query)

total_hits = data['hits']['total']
returned_hits = len(data['hits']['hits'])

print("%s, hits:%d, documents:%d, scroll id:%s" % (term, total_hits, returned_hits, data['_scroll_id']))
##print("%s, hits:%d, documents:%d" % (term, data['hits']['total'], len(data['hits']['hits'])))
if data['hits']['total'] < 3:
    print(json.dumps(data,indent=2))

while returned_hits < total_hits:
    scrollID = data['_scroll_id']

    data = ESutil2.es_query_scroll(scrollID)

    hits = data['hits']['total']
    scroll_hits = len(data['hits']['hits'])
    if (scroll_hits == 0):
        print("ABORTING: no further hits in scrolling")
        returned_hits = total_hits
    returned_hits += scroll_hits
    print("%s, hits:%d, documents:%d, scroll id:%s" % (term, hits, returned_hits, data['_scroll_id']))
