
package it.nexi.sampleecr_01.sampleecr01;

/**
 *
 * Nexi Payments.
 * 
 * Support class for managing additional data
 * 
 */
public class MapAdditionalData {
    
    private int index;
    private String value;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MapAdditionalData{" + "index=" + index + ", value=" + value + '}';
    }
    
}
