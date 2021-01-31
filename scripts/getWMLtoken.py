import json, os
import requests

# urllib3.disable_warnings()

def getWMLtoken():
    cpd_url = os.getenv('CP4D_AUTH_URL','')
    cpd_user_name = os.getenv('CP4D_USER')
    cpd_api_key = os.getenv('CP4D_APIKEY')
    resp=requests.post('{0}'.format(cpd_url),verify=False,json={'username':cpd_user_name,'api_key':cpd_api_key})
    return resp.json()['token']


print(getWMLtoken())