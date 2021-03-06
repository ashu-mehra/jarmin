# Jar Minimizer
The JAR minimizer is a standalone tool for statically processing a set of JAR files to produce copies excluding components not
specifically required for an application. The tool uses the ASM bytecode library and handles reflection and other operations using an
extension of the ASM absract interpreter intraprocedurally.

At present this tool and its techniques are IBM Confidential and not for external discussion / disclosure.

## Building
```
cd src
../build.cmd
```
You need to have a Java 8 JDK on the command line

## Running
```
/path/to/java -cp ./bin:./lib/asm-8.0.1.jar:./lib/asm-analysis-8.0.1.jar:./lib/asm-tree-8.0.1.jar:./lib/asm-util-8.0.1.jar org.eclipse.openj9.JMin <classpath to search> <main class> <main method name> <main method signature>
```
The tool had three system properties that control its operation:

Set org.eclipse.openj9.jmin.reduction_mode to control how minimization is performed:
- class (remove only unused classes) [default]
- method (remove unused classes and methods)
- field (remove unused classes, methods and fields)

Set org.eclipse.openj9.jmin.inclusion_mode to control how classes are included in the analysis: 
- reference (overriden / implemented methods are scanned when the class is first referenced)
- instantiation (overridden / implemented methods are scanned when the class is first instantiated) [default]

Set org.eclipse.openj9.jmin.trace to control output verbosity: 
- true (verbose output will be printed to stdout)
- false (only impotant diagnostic output will be printed to stdout) [default]

At the present time the `method` and `field` modes are experimental and do not support fields only initialized but not read after
initialization so results will vary.
