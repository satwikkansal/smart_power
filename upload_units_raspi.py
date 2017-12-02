import requests
import json
import random
import time

meter_address = "1x11"

i = 1

while i <= 20:
    data = {}
    data['meter_address'] = meter_address
    data['meter_usage'] = random.randint(5,15)
    data['timestamp'] = time.time()

    print(data)

    time.sleep(2)

    t = requests.post("http://127.0.0.1:5000/transactions/new", json=data)

    print(t.text)

    if i%4==0:
        # Mine the pushed transactions for a single block
        m = requests.get("http://127.0.0.1:5000/mine")
        j_m = json.loads(m.text)

    i += 1


