Provenance
==========

Have you just inherited an Ant project? Maybe you have a "lib" dir full of random
jar files? Worse, some thoughtless developer has neglected to put version numbers on the jars?


This program can help you determine the provenance of such files. It will recursively examine
a given directory for *.jar files. It computes the SHA1 hash of the files, and then uses that
hash to search a REST API for the Maven coordinates of the given artifact.

For each identified jar file, it will print out a snippet of XML that you can include
in the dependencies section of your pom.xml

Artifacts that are not found are printed separately and referenced as local libraries within the pom.

Build:
------
In the directory where the pom.xml is, run:

    mvn package
	
Then copy the final jar wherever you want:  

    cp target/provenance-1.1-jar-with-dependencies.jar  somewhere/provenance.jar

Run like:
---------

    $ java -jar provenance.jar lib_dir 	add_comments

If you want to write results to a file instead of the console:

    $ java -jar provenance.jar lib_dir add_comments > deps.xml
    	
If behind a proxy be sure to provide information for https proxying:

    $ java -Dhttps.proxyHost=proxyhostURL -Dhttps.proxyPort=proxyPortNumber -Dhttps.proxyUser=someUserName -Dhttps.proxyPassword=somePassword -jar provenance.jar lib_dir add_comments > deps.xml
