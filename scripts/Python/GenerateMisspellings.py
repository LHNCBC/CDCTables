#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Nov 27 11:01:38 2018

"""
# Generate misspellings

import pandas as pd
import requests
import numpy as np
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')


### get synonyms and IN+PIN+BN relationship between drug entities
inpinbn   = dict()


with open('Data/inpinbn.json') as fi:   inpinbn = json.load(fi)


####################### Misspellings Generation ###############################
import re
import nltk
arpabet = nltk.corpus.cmudict.dict()    ## will be used to detect existing English Word


# generate a list of variant with 1 edit distance
import string
alphabet = string.ascii_lowercase
def generate_variant(word):
    word = word.lower()
    variants = set()
    
    for i in range(len(word)):
        #deletion
        variant = word[:i] + word[(i+1):]
        variants.add(variant)
        
        for j in range(len(alphabet)):
            #addition
            variant = word[:i] + alphabet[j] + word[i:]
            variants.add(variant)
            #substitution
            if word[i] != alphabet[j]:
                variant = word[:i] + alphabet[j] + word[(i+1):]
                variants.add(variant)
        
    #addition after the word last position
    for j in range(len(alphabet)):
        variant = word + alphabet[j]
        variants.add(variant)
     
    return variants


# filter variant based on metaphone phoneme
from metaphone import doublemetaphone as dmtp
def filter_metaphone(variants, original):
    filtered = []
    standard = dmtp(original.lower())
    standard = list(filter(None, standard)) # remove 2nd item if it is empty string
    
    for x in variants:
        compare = dmtp(x)
        if compare[0] in standard  or  compare[1] in standard:   filtered.append(x)

    return filtered


# input: array of drug names (string)
# output: array of number of death cert mentioning each drug names
##import ESutil
def get_es_hit(variants):
    hits = []
    for x in variants:
##      comment out for testing - LP 11/27
##        res = ESutil.es_string_search(x)
##        hits.append(res['hits']['total'])
        hits.append(1)        
    return hits



##print('reading filtered_RxNorm2.tsv (before getting drug names and synonyms)');
filtered_rxnorm = pd.read_csv('Data/filtered_RxNorm2.tsv', sep='\t', dtype=str)

## get existing drug names & their synonyms
allrxname = []
for unii, item in inpinbn.items():
    allrxname.append(item['name'])
    if 'SYN' in item.keys():
        for eachsyn in item['SYN']:
            allrxname.append(eachsyn)


filtered_rxnorm['misspelling'] = ''
for rid, eachrow in filtered_rxnorm.iterrows():
#    if rid < 10600: continue
##    if rid %200 == 0:   print('Generating misspellings: row', rid)
    
    rxname = eachrow['name']
    rxcui  = eachrow['rxcui']
    ## generation phase
    if len(rxname) <= 4:    continue        # if name is less than 4 alphabets, skip (e.g. air, mica, RNA)
##    match = re.search("[,\-\d ]+", rxname)  # if name is chemical name (has dash'-', has digits, has space ' '), skip
    match = re.search("[,\-\(\d ]+", rxname)  # if name is chemical name (has dash'-', paren'(',has digits, has space ' '), skip
    if match != None:   continue
##  lp added 27-Nov-2019 - limit to 5-20 length
    if len(rxname) > 20:    continue

##    print('##', rxname, '##')
    provar = generate_variant(rxname)
##    print('   ', len(provar), ' variants generated') 
    
    ## filtering phase
    filvar = filter_metaphone(provar, rxname)   # Phonome filtering
##    print('   ', len(filvar), ' variants after phonome filtering')

##    print("Calling get_es_hit")
    # filter variants that do not retrieve any death certificate
    # NOTE: in paper, we do not have this step. but in practice, we perform this to decrease computational time
    varhit = get_es_hit(filvar)
##    print("Finished get_es_hit")
    misses = np.array(filvar)[ [x > 0 for x in varhit] ]
    
##    print('   ', len(misses), ' variants after get_es_hit')

    if len(misses) > 0:     # Short variant filtering (e.g. brain, brian, bryn, ement)
        misses = misses[ [len(x) > 5 for x in misses] ]
##        print('   ', len(misses), ' variants after short variant filtering')
    
    if len(misses) > 0:     # Existing English word filtering (as it indicates common english word)
        misses = misses[ [x not in arpabet.keys() for x in misses] ]
##        print('   ', len(misses), ' variants after English word filtering')
    
    if len(misses) > 0:     # Existing drug name filtering
        misses = misses[ [x not in allrxname for x in misses] ]
##        print('   ', len(misses), ' variants after existing name filtering')
    
    retained = []
    if len(misses) > 0:     # semantic types filtering
        for eachmsp in misses:
##          skip checking UMLS, check if in name list
            if eachmsp not in allrxname:
                retained.append(eachmsp)
##            else:
##                print('   ', eachmsp, ' dropped from misspellings (in RxNorm)')

    if len(retained) > 0:
        filtered_rxnorm.at[rid, 'misspelling'] = str(retained)
        inpinbn[rxcui]['MSP'] = retained
##        print('   ', len(retained), ' variants retained')
    for x in retained:
        print('{}|{}'.format(x, filtered_rxnorm.at[rid,'name']))
#print('Writing filtered_RxNorm3.tsv')
#filtered_rxnorm.to_csv('Data/filtered_RxNorm3.tsv', sep='\t', index=False)
#with open('Data/inpinbn.json', 'w') as fo:   json.dump(inpinbn, fo)


