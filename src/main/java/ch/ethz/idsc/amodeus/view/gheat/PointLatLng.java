/* Copyright (c) 2014 Varun Pant
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * original code retrieved from
 * https://github.com/varunpant/GHEAT-JAVA
 *
 * The code was modified by the IDSC-Frazzoli team at the
 * Institute for Dynamic Systems and Control of ETH Zurich 
 * for use in the amodeus library, 2017-2018. */

package ch.ethz.idsc.amodeus.view.gheat;

public class PointLatLng implements Comparable<PointLatLng> {
    private double weight;
    private double longitude;
    private double latitude;
    private Object opt_value;

    public Object getValue() {
        return opt_value;
    }

    public void setValue(Object opt_value) {
        this.opt_value = opt_value;
    }

    public PointLatLng(double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PointLatLng(double longitude, double latitude, double weight) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.weight = weight; // LONGTERM is this being ack'ed
    }

    public PointLatLng(double longitude, double latitude, Object opt_value) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.opt_value = opt_value;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "(" + this.longitude + ", " + this.latitude + ")";
    }

    @Override
    public int compareTo(PointLatLng o) {
        PointLatLng tmp = o;
        if (this.longitude < tmp.longitude) {
            return -1;
        } else if (this.longitude > tmp.longitude) {
            return 1;
        } else {
            if (this.latitude < tmp.latitude) {
                return -1;
            } else if (this.latitude > tmp.latitude) {
                return 1;
            }
            return 0;
        }
    }
}
