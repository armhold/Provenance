Provenance
==========

Have you just inherited an Ant project? Maybe you have a "lib" dir full of random
jar files? Worse, some thoughtless developer has neglected to put version numbers on the jars?


This program can help you determine the provenance of such files. It will recursively examine
a given directory for *.jar files. It computes the SHA1 hash of the files, and then uses that
hash to search a REST API for the Maven coordinates of the given artifact.

For each identified jar file, it will print out a snippet of XML that you can include
in the dependencies section of your pom.xml

Build:
------
	in the directory where is pom.xml run:
		mvn package
	
	then copy the final jar whenever you want:  
		cp target/provenance-1.1-jar-with-dependencies.jar  somewhere/provenance.jar

Run like:
---------
    	$ java -jar Provenance.jar lib_dir 	

