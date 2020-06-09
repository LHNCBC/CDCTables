#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Nov 27 11:01:38 2018

@author: soonjye
"""

import pandas as pd
import requests
import numpy as np
import json
import sys
###sys.path.append('/home/soonjye/Documents/Death/MedInfo/Code')
sys.path.append('/nfsvol/mor-nchs/lee/python-code')


################ Collecting drug names from RxNorm ############################
rxnorm = requests.get('https://rxnavstage.nlm.nih.gov/REST/allconcepts.json?tty=IN+PIN+BN')
rxnorm = rxnorm.json()
rxnorm = pd.DataFrame.from_dict(rxnorm['minConceptGroup']['minConcept'])
print('Writing RxNorm.tsv')
rxnorm.to_csv('Data/RxNorm.tsv', sep='\t', index=False)


### get synonyms and IN+PIN+BN relationship between drug entities
inpinbn   = dict()

for rid, eachrow in rxnorm.iterrows():
    if rid%100 == 0:  print('Processing row', rid, '...')

    rxcui = str(eachrow['rxcui'])
    inpinbn[rxcui] = dict()
    inpinbn[rxcui]['tty'] = eachrow['tty']
    inpinbn[rxcui]['name'] = eachrow['name']
    
    ## get synonym for every rxcui
    url = 'https://rxnavstage.nlm.nih.gov/REST/rxcui/'+rxcui+'/property.json?propName=RxNorm Synonym'
    r = requests.get(url).json()
    
    if pd.isnull(r['propConceptGroup']) == False:
        inpinbn[rxcui]['SYN'] = []
        for eachatc in r['propConceptGroup']['propConcept']:
            inpinbn[rxcui]['SYN'].append(eachatc['propValue'])

    ## get PIN, only for IN
    if eachrow['tty'] != 'IN':  continue
    url = 'https://rxnavstage.nlm.nih.gov/REST/rxcui/'+rxcui+'/related.json?tty=PIN'
    r = requests.get(url).json()
    
    for eachCP in r['relatedGroup']['conceptGroup']:
        if 'conceptProperties' in eachCP.keys():
            inpinbn[rxcui]['PIN'] = []
            for eachrxcui in eachCP['conceptProperties']:
                inpinbn[rxcui]['PIN'].append(eachrxcui['rxcui'])
                
                
    ## get brandname, only for IN & PIN
    if eachrow['tty'] == 'BN':  continue
    url = 'https://rxnavstage.nlm.nih.gov/REST/rxcui/'+rxcui+'/related.json?tty=BN'
    r = requests.get(url).json()
    
    for eachCP in r['relatedGroup']['conceptGroup']:
        if 'conceptProperties' in eachCP.keys():
            inpinbn[rxcui]['BN'] = []
            for eachrxcui in eachCP['conceptProperties']:
                inpinbn[rxcui]['BN'].append(eachrxcui['rxcui'])

with open('Data/inpinbn.json', 'w') as fo:   json.dump(inpinbn, fo)







################ Eliminating undesirable drug entities#########################
rxnorm = pd.read_csv('Data/RxNorm.tsv', sep='\t', dtype=str)
##with open('Data/inpinbn.json') as fi:   inpinbn = json.load(fi)


from umlsAPI import *
AuthClient, tgt = umls_authenticate()


rxnorm['UMLScui'] = ''
rxnorm['UMLS'] = ''
rxnorm['TUI'] = ''
for rowid, eachrow in rxnorm.iterrows():
    if rowid%100 == 0:  print('Obtaining UMLS ID: querying row', rowid)
    rxname = eachrow['name']
    
    ## get UMLS concept
    res1 = umls_retrieve_CUI(rxname, AuthClient, tgt)
    if res1.status_code != 200: print('Could not find name in UMLS:', rxname)
    if res1.status_code != 200:  continue
    
    res1 = res1.json()
    firstres = res1['result']['results'][0]
    res_cui  = firstres['ui']
    res_name = firstres['name']
    
    if res_cui == 'NONE':   continue
    rxnorm.at[rowid, 'UMLScui'] = res_cui
    rxnorm.at[rowid, 'UMLS'] = res_name

    
    ## get SemanticType of UMLS concept
    res2 = umls_retrieve_info(res_cui, AuthClient, tgt)
    if res2.status_code != 200: print('Could not retrieve UMLS info for:', res_cui)
    if res2.status_code != 200: continue

    res2 = res2.json()
    semtype = res2['result']['semanticTypes']
    semtypes = [ x['uri'] for x in semtype]      # one CUI might have > 1 semantic type
    semtypes = [ x[-4:] for x in semtypes]
    
    rxnorm.at[rowid, 'TUI'] = semtypes

print('writing RxNorm2.tsv')
rxnorm.to_csv('Data/RxNorm2.tsv', sep='\t', index=False)

print('Starting semantic filtering...')
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


