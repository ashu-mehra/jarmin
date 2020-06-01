package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.tree.analysis.BasicValue;

public class StringValue extends BasicValue {
    private String contents;
    
    public StringValue() {
        super(null);
        this.contents = null;
    }
    
    public StringValue(String contents) {
        super(null);
        this.contents = contents;
    }
    
    public StringValue(StringValue v) {
        super(null);
        this.contents = new String(v.getContents());
    }
    
    public String getContents() {
        return contents;
    }
    
    public void setContents(String contents) {
        if (this.contents != null) {
            this.contents = null;
        } else {
            this.contents = contents;
        }
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof StringValue) {
            String ocontents = ((StringValue)o).contents;
            return (ocontents == contents) || (ocontents != null && contents != null && contents.equals(ocontents));
        }
        return false;
    }
}