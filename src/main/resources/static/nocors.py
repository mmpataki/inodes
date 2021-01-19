from flask import Flask, request
from flask_cors import CORS
import sys, json, requests
from urllib.request import Request, urlopen

app = Flask(__name__, static_folder='/mnt/c/Users/mpataki/IdeaProjects/inodes/src/main/resources/static', static_url_path = '')
CORS(app)

@app.route('/nocors', methods=['POST'])
def do_nocors_post():
	r = request.get_json(force=True)
	if 'headers' not in r:
		r['headers'] = {}
	print(r['url'], r['data'], (r['username'], r['password']), r['headers'])
	p=requests[r['method']](r['url'], json=r['data'], auth=(r['username'], r['password']), headers=r['headers'])
	print(p)
	return 'hello'

app.run(debug=True)
