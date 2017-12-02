package com.flash.brainbreaker.flash;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brainbreaker on 02/12/17.
 */

public class APIResult {
    @SerializedName("meter_address")
    public String meter_add;

    @SerializedName("rate_per_unit")
    public String rate;

    @SerializedName("meter_usage_amount")
    public String units;

    @SerializedName("meter_bill_amount")
    public String amount;
}
