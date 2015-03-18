(This page is under construction.)

# Dependencies #

ExtC makes use of the following open-source packages.  The libraries are included in the distribution and should require no effort on your part to install.  The plugins need to be installed independently.  Each of the required packages may themselves rely on other open-source packages.  Please see their web pages for their up-to-date dependencies.

## Platform ##
  * Java 1.6 or greater
  * Eclipse 3.5.1 or greater

## Eclipse Plugins ##

  * [net.sourceforge.metrics](http://sourceforge.net/projects/metrics2/)  collects software metrics and can store them to a database.  An alpha version of the 2.0 release is provided with extc in the Downloads page.  License - CPL1.0, the same license as Eclipse itself .
  * [Apache Derby](http://db.apache.org/derby/) or.g.apache.derby.core 10.5.3 (optional) provides database functionality.  License - [Apache License, Version 2.0](http://www.apache.org/licenses/)
  * [Apache Derby](http://db.apache.org/derby/) org.apache.derby.plugin.doc (optional) provides database functionality.  License - [Apache License, Version 2.0](http://www.apache.org/licenses/)
  * [Apache Derby](http://db.apache.org/derby/) org.apache.derby.ui (optional) provides database functionality.  License - [Apache License, Version 2.0](http://www.apache.org/licenses/)

## Libraries ##
  * [Archaeopteryx/forester](http://www.phylosoft.org/archaeopteryx/) provides dendrogram viewing functionality.  License - GNU Lesser General Public License 2.1
  * [Collections-generic-4.0.1](http://larvalabs.com/collections/) is a general-purpose collections package required by Jung.  License - Apache License V2.0
  * [colt-1.2.0](http://acs.lbl.gov/software/colt/) scientific computing package is required by JUNG.  License [here](http://acs.lbl.gov/software/colt/license.html).
  * [concurrent-1.3.4.jar](http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html) contains utility   packages for concurrent programming and is required by JUNG.   No license required.
  * [epsdump](http://www.jibble.org/epsgraphics/) provides a way to dump java graphics to an EPS file.  License - formerly GPL, now has a license fee.
  * [json-extracts](http://www.json.org/java/index.html) provides a data interchange format.  License [here](https://github.com/douglascrockford/JSON-java).
  * [jung 2.0](http://jung.sourceforge.net/) provides graph visualizations, algorithms, and more. License - Berkeley Software Distribution (BSD) license (http://jung.sourceforge.net/site/jung-graph-impl/license.html).
  * [S-Space lsa-vsm](http://code.google.com/p/airhead-research/) provides semantic space functionality.  License - [GPL v. 2](http://code.google.com/p/airhead-research/wiki/LicenseAndRestrictions).
  * [Streaming API for XML](http://stax.codehaus.org/Home) stax-api-1.0.1 provides XML capabilities required by JUNG.  License - Apache License 2.0
  * [Woodstox](http://woodstox.codehaus.org/) wstx-asl-3.2.6 is an XML processor required by JUNG.  Licenses: Free Software Foundataion's LGPL 2.1 and Apache Foundation's ASL 2.0.

# Installing #

Install the plugins listed in the Dependencies section (metrics2 and Apache Derby).  Then, install ext-c using either the pre-built plugins or the available source.  Your dropins directory should end up looking something like:
```
plugins
plugins/org.apache.derby.ui_1.1.2/*
plugins/classRefactoringPlugin_1.0.0.201108031258.jar
plugins/org.apache.derby.core_10.5.3/*
plugins/org.apache.derby.plugin.doc_1.1.2/*
plugins/net.sourceforge.metrics_2.0.0.201105311430.jar
```

This [web page](http://stackoverflow.com/questions/1489648/eclipse-doesnt-load-plugins-in-the-dropins-folder/) addresses problems with installing plugins/dropins.
## Installing from pre-built plugins ##

Download the most recent ClassRefactoringPlugin from http://code.google.com/p/ext-c/downloads/list and place it in your Eclipse dropins directory.

## Installing from source ##
The project is hosted at http://code.google.com/p/ext-c/.

Download the source from the command line: svn checkout https://ext-c.googlecode.com/svn/trunk

Download the source into Eclipse:
  1. File -> Import -> SVN -> Checkout Projects from SVN (Next)
  1. Use repository location  https://ext-c.googlecode.com/svn/trunk (Next)
  1. Select Folder trunk (Next)
  1. Checkout From SVN
    1. Check out as a project in the workspace
    1. Check out HEAD revision
    1. Allow unversioned obstructions (Finish)

# Customization #
There is a limited amount of customization that can be done by setting system properties when eclipse is started, e.g. by supplying “-D” command line arguments e.g.:
```
    eclipse -vmargs -Xms128m -Xmx512m -XX:MaxPermSize=128m  -Dextc.properties=H:\workspace\ClassRefactoringPlugin\extc.properties
```

ExtC recognizes the following properties:
  1. **extc.project.root** indicates where files should be read or written by default, e.g. graph images.
  1. **extc.properties**  indicates the location of a property file that contains additional customizable parameters.

# Example Property File (extc.properties) #
Most of these properties can be modified via the Graph View of the UI.  They are mostly for convenience for setting preferences or for running in batch mode.
```
### Parameters influencing the graph construction and display
# These keys should be kept consistent with
# nz.ac.vuw.ecs.kcassell.utils.ParameterConstants

# See edu.uci.ics.jung.graph.util.EdgeType enum for acceptable values
edgeType=DIRECTED

# See nz.ac.vuw.ecs.kcassell.callgraph.gui.GraphLayoutEnum for acceptable values
graphLayout=FRLayout

# see nz.ac.vuw.ecs.kcassell.callgraph.ScoreType for acceptable values
nodeSizing=Authority

# Indicates which graph nodes should be condensed into a single node.
condenseInherited=true
condenseObjectMethods=false
condenseInterfaces=true
condenseRecursiveCycles=false

# Indicates which nodes should be removed from the graph
filterObject = true

# should constructors be shown in the graph? */
includeConstructors = false

# preferred colors for graph nodes and edges.
# RGB value consisting of the red component
# in bits 16-23, the green component in bits 8-15,
# and the blue component in bits 0-7
# see http://www.web-source.net/216_color_chart.htm
fieldColor         = 0x3333CC
methodColor        = 0xFFFFFF
connectedEdgeColor = 0x000000
deletedEdgeColor   = 0xDDDDDD
unpickedColor      = 0x000000
pickedColor        = 0x00FFFF

### Parameters influencing class selection

# The max size threshold for the number of members in a class.
maxMembers = 20


### Parameters influencing clustering

# the clusterer to use
# see nz.ac.vuw.ecs.kcassell.similarity.ClustererEnum
clusterer=Mixed_Mode

# the distance calculator to use for agglomerative clustering
# see nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum
distanceCalculator=Identifier

# Tokens from identifiers that shouldn't be considered properties
identifierPartsToIgnore=get,set,m,i,d,l,s

# Link type (how to combine clusters) for agglomerative clustering
# see Jain, Murphy, and Flynn, "Data clustering: a review",
# ACM Computing Surveys, 1999
# acceptable value are static strings in MatrixBasedAgglomerativeClusterer
linkType = single

# how many new betweenness clusters should be created
newBetweennessClusters = 1

```

# Graphing #

Clicking on the graph icon will bring up ExtC in graph mode for the file currently being edited in Eclipse, assuming that the file is part of an Eclipse project.  This enables the following tabs of ExtC:
  * Graph
  * Betweenness
  * Agglomeration
  * Spanning Forest
Of these, Graph and Betweenness will probably be the most useful.  Agglomeration is in the midst of revision.

# Metrics #
The installation includes my enhancements to the metrics2 (http://sourceforge.net/projects/metrics2/)   Eclipse plug-in.  Using metrics2, you can collect OO metrics on your Eclipse project.  With additional work, you can save the metric data to a Derby database.  ExtC can run queries on that database and display the results in the Metrics View.  Clicking on a row in the Metrics View will display that class in the Graph View (and others).

I haven't yet prepared instructions for the database set up.