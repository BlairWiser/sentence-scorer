import json
import sys

s = sys.stdin.read()
data = json.loads(s)
print json.dumps(data, indent=1)
