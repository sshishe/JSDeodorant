# JSDeodorant

This tool is aimed at detecting JavaScript class emulation structures with respect to popular class/namespace emulation patterns introduced by major JavaScript books, blogs and authors.

The tool leverages AST tree generated by Closure Compiler and is able to find class emulation structures, namespaces and CommonsJS style modules with applying a light-weight data-flow analysis. 

# Usage
It requires you have at least JDK 7 installed on your machine with an Eclipse instance that has Maven plugin installed on it.

Alternatively you can resolve dependencies with `mvn install` to install JAR dependencies without the need for Maven plugin on Eclsipe.

This tool also comes with an Eclipse plugin, which itself is able to analyse JavaScript projects. But we will explore command-line mode to see how generate CSV outputs and console logs for experimental purpose.

Here is the list of switches you can pass to the command-line runner:

+ `-class_analysis`             : Advanceed static analysis to match function definitions with function calls (call-site)
+ `-function_analysis`          : Advanceed function analysis to match class definitions with initialization (call-site)
+ `-calculate_cyclomatic`       : Enable calculation of cyclomatic complexity
+ `-js`                         : The JavaScript filenames
+ `-directory_path`       	     : Directory path for javascript project
+ `-analyze-lbClasses`          : Analyze libraries to find class usage in them
+ `-builtin-libraries` 	    	 : List of libraries located somewhere on the system such as Node's built-in libraries i.e. Error or Util
+ `-disable_log`                : Enable logging mechanism
+ `-externs`          		    	 : List of externs files to use in the compilation.
+ `-libraries`                  : List of libraries to distinguish between production/test codes.
+ `-module-analysis`            : Enable module analysis for CommonJS or Closure Library style packaging
+ `-package-system`             : Select the package system including CommonJS and Closure Library
+ `-output_csv`                 : Generate a CSV file containing analysis info
+ `-output_db`                  : Put analysis info into a Postgres DB
+ `-name`                       : Project name
+ `-version`                    : Project version
+ `-psqlServer`                 : Postgres password
+ `-psqlPort`                   : Postgres port
+ `-psqlDbName`                 : Postgres database name
+ `-psqlUser`                   : Postgres user
+ `-psqlPassword`               : Postgres password

To be able to run the tool without eclipse, you may run `mvn clean compile assembly:single` in the `core` folder of JSDeodorant to build the appropriate JAR file in the target folder.

Then, you can run the tool with the following command:
`java -jar target/jsdeodorant-0.0-SNAPSHOT-jar-with-dependencies.jar -help` to show the switches that you can pass to the tool.

An example of a working set of switches for project **Closure Library** is:
<br />
`
-output_csv -class_analysis -module-analysis -package-system "ClosureLibrary" -analyze-lbClasses
-directory_path "/Users/Shahriar/Documents/workspace/era/dataset/closure-library-v20160315"
-name "closure-library"
`

## Evaluation of the tool
We ran the tool to evaluate performance of JSDeodorant (precision and recall) are shown for three different projects written in JavaScript, CoffeeScript and TypeScript. Note that we choose these three projects because we can create an oracle based on JSDoc annotations for JavaScript project, and TypeScript and CoffeeScript **class** nodes which will be compiled to vanilla JavaScript.

| Program        | Identified Function Constructors           | TP  | FP  | FN | Precision | Recall |
| -------------- |:------------------------------------------:| :--:| --- | --- | --------- | ------ |
| Closure-library| 1008 | 907 | 101 | 39 | 90% | 96% |
| Doppio (TypeScript)     | 154      |   153 | 1 | 1 | 99% | 99% |
| Atom (CoffeScript) | 106      |    101 | 5 | 1 | 95% | 99% |

##### Closure-library evaluation files
* [Closure-library oracle](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/closure-oracle.htm?token=AC-lR1IPVi8VLiHRJRnztVDa_i8NjnOPks5XcqunwA%3D%3D)
* [Closure-library oracle extra found](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/closure-extras.htm?token=AC-lR_1d0cGhf17ByidutToF0KWhCI_vks5XcqvKwA%3D%3D)
* [Closure-library comparison with js classes](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/closure-comparison.htm?token=AC-lR0RCUtibcCmalUoAgpz5XvidRMAeks5XcqvkwA%3D%3D)
* [Full Closure-library file](https://github.com/sshishe/jsdeodorant/blob/master/evaluation/closure.xlsx)

##### Doppio evaluation files
* [Doppio oracle](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/doppio-oracle.htm?token=AC-lR3QIJR3A4MDiBTfdkQ39ThJbylngks5XcqwbwA%3D%3D)
* [Doppio oracle extra found](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/doppio-extras.htm?token=AC-lR4D_5cD-OgxhStMQmQ8QQ7E6dPfKks5XcqwtwA%3D%3D)
* [Doppio comparison with js classes](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/doppio-comparison.htm?token=AC-lR7VnBYQ_QR1y50fnDnCiPnEBDJ8Kks5Xcqw_wA%3D%3D)
* [Full Doppio analysis file](https://github.com/sshishe/jsdeodorant/blob/master/evaluation/doppio.xlsx)

##### Atom evaluation files
* [Atom oracle](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/atom-oracle.htm?token=AC-lRw19aCpDP3-bIdBQaJgjAE8NjgWMks5XcqsowA%3D%3D)
* [Atom oracle extra found](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/atom-extra.htm?token=AC-lR3pzeQjb3_XYHmlY9OMPgt_nGw_Nks5XcqttwA%3D%3D)
* [Atom comparison with js classes](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant/master/evaluation/atom-comparison.htm?token=AC-lR_1oCBFeb-Rk7lyTE14n9XwONmvuks5XcqtRwA%3D%3D)
* [Full Atom analysis file](https://github.com/sshishe/jsdeodorant/blob/master/evaluation/atom.xlsx)



## License
This project is licensed under the MIT License.

## Links to external resources:
* [Learning JavaScript Design Patterns][1]
* [JavaScript: The Good Parts][2]
* [How do I declare a namespace in JavaScript?][3]
* [Javascript Namespaces and Modules][4]

[1]: http://shop.oreilly.com/product/0636920025832.do
[2]: http://shop.oreilly.com/product/9780596517748.do
[3]: http://stackoverflow.com/questions/881515/how-do-i-declare-a-namespace-in-javascript.
[4]: https://www.kenneth-truyers.net/2013/04/27/javascript-namespaces-and-modules/
