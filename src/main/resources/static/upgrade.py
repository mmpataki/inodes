import json, time, requests
from urllib.request import Request, urlopen

resp = json.loads(open('./backup.json').read())
sing = ['content', 'type', 'owner', 'visibility']

for doc in resp['response']['docs']:
	data = {}
	for k in doc:
		if k in sing: data[k] = doc[k][0]
		else: data[k] = doc[k]
		if k == 'type':
			if data[k] == 'apps':
				data[k] = 'instances'
			elif data[k] == 'scripts':
				data[k] = 'posts'
	if data['owner'] == 'global':
		data['owner'] = 'mmp'
	#data['visibility'] = 'public'
	print(data['type'], data['owner'] + ":" + data['owner'][0] + '@123')
	print(requests.post('http://inedctst01:8080/data', json=data, auth=(data['owner'], data['owner'][0] + '@123')))

