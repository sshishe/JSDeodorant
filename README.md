# JSDeodorant

This tool is aimed at detecting JavaScript class emulation structures with respect to popular class/namespace emulation patterns introduced by major JavaScript books, blogs and authors.

The tool leverages AST tree generated by Closure Compiler and is able to find class emulation structures, namespaces and CommonsJS style modules with applying a light-weight data-flow analysis. 

# Usage
It requires you have at least JDK 7 installed on your machine with an Eclipse instance that has Gradle plugin installed on it or Gradle installed on machine.

Alternatively you can resolve dependencies with `gradle build` to install JAR dependencies without the need for Gradle plugin on Eclsipe.

This tool also comes with an Eclipse plugin, which itself is able to analyse JavaScript projects. But we will explore command-line mode to see how to generate CSV outputs and console logs for experimental purpose.

Here is the list of switches you can pass to the command-line runner:

+ `-class-analysis`             : Advanced static analysis to match function definitions with function calls (call-site)
+ `-function-analysis`          : Advanced function analysis to match class definitions with initialization (call-site)
+ `-calculate-cyclomatic`       : Enable calculation of cyclomatic complexity
+ `-js`                         : The JavaScript filenames
+ `-directory-path`       	     : Directory path for javascript project
+ `-analyze-lbClasses`          : Analyze libraries to find class usage in them
+ `-builtin-libraries` 	    	 : List of libraries located somewhere on the system such as Node's built-in libraries i.e. Error or Util
+ `-disable-log`                : Enable logging mechanism
+ `-externs`          		    	 : List of externs files to use in the compilation.
+ `-libraries`                  : List of libraries to distinguish between production/test codes.
+ `-module-analysis`            : Enable module analysis for CommonJS or Closure Library style packaging
+ `-package-system`             : Select the package system including CommonJS and Closure Library
+ `-output-csv`                 : Generate a CSV file containing analysis info
+ `-output-db`                  : Put analysis info into a Postgres DB
+ `-name`                       : Project name
+ `-version`                    : Project version
+ `-psqlServer`                 : Postgres server name
+ `-psqlPort`                   : Postgres port
+ `-psqlDbName`                 : Postgres database name
+ `-psqlUser`                   : Postgres user
+ `-psqlPassword`               : Postgres password

To be able to run the tool without eclipse, you should run `gradle assemble` in the `core` folder of JSDeodorant to build the appropriate JAR file in the target folder.

To create eclipse project for the `core` you can run `gradle build eclipse`.

To be able to import Eclipse Plugin into the workspace, you have to navigate to plugin root folder and run `gradle buildAndCopyLibs`. This way gradle would build JSDeodorant's core component to resolve plugin's dependency.

Then, you can run the tool with the following command:
`java -jar target/jsdeodorant-0.0-SNAPSHOT-jar-with-dependencies.jar -help` to show the switches that you can pass to the tool.

An example of a working set of switches for project **Closure Library** is:
<br />
`
-output-csv -class-analysis -module-analysis -package-system "ClosureLibrary" -analyze-lbClasses
-directory-path "/Users/Shahriar/Documents/workspace/era/dataset/closure-library-v20160315"
-name "closure-library"
`

After running this command, take a look at following paths: `log/classes` and `log/functions` folders.

## Evaluation of the tool
We ran the tool to evaluate performance of JSDeodorant (precision and recall) for three different projects written in JavaScript, CoffeeScript and TypeScript. Note that we choose these three projects because we can create an oracle based on JSDoc annotations for JavaScript project, and TypeScript and CoffeeScript **class** nodes which will be compiled to vanilla JavaScript.

| Program        | Identified Function Constructors           | TP  | FP  | FN | Precision | Recall |
| -------------- |:------------------------------------------:| :--:| --- | --- | --------- | ------ |
| Closure-library| 1008 | 907 | 101 | 39 | 90% | 96% |
| Doppio (TypeScript)     | 154      |   153 | 1 | 1 | 99% | 99% |
| Atom (CoffeScript) | 106      |    101 | 5 | 1 | 95% | 99% |

##### Closure-library evaluation files
* [Closure-library oracle](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/closure-oracle.htm)
* [Closure-library oracle extra found](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/closure-extras.htm)
* [Closure-library comparison with js classes](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/closure-comparison.htm)

##### Doppio evaluation files
* [Doppio oracle](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/doppio-oracle.htm)
* [Doppio oracle extra found](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/doppio-extras.htm)
* [Doppio comparison with js classes](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/doppio-comparison.htm)

##### Atom evaluation files
* [Atom oracle](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/atom-oracle.htm)
* [Atom oracle extra found](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/atom-extra.htm)
* [Atom comparison with js classes](http://htmlpreview.github.io/?https://raw.githubusercontent.com/sshishe/jsdeodorant-evaluation/master/evaluation/atom-comparison.htm)

## Research
If you are interested to learn how exactly JSDeodorant works, please have a look at the following paper:

Shahriar Rostami, Laleh Eshkevari, Davood Mazinanian, and Nikolaos Tsantalis, "[Detecting Function Constructors in JavaScript](https://users.encs.concordia.ca/~nikolaos/publications/ICSME_2016.pdf)," 32nd IEEE International Conference on Software Maintenance and Evolution (ICSME'2016), ERA Track, Raleigh, North Carolina, USA, October 2-10, 2016.

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
