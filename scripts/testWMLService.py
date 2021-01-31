import json, requests, os
from getWMLtoken import getWMLtoken 


cpd_user_access_token = getWMLtoken()
wml_url = os.getenv('ANOMALY_DETECTION_URL')
header = {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + cpd_user_access_token}
# NOTE: manually define and pass the array(s) of values to be scored in the next line
payload_scoring = {"input_data":[{"fields":["temperature","ambiant_temperature","kilowatts","oxygen_level","nitrogen_level","humidity_level","fan_1","vent_2"],"values":[[4.73,19.048,4.505,1,3,76.98,40.153,4.181]]}]}


response_scoring = requests.post(wml_url, verify=False,json=payload_scoring, headers=header)
print("Scoring response")
print(json.loads(response_scoring.text))