/*
   This is a port of the Swiss Ephemeris Free Edition, Version 2.00.00
   of Astrodienst AG, Switzerland from the original C Code to Java. For
   copyright see the original copyright notices below and additional
   copyright notes in the file named LICENSE, or - if this file is not
   available - the copyright notes at http://www.astro.ch/swisseph/ and
   following.

   For any questions or comments regarding this port to Java, you should
   ONLY contact me and not Astrodienst, as the Astrodienst AG is not involved
   in this port in any way.

   Thomas Mack, mack@ifis.cs.tu-bs.de, 23rd of April 2001

*/
/* Copyright (C) 1997 - 2008 Astrodienst AG, Switzerland.  All rights reserved.

  License conditions
  ------------------

  This file is part of Swiss Ephemeris.

  Swiss Ephemeris is distributed with NO WARRANTY OF ANY KIND.  No author
  or distributor accepts any responsibility for the consequences of using it,
  or for whether it serves any particular purpose or works at all, unless he
  or she says so in writing.

  Swiss Ephemeris is made available by its authors under a dual licensing
  system. The software developer, who uses any part of Swiss Ephemeris
  in his or her software, must choose between one of the two license models,
  which are
  a) GNU public license version 2 or later
  b) Swiss Ephemeris Professional License

  The choice must be made before the software developer distributes software
  containing parts of Swiss Ephemeris to others, and before any public
  service using the developed software is activated.

  If the developer choses the GNU GPL software license, he or she must fulfill
  the conditions of that license, which includes the obligation to place his
  or her whole software project under the GNU GPL or a compatible license.
  See http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

  If the developer choses the Swiss Ephemeris Professional license,
  he must follow the instructions as found in http://www.astro.com/swisseph/
  and purchase the Swiss Ephemeris Professional Edition from Astrodienst
  and sign the corresponding license contract.

  The License grants you the right to use, copy, modify and redistribute
  Swiss Ephemeris, but only under certain conditions described in the License.
  Among other things, the License requires that the copyright notices and
  this notice be preserved on all copies.

  Authors of the Swiss Ephemeris: Dieter Koch and Alois Treindl

  The authors of Swiss Ephemeris have no control or influence over any of
  the derived works, i.e. over software or services created by other
  programmers which use Swiss Ephemeris functions.

  The names of the authors or of the copyright holder (Astrodienst) must not
  be used for promoting any software, product or service which uses or contains
  the Swiss Ephemeris. This copyright notice is the ONLY place where the
  names of the authors can legally appear, except in cases where they have
  given special permission in writing.

  The trademarks 'Swiss Ephemeris' and 'Swiss Ephemeris inside' may be used
  for promoting such software, products or services.
*/
package swisseph;

/*
* This class contains many constants for internal use only.
* It does not need to be instantiated.
*/
class SwephData
		implements java.io.Serializable
		{


  static final String SE_VERSION = "2.01.00";
  static final String SE_JAVA_VERSION = "2.01.00_01";

  // Aus sweph.h:
  static final int[] PNOINT2JPL = new int[] {SwephJPL.J_EARTH, SwephJPL.J_MOON,
                    SwephJPL.J_MERCURY, SwephJPL.J_VENUS, SwephJPL.J_MARS,
                    SwephJPL.J_JUPITER, SwephJPL.J_SATURN, SwephJPL.J_URANUS,
                    SwephJPL.J_NEPTUNE, SwephJPL.J_PLUTO, SwephJPL.J_SUN};
  static final int[] pnoint2jpl = PNOINT2JPL;

  /* planetary radii in meters */
  static final int NDIAM = (SweConst.SE_VESTA + 1);
  static final double pla_diam[] = {1392000000.0, /* Sun */
                           3476300.0, /* Moon */
                           2439000.0 * 2, /* Mercury */
                           6052000.0 * 2, /* Venus */
                           3397200.0 * 2, /* Mars */
                          71398000.0 * 2, /* Jupiter */
                          60000000.0 * 2, /* Saturn */
                          25400000.0 * 2, /* Uranus */
                          24300000.0 * 2, /* Neptune */
                           2500000.0 * 2, /* Pluto */
                           0, 0, 0, 0,    /* nodes and apogees */
                           6378140.0 * 2, /* Earth */
                                 0.0, /* Chiron */
                                 0.0, /* Pholus */
                            913000.0, /* Ceres */
                            523000.0, /* Pallas */
                            244000.0, /* Juno */
                            501000.0, /* Vesta */
                        };

  static final double J2000 = 2451545.0;        // 2000 January 1.5
  static final double B1950 = 2433282.42345905; // 1950 January 0.923
  static final double J1900 = 2415020.0;        // 1900 January 0.5

  static final int MPC_CERES = 1;
  static final int MPC_PALLAS = 2;
  static final int MPC_JUNO = 3;
  static final int MPC_VESTA = 4;
  static final int MPC_CHIRON = 2060;
  static final int MPC_PHOLUS = 5145;

  static final String SE_NAME_SUN = "Sun";
  static final String SE_NAME_MOON = "Moon";
  static final String SE_NAME_MERCURY = "Mercury";
  static final String SE_NAME_VENUS = "Venus";
  static final String SE_NAME_MARS = "Mars";
  static final String SE_NAME_JUPITER = "Jupiter";
  static final String SE_NAME_SATURN = "Saturn";
  static final String SE_NAME_URANUS = "Uranus";
  static final String SE_NAME_NEPTUNE = "Neptune";
  static final String SE_NAME_PLUTO = "Pluto";
  static final String SE_NAME_MEAN_NODE = "mean Node";
  static final String SE_NAME_TRUE_NODE = "true Node";
  static final String SE_NAME_MEAN_APOG = "mean Apogee";
  static final String SE_NAME_OSCU_APOG = "osc. Apogee";
  static final String SE_NAME_INTP_APOG = "intp. Apogee";
  static final String SE_NAME_INTP_PERG = "intp. Perigee";
  static final String SE_NAME_EARTH = "Earth";
  static final String SE_NAME_CERES = "Ceres";
  static final String SE_NAME_PALLAS = "Pallas";
  static final String SE_NAME_JUNO = "Juno";
  static final String SE_NAME_VESTA = "Vesta";
  static final String SE_NAME_CHIRON = "Chiron";
  static final String SE_NAME_PHOLUS = "Pholus";


  static final String SE_NAME_CUPIDO = "Cupido";
  static final String SE_NAME_HADES = "Hades";
  static final String SE_NAME_ZEUS = "Zeus";
  static final String SE_NAME_KRONOS = "Kronos";
  static final String SE_NAME_APOLLON = "Apollon";
  static final String SE_NAME_ADMETOS = "Admetos";
  static final String SE_NAME_VULKANUS = "Vulkanus";
  static final String SE_NAME_POSEIDON = "Poseidon";
  static final String SE_NAME_ISIS = "Isis";
  static final String SE_NAME_NIBIRU = "Nibiru";
  static final String SE_NAME_HARRINGTON = "Harrington";
  static final String SE_NAME_NEPTUNE_LEVERRIER = "Leverrier";
  static final String SE_NAME_NEPTUNE_ADAMS = "Adams";
  static final String SE_NAME_PLUTO_LOWELL = "Lowell";
  static final String SE_NAME_PLUTO_PICKERING = "Pickering";
  static final String SE_NAME_VULCAN = "Vulcan";
  static final String SE_NAME_WHITE_MOON = "White Moon";

  /* Ayanamsas 
   * For each ayanamsa, there are two values:
   * t0       epoch of ayanamsa, TDT (ET)
   * ayan_t0  ayanamsa value at epoch
   */
  static final AyaInit[] ayanamsa = new AyaInit[] {
    new AyaInit(2433282.5, 24.042044444),  /* 0: Fagan/Bradley (Default) */
//    new AyaInit(J1900, 360 - 337.53953),   /* 1: Lahiri (Robert Hand) */
    /*{J1900, 360 - 337.53953},   * 1: Lahiri (Robert Hand) */
    new AyaInit(2435553.5, 23.250182778 - 0.004660222),   /* 1: Lahiri (derived from:
                           * Indian Astronomical Ephemeris 1989, p. 556;
                           * the subtracted value is nutation) */
    new AyaInit(J1900, 360 - 333.58695),   /* 2: De Luce (Robert Hand) */
    new AyaInit(J1900, 360 - 338.98556),   /* 3: Raman (Robert Hand) */
    new AyaInit(J1900, 360 - 341.33904),   /* 4: Ushashashi (Robert Hand) */
    new AyaInit(J1900, 360 - 337.636111),  /* 5: Krishnamurti (Robert Hand) */
    new AyaInit(J1900, 360 - 333.0369024), /* 6: Djwhal Khool; (Graham Dawson)
                                            *    Aquarius entered on 1 July 2117 */
    new AyaInit(J1900, 360 - 338.917778),  /* 7: Yukteshwar; (David Cochrane) */
    new AyaInit(J1900, 360 - 338.634444),  /* 8: JN Bhasin; (David Cochrane) */
    new AyaInit(1684532.5, -3.36667),      /* 9: Babylonian, Kugler 1 */
    new AyaInit(1684532.5, -4.76667),      /*10: Babylonian, Kugler 2 */
    new AyaInit(1684532.5, -5.61667),      /*11: Babylonian, Kugler 3 */
    new AyaInit(1684532.5, -4.56667),      /*12: Babylonian, Huber */
    new AyaInit(1673941, -5.079167),       /*13: Babylonian, Mercier;
                                            *    eta Piscium culminates with zero point */
    new AyaInit(1684532.5, -4.44088389),   /*14: t0 is defined by Aldebaran at 15 Taurus */
    new AyaInit(1674484, -9.33333),        /*15: Hipparchos */
    new AyaInit(1927135.8747793, 0),       /*16: Sassanian */
    /*{1746443.513, 0},                     *17: Galactic Center at 0 Sagittarius */
    new AyaInit(1746447.518, 0),           /*17: Galactic Center at 0 Sagittarius */
    new AyaInit(J2000, 0),                 /*18: J2000 */
    new AyaInit(J1900, 0),                 /*19: J1900 */
    new AyaInit(B1950, 0),                 /*20: B1950 */
    new AyaInit(1903396.8128654, 0),       /*21: Suryasiddhanta, assuming
                                                 ingress of mean Sun into Aries at point
                                                 of mean equinox of date on
                                                 21.3.499, noon, Ujjain (75.7684565 E)
                                                 = 7:30:31.57 UT */
    new AyaInit(1903396.8128654,-0.21463395),/*22: Suryasiddhanta, assuming
                                                 ingress of mean Sun into Aries at
                                                 true position of mean Sun at same epoch */
    new AyaInit(1903396.7895321, 0),       /*23: Aryabhata, same date, but UT 6:56:55.57
                                                 analogous 21 */
    new AyaInit(1903396.7895321,-0.23763238),/*24: Aryabhata, analogous 22 */
    new AyaInit(1903396.8128654,-0.79167046),/*25: SS, Revati/zePsc at polar long. 359Âø50'*/
    new AyaInit(1903396.8128654, 2.11070444),/*26: SS, Citra/Spica at polar long. 180Âø */
    new AyaInit(0, 0),	                /*27: True Citra (Spica exactly at 0 Libra) */
    new AyaInit(0, 0),	                /*28: True Revati (zeta Psc exactly at 0 Aries) */
    new AyaInit(0, 0),			/*29: True Pushya (delta Cnc exactly a 16 Cancer */
    new AyaInit(0, 0),                     /*30: - */
        };

/*
 * earlier content
 */

  static final double TWOPI = 2.0 * SMath.PI;

//  static final int ENDMARK = -99;

  static final int SEI_EPSILON = -2;
  static final int SEI_NUTATION = -1;
  static final int SEI_EMB = 0;
  static final int SEI_EARTH = 0;
  static final int SEI_SUN = 0;
  static final int SEI_MOON = 1;
  static final int SEI_MERCURY = 2;
  static final int SEI_VENUS = 3;
  static final int SEI_MARS = 4;
  static final int SEI_JUPITER = 5;
  static final int SEI_SATURN = 6;
  static final int SEI_URANUS = 7;
  static final int SEI_NEPTUNE = 8;
  static final int SEI_PLUTO = 9;
  static final int SEI_SUNBARY = 10;     // barycentric sun
  static final int SEI_ANYBODY = 11;     // any asteroid
  static final int SEI_CHIRON = 12;
  static final int SEI_PHOLUS = 13;
  static final int SEI_CERES = 14;
  static final int SEI_PALLAS = 15;
  static final int SEI_JUNO = 16;
  static final int SEI_VESTA = 17;

  static final int SEI_NPLANETS = 18;

  static final int SEI_MEAN_NODE = 0;
  static final int SEI_TRUE_NODE = 1;
  static final int SEI_MEAN_APOG = 2;
  static final int SEI_OSCU_APOG = 3;
  static final int SEI_INTP_APOG = 4;
  static final int SEI_INTP_PERG = 5;

  static final int SEI_NNODE_ETC = 6;

  static final int SEI_FLG_HELIO = 1;
  static final int SEI_FLG_ROTATE = 2;
  static final int SEI_FLG_ELLIPSE = 4;
  static final int SEI_FLG_EMBHEL = 8; // TRUE, if heliocentric earth is given
                                     // instead of barycentric sun
                                     // i.e. bary sun is computed from
                                     // barycentric and heliocentric earth

  static final int SEI_FILE_PLANET = 0;
  static final int SEI_FILE_MOON = 1;
  static final int SEI_FILE_MAIN_AST = 2;
  static final int SEI_FILE_ANY_AST = 3;
  static final int SEI_FILE_FIXSTAR = 4;

  // Aus swephexph.h:
  static final int SEI_FILE_TEST_ENDIAN = 0x616263;   // abc
  static final int SEI_FILE_BIGENDIAN = 0;
  static final int SEI_FILE_NOREORD = 0;
  static final int SEI_FILE_LITENDIAN = 1;
  static final int SEI_FILE_REORD = 2;

  static final int SEI_FILE_NMAXPLAN = 50;
  static final int SEI_FILE_EFPOSBEGIN = 500;

  static final String SE_FILE_SUFFIX = "se1";

  static final int SEI_NEPHFILES = 7;
  static final int SEI_CURR_FPOS = -1;
  static final int SEI_NMODELS = 20;

  static final double SEI_ECL_GEOALT_MAX =  25000.0;
  static final double SEI_ECL_GEOALT_MIN =  (-500.0);

/* Chiron's orbit becomes chaotic
 * before 720 AD and after 4606 AD, because of close encounters
 * with Saturn. Accepting a maximum error of 5 degrees,
 * the ephemeris is good between the following dates:
 */
  /*static final double CHIRON_START = 1958470.5;      * 1.1.650 old limit until v. 2.00 */
  static final double CHIRON_START = 1967601.5;  	/* 1.1.675 */
  static final double CHIRON_END = 3419437.5;        // 1.1.4650

/* Pholus's orbit is unstable as well, because he sometimes
 * approaches Saturn.
 * Accepting a maximum error of 5 degrees,
 * the ephemeris is good after the following date:
 */
  /* static final double PHOLUS_START = 314845.5;       * 1.1.-3850  old limit until v. 2.00 */
  static final double PHOLUS_START = 640648.5;	/* 1.1.-2958 jul */
  static final double PHOLUS_END =   4390617.5;  	/* 1.1.7309 */

  static final double MOSHPLEPH_START =  625000.5;
  static final double MOSHPLEPH_END =   2818000.5;
  static final double MOSHLUEPH_START =  625000.5;
  static final double MOSHLUEPH_END =   2818000.5;
  /* static final double MOSHNDEPH_START = -254900.5; // 14 Feb -5410 00:00 ET jul.cal.*/
  /* static final double MOSHNDEPH_END =   3697000.5; // 11 Dec 5409 00:00 ET, greg. cal. */
  static final double MOSHNDEPH_START = -3100015.5; // 15 Aug -13200 00:00 ET jul.cal.*/
  static final double MOSHNDEPH_END =   8000016.5; // 15 Mar 17191 00:00 ET, greg. cal */
/*
*/
static final double JPL_DE431_START = -3027215.5;
static final double JPL_DE431_END   =  7930192.5;

  static final int MAXORD = 40;

  static final double NCTIES = 6.0;    // number of centuries per eph. file

  static final int NOT_AVAILABLE = -2;
  static final int BEYOND_EPH_LIMITS = -3;

  static final int J_TO_J2000 = 1;
  static final int J2000_TO_J = -1;


  // we always use Astronomical Almanac constants, if available
  static final double MOON_MEAN_DIST = 384400000.0;           // in m, AA 1996, F2
  static final double MOON_MEAN_INCL = 5.1453964;             // AA 1996, D2
  static final double MOON_MEAN_ECC = 0.054900489;            // AA 1996, F2
  // static final double SUN_EARTH_MRAT = 328900.561400;         Su/(Ea+Mo) AA 2006 K7
  static final double SUN_EARTH_MRAT = 332946.050895;         // Su / (Ea only) AA 2006 K7
  static final double EARTH_MOON_MRAT = (1 / 0.0123000383);   // AA 2006, K7
  static final double AUNIT = 1.49597870691e+11;              // au in meters, AA 2006 K6
  static final double CLIGHT = 2.99792458e+8;                 // m/s, AA 1996 K6
  static final double HELGRAVCONST = 1.32712440017987e+20;    // G * M(sun), m^3/sec^2, AA 2006 K6
  static final double GEOGCONST = 3.98600448e+14; // G * M(earth) m^3/sec^2, AA 1996 K6
  static final double KGAUSS = 0.01720209895; // Gaussian gravitational constant K6
  static final double KGAUSS_GEO = 0.0000298122353216;        // Earth only
  // static final double KGAUSS_GEO = 0.0000299502129737        // Earth + Moon
  static final double SUN_RADIUS = 959.63 / 3600 * SwissData.DEGTORAD;  // Meeus germ. p 391
  static final double EARTH_RADIUS = 6378136.6;               // AA 2006 K6
  /*static final double EARTH_OBLATENESS = (1.0/ 298.257223563); * AA 1998 K13 */
  static final double EARTH_OBLATENESS = (1.0/ 298.25642);    // AA 2006 K6
  static final double EARTH_ROT_SPEED = 7.2921151467e-5 * 86400; // in rad/day, expl. suppl., p 162

  static final double LIGHTTIME_AUNIT = (499.0047838061/3600/24); // 8.3167 minutes (days), AA 2006 K6

  /* node of ecliptic measured on ecliptic 2000 */
  static final double SSY_PLANE_NODE_E2000 = 107.582569 * SwissData.DEGTORAD;
  /* node of ecliptic measured on solar system rotation plane */
  static final double SSY_PLANE_NODE = 107.58883388 * SwissData.DEGTORAD;
  /* inclination of ecliptic against solar system rotation plane */
  static final double SSY_PLANE_INCL = 1.578701 * SwissData.DEGTORAD;

  static final double KM_S_TO_AU_CTY = 21.095;           // km/s to AU/century
  static final double MOON_SPEED_INTV = 0.00005;         // 4.32 seconds (in days)
  static final double PLAN_SPEED_INTV = 0.0001;          // 8.64 seconds (in days)
  static final double MEAN_NODE_SPEED_INTV = 0.001;
  static final double NODE_CALC_INTV = 0.0001;
  static final double NODE_CALC_INTV_MOSH = 0.1;
  static final double NUT_SPEED_INTV = 0.0001;
  static final double DEFL_SPEED_INTV = 0.0000005;

  static final double SE_LAPSE_RATE = 0.0065;  /* deg K / m, for refraction */


/*
 * stuff exported from swemplan.c and swemmoon.c
 * and constants used inside these functions.
************************************************************/

  static final double STR = 4.8481368110953599359e-6;   // radians per arc second

  /* dpsi and deps loaded for 100 years after 1962 */
  static final int SWE_DATA_DPSI_DEPS = 36525;


  // Aus sweph.c:
  static final int IS_PLANET = 0;
  static final int IS_MOON = 1;
  static final int IS_ANY_BODY = 2;
  static final int IS_MAIN_ASTEROID = 3;

  static final boolean DO_SAVE = true;
  static final boolean NO_SAVE = false;

//  java.io.RandomAccessFile fixfp = null;     // fixed stars


//  static final int pnoext2int[] = {SEI_SUN, SEI_MOON, SEI_MERCURY, SEI_VENUS,
//    SEI_MARS, SEI_JUPITER, SEI_SATURN, SEI_URANUS, SEI_NEPTUNE, SEI_PLUTO,
//    0, 0, 0, 0, SEI_EARTH, SEI_CHIRON, SEI_PHOLUS, SEI_CERES, SEI_PALLAS,
//    SEI_JUNO, SEI_VESTA, };



//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////

  /**
  * Just to inhibit instantiation of this class, which is never necessary.
  */
  private SwephData() { }
}
