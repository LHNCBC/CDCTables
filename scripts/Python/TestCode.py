#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Nov 27 11:01:38 2018

"""

import pandas as pd
import requests
import numpy as np
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')


################ Collecting drug names from RxNorm ############################
###rxnorm = requests.get('https://rxnavstage.nlm.nih.gov/REST/allconcepts.json?tty=IN+PIN+BN')
###rxnorm = rxnorm.json()
###rxnorm = pd.DataFrame.from_dict(rxnorm['minConceptGroup']['minConcept'])
###print('Writing RxNorm.tsv')
###rxnorm.to_csv('Data/RxNorm.tsv', sep='\t', index=False)


### get synonyms and IN+PIN+BN relationship between drug entities
inpinbn   = dict()
###
###for rid, eachrow in rxnorm.iterrows():
###    if rid%100 == 0:  print('Processing row', rid, '...')

###    rxcui = str(eachrow['rxcui'])
###    inpinbn[rxcui] = dict()
###    inpinbn[rxcui]['tty'] = eachrow['tty']
###    inpinbn[rxcui]['name'] = eachrow['name']
    
    ## get synonym for every rxcui
###    url = 'https://rxnavstage.nlm.nih.gov/REST/rxcui/'+rxcui+'/property.json?propName=RxNorm Synonym'
###    r = requests.get(url).json()
    
###    if pd.isnull(r['propConceptGroup']) == False:
###        inpinbn[rxcui]['SYN'] = []
###        for eachatc in r['propConceptGroup']['propConcept']:
###            inpinbn[rxcui]['SYN'].append(eachatc['propValue'])

    ## get PIN, only for IN
###    if eachrow['tty'] != 'IN':  continue
###    url = 'https://rxnavstage.nlm.nih.gov/REST/rxcui/'+rxcui+'/related.json?tty=PIN'
###    r = requests.get(url).json()
    
###    for eachCP in r['relatedGroup']['conceptGroup']:
###        if 'conceptProperties' in eachCP.keys():
###            inpinbn[rxcui]['PIN'] = []
###            for eachrxcui in eachCP['conceptProperties']:
###                inpinbn[rxcui]['PIN'].append(eachrxcui['rxcui'])
                
                
    ## get brandname, only for IN & PIN
###    if eachrow['tty'] == 'BN':  continue
###    url = 'https://rxnavstage.nlm.nih.gov/REST/rxcui/'+rxcui+'/related.json?tty=BN'
###    r = requests.get(url).json()
    
###    for eachCP in r['relatedGroup']['conceptGroup']:
###        if 'conceptProperties' in eachCP.keys():
###            inpinbn[rxcui]['BN'] = []
###            for eachrxcui in eachCP['conceptProperties']:
###                inpinbn[rxcui]['BN'].append(eachrxcui['rxcui'])

###with open('Data/inpinbn.json', 'w') as fo:   json.dump(inpinbn, fo)







################ Eliminating undesirable drug entities#########################
###rxnorm = pd.read_csv('Data/RxNorm.tsv', sep='\t', dtype=str)
with open('Data/inpinbn.json') as fi:   inpinbn = json.load(fi)


from umlsAPI import *
AuthClient, tgt = umls_authenticate()


###rxnorm['UMLScui'] = ''
###rxnorm['UMLS'] = ''
###rxnorm['TUI'] = ''
###for rowid, eachrow in rxnorm.iterrows():
###    if rowid%100 == 0:  print('Obtaining UMLS ID: querying row', rowid)
###    rxname = eachrow['name']
    
    ## get UMLS concept
###    res1 = umls_retrieve_CUI(rxname, AuthClient, tgt)
###    if res1.status_code != 200: print('Could not find name in UMLS:', rxname)
###    if res1.status_code != 200:  continue
    
###    res1 = res1.json()
###    firstres = res1['result']['results'][0]
###    res_cui  = firstres['ui']
###    res_name = firstres['name']
    
###    if res_cui == 'NONE':   continue
###    rxnorm.at[rowid, 'UMLScui'] = res_cui
###    rxnorm.at[rowid, 'UMLS'] = res_name

    
    ## get SemanticType of UMLS concept
###    res2 = umls_retrieve_info(res_cui, AuthClient, tgt)
###    if res2.status_code != 200: print('Could not retrieve UMLS info for:', res_cui)
###    if res2.status_code != 200: continue

###    res2 = res2.json()
###    semtype = res2['result']['semanticTypes']
###    semtypes = [ x['uri'] for x in semtype]      # one CUI might have > 1 semantic type
###    semtypes = [ x[-4:] for x in semtypes]
    
###    rxnorm.at[rowid, 'TUI'] = semtypes

###print('writing RxNorm2.tsv')
###rxnorm.to_csv('Data/RxNorm2.tsv', sep='\t', index=False)
print('reading RxNorm2.tsv')
rxnorm = pd.read_csv('Data/RxNorm2.tsv', sep='\t', dtype=str)
print('Starting semantic filtering...')
#### semantic filtering
filtered_rxnorm = rxnorm.copy()
filtered_rxnorm.to_csv('Data/filtered_RxNorm0.tsv', sep='\t', index=False)

tui1 = 'T116'
tui2 = 'T126'
tui3 = 'T121'
tui4 = 'T109'
tui5 = 'T195'   # Antibiotic
filtered_rxnorm = filtered_rxnorm[ [type(x) == str and (tui1 in x or tui2 in x or tui3 in x or tui4 in x or tui5 in x) for x in filtered_rxnorm.TUI] ]
print('Writing filtered_RxNorm1.tsv (after filtering)')
filtered_rxnorm.to_csv('Data/filtered_RxNorm1.tsv', sep='\t', index=False)

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

print('writing filtered_RxNorm2.tsv (after eliminating common words)')
filtered_rxnorm.to_csv('Data/filtered_RxNorm2.tsv', sep='\t', index=False)





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
import ESutil
def get_es_hit(variants):
    hits = []
    for x in variants:
##      comment out for testing - LP 11/27
##        res = ESutil.es_string_search(x)
##        hits.append(res['hits']['total'])
        hits.append(1)        
    return hits



###filtered_rxnorm = pd.read_csv('Data/filtered_RxNorm.tsv', sep='\t', dtype=str)
print('reading filtered_RxNorm2.tsv (before getting drug names and synonyms)');
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
    if rid %200 == 0:   print('Generating misspellings: row', rid)
    
    rxname = eachrow['name']
    rxcui  = eachrow['rxcui']
    ## generation phase
    if len(rxname) <= 4:    continue        # if name is less than 4 alphabets, skip (e.g. air, mica, RNA)
##    match = re.search("[,\-\d ]+", rxname)  # if name is chemical name (has dash'-', has digits, has space ' '), skip
    match = re.search("[,\-\(\d ]+", rxname)  # if name is chemical name (has dash'-', paren'(',has digits, has space ' '), skip
    if match != None:   continue
##  lp added 27-Nov-2019 - limit to 5-20 length
    if len(rxname) > 20:    continue

    print('##', rxname, '##')
    provar = generate_variant(rxname)
    print('   ', len(provar), ' variants generated') 
    
    ## filtering phase
    filvar = filter_metaphone(provar, rxname)   # Phonome filtering
    print('   ', len(filvar), ' variants after phonome filtering')

##    print("Calling get_es_hit")
    # filter variants that do not retrieve any death certificate
    # NOTE: in paper, we do not have this step. but in practice, we perform this to decrease computational time
    varhit = get_es_hit(filvar)
##    print("Finished get_es_hit")
    misses = np.array(filvar)[ [x > 0 for x in varhit] ]
    
    print('   ', len(misses), ' variants after get_es_hit')

    if len(misses) > 0:     # Short variant filtering (e.g. brain, brian, bryn, ement)
        misses = misses[ [len(x) > 5 for x in misses] ]
        print('   ', len(misses), ' variants after short variant filtering')
    
    if len(misses) > 0:     # Existing English word filtering (as it indicates common english word)
        misses = misses[ [x not in arpabet.keys() for x in misses] ]
        print('   ', len(misses), ' variants after English word filtering')
    
    if len(misses) > 0:     # Existing drug name filtering
        misses = misses[ [x not in allrxname for x in misses] ]
        print('   ', len(misses), ' variants after existing name filtering')
    
    retained = []
    if len(misses) > 0:     # semantic types filtering
        for eachmsp in misses:
##          skip checking UMLS, check if in name list
            if eachmsp not in allrxname:
                retained.append(eachmsp)
            else:
                print('   ', eachmsp, ' dropped from misspellings (in RxNorm)')
            ## get UMLS concept
##            print('get UMLS concept:', eachmsp);
##            res1 = umls_retrieve_CUI(eachmsp, AuthClient, tgt, searchType='exact')
##            if res1.status_code != 200:  continue
            
##            res1 = res1.json()
##            firstres = res1['result']['results'][0]
##            res_cui  = firstres['ui']
##            res_name = firstres['name']
##            
##            if res_cui == 'NONE':   # if the variant could not retrieve any UMLS concept, retain
##                retained.append(eachmsp)
##            else:                   # else, get SemanticType of UMLS concept
##                res2 = umls_retrieve_info(res_cui, AuthClient, tgt)
##                if res2.status_code != 200: continue
##            
##                res2 = res2.json()
##                semtype = res2['result']['semanticTypes']
##                semtypes = [ x['uri'] for x in semtype]
##                semtypes = [ x[-4:] for x in semtypes]
                
##                if 'T109' in semtypes or 'T116' in semtypes or 'T121' in semtypes or 'T126' in semtypes:
##                    retained.append(eachmsp)

    if len(retained) > 0:
        filtered_rxnorm.at[rid, 'misspelling'] = str(retained)
        inpinbn[rxcui]['MSP'] = retained
        print('   ', len(retained), ' variants retained')
print('Writing filtered_RxNorm3.tsv')
filtered_rxnorm.to_csv('Data/filtered_RxNorm3.tsv', sep='\t', index=False)
with open('Data/inpinbn.json', 'w') as fo:   json.dump(inpinbn, fo)


