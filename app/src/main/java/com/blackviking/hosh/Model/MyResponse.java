package com.blackviking.hosh.Model;

import java.util.List;

/**
 * Created by Scarecrow on 4/1/2018.
 */

public class MyResponse {
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;
}
