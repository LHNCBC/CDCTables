#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Nov  7 11:35:01 2018

@author: soonjye
"""

from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
fields = ['COD-TEXT-1', 'COD-TEXT-2', 'COD-TEXT-3', 'COD-TEXT-4', 'COD-OTHER-TEXT', 'INJURY-DESC']


## input: single_string
## search on every fields of document and retrieve those mentioning 'single_string'
## output: es_result
def es_string_search(x):
    res = es.search(index='death', body={'query': {"multi_match": {"query": x, "type": 'phrase', "fields": fields}}})
    return res


def initialize_mmquery():
    query = {'query': {'bool': {'should': [] }}}
    return query

def append_mmquery_term(term, query):
    query['query']['bool']['should'].append( {"multi_match": {"query": term, "type": 'phrase', "fields": fields}} )
    return query

def es_query_search(q):
    res = es.search(index='death', size=10000, body=q)
    return res

def es_query_searchall(q):
    res = es.search(index='death', size=20000, body=q)
    return res
