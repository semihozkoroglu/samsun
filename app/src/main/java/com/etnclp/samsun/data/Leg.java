package com.etnclp.samsun.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Leg implements Serializable {
    public Distance distance;
    public Duration duration;
    public ArrayList<LegStep> steps;
}
