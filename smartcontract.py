
import hashlib as hasher
import requests
from flask import Flask
from flask import request
import json


URL_UNIT_BLOCKCHAIN = "http://127.0.0.1:5000/get_usage"
URL_PAYMENT_BLOCKCHAIN = "http://127.0.0.1:5050/transactions/new"
BILL_RATE_PER_UNIT = 100

node = Flask(__name__)


def setNextInMeter(meter_address):
    
    meter_endpoint = ""
    data = {"next:1"}
    r = requests.post(url = meter_endpoint, json=data)
    return r.status_code


def getMeterUsage(meter_address):
    
    data = {"meter_address":meter_address}
    r = requests.post(url = URL_UNIT_BLOCKCHAIN, json = data, headers={'Content-type': 'application/json'})
    data = r.json()
    return data["usage_amount"]


def payMeterBill(meter_address, meter_bill_amount):
    
    data = {"meter_address":meter_address, "meter_bill_amount":meter_bill_amount}
    r = requests.get(url = URL_PAYMENT_BLOCKCHAIN, json = data, headers={'Content-type': 'application/json'})
    return r.json();


@node.route('/get_bill', methods=['POST'])
def getBill():
    
    meter_to_fetch = request.get_json()
    meter_address = meter_to_fetch["meter_address"]
    meter_usage_amount = getMeterUsage(meter_address)

    meter_bill_amount = meter_usage_amount * BILL_RATE_PER_UNIT

    return json.dumps({
        "meter_address" : meter_address,
        "rate_per_unit"  : BILL_RATE_PER_UNIT,
        "meter_usage_amount" : meter_usage_amount,
        "meter_bill_amount" : meter_bill_amount
    })


@node.route('/pay_bill', methods=['POST'])
def payBill():
    
    meter_to_fetch = request.get_json()
    meter_address = meter_to_fetch["meter_address"]
    meter_bill_amount = meter_to_fetch["meter_bill_amount"]
    meter_bill_timestamp = meter_to_fetch["bill_timestamp"]

    meter_usage_amount = getMeterUsage(meter_address)
    check_amount = meter_usage_amount * BILL_RATE_PER_UNIT

    if int(meter_bill_amount) == check_amount :
        data = {"meter_address":meter_address, "paid_amount":meter_bill_amount, "time_of_payment":meter_bill_timestamp}
        r = requests.post(url = URL_PAYMENT_BLOCKCHAIN, json = data)
        hash_code = r.json()["hash_code"]

        # status = setNextInMeter(meter_address)

        return json.dumps({
            "payment_status" : "success",
            "hash_code" : hash_code
        })
    else :
        return json.dumps({
            "payment_status" : "unsuccessful",
            "given_bill" : meter_bill_amount,
            "actual_bill" : check_amount
        })


node.run(host='127.0.0.1', port=8000, debug=True)


