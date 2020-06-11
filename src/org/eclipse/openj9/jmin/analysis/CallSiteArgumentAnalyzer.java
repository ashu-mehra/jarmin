package org.eclipse.openj9.jmin.analysis;

import org.eclipse.openj9.jmin.info.CallKind;
import org.eclipse.openj9.jmin.info.CallSite;
import org.eclipse.openj9.jmin.info.MethodInfo;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

public class CallSiteArgumentAnalyzer {
    public static boolean analyze(MethodInfo minfo, MethodNode methodNode) {
        InterospectiveAnalyzer a = new InterospectiveAnalyzer(new ReflectionInterpreter());
        try {
            a.analyze(minfo.clazz(), methodNode);
        } catch (AnalyzerException e) {
            return false;
        }
        Frame<BasicValue>[] frames = a.getFrames();
        for (CallSite callSite: minfo.getCallSites()) {
            if (callSite.kind == CallKind.DYNAMIC || callSite.desc.equals("*")) {
                continue;
            }
            assert frames.length >= callSite.getInstructionIndex() : frames.length + " " + callSite.getInstructionIndex();
            Frame<BasicValue> frame = frames[callSite.getInstructionIndex()];
            if (frame != null) {
                BasicValue[] argValueList = new BasicValue[Type.getArgumentTypes(callSite.desc).length];
                int top = frame.getStackSize() - 1;
                for (int argIndex = 0; argIndex < argValueList.length; argIndex++) {
                    argValueList[argIndex] = frame.getStack(top - (argValueList.length - argIndex - 1));
                }
                callSite.setArgValueList(argValueList);
            }
        }
        return true;
    }
}
