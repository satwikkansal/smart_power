package com.flash.brainbreaker.flash;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brainbreaker on 03/12/17.
 */

public class PayResponse {
    @SerializedName("meter_address")
    public String meter_add;

    @SerializedName("hash_code")
    public String hash;
}
