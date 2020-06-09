#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Nov 27 12:12:00 2018

@author: soonjye
"""
import Authentication
import requests


## based on my APIkey, generate a ticket-granting ticket (TGT), which is valid for 8 hours
## input: None
## output: authenticated_client, TGT
def umls_authenticate():    
    apikey = '7078ab75-9c00-4fcf-b1e8-6800a4385857'	#insert your umls-API key here
    AuthClient = Authentication.Authentication(apikey) 
    tgt = AuthClient.gettgt() # generate ticket-granting ticket (TGT), valid for 8 hours
    
    return AuthClient, tgt


## retrieve UMLS CUI based on string or term
## input: string_query, authenticated_client, TGT
## output: requests_result
def umls_retrieve_CUI(query, AuthClient, tgt, searchType='notExact'):
    uts = 'https://uts-ws.nlm.nih.gov/rest'
    uri = '/search/current'
    if searchType == 'exact':
        param = {'string':query, 'searchType':'exact', 'ticket':AuthClient.getst(tgt)}
    else:
        param = {'string':query, 'ticket':AuthClient.getst(tgt)}  ##using TGT to generate service ticket (ST)
    res = requests.get(uts+uri, params=param)

    return res


## retrieve information based on known CUI
## input: string_CUI, authenticated_client, TGT
## output: requests_result
def umls_retrieve_info(cui, AuthClient, tgt):
    uts = 'https://uts-ws.nlm.nih.gov/rest'
    uri = '/content/current/CUI/' + cui
    param = {'ticket':AuthClient.getst(tgt)}
    res = requests.get(uts+uri, params=param)
    
    return res


## retrieve atoms in english language based on known CUI
## input: string_CUI, authenticated_client, TGT
## output: requests_result
def umls_retrieve_atoms(cui, AuthClient, tgt):
    uts = 'https://uts-ws.nlm.nih.gov/rest'
    uri = '/content/current/CUI/' + cui + '/atoms'
    param = {'language':'ENG', 'ticket':AuthClient.getst(tgt)}
    res = requests.get(uts+uri, params=param)

    return res

