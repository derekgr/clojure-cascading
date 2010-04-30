clojure-cascading

This is very much a WIP; I'm still learning clojure, so it's not very
idiomatic. It feels too imperative, and it's probably just as easy to wrap a
few macros around the cascading API itself.

DEPENDENCIES

This library requires Java 6 JDK, http://java.sun.com/

To build from source, you need Ant, a 0.20 or above hadoop distribution, 
the clojure jar (I am using 1.1.0), and cascading dependencies. See BUILDING below.

BUILDING

You need to provide the locations of the cascading JARs, the clojure JAR, and the
hadoop core JAR. You can specify the locations by defining cascading.home, clojure.home, and
hadoop.home when running ant:

ant -Dcascading.home=... -Dclojure.home=... -Dhadoop.home=... jar

Alternatively you can set CASCADING_HOME, CLOJURE_HOME, and HADOOP_HOME environment
variables in your shell and just run "ant jar".

I should convert this to Maven, but my Java-fu is quite rusty.

After building, include the "clojure-cascading.jar" file
in the lib/ directory of the JAR you submit as your Hadoop job. You must also
include the clojure core jarfile, as well as the cascading dependencies you would need
to include anyway to run a cascading flow.
