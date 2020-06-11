package org.eclipse.openj9.jmin.methodsummary;

import org.eclipse.openj9.jmin.analysis.ParameterValue;
import org.eclipse.openj9.jmin.analysis.StringBuilderValue;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.ArrayList;
import java.util.List;

public class MethodSummary {
    ArrayList<BasicValue> instantiatedValues;

    public MethodSummary() {
        instantiatedValues = new ArrayList();
    }

    public void addInstantiatedValue(BasicValue iv) {
        for (BasicValue bv: instantiatedValues) {
            if (bv.equals(iv)) return;
        }
        instantiatedValues.add(iv);
    }

    public List<BasicValue> getInstantiatedValues() {
        return new ArrayList(instantiatedValues);
    }
}
