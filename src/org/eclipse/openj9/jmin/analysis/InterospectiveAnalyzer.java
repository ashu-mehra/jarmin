package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class InterospectiveAnalyzer extends Analyzer<BasicValue> {
    public InterospectiveAnalyzer(final Interpreter<BasicValue> interpreter) {
        super(interpreter);
    }
    @Override
    protected Frame<BasicValue> newFrame(final int numLocals, final int numStack) {
        return new InterospectiveFrame(numLocals, numStack);
    }
    @Override
    protected Frame<BasicValue> newFrame(final Frame<? extends BasicValue> frame) {
        return new InterospectiveFrame(frame);
    }
}