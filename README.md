# swisseph-module
Java module of Thomas Mach Swiss Ephemeris Port.

The Java Source was generated from Thomas Mack Java port with the command:

    java Precompile -DNO_MOSHIER -DSTRICTMATH -qfc -iswesrc

The Java Source has slightly modified, see Change Log below, to remove deprecated features and to use
preferred types in public methods.
The Java Source files are encoded by UTF-8, this is only relevant for ODEGREE_-, OMINUTE_-, and
OSECOND_STRING constants in SwissData.java. 

The module exports all public classes and methods of Thomas Macks Java port - it is defined by:

module swisseph {
	exports swisseph;
}

The file create-jar.txt is a list of Windows/Unix commands to create a swisseph.jar Java Module File with
embedded Java Source and to create a Java Doc folder.


This project is under the GNU General Public License, version 2.
See README.txt and LICENSE.txt for alternative licensing model of the C Swiss Ephemeris Code.

Change Log:
2018-05-22 Changed StringBuffer to StringBuilder.
2018-05-22 Changed new Double(string).doubleValue() to Double.parseDouble(string).
