from urllib.request import Request, urlopen

url = f'http://localhost:8983/solr/inodes/select?q=*%3A*'
print(f'GET {url}')
fp=open('./backup.json', 'w')
fp.write(urlopen(Request(url, None, {})).read().decode('utf-8'))
fp.close()

