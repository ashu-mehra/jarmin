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
/path/to/java -cp ./bin:./lib/asm-8.0.1.jar:./lib/asm-analysis-8.0.1.jar:./lib/asm-tree-8.0.1.jar:./lib/asm-util-8.0.1.jar org.eclipse.openj9.JMin <classpath to search> <main class> <main method name> <main method signature> [mode]
```
The tool supports 3 modes of operation:
- class (default): unreferenced classes will be removed
- method: unreferenced classes will be removed and unreferenced methods will be removed from classes which are retained
- field: unreferenced classes will be remove adn unreferenced methods and fields will be removed from classes which are retained

At the present time the `method` and `field` modes are experimental and do not support fields only initialized but not read after
initialization so results will vary.
