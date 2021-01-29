import json, os
import requests, urllib3

urllib3.disable_warnings()

cpd_url = os.getenv('CP4D_URL','')
cpd_user_name = os.getenv('CP4D_USER')
cpd_api_key = os.getenv('CP4D_APIKEY')
resp=requests.post('{0}/icp4d-api/v1/authorize'.format(cpd_url),verify=False,json={'username':cpd_user_name,'api_key':cpd_api_key})
cpd_user_access_token=resp.json()['token']
print(cpd_user_access_token)