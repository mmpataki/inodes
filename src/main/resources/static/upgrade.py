import json, time, requests
from urllib.request import Request, urlopen

resp = json.loads(open('./backup.json').read())
sing = ['content', 'type']

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
	print(data['type'])
	print(requests.post('http://localhost:8080/data', json=data, auth=('mmp', 'infa@123')))

