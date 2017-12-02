import matplotlib.pyplot as plt
from flask import Flask, render_template, jsonify
import requests
import json
import numpy as np
import time

app = Flask(__name__)


@app.route('/')
def index():
    r = requests.get("http://127.0.0.1:5000/chain").text
    r = json.loads(r)

    # Fetch the chain length

    chain_length = len(r["chain"])

    blocks_data = []

    for i in range(1,chain_length):
        block_dict = {}
        transaction_length = len(r["chain"][i]["transactions"])
        block_dict["Block Number"] = r["chain"][i]["index"]
        block_dict["Previous Hash"] = r["chain"][i]["previous_hash"]
        block_dict["Timestamp"] = r["chain"][i]["timestamp"]
        block_dict["Total Transactions"] = transaction_length

        meter_address = []
        meter_usage = []
        transaction_number = []

        total_usage = 0

        for j in range(0, transaction_length):
            # meter_address.append(str(r["chain"][i]["transactions"][j]["meter_address"]))
            meter_usage.append(int(r["chain"][i]["transactions"][j]["meter_usage"]))
            transaction_number.append(j + 1)
            total_usage += int(r["chain"][i]["transactions"][j]["meter_usage"])

        block_dict["Total Units Usage"] = total_usage

        blocks_data.append(block_dict)


        fig, ax = plt.subplots()
        plt.title("Transaction Number vs Meter Usage")
        ax.errorbar(transaction_number, meter_usage, xerr=0.2, yerr=0.4)
        # plt.show()
        path = "/home/dushyant/Desktop/Github/smart_power/images/" + str(i) + "_" + str(j) + ".png"
        plt.savefig(path)


    return render_template("graph.html", data=blocks_data, )


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000, debug=True)