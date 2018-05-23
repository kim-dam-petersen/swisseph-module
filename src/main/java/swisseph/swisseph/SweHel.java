/*
   This is a port of the Swiss Ephemeris Free Edition, Version 2.00.00
   of Astrodienst AG, Switzerland from the original C Code to Java. For
   copyright see the original copyright notices below and additional
   copyright notes in the file named LICENSE, or - if this file is not
   available - the copyright notes at http://www.astro.com/swisseph/ and
   following.

   For any questions or comments regarding this port to Java, you should
   ONLY contact me and not Astrodienst, as the Astrodienst AG is not involved
   in this port in any way.

   Thomas Mack, mack@ifis.cs.tu-bs.de, 19th July 2010

*/
/* SWISSEPH 
 $Header: /home/dieter/sweph/RCS/swehel.c,v 1.1 2009/04/21 06:05:59 dieter Exp dieter $

  Heliacal risings and related calculations
  
  Author: Victor Reijs
  This program code is a translation of part of:
  Victor Reijs' software ARCHAEOCOSMO (archaeoastronomy and
  geodesy functions), 
  http://www.iol.ie/~geniet/eng/archaeocosmoprocedures.htm

  Translation from VB into C by Dieter Koch

  Problem reports can be sent to victor.reijs@gmail.com or dieter@astro.ch
  
  Copyright (c) Victor Reijs, 2008

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

  The authors of Swiss Ephemeris have no control or influence over any of
  the derived works, i.e. over software or services created by other
  programmers which use Swiss Ephemeris functions.

  The names of the authors or of the copyright holder must not
  be used for promoting any software, product or service which uses or contains
  the Swiss Ephemeris. This copyright notice is the ONLY place where the
  names of the authors can legally appear, except in cases where they have
  given special permission in writing.

  The trademarks 'Swiss Ephemeris' and 'Swiss Ephemeris inside' may be used
  for promoting such software, products or services.
*/

package swisseph;

import java.util.Locale;


public class SweHel implements java.io.Serializable {

  private SwissEph sw;
  private Swemmoon sm;
  private SwissLib sl;
  private Swecl sc;
  private SwissData swed;

  private static final int PLSV = 0; /*if Planet, Lunar and Stellar Visibility formula is needed PLSV=1*/
  private static final double criticalangle = 0.0; /*[deg]*/
  private static final double BNIGHT        = 1479.0; /*[nL]*/
  private static final double BNIGHT_FACTOR = 1.0;
  // private static final int PI            = M_PI;
  private static final double Min2Deg = (1.0 / 60.0);
  private static final int DONE = 1;
  private static final int MaxTryHours = 4 ;
  private static final int TimeStepDefault = 1;
  private static final int LocalMinStep	= 8;

  /* time constants */
  private static final double Y2D = 365.25; /*[Day]*/
  private static final double D2Y = (1 / Y2D); /*[Year]*/
  private static final double D2H = 24.0; /*[Hour]*/
  private static final double H2S = 3600.0; /*[sec]*/
  private static final double D2S = (D2H * H2S); /*[sec]*/
  private static final double S2H = (1.0 / H2S); /*[Hour]*/
  private static final double JC2D = 36525.0; /*[Day]*/
  private static final double M2S = 60.0; /*[sec]*/

  /* Determines which algorimths are used*/
  private static final boolean USE_DELTA_T_VR = false;
  private static final int REFR_SINCLAIR  = 0;
  private static final int REFR_BENNETTH  = 1;
  private static final int FormAstroRefrac = REFR_SINCLAIR; /*for Astronomical refraction can be "bennetth" or "sinclair"*/
  private static final int GravitySource = 2; /*0=RGO, 1=Wikipedia,2=Exp. Suppl. 1992,3=van der Werf*/
  private static final int REarthSource = 1; /*0=RGO (constant), 1=WGS84 method*/

  private static final int StartYear = 1820; /*[year]*/
  private static final double Average = 1.80546834626888; /*[msec/cy]*/
  private static final double Periodicy = 1443.67123144531; /*[year]*/
  private static final double Amplitude = 3.75606495492684; /*[msec]*/
  private static final double phase = 0; /*[deg]*/	// Unused
  private static final int MAX_COUNT_SYNPER         = 5;  /* search within 10 synodic periods */
  private static final int MAX_COUNT_SYNPER_MAX = 1000000;  /* high, so there is not max count */
  private static final double AvgRadiusMoon = (15.541 / 60); /* '[Deg] at 2007 CE or BCE*/

  /* WGS84 ellipsoid constants
   * http://w3sli.wcape.gov.za/Surveys/Mapping/wgs84.htm*/
  private static final double Ra = 6378136.6;   /*'[m]*/
  private static final double Rb = 6356752.314; /*'[m]*/

  /* choices in Schaefer's model */
  private static final double nL2erg   = (1.02E-15);
  private static final double erg2nL   = (1 / nL2erg); /*erg2nL to nLambert*/
  private static final double MoonDistance    = 384410.4978; /*[km]*/
  private static final double scaleHwater     = 3000.0; /*[m] Ricchiazzi [1997] 8200 Schaefer [2000]*/
  private static final double scaleHrayleigh  =	8515.0; /*[m] Su [2003] 8200 Schaefer [2000]*/
  private static final double scaleHaerosol   =  3745.0; /*m Su [2003] 1500 Schaefer [2000]*/
  private static final double scaleHozone     = 20000.0; /*[m] Schaefer [2000]*/
  private static final double astr2tau = 0.921034037197618;  /*LN(10 ^ 0.4)*/
  private static final double tau2astr = 1 / astr2tau;

  /* meteorological constants*/
  private static final double C2K = 273.15; /*[K]*/
  private static final double DELTA = 18.36;
  private static final double TempNulDiff = 0.000001;
  private static final double PressRef = 1000; /*[mbar]*/	// Unused
  private static final double MD = 28.964; /*[kg] Mol weight of dry air van der Werf*/
  private static final double MW = 18.016; /*[kg] Mol weight of water vapor*/
  private static final double GCR = 8314.472; /*[L/kmol/K] van der Werf*/
  private static final double LapseSA = 0.0065; /*[K/m] standard atmosphere*/
  private static final double LapseDA = 0.0098; /*[K/m] dry adiabatic*/

  /* lowest apparent altitude to provide*/
  private static final double LowestAppAlt = -3.5; /*[Deg]*/

  /*optimization delta*/
  private static final double epsilon = 0.001;
  /* for Airmass usage*/
  private static final int staticAirmass = 0; /* use staticAirmass=1 instead depending on difference k's*/

  /* optic stuff */
  private static final double GOpticMag = 1; /*telescope magnification*/	// Unused
  private static final double GOpticTrans = 0.8; /*telescope transmission*/	// Unused
  private static final double GBinocular = 1; /*1-binocular 0=monocular*/	// Unused
  private static final double GOpticDia = 50; /*telescope diameter [mm]*/	// Unused


  public SweHel() {
    this(null, null, null, null);
  }
  public SweHel(SwissEph sw, SwissLib sl, Swemmoon sm, SwissData swed) {
    this.sw=sw;
    this.sl=sl;
    this.sm=sm;
    this.swed=swed;
    if (sw==null) { this.sw=new SwissEph(); }
    if (sl==null) { this.sl=new SwissLib(); }
    if (sm==null) { this.sm=new Swemmoon(); }
    if (swed==null) { this.swed=new SwissData(); }

    this.sc = new Swecl(this.sw, this.sl, this.sm, this.swed);
  }

  /*###################################################################*/
  private double Tanh(double x) {
    return (SMath.exp(x) - SMath.exp(-x)) / (SMath.exp(x) + SMath.exp(-x));
  }

  /*
  ' B [nL]
  ' SN [-]
  ' CVA [deg]
  */
  private double CVA(double B, double SN) {
    /*Schaefer, Astronomy and the limits of vision, Archaeoastronomy, 1993*/
    if (B > BNIGHT)
      return (40.0 / SN) * SMath.pow(10, (8.28 * SMath.pow(B, (-0.29)))) / 60.0 / 60.0;
    else
      return SMath.min(900, 380 / SN * SMath.pow(10, (0.3 * SMath.pow(B, (-0.29))))) / 60.0 / 60.0;
  }

  /*
  ' age [year]
  ' B [nL]
  ' PupilDia [mm]
  */
  private double PupilDia(double Age, double B) {
    /* age dependancy from Garstang [2000]*/
    return (0.534 - 0.00211 * Age - (0.236 - 0.00127 * Age) * Tanh(0.4 * SMath.log(B) / SMath.log(10) - 2.2)) * 10;
  }

  /*
  'Input
  ' Bback [nL]
  ' kX [-]
  ' Binocular [-]
  ' OpticMag [-]
  ' OpticDia [mm]
  ' OpticTrans [-]
  ' JDNDaysUT [JDN]
  ' Age [Year]
  ' SN [-]
  ' ObjectName
  ' TypeFactor [0=itensity factor 1=background factor]
  'Output
  ' OpticFactor [-]
  */
  private double OpticFactor(double Bback,
      double kX,
      double[] dobs,
      double JDNDaysUT,
      String ObjectName /* unused parameter */,
      int TypeFactor,
      int helflag) {
    double Pst, CIb, CIi, ObjectSize, Fb, Fe, Fsc, Fci, Fcb, Ft, Fp, Fa, Fr, Fm;
    double Age = dobs[0];
    double SN = dobs[1], SNi;
    double Binocular = dobs[2];
    double OpticMag = dobs[3];
    double OpticDia = dobs[4];
    double OpticTrans = dobs[5];
    SNi = SN;
    if (SNi <= 0.00000001) SNi = 0.00000001;
    /* 23 jaar as standard from Garstang*/
    Pst = PupilDia(23, Bback);
    if (OpticMag == 1) { /*OpticMagn=1 means using eye*/
       OpticTrans = 1;
       OpticDia = Pst;
    }
    /* Schaefer, Astronomy and the limits of vision, Archaeoastronomy, 1993*/
    CIb = 0.7; /* color of background (from Ben Sugerman)*/
    CIi = 0.5; /* Color index for white (from Ben Sugerman), should be function of ObjectName*/
    ObjectSize = 0;
    if (ObjectName.equals("moon")) {
      /*ObjectSize and CI needs to be determined (depending on JDNDaysUT)*/
      ;
    }
    Fb = 1;
    if (Binocular == 0) Fb = 1.41;
    if (Bback <= BNIGHT && (helflag & SweConst.SE_HELFLAG_VISLIM_PHOTOPIC) == 0) {
      Fe = SMath.pow(10, (0.48 * kX));
      Fsc = SMath.min(1, (1 - SMath.pow(Pst / 124.4, 4)) / (1 - SMath.pow((OpticDia / OpticMag / 124.4), 4)));
      Fci = SMath.pow(10, (-0.4 * (1 - CIi / 2.0)));
      Fcb = SMath.pow(10, (-0.4 * (1 - CIb / 2.0)));
    } else {
       Fe = SMath.pow(10, (0.4 * kX));
       Fsc = SMath.min(1, SMath.pow((OpticDia / OpticMag / Pst), 2) * (1 - SMath.exp(-SMath.pow((Pst / 6.2), 2))) / (1 - SMath.exp(-SMath.pow((OpticDia / OpticMag / 6.2), 2))));
       Fci = 1;
       Fcb = 1;
    }
    Ft = 1 / OpticTrans;
    Fp = SMath.max(1, SMath.pow((Pst / (OpticMag * PupilDia(Age, Bback))), 2));
    Fa = SMath.pow((Pst / OpticDia), 2);
    Fr = (1 + 0.03 * SMath.pow((OpticMag * ObjectSize / CVA(Bback, SNi)), 2)) / SMath.pow(SNi, 2);
    Fm = SMath.pow(OpticMag, 2);
    if (TypeFactor == 0) 
      return Fb * Fe * Ft * Fp * Fa * Fr * Fsc * Fci;
    else
      return Fb * Ft * Fp * Fa * Fm * Fsc * Fcb;
}

/*###################################################################
*/
  private int DeterObject(StringBuilder ObjectName) {
    return DeterObject(ObjectName.toString());
  }
  private int DeterObject(String ObjectName) {
    String s = ObjectName.toLowerCase();
    int ipl = -1;
    if (s.startsWith("sun"))
      return SweConst.SE_SUN;
    if (s.startsWith("venus"))
      return SweConst.SE_VENUS;
    if (s.startsWith("mars"))
      return SweConst.SE_MARS;
    if (s.startsWith("mercur"))
      return SweConst.SE_MERCURY;
    if (s.startsWith("jupiter"))
      return SweConst.SE_JUPITER;
    if (s.startsWith("saturn"))
      return SweConst.SE_SATURN;
    if (s.startsWith("uranus"))
      return SweConst.SE_URANUS;
    if (s.startsWith("neptun"))
      return SweConst.SE_NEPTUNE;
    if (s.startsWith("moon"))
      return SweConst.SE_MOON;
    if ((ipl = SwissLib.atoi(s)) > 0) {
      ipl += SweConst.SE_AST_OFFSET;
    return ipl;
    }
    return -1;
  }


  /* avoids problems with star name string that may be overwritten by 
     swe_fixstar() */
  int call_swe_fixstar(StringBuilder star, double tjd, int iflag, double[] xx, StringBuilder serr) {
    int retval;
    StringBuilder star2 = new StringBuilder(star);
    retval =  sw.swe_fixstar(star2, tjd, iflag, xx, serr);
    return retval;
  }

  /* avoids problems with star name string that may be overwritten by 
     swe_fixstar_mag() */
// (C-)static variables for this method:
double call_swe_fixstar_mag__dmag[] = new double[] {0};
StringBuilder call_swe_fixstar_mag__star_save = new StringBuilder();

  int call_swe_fixstar_mag(StringBuilder star, double mag[], int mag_offset, StringBuilder serr) {
    int retval;
    StringBuilder star2 = new StringBuilder();
    if (star.equals(call_swe_fixstar_mag__star_save)) {
      mag[mag_offset] = call_swe_fixstar_mag__dmag[0];
      return SweConst.OK;
    }
    call_swe_fixstar_mag__star_save.setLength(0);
    call_swe_fixstar_mag__star_save.append(star);
    star2.setLength(0);
    star2.append(star);
    retval = sw.swe_fixstar_mag(star2, call_swe_fixstar_mag__dmag, serr);
    mag[mag_offset] = call_swe_fixstar_mag__dmag[0];
    return retval;
  }

  /* avoids problems with star name string that may be overwritten by 
     swe_fixstar() */
  int call_swe_rise_trans(double tjd, int ipl, String star, int helflag, int eventtype, double[] dgeo, double atpress, double attemp, double[] tret, StringBuilder serr) {
    int retval;
    int iflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    StringBuilder star2 = new StringBuilder(star);
    DblObj oTret = new DblObj();
    oTret.val = tret[0];
    retval = sc.swe_rise_trans(tjd, ipl, star2, iflag, eventtype, dgeo, atpress, attemp, oTret, serr);
    tret[0] = oTret.val;
    return retval;
  }

  /* 
   * Written by Dieter Koch:
   * Fast function for risings and settings of planets, can be used instead of 
   * swe_rise_trans(), which is much slower.
   * For circumpolar and near-circumpolar planets use swe_rise_trans(), or 
   * generally use it for geographical latitudes higher than 58N/S.
   * For fixed stars, swe_rise_trans() is fast enough.
   */
//private int my_rise_trans(double tjd, int ipl, String starname, int eventtype, int helflag, double *dgeo, double *datm, double *tret, StringBuilder serr)
//my_rise_trans:
//    retc = calc_rise_and_set(tjd, ipl, dgeo, datm, eventtype, helflag, tret, serr);
//    dgeo: double*
//       ist in der Praxis ein Eingabeparameter "dgeo[3] geograph. longitude, latitude, height above sea"
//    datm: double*
//       ist in der Praxis ein Eingabeparameter von "datm[] = new double[] {atpress, attemp}"
//    trise: double* == R?ckgabe eines einzelnen double "double[0] trise"
//  RiseSet:
//    retval = my_rise_trans(JDNDaysUT, Planet, "", eventtype, helflag, dgeo, datm, tret, serr);
//    retval = my_rise_trans(JDNDaysUT, -1, ObjectName, eventtype, helflag, dgeo, datm, tret, serr);
//    dgeo: double*
//    datm: double*
//  heliacal_ut_arc_vis:
//	if ((retval = my_rise_trans(JDNDaysUTstep, SweConst.SE_SUN, "", eventtype, helflag, dgeo, datm, &tret, serr)) == SweConst.ERR)
//    dgeo: double*
//    datm: double*
//  get_heliacal_day:
//    if ((retval = my_rise_trans(tday, SweConst.SE_SUN, "", is_rise_or_set, helflag, dgeo, datm, &tret, serr)) == SweConst.ERR)
//    dgeo: double*
//    datm: double*
//  [get_acronychal_day_new:]
//    - if ((retval = my_rise_trans(tret, 0, ObjectName, SweConst.SE_CALC_RISE, helflag, dgeo, datm, &tret, serr)) == SweConst.ERR)
//    - if ((retval = my_rise_trans(tret, 0, ObjectName, SweConst.SE_CALC_SET, helflag, dgeo, datm, &tret, serr)) == SweConst.ERR)
//    dgeo: double*
//    datm: double*
//  get_acronychal_day:
//    retval = my_rise_trans(tret, SweConst.SE_SUN, "", is_rise_or_set, helflag, dgeo, datm, &tret, serr);
//    dgeo: double*
//    datm: double*
//  moon_event_vis_lim:
//    if ((retval = my_rise_trans(tjd, SweConst.SE_SUN, "", SweConst.SE_CALC_SET, helflag, dgeo, datm, &trise, serr)) == SweConst.ERR)
//    if ((retval = my_rise_trans(dret[1], SweConst.SE_SUN, "", SweConst.SE_CALC_RISE, helflag, dgeo, datm, &trise, serr)) == SweConst.ERR)
//    dgeo: double*
//    datm: double*
  private int calc_rise_and_set(double tjd_start, int ipl, double[]dgeo, double[]datm, int eventflag, int helflag, double[] trise /* double[0] as return parameter */, StringBuilder serr) {
    int retc = SweConst.OK, i;
    double sda, xs[]=new double[6], xx[]=new double[6], xaz[]=new double[6], xaz2[]=new double[6], dfac = 1/365.25;
    double rdi, rh;
    double tjd0 = tjd_start, tjdrise;
    double tjdnoon = (int) tjd0 - dgeo[0] / 15.0 / 24.0;
    int iflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    int epheflag = iflag;
    iflag |= SweConst.SEFLG_EQUATORIAL;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0) 
      iflag |= SweConst.SEFLG_NONUT|SweConst.SEFLG_TRUEPOS;
    if (sw.swe_calc_ut(tjd0, SweConst.SE_SUN, iflag, xs, serr) == 0) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("error in calc_rise_and_set(): calc(sun) failed ");
      }
      return SweConst.ERR;
    }
    if (sw.swe_calc_ut(tjd0, ipl, iflag, xx, serr) == 0) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("error in calc_rise_and_set(): calc(sun) failed ");
      }
      return SweConst.ERR;
    }
    tjdnoon -= sl.swe_degnorm(xs[0] - xx[0])/360.0 + 0;
    /* is planet above horizon or below? */
    sc.swe_azalt(tjd0, SweConst.SE_EQU2HOR, dgeo, datm[0], datm[1], xx, xaz);
    if ((eventflag & SweConst.SE_CALC_RISE) != 0) {
      if (xaz[2] > 0) {
        while (tjdnoon - tjd0 < 0.5) {/*printf("e");*/tjdnoon += 1;}
        while (tjdnoon - tjd0 > 1.5) {/*printf("f");*/tjdnoon -= 1;}
      } else {
        while (tjdnoon - tjd0 < 0.0) {/*printf("g");*/tjdnoon += 1;}
        while (tjdnoon - tjd0 > 1.0) {/*printf("h");*/tjdnoon -= 1;}
      }
    } else {
      if (xaz[2] > 0) {
        while (tjd0 - tjdnoon > 0.5) {/*printf("a");*/ tjdnoon += 1;}
        while (tjd0 - tjdnoon < -0.5) {/*printf("b");*/ tjdnoon -= 1;}
      } else {
        while (tjd0 - tjdnoon > 0.0) {/*printf("c");*/ tjdnoon += 1;}
        while (tjd0 - tjdnoon < -1.0) {/*printf("d");*/ tjdnoon -= 1;}
      }
    }
    /* position of planet */
    if (sw.swe_calc_ut(tjdnoon, ipl, iflag, xx, serr) == SweConst.ERR) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("error in calc_rise_and_set(): calc(sun) failed ");
      }
      return SweConst.ERR;
    }
    /* apparent radius of solar disk (ignoring refraction) */
    rdi = SMath.asin(696000000.0 / 1.49597870691e+11 / xx[2]) / SwissData.DEGTORAD;
    if ((eventflag & SweConst.SE_BIT_DISC_CENTER) != 0)
      rdi = 0;
    /* true altitude of sun, when it appears at the horizon */
    /* refraction for a body visible at the horizon at 0m above sea,
     * atmospheric temperature 10 deg C, atmospheric pressure 1013.25 is 34.5 arcmin*/
    rh = -(34.5 / 60.0 + rdi);
    /* semidiurnal arc of sun */
    sda = SMath.acos(-SMath.tan(dgeo[1] * SwissData.DEGTORAD) * SMath.tan(xx[1] * SwissData.DEGTORAD)) * SwissData.RADTODEG;
    /* rough rising and setting times */
    if ((eventflag & SweConst.SE_CALC_RISE) != 0)
      tjdrise = tjdnoon - sda / 360.0;
    else
      tjdrise = tjdnoon + sda / 360.0;
    /*ph->tset = tjd_start + sda / 360.0;*/
    /* now calculate more accurate rising and setting times.
     * use vertical speed in order to determine crossing of the horizon  
     * refraction of 34' and solar disk diameter of 16' = 50' = 0.84 deg */
    iflag = epheflag|SweConst.SEFLG_SPEED|SweConst.SEFLG_EQUATORIAL;
    if (ipl == SweConst.SE_MOON)
      iflag |= SweConst.SEFLG_TOPOCTR;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0) 
      iflag |= SweConst.SEFLG_NONUT|SweConst.SEFLG_TRUEPOS;
    for (i = 0; i < 2; i++) {
      if (sw.swe_calc_ut(tjdrise, ipl, iflag, xx, serr) == SweConst.ERR) {
        /*fprintf(stderr, "hev4 tjd=%f, ipl=%d, iflag=%d\n", tjdrise, ipl, iflag);*/
        return SweConst.ERR;
      }
      sc.swe_azalt(tjdrise, SweConst.SE_EQU2HOR, dgeo, datm[0], datm[1], xx, xaz);
      xx[0] -= xx[3] * dfac; 
      xx[1] -= xx[4] * dfac;
      sc.swe_azalt(tjdrise - dfac, SweConst.SE_EQU2HOR, dgeo, datm[0], datm[1], xx, xaz2);
      tjdrise -= (xaz[1] - rh) / (xaz[1] - xaz2[1]) *  dfac;
      /*fprintf(stderr, "%f\n", ph->trise);*/
    }
    trise[0] = tjdrise;
    return retc;
  }

  private int my_rise_trans(double tjd, int ipl, String starname, int eventtype, int helflag, double[] dgeo, double[] datm, double[] tret, StringBuilder serr) {
    int retc = SweConst.OK;
    if (starname != null && !"".equals(starname))
      ipl = DeterObject(starname);
    /* for non-circumpolar planets we can use a faster algorithm */
    /*if (!(helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) && ipl != -1 && SMath.abs(dgeo[1]) < 58) {*/
    if (ipl != -1 && SMath.abs(dgeo[1]) < 63) {
      retc = calc_rise_and_set(tjd, ipl, dgeo, datm, eventtype, helflag, tret, serr);
    /* for stars and circumpolar planets we use a rigorous algorithm */
    } else {
      retc = call_swe_rise_trans(tjd, ipl, starname, helflag, eventtype, dgeo, datm[0], datm[1], tret, serr);
    }
/*  printf("%f, %f\n", tjd, *tret);*/
    return retc;
  }

  /*###################################################################
  ' JDNDaysUT [Days]
  ' dgeo [array: longitude, latitude, eye height above sea m]
  ' TempE [C]
  ' PresE [mbar]
  ' ObjectName (string)
  ' RSEvent (1=rise, 2=set,3=up transit,4=down transit)
  ' Rim [0=center,1=top]
  ' RiseSet [Day]
  */
  private int RiseSet(double JDNDaysUT, double[] dgeo, double[] datm, String ObjectName, int RSEvent, int helflag, int Rim, double[] tret /* double[0] as output parameter */, StringBuilder serr) {
    int eventtype = RSEvent, Planet, retval;
    if (Rim == 0)
      eventtype |= SweConst.SE_BIT_DISC_CENTER;
    Planet = DeterObject(ObjectName);
    if (Planet != -1)
      retval = my_rise_trans(JDNDaysUT, Planet, "", eventtype, helflag, dgeo, datm, tret, serr);
    else
      retval = my_rise_trans(JDNDaysUT, -1, ObjectName, eventtype, helflag, dgeo, datm, tret, serr);
    return retval;
  }

private double sunRA_tjdlast;
private double sunRA_ralast;
  /*###################################################################
  ' JDNDaysUT [Days]
  ' actual [0= approximation, 1=actual]
  ' SunRA [deg]
  */
  private double SunRA(double JDNDaysUT, int helflag, StringBuilder serr) {
    int imon, iday, iyar;
    boolean calflag = SweDate.SE_GREG_CAL;
    double dut;
    if (JDNDaysUT == sunRA_tjdlast)
      return sunRA_ralast;
    //JAVA: swe_revjul(JDNDaysUT, calflag, &iyar, &imon, &iday, &dut); /* this seems to be much faster than calling swe_revjul() ! Note: only because SunRA is called 1000s of times */
    SweDate sd = new SweDate(JDNDaysUT, calflag);
    imon = sd.getMonth();
    iday = sd.getDay();
    sunRA_tjdlast = JDNDaysUT;
    sunRA_ralast = sl.swe_degnorm((imon + (iday - 1) / 30.4 - 3.69) * 30);
    /*sunRA_ralast = (DatefromJDut(JDNDaysUT, 2) + (DatefromJDut(JDNDaysUT, 3) - 1) / 30.4 - 3.69) * 30;*/
    return sunRA_ralast;
  }

  /*###################################################################
  ' Temp [C]
  ' Kelvin [K]
  */
  private double Kelvin(double Temp) {
    /*' http://en.wikipedia.org/wiki/Kelvin*/
    return Temp + C2K;
  }

  /*###################################################################
  ' AppAlt [deg]
  ' TempE [C]
  ' PresE [mbar]
  ' TopoAltitudefromAppAlt [deg]
  */
  private double TopoAltfromAppAlt(double AppAlt, double TempE, double PresE) {
    double R = 0;
    double retalt = 0;
    if (AppAlt >= LowestAppAlt) {
      if (AppAlt > 17.904104638432)
        R = 0.97 / SMath.tan(AppAlt * SwissData.DEGTORAD);
      else
        R = (34.46 + 4.23 * AppAlt + 0.004 * AppAlt * AppAlt) / (1 + 0.505 * AppAlt + 0.0845 * AppAlt * AppAlt);
      R = (PresE - 80) / 930 / (1 + 0.00008 * (R + 39) * (TempE - 10)) * R;
      retalt = AppAlt - R * Min2Deg;
    } else {
      retalt = AppAlt;
    }
    return retalt;
  }

  /*###################################################################
  ' TopoAlt [deg]
  ' TempE [C]
  ' PresE [mbar]
  ' AppAltfromTopoAlt [deg]
  ' call this instead of swe_azalt(), because it is faster (lower precision
  ' is required)
  */
  private double AppAltfromTopoAlt(double TopoAlt, double TempE, double PresE, int helflag) {
    /* using methodology of Newtown derivatives (analogue to what Swiss Emphemeris uses)*/
    int i, nloop = 2;
    double newAppAlt = TopoAlt;
    double newTopoAlt = 0.0;
    double oudAppAlt = newAppAlt;
    double oudTopoAlt = newTopoAlt;
    double verschil, retalt;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) != 0)
      nloop = 5;
    for (i = 0; i <= nloop; i++) {
      newTopoAlt = newAppAlt - TopoAltfromAppAlt(newAppAlt, TempE, PresE);
      /*newTopoAlt = newAppAlt - swe_refrac(newAppAlt, PresE, TempE, SE_CALC_APP_TO_TRUE);*/
      verschil = newAppAlt - oudAppAlt;
      oudAppAlt = newTopoAlt - oudTopoAlt - verschil;
      if ((verschil != 0) && (oudAppAlt != 0))
        verschil = newAppAlt - verschil * (TopoAlt + newTopoAlt - newAppAlt) / oudAppAlt;
      else
        verschil = TopoAlt + newTopoAlt;
      oudAppAlt = newAppAlt;
      oudTopoAlt = newTopoAlt;
      newAppAlt = verschil;
    }
    retalt = TopoAlt + newTopoAlt;
    if (retalt < LowestAppAlt)
      retalt = TopoAlt;
    return retalt;
  }

  /*###################################################################
  ' TopoAlt [deg]
  ' TopoDecl [deg]
  ' Lat [deg]
  ' HourAngle [hour]
  */
  private double HourAngle(double TopoAlt, double TopoDecl, double Lat) {
    double Alti = TopoAlt * SwissData.DEGTORAD;
    double decli = TopoDecl * SwissData.DEGTORAD;
    double Lati = Lat * SwissData.DEGTORAD;
    double ha = (SMath.sin(Alti) - SMath.sin(Lati) * SMath.sin(decli)) / SMath.cos(Lati) / SMath.cos(decli);
    if (ha < -1) ha = -1; 
    if (ha > 1) ha = 1;
    /* from http://star-www.st-and.ac.uk/~fv/webnotes/chapt12.htm*/
    return SMath.acos(ha) / SwissData.DEGTORAD / 15.0;
  }

  /*###################################################################
  ' JDNDays [Days]
  ' COD [msec/cy]
  ' DeltaTSE [Sec]
  */
  private double DeltaTSE(double JDNDays, int COD) {
    double OffSetYear;
    boolean gregflag = SweDate.SE_GREG_CAL;
    if (StartYear < 1583)
      gregflag = SweDate.SE_JUL_CAL;
    /* from Swiss Emphemeris */
    if (COD != 0) {
      /* Determined by V. Reijs*/
      OffSetYear = (SweDate.getJulDay(StartYear, 1, 1, 0, gregflag) - JDNDays) / 365.25;
      return (OffSetYear * OffSetYear / 100.0 / 2.0 * COD * Y2D) / 1000.0;
    }
    return SweDate.getDeltaT(JDNDays) * D2S;
  }

  /*###################################################################
  ' JDNDays [Day]
  ' COD [msec/cy]
  ' DeltaTVR [Sec]
  */
  private double DeltaTVR(double JDNDays, int COD) {
    /* Determined by V. Reijs */
    double DeltaTVR;
    boolean gregflag = SweDate.SE_GREG_CAL;
    double OffSetYear;
    if (StartYear < 1583)
      gregflag = SweDate.SE_JUL_CAL;
    OffSetYear = (SweDate.getJulDay(StartYear, 1, 1, 0, gregflag) - JDNDays) / 365.25;
    if (COD == 0) {
      DeltaTVR = (OffSetYear * OffSetYear / 100.0 / 2.0 * Average + Periodicy / 2.0 / SMath.PI * Amplitude * (SMath.cos((2 * SMath.PI * OffSetYear / Periodicy)) - 1)) * Y2D;
    } else {
      DeltaTVR = OffSetYear * OffSetYear / 100.0 / 2.0 * COD * Y2D;
    }
    return DeltaTVR / 1000.0;
  }

  /*###################################################################
  ' JDNDays [Days]
  ' COD [msec/cy]
  ' DeltaT [Sec]
  */
  private double DeltaT(double JDNDays, int COD) {
    if (USE_DELTA_T_VR)
      return DeltaTVR(JDNDays, COD);
    return DeltaTSE(JDNDays, COD);
  }

  /*###################################################################
  ' JDNDaysUT [Days]
  ' dgeo [array: longitude, latitude, eye height above sea m]
  ' TempE [C]
  ' PresE [mbar]
  ' ObjectName [-]
  ' Angle (0 = TopoAlt, 1 = Azi, 2=Topo Declination, 3=Topo Rectascension, 4=AppAlt,5=Geo Declination, 6=Geo Rectascension)
  ' ObjectLoc [deg]
   */
// dret = single double output value
  private int ObjectLoc(double JDNDaysUT, double[] dgeo, double[] datm, String ObjectName, int Angle, int helflag, double[] dret, StringBuilder serr) {
    return ObjectLoc(JDNDaysUT, dgeo, datm, new StringBuilder(ObjectName), Angle, helflag, dret, serr);
  }
  private int ObjectLoc(double JDNDaysUT, double[] dgeo, double[] datm, StringBuilder ObjectName, int Angle, int helflag, double[] dret, StringBuilder serr) {
    double x[]=new double[6], xin[]=new double[3], xaz[]=new double[3], tjd_tt;
    int Planet;
    int epheflag;
    int iflag = SweConst.SEFLG_EQUATORIAL;
    epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    iflag |= epheflag;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0)
      iflag |= SweConst.SEFLG_NONUT | SweConst.SEFLG_TRUEPOS;
    if (Angle < 5) iflag = iflag | SweConst.SEFLG_TOPOCTR;
    if (Angle == 7) Angle = 0;
    tjd_tt = JDNDaysUT + DeltaT(JDNDaysUT, 0) / D2S;
    Planet = DeterObject(ObjectName);
    if (Planet != -1) {
      if (sw.swe_calc(tjd_tt, Planet, iflag, x, serr) == SweConst.ERR)
        return SweConst.ERR;
    } else {
      if (call_swe_fixstar(ObjectName, tjd_tt, iflag, x, serr) == SweConst.ERR)
        return SweConst.ERR;
    }
    if (Angle == 2 ||  Angle == 5) {
      dret[0] = x[1];
    } else {
      if (Angle == 3 || Angle == 6) {
        dret[0] = x[0];
      } else {
        xin[0] = x[0];
        xin[1] = x[1];
        sc.swe_azalt(JDNDaysUT, SweConst.SE_EQU2HOR, dgeo, datm[0], datm[1], xin, xaz);
        if (Angle == 0)
          dret[0] = xaz[1];
        if (Angle == 4)
          dret[0] = AppAltfromTopoAlt(xaz[1], datm[0], datm[1], helflag);
        if (Angle == 1) {
          xaz[0] += 180;
          if (xaz[0] >= 360)
            xaz[0] -= 360;
          dret[0] = xaz[0];
        }
      }
    }
    return SweConst.OK;
  }

  /*###################################################################
  ' JDNDaysUT [Days]
  ' dgeo [array: longitude, latitude, eye height above sea m]
  ' TempE [C]
  ' PresE [mbar]
  ' ObjectName [-]
  ' Angle (0 = TopoAlt, 1 = Azi, 2=Topo Declination, 3=Topo Rectascension, 4=AppAlt,5=Geo Declination, 6=Geo Rectascension)
  ' ObjectLoc [deg]
   */
  private int azalt_cart(double JDNDaysUT, double[] dgeo, double[] datm, StringBuilder ObjectName, int helflag, double[] dret, StringBuilder serr) {
    return azalt_cart(JDNDaysUT, dgeo, datm, ObjectName, helflag, dret, 0, serr);
  }
  private int azalt_cart(double JDNDaysUT, double[] dgeo, double[] datm, StringBuilder ObjectName, int helflag, double[] dret, int dret_offset, StringBuilder serr) {
    double x[] = new double[6], xin[] = new double[3], xaz[] = new double[3], tjd_tt;
    int Planet;
    int epheflag;
    int iflag = SweConst.SEFLG_EQUATORIAL;
    epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    iflag |= epheflag;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0)
      iflag |= SweConst.SEFLG_NONUT | SweConst.SEFLG_TRUEPOS;
    iflag = iflag | SweConst.SEFLG_TOPOCTR;
    tjd_tt = JDNDaysUT + DeltaT(JDNDaysUT, 0) / D2S;
    Planet = DeterObject(ObjectName);
    if (Planet != -1) {
      if (sw.swe_calc(tjd_tt, Planet, iflag, x, serr) == SweConst.ERR)
      return SweConst.ERR;
    } else {
      if (call_swe_fixstar(ObjectName, tjd_tt, iflag, x, serr) == SweConst.ERR)
        return SweConst.ERR;
    }
    xin[0] = x[0];
    xin[1] = x[1];
    sc.swe_azalt(JDNDaysUT, SweConst.SE_EQU2HOR, dgeo, datm[0], datm[1], xin, xaz);
    dret[dret_offset] = xaz[0];
    dret[1+dret_offset] = xaz[1]; /* true altitude */
    dret[2+dret_offset] = xaz[2]; /* apparent altitude */
    /* also return cartesian coordinates, for apparent altitude */
    xaz[1] = xaz[2];
    xaz[2] = 1;
    sl.swi_polcart(xaz, xaz);
    dret[3+dret_offset] = xaz[0];
    dret[4+dret_offset] = xaz[1];
    dret[5+dret_offset] = xaz[2];
    return SweConst.OK;
  }

  /*###################################################################
  ' LatA [rad]
  ' LongA [rad]
  ' LatB [rad]
  ' LongB [rad]
  ' DistanceAngle [rad]
  */
  private double DistanceAngle(double LatA, double LongA, double LatB, double LongB) {
    double dlon = LongB - LongA;
    double dlat = LatB - LatA;
    /* Haversine formula
     * http://www.movable-type.co.uk/scripts/GIS-FAQ-5.1.html
     * R.W. Sinnott, Virtues of the Haversine, Sky and Telescope, vol. 68, no. 2, 1984, p. 159
     */
    double sindlat2 = SMath.sin(dlat / 2);
    double sindlon2 = SMath.sin(dlon / 2);
    double corde = sindlat2 * sindlat2 + SMath.cos(LatA) * SMath.cos(LatB) * sindlon2 *sindlon2;
    if (corde > 1) corde = 1;
    return 2 * SMath.asin(SMath.sqrt(corde));
  }

  /*###################################################################
  ' heighteye [m]
  ' TempS [C]
  ' RH [%]
  ' kW [-]
  */
  private double kW(double HeightEye, double TempS, double RH) {
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 128*/
    double WT = 0.031;
    WT *= 0.94 * (RH / 100.0) * SMath.exp(TempS / 15) * SMath.exp(-1 * HeightEye / scaleHwater);
    return WT;
  }

  // (C-)static variables for this method:
  private double koz__koz_last, koz__alts_last, koz__sunra_last;
  /*###################################################################
  ' JDNDaysUT [-]
  ' AltS [deg]
  ' lat [deg]
  ' kOZ [-]
  */
  private double kOZ(double AltS, double sunra, double Lat) {
    double CHANGEKO, OZ, LT, kOZret;
    if (AltS == koz__alts_last && sunra == koz__sunra_last)
      return koz__koz_last;
    koz__alts_last = AltS; koz__sunra_last = sunra;
    OZ = 0.031;
    LT = Lat * SwissData.DEGTORAD;
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 128*/
    kOZret = OZ * (3.0 + 0.4 * (LT * SMath.cos(sunra * SwissData.DEGTORAD) - SMath.cos(3 * LT))) / 3.0;
    /* depending on day/night vision (altitude of sun < start astronomical twilight), KO changes from 100% to 30%
     * see extinction section of Vistas in Astronomy page 343*/
    CHANGEKO = (100 - 11.6 * SMath.min(6, SMath.max(-AltS - 12, 0))) / 100;
    koz__koz_last = kOZret * CHANGEKO;
    return koz__koz_last;
  }

  /*###################################################################
  ' AltS [deg]
  ' heighteye [m]
  ' kR [-]
  */
  private double kR(double AltS, double HeightEye) {
    /* depending on day/night vision (altitude of sun < start astronomical twilight),
     * lambda eye sensibility changes
     * see extinction section of Vistas in Astronomy page 343*/
    double CHANGEK, LAMBDA;
    double val = -AltS - 12;
    if (val < 0) val = 0;
    if (val > 6) val = 6;
    /*CHANGEK = (1 - 0.166667 * Min(6, Max(-AltS - 12, 0)));*/
    CHANGEK = (1 - 0.166667 * val );
    LAMBDA = 0.55 + (CHANGEK - 1) * 0.04;
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 128 */
    return 0.1066 * SMath.exp(-1 * HeightEye / scaleHrayleigh) * SMath.pow(LAMBDA / 0.55 , -4);
  }

  private int Sgn(double x) {
    if (x < 0) 
      return -1;
    return 1;
  }

  // (C-)static variables for this method:
  private double ka__alts_last, ka__sunra_last, ka__ka_last;
  /*###################################################################
  ' JDNDaysUT [-]
  ' AltS [deg]
  ' lat [deg]
  ' heighteye [m]
  ' TempS [C]
  ' RH [%]
  ' VR [km]
  ' ka [-]
  */
  private double ka(double AltS, double sunra, double Lat, double HeightEye, double TempS, double RH, double VR, StringBuilder serr) {
    double CHANGEKA, LAMBDA, BetaVr, Betaa, kaact;
    double SL = Sgn(Lat);
    /* depending on day/night vision (altitude of sun < start astronomical twilight),
     * lambda eye sensibility changes
     * see extinction section of Vistas in Astronomy page 343 */
    if (AltS == ka__alts_last && sunra == ka__sunra_last)
      return ka__ka_last;
    ka__alts_last = AltS; ka__sunra_last = sunra;
    CHANGEKA = (1 - 0.166667 * SMath.min(6, SMath.max(-AltS - 12, 0)));
    LAMBDA = 0.55 + (CHANGEKA - 1) * 0.04;
    if (VR != 0) {
      if (VR >= 1) {
        /* Visbility range from http://www1.cs.columbia.edu/CAVE/publications/pdfs/Narasimhan_CVPR03.pdf
         * http://www.icao.int/anb/SG/AMOSSG/meetings/amossg3/wp/SN11Rev.pdf where MOR=2.995/ke
         * factor 1.3 is the relation between "prevailing visibility" and 
         * meteorological range was derived by Koshmeider in the 1920's */
        BetaVr = 3.912 / VR;
        Betaa = BetaVr - (kW(HeightEye, TempS, RH) / scaleHwater + kR(AltS, HeightEye) / scaleHrayleigh) * 1000 * astr2tau;
        kaact = Betaa * scaleHaerosol / 1000 * tau2astr;
        if (kaact < 0) {
          if (serr != null) {
            serr.setLength(0);
            serr.append("The provided Meteorological range is too long, when taking into acount other atmospheric parameters"); /* is a warning */
          }
          /* return 0; * return "#HIGHVR"; */
        }
      } else {
        kaact = VR - kW(HeightEye, TempS, RH) - kR(AltS, HeightEye) - kOZ(AltS, sunra, Lat);
        if (kaact < 0) {
          if (serr != null) {
            serr.setLength(0);
            serr.append("The provided atmosphic coeefficent (ktot) is too low, when taking into acount other atmospheric parameters"); /* is a warning */
          }
          /* return 0; * "#LOWktot"; */
        }
      }
    } else {
      /* From Schaefer , Archaeoastronomy, XV, 2000, page 128 */
      if (RH <= 0.00000001) RH = 0.00000001;
      if (RH >= 99.99999999) RH = 99.99999999;
      kaact = 0.1 * SMath.exp(-1 * HeightEye / scaleHaerosol) * SMath.pow(1 - 0.32 / SMath.log(RH / 100.0), 1.33) * (1 + 0.33 * SL * SMath.sin(sunra * SwissData.DEGTORAD));
      kaact = kaact * SMath.pow(LAMBDA / 0.55, -1.3);
    }
    ka__ka_last = kaact;
    return kaact;
  }

  /*###################################################################
  ' JDNDaysUT [-]
  ' AltS [deg]
  ' lat [deg]
  ' heighteye [m]
  ' TempS [C]
  ' RH [%]
  ' VR [km]
  ' ExtType [0=ka,1=kW,2=kR,3=kOZ,4=ktot]
  ' kt [-]
  */
  private double kt(double AltS, double sunra, double Lat, double HeightEye, double TempS, double RH, double VR, int ExtType, StringBuilder serr) {
    double kRact = 0;
    double kWact = 0;
    double kOZact = 0;
    double kaact = 0;
    if (ExtType == 2 || ExtType == 4)
      kRact = kR(AltS, HeightEye);
    if (ExtType == 1 || ExtType == 4)
      kWact = kW(HeightEye, TempS, RH);
    if (ExtType == 3 || ExtType == 4)
      kOZact = kOZ(AltS, sunra, Lat);
    if (ExtType == 0 || ExtType == 4)
      kaact = ka(AltS, sunra, Lat, HeightEye, TempS, RH, VR, serr);
    if (kaact < 0)
      kaact = 0;
    return kWact + kRact + kOZact + kaact;
  }

  /*###################################################################
  ' AppAlt0 [deg]
  ' PresS [mbar]
  ' Airmass [??]
  */
  private double Airmass(double AppAltO, double Press) {
    double airm, zend;
    zend = (90 - AppAltO) * SwissData.DEGTORAD;
    if (zend > SMath.PI / 2)
      zend = SMath.PI / 2;
    airm = 1 / (SMath.cos(zend) + 0.025 * SMath.exp(-11 * SMath.cos(zend)));
    return Press / 1013 * airm;
  }

  /*###################################################################
  ' scaleH '[m]
  ' zend [rad]
  ' PresS [mbar]
  ' Xext [-]
  */
  private double Xext(double scaleH, double zend, double Press) {
    return Press / 1013.0 / (SMath.cos(zend) + 0.01 * SMath.sqrt(scaleH / 1000.0) * SMath.exp(-30.0 / SMath.sqrt(scaleH / 1000.0) * SMath.cos(zend)));
  }

  /*###################################################################
  ' scaleH '[m]
  ' zend [rad]
  ' PresS [mbar]
  ' Xlay [-]
  */
  private double Xlay(double scaleH, double zend, double Press) {
    /*return Press / 1013.0 / SMath.sqrt(1.0 - SMath.pow(SMath.sin(zend) / (1.0 + (scaleH / Ra)), 2));*/
    double a = SMath.sin(zend) / (1.0 + (scaleH / Ra));
    return Press / 1013.0 / SMath.sqrt(1.0 - a * a);
  }

  /*###################################################################
  ' Meteorological formula
  '###################################################################
  ' TempS [C]
  ' HeightEye [m]
  ' TempEfromTempS [C]
  */
  private double TempEfromTempS(double TempS, double HeightEye, double Lapse) {
    return TempS - Lapse * HeightEye;
  }

  /*###################################################################
  ' TempS [C]
  ' PresS [mbar]
  ' HeightEye [m]
  ' PresEfromPresS [mbar]
  */
  private double PresEfromPresS(double TempS, double Press, double HeightEye) {
    return Press * SMath.exp(-9.80665 * 0.0289644 / (Kelvin(TempS) + 3.25 * HeightEye / 1000) / 8.31441 * HeightEye);
  }

  // (C-)static variables for this method:
  private double Deltam__alts_last, Deltam__alto_last, Deltam__sunra_last, Deltam__deltam_last;
  /*###################################################################
  ' AltO [deg]
  ' JDNDaysUT [-]
  ' AltS [deg]
  ' lat [deg]
  ' heighteye [m]
  ' TempS [C]
  ' PresS [mbar]
  ' RH [%]
  ' VR [km]
  ' Deltam [-]
  */
// *datm = double[], see int swe_heliacal_ut(...);
  private double Deltam(double AltO, double AltS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, StringBuilder serr) {
    double zend, xR, XW, Xa, XOZ;
    double PresE = PresEfromPresS(datm[1], datm[0], HeightEye);
    double TempE = TempEfromTempS(datm[1], HeightEye, LapseSA);
    double AppAltO = AppAltfromTopoAlt(AltO, TempE, PresE, helflag);
    double deltam;
    if (AltS == Deltam__alts_last && AltO == Deltam__alto_last && sunra == Deltam__sunra_last)
      return Deltam__deltam_last;
    Deltam__alts_last = AltS; Deltam__alto_last = AltO; Deltam__sunra_last = sunra;
    if (staticAirmass == 0) {
      zend = (90 - AppAltO) * SwissData.DEGTORAD;
      if (zend > SMath.PI / 2)
        zend = SMath.PI / 2;
      /* From Schaefer , Archaeoastronomy, XV, 2000, page 128*/
      xR = Xext(scaleHrayleigh, zend, datm[0]);
      XW = Xext(scaleHwater, zend, datm[0]);
      Xa = Xext(scaleHaerosol, zend, datm[0]);
      XOZ = Xlay(scaleHozone, zend, datm[0]);
      deltam = kR(AltS, HeightEye) * xR + kt(AltS, sunra, Lat, HeightEye, datm[1], datm[2], datm[3], 0, serr) * Xa + kOZ(AltS, sunra, Lat) * XOZ + kW(HeightEye, datm[1], datm[2]) * XW;
    } else {
      deltam = kt(AltS, sunra, Lat, HeightEye, datm[1], datm[2], datm[3], 4, serr) * Airmass(AppAltO, datm[0]);
    }
    Deltam__deltam_last= deltam;
    return deltam;
  }

  /*###################################################################
  ' AltO [deg]
  ' JDNDaysUT [-]
  ' AltS [deg]
  ' lat [deg]
  ' heighteye [m]
  ' TempS [C]
  ' PresS [mbar]
  ' RH [%]
  ' VR [km]
  ' Bn [nL]
  */
  private double Bn(double AltO, double JDNDayUT, double AltS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, StringBuilder serr) {
    double PresE = PresEfromPresS(datm[1], datm[0], HeightEye);
    double TempE = TempEfromTempS(datm[1], HeightEye, LapseSA);
    double AppAltO = AppAltfromTopoAlt(AltO, TempE, PresE, helflag);
    double zend, YearB, MonthB, DayB, Bna, kX, Bnb;
    double B0 = 0.0000000000001, dut;
    //int iyar, imon, iday;
    /* Below altitude of 10 degrees, the Bn stays the same (see page 343 Vistas in Astronomy) */
    if (AppAltO < 10)
      AppAltO = 10;
    zend = (90 - AppAltO) * SwissData.DEGTORAD;
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 128 and adjusted for sunspot period*/
    /*YearB = DatefromJDut(JDNDayUT, 1);
      MonthB = DatefromJDut(JDNDayUT, 2);
      DayB = DatefromJDut(JDNDayUT, 3);*/
    // swe_revjul(JDNDayUT, SE_GREG_CAL, &iyar, &imon, &iday, &dut); 
    SweDate sd = new SweDate(JDNDayUT, SweDate.SE_GREG_CAL);
    YearB = sd.getYear(); MonthB = sd.getMonth(); DayB = sd.getDay();
    Bna = B0 * (1 + 0.3 * SMath.cos(6.283 * (YearB + ((DayB - 1) / 30.4 + MonthB - 1) / 12 - 1990.33) / 11.1));
    kX = Deltam(AltO, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 129 */
    Bnb = Bna * (0.4 + 0.6 / SMath.sqrt(1 - 0.96 * SMath.pow(SMath.sin(zend), 2))) * SMath.pow(10, -0.4 * kX);
    return SMath.max(Bnb, 0) * erg2nL;
  }

  /*###################################################################
  ' JDNDaysUT [-]
  ' dgeo [array: longitude, latitude, eye height above sea m]
  ' TempE [C]
  ' PresE [mbar]
  ' ObjectName [-]
  ' Magnitude [-]
  */
private int Magnitude(double JDNDaysUT, double[] dgeo, StringBuilder ObjectName, int helflag, double[] dmag, int dmag_offset, StringBuilder serr) {
  double x[]=new double[20];
  int Planet, iflag, epheflag;
  epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
  dmag[dmag_offset] = -99.0;
  Planet = DeterObject(ObjectName);
  iflag = SweConst.SEFLG_TOPOCTR | SweConst.SEFLG_EQUATORIAL | epheflag;
  if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0)
    iflag |= SweConst.SEFLG_NONUT|SweConst.SEFLG_TRUEPOS;
  if (Planet != -1) {
    /**dmag = Phenomena(JDNDaysUT, Lat, Longitude, HeightEye, TempE, PresE, ObjectName, 4);*/
    sw.swe_set_topo(dgeo[0], dgeo[1], dgeo[2]);
    if (sw.swe_pheno_ut(JDNDaysUT, Planet, iflag, x, serr) == SweConst.ERR)
      return SweConst.ERR;
    dmag[dmag_offset] = x[4];
  } else {
    if (call_swe_fixstar_mag(ObjectName, dmag, dmag_offset, serr) == SweConst.ERR)
      return SweConst.ERR;
  }
  return SweConst.OK;
}


  /*###################################################################
  ' dist [km]
  ' phasemoon [-]
  ' MoonsBrightness [-]
  */
  private double MoonsBrightness(double dist, double phasemoon) {
    double log10 = 2.302585092994;
    /*Moon's brightness changes with distance: http://hem.passagen.se/pausch/comp/ppcomp.html#15 */
    return -21.62 + 5 * SMath.log(dist / (Ra / 1000)) / log10 + 0.026 * SMath.abs(phasemoon) + 0.000000004 * SMath.pow(phasemoon, 4);
  }

  /*###################################################################
  ' AltM [deg]
  ' AziM [deg]
  ' AziS [deg]
  ' MoonPhase [deg]
  */
  private double MoonPhase(double AltM, double AziM, double AziS) {
    double AltMi = AltM * SwissData.DEGTORAD;
    double AziMi = AziM * SwissData.DEGTORAD;
    double AziSi = AziS * SwissData.DEGTORAD;
    return 180 - SMath.acos(SMath.cos(AziSi - AziMi) * SMath.cos(AltMi + 0.95 * SwissData.DEGTORAD)) / SwissData.DEGTORAD;
  }

  /*###################################################################
  ' Pressure [mbar]
  */
  private double Bm(double AltO, double AziO, double AltM, double AziM, double AltS, double AziS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, StringBuilder serr) {
    double M0 = -11.05;
    double Bm = 0;
    double RM, kXM, kX, C3, FM, phasemoon, MM;
    if (AltM > -0.26) {
    /* moon only adds light when (partly) above horizon
     * From Schaefer , Archaeoastronomy, XV, 2000, page 129*/
      RM = DistanceAngle(AltO * SwissData.DEGTORAD, AziO * SwissData.DEGTORAD, AltM * SwissData.DEGTORAD, AziM * SwissData.DEGTORAD) / SwissData.DEGTORAD;
      kXM = Deltam(AltM, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
      kX = Deltam(AltO, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
      C3 = SMath.pow(10, -0.4 * kXM);
      FM = (62000000.0) / RM / RM + SMath.pow(10, 6.15 - RM / 40) + SMath.pow(10, 5.36) * (1.06 + SMath.pow(SMath.cos(RM * SwissData.DEGTORAD), 2));
      Bm = FM * C3 + 440000 * (1 - C3);
      phasemoon = MoonPhase(AltM, AziM, AziS);
      MM = MoonsBrightness(MoonDistance, phasemoon);
      Bm = Bm * SMath.pow(10, -0.4 * (MM - M0 + 43.27));
      Bm = Bm * (1 - SMath.pow(10, -0.4 * kX));
    }
    Bm = SMath.max(Bm, 0) * erg2nL;
    return Bm;
  }

  /*###################################################################
  ' Pressure [mbar]
  */
  private double Btwi(double AltO, double AziO, double AltS, double AziS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, StringBuilder serr) {
    double M0 = -11.05;
    double MS = -26.74;
    double PresE = PresEfromPresS(datm[1], datm[0], HeightEye);
    double TempE = TempEfromTempS(datm[1], HeightEye, LapseSA);
    double AppAltO = AppAltfromTopoAlt(AltO, TempE, PresE, helflag);
    double ZendO = 90 - AppAltO;
    double RS = DistanceAngle(AltO * SwissData.DEGTORAD, AziO * SwissData.DEGTORAD, AltS * SwissData.DEGTORAD, AziS * SwissData.DEGTORAD) / SwissData.DEGTORAD;
    double kX = Deltam(AltO, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
    double k = kt(AltS, sunra, Lat, HeightEye, datm[1], datm[2], datm[3], 4, serr);
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 129*/
    double Btwi = SMath.pow(10, -0.4 * (MS - M0 + 32.5 - AltS - (ZendO / (360 * k))));
    Btwi = Btwi * (100 / RS) * (1 - SMath.pow(10, -0.4 * kX));
    Btwi = SMath.max(Btwi, 0) * erg2nL;
    return Btwi;
  }

  /*###################################################################
  ' Pressure [mbar]
  */
  private double Bday(double AltO, double AziO, double AltS, double AziS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, StringBuilder serr) {
    double M0 = -11.05;
    double MS = -26.74;
    double RS = DistanceAngle(AltO * SwissData.DEGTORAD, AziO * SwissData.DEGTORAD, AltS * SwissData.DEGTORAD, AziS * SwissData.DEGTORAD) / SwissData.DEGTORAD;
    double kXS = Deltam(AltS, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
    double kX = Deltam(AltO, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 129*/
    double C4 = SMath.pow(10, -0.4 * kXS);
    double FS = (62000000.0) / RS / RS + SMath.pow(10, (6.15 - RS / 40)) + SMath.pow(10, 5.36) * (1.06 + SMath.pow(SMath.cos(RS * SwissData.DEGTORAD), 2));
    double Bday = FS * C4 + 440000.0 * (1 - C4);
    Bday = Bday * SMath.pow(10, (-0.4 * (MS - M0 + 43.27)));
    Bday = Bday * (1 - SMath.pow(10, -0.4 * kX));
    Bday = SMath.max(Bday, 0) * erg2nL;
    return Bday;
  }

  /*###################################################################
  ' Value [nL]
  ' PresS [mbar]
  ' Bcity [nL]
  */
  private double Bcity(double Value, double Press) {
    double Bcity = Value;
    Bcity = SMath.max(Bcity, 0);
    return Bcity;
  }

  /*###################################################################
  ' Pressure [mbar]
  */
  private double Bsky(double AltO, double AziO, double AltM, double AziM, double JDNDaysUT, double AltS, double AziS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, StringBuilder serr) {
    double Bsky = 0;
    if (AltS < -3) {
      Bsky += Btwi(AltO, AziO, AltS, AziS, sunra, Lat, HeightEye, datm, helflag, serr);
    } else {
      if (AltS > 4) {
        Bsky += Bday(AltO, AziO, AltS, AziS, sunra, Lat, HeightEye, datm, helflag, serr);
      } else {
        Bsky += SMath.min(Bday(AltO, AziO, AltS, AziS, sunra, Lat, HeightEye, datm, helflag, serr), Btwi(AltO, AziO, AltS, AziS, sunra, Lat, HeightEye, datm, helflag, serr));
      }
    }
    /* if max. Bm [1E7] <5% of Bsky don't add Bm*/
    if (Bsky < 200000000.0) 
      Bsky += Bm(AltO, AziO, AltM, AziM, AltS, AziS, sunra, Lat, HeightEye, datm, helflag, serr);
    if (AltS <= 0)
      Bsky += Bcity(0, datm[0]);
    /* if max. Bn [250] <5% of Bsky don't add Bn*/
    if (Bsky < 5000)
      Bsky = Bsky + Bn(AltO, JDNDaysUT, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
    /* if max. Bm [1E7] <5% of Bsky don't add Bm*/
    return Bsky;
  }

  /* default handling:
   * 1. datm (atmospheric conditions):
   * datm consists of 
   *     [0]  atmospheric pressure
   *     [1]  temperature
   *     [2]  relative humidity
   *     [3]  extinction coefficient
   * In order to get default values for [0..2], set datm[0] = 0.
   * Default values for [1-2] are only provided if [0] == 0.
   * [3] defaults outside this function, depending on [0-2].
   * 
   * 2. dobs (observer definition):
   *     [0]  age (default 36)
   *     [1]  Snellen ratio or visual acuity of observer (default 1)
   */
  private void default_heliacal_parameters(double[] datm, double[] dgeo, double[] dobs, int helflag) {
    int i;
    if (datm[0] <= 0) {
      /* estimate atmospheric pressure, according to the
       * International Standard Atmosphere (ISA) */
      datm[0] = 1013.25 * SMath.pow(1 - 0.0065 * dgeo[2] / 288, 5.255);
      /* temperature */
      if (datm[1] == 0)
        datm[1] = 15 - 0.0065 * dgeo[2];
      /* relative humidity, independent of atmospheric pressure and altitude */
      if (datm[2] == 0)
        datm[2] = 40;
      /* note: datm[3] / VR defaults outside this function */
    } else {
    }
    /* age of observer */
    if (dobs[0] == 0)
      dobs[0] = 36;
    /* SN Snellen factor of the visual acuity of the observer */
    if (dobs[1] == 0)
      dobs[1] = 1;
    if ((helflag & SweConst.SE_HELFLAG_OPTICAL_PARAMS) == 0) {
      for (i = 2; i <= 5; i++)
        dobs[i] = 0;
    }
    /* OpticMagn undefined -> use eye */
    if (dobs[3] == 0) {
      dobs[2] = 1; /* Binocular = 1 */
      dobs[3] = 1; /* OpticMagn = 1: use eye */
      /* dobs[4] and dobs[5] (OpticDia and OpticTrans) will be defaulted in 
       * OpticFactor() */
    }
  }

  /*###################################################################
  ' age [Year]
  ' SN [-]
  ' AltO [deg]
  ' AziO [deg]
  ' AltM [deg]
  ' AziM [deg]
  ' MoonDistance [km]
  ' JDNDaysUT [-]
  ' AltS [deg]
  ' AziS [deg]
  ' lat [deg]
  ' heighteye [m]
  ' TempS [C]
  ' PresS [mbar]
  ' RH [%]
  ' VR [km]
  ' VisLimMagn [-]
  */
  private double VisLimMagn(double[] dobs, double AltO, double AziO, double AltM, double AziM, double JDNDaysUT, double AltS, double AziS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, int[] scotopic_flag, StringBuilder serr) {
    double C1, C2, Th, kX, Bsk, CorrFactor1, CorrFactor2;
    double log10 = 2.302585092994;
    /*double Age = dobs[0];*/
    /*double SN = dobs[1];*/
    Bsk = Bsky(AltO, AziO, AltM, AziM, JDNDaysUT, AltS, AziS, sunra, Lat, HeightEye, datm, helflag, serr);
    /* Schaefer, Astronomy and the limits of vision, Archaeoastronomy, 1993 Verder:*/
    kX = Deltam(AltO, AltS, sunra, Lat, HeightEye, datm, helflag, serr);
    /* influence of age*/
    /*Fa = SMath.max(1, SMath.pow(p(23, Bsk) / p(Age, Bsk), 2)); */
    CorrFactor1 = OpticFactor(Bsk, kX, dobs, JDNDaysUT, "", 1, helflag);
    CorrFactor2 = OpticFactor(Bsk, kX, dobs, JDNDaysUT, "", 0, helflag);
    /* From Schaefer , Archaeoastronomy, XV, 2000, page 129*/
    if (Bsk < BNIGHT && (helflag & SweConst.SE_HELFLAG_VISLIM_PHOTOPIC) == 0) {
      C1 = 1.5848931924611e-10; /*SMath.pow(10, -9.8);*/ /* C1 = 10 ^ (-9.8);*/
      C2 = 0.012589254117942; /*SMath.pow(10, -1.9);*/ /* C2 = 10 ^ (-1.9);*/
      if (scotopic_flag != null) 
        scotopic_flag[0] = 1;
    } else {
      C1 = 4.4668359215096e-9; /*SMath.pow(10, -8.35);*/ /* C1 = 10 ^ (-8.35);*/
      C2 = 1.2589254117942e-6; /*SMath.pow(10, -5.9);*/ /* C2 = 10 ^ (-5.9);*/
      if (scotopic_flag != null) 
        scotopic_flag[0] = 0;
    }
    if (scotopic_flag != null) {
      if (BNIGHT * BNIGHT_FACTOR > Bsk && BNIGHT / BNIGHT_FACTOR < Bsk)
        scotopic_flag[0] |= 2;
    }
    /*Th = C1 * SMath.pow(1 + SMath.sqrt(C2 * Bsk), 2) * Fa;*/
    Bsk = Bsk / CorrFactor1;
    Th = C1 * SMath.pow(1 + SMath.sqrt(C2 * Bsk), 2) * CorrFactor2;
    /* Visual limiting magnitude of point source*/
    return -16.57 - 2.5 * (SMath.log(Th) / log10);
  }

  /** Limiting magnitude in dark skies<br>
  * <b>ATTENTION: This method possibly (re-)sets a global parameter used
  * in calculation of delta T. See SweDate.setGlobalTidalAcc(double).</b>
  * @param tjdut UT julian day number
  * @param dgeo[] geographic position<br>
  * <pre> dgeo[0]: geographic longitude
  *     dgeo[1]: geographic latitude
  *     dgeo[2]: geographic altitude (eye height) in meters</pre>
  * @param datm[] atmospheric conditions<br>
  * <pre>datm[0]: atmospheric pressure in mbar (hPa)
  *     datm[1]: atmospheric temperature in degrees Celsius
  *     datm[2]: relative humidity in %
  *     datm[3]: if datm[3]&gt;=1, then it is Meteorological Range [km]
  *          if 1&gt;datm[3]&gt;0, then it is the total atmsopheric coeffcient (ktot)
  *          if datm[3]=0, then the other atmospheric parameters determine the total atmospheric coefficient (ktot)
  *     Default values:
  *     If this is too much for you, set all these values to 0. The software will then set the following defaults:
  *     Pressure 1013.25, temperature 15, relative humidity 40. The values will be modified depending
  *     on the altitude of the observer above sea level.
  *     If the extinction coefficient (meteorological range) datm[3] is 0, the software will calculate its value
  *     from datm[0..2].</pre>
  * @param dobs[] observer description<br>
  * <pre>dobs[0]: age of observer in years (default = 36)
  *     dobs[1]: Snellen ratio of observers eyes (default = 1 = normal)
  * The following parameters are only relevant if the flag SE_HELFLAG_OPTICAL_PARAMS is set:
  *      dobs[2]: 0 = monocular, 1 = binocular (actually a boolean)
  *      dobs[3]: telescope magnification: 0 = default to naked eye (binocular), 1 = naked eye
  *      dobs[4]: optical aperture (telescope diameter) in mm
  *      dobs[5]: optical transmission</pre>
  * @param ObjectName of fixstar or asteroid number or "sun", "venus", "mars",
  * "mercury", "jupiter", "saturn", "uranus", "neptune", "moon" (case insensitive)
  * @param helflag calculation flag, bitmap<br>
  * helflag contains ephemeris flag, like iflag in swe_calc() etc. In addition it can contain the following bits:
  * <pre>     SweConst.SE_HELFLAG_OPTICAL_PARAMS (512): Use this with calculations for optical instruments.
  *            Unless this bit is set, the values of dobs[2-5] are ignored.
  *      SweConst.SE_HELFLAG_NO_DETAILS (1024): provide the date, but not details like visibility start,
  *            optimum, and end. This bit makes the program a bit faster.
  *      SweConst.SE_HELFLAG_VISLIM_DARK (4096): function behaves as if the Sun were at nadir.
  *      SweConst.SE_HELFLAG_VISLIM_NOMOON (8192): function behaves as if the Moon were at nadir, i. e. the
  *            Moon as a factor disturbing the observation is excluded. This flag is useful if one is not really
  *            interested in the heliacal date of that particular year but in the heliacal date of that epoch.</pre>
  * @param dret[50] (output parameter)<br>
  * <pre>        dret[0]: start visibility (Julian day number)
  *      dret[1]: optimum visibility (Julian day number), zero if helflag &gt;= SweConst.SE_HELFLAG_AV
  *      dret[2]: end of visibility (Julian day number), zero if helflag &gt;= SweConst.SE_HELFLAG_AV
  *      dret[3] ... dret[49]: unused but required on input</pre>
  * @param serr error string
  * @return <pre>
  * -1   Error
  * -2   Object is below horizon
  *  0   OK, photopic vision
  *  |1  OK, scotopic vision
  *  |2  OK, near limit photopic/scotopic
  * </pre>
  * @see SweDate#setGlobalTidalAcc(double)
  */
  public int swe_vis_limit_mag(double tjdut, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int helflag, double[] dret, StringBuilder serr) {
    int retval = SweConst.OK, i;
    int scotopic_flag[] = new int[]{0};
    double AltO[] = new double[1], AziO[] = new double[1], AltM[] = new double[1], AziM[] = new double[1], AltS[] = new double[1], AziS[] = new double[1];
    double sunra;
    SweDate.swi_set_tid_acc(tjdut, helflag, 0);
    sunra = SunRA(tjdut, helflag, serr);
    default_heliacal_parameters(datm, dgeo, dobs, helflag);
    sw.swe_set_topo(dgeo[0], dgeo[1], dgeo[2]);
    for (i = 0; i < 7; i++)
      dret[i] = 0;
    if (ObjectLoc(tjdut, dgeo, datm, ObjectName, 0, helflag, AltO, serr) == SweConst.ERR)
      return SweConst.ERR;
    if (AltO[0] < 0 && serr != null) {
      serr.setLength(0);
      serr.append("object is below local horizon");
      dret[0] = -100;
      return -2;
    }
    if (ObjectLoc(tjdut, dgeo, datm, ObjectName, 1, helflag, AziO, serr) == SweConst.ERR)
      return SweConst.ERR;
    if ((helflag & SweConst.SE_HELFLAG_VISLIM_DARK) != 0) {
      AltS[0] = -90;
      AziS[0] = 0;
    } else {
      if (ObjectLoc(tjdut, dgeo, datm, "sun", 0, helflag, AltS, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (ObjectLoc(tjdut, dgeo, datm, "sun", 1, helflag, AziS, serr) == SweConst.ERR)
        return SweConst.ERR;
    }
    if (ObjectName.toString().startsWith("moon") ||
      ((helflag & SweConst.SE_HELFLAG_VISLIM_DARK) != 0) ||
      ((helflag & SweConst.SE_HELFLAG_VISLIM_NOMOON) != 0)
       ) {
      AltM[0] = -90; AziM[0] = 0;
    } else {
      if (ObjectLoc(tjdut, dgeo, datm, "moon", 0, helflag, AltM, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (ObjectLoc(tjdut, dgeo, datm, "moon", 1, helflag, AziM, serr) == SweConst.ERR)
        return SweConst.ERR;
    }
    dret[0] = VisLimMagn(dobs, AltO[0], AziO[0], AltM[0], AziM[0], tjdut, AltS[0], AziS[0], sunra, dgeo[1], dgeo[2], datm, helflag, scotopic_flag, serr);
    dret[1] = AltO[0];
    dret[2] = AziO[0];
    dret[3] = AltS[0];
    dret[4] = AziS[0];
    dret[5] = AltM[0];
    dret[6] = AziM[0];
    if (Magnitude(tjdut, dgeo, ObjectName, helflag, dret, 7, serr) == SweConst.ERR)
      return SweConst.ERR;
    retval = scotopic_flag[0];
    /*dret[8] = (double) is_scotopic;*/
    /*if (*serr != '\0') * in VisLimMagn(), serr is only a warning *
      retval = SweConst.ERR; */
    return retval;
  }

  /*###################################################################
  ' Magn [-]
  ' age [Year]
  ' SN [-]
  ' AltO [deg]
  ' AziO [deg]
  ' AltM [deg]
  ' AziM [deg]
  ' MoonDistance [km]
  ' JDNDaysUT [-]
  ' AziS [deg]
  ' lat [deg]
  ' heighteye [m]
  ' Temperature [C]
  ' Pressure [mbar]
  ' RH [%]
  ' VR [km]
  ' TopoArcVisionis [deg]
  */
  private int TopoArcVisionis(double Magn, double[] dobs, double AltO, double AziO, double AltM, double AziM, double JDNDaysUT, double AziS, double sunra, double Lat, double HeightEye, double[] datm, int helflag, double[] dret, StringBuilder serr) {
    double Xm, Ym, AltSi, AziSi;
    double xR = 0;
    double Xl = 45;
    double Yl, Yr;
    Yl = Magn - VisLimMagn(dobs, AltO, AziO, AltM, AziM, JDNDaysUT, AltO - Xl, AziS, sunra, Lat, HeightEye, datm, helflag, null, serr);
    /* if (*serr != '\0') return SweConst.ERR; * serr is only a warning */
    Yr = Magn - VisLimMagn(dobs, AltO, AziO, AltM, AziM, JDNDaysUT, AltO - xR, AziS, sunra, Lat, HeightEye, datm, helflag, null, serr);
    /* if (*serr != '\0') return SweConst.ERR; * serr is only a warning */
    /* http://en.wikipedia.org/wiki/Bisection_method*/
    if ((Yl * Yr) <= 0) {
      while(SMath.abs(xR - Xl) > epsilon) {
        /*Calculate midpoint of domain*/
        Xm = (xR + Xl) / 2.0;
        AltSi = AltO - Xm;
        AziSi = AziS;
        Ym = Magn - VisLimMagn(dobs, AltO, AziO, AltM, AziM, JDNDaysUT, AltSi, AziSi, sunra, Lat, HeightEye, datm, helflag, null, serr);
        /* if (*serr != '\0') return SweConst.ERR; * serr is only a warning */
        if ((Yl * Ym) > 0) {
          /* Throw away left half*/
          Xl = Xm;
          Yl = Ym;
        } else {
          /* Throw away right half */
          xR = Xm;
          Yr = Ym;
        }
      }
      Xm = (xR + Xl) / 2.0;
    } else {
      Xm = 99;
    }
    if (Xm < AltO) 
      Xm = AltO;
    dret[0] = Xm;
    return SweConst.OK;
  }

  /**
  * <b>ATTENTION: This method possibly (re-)sets a global parameter used
  * in calculation of delta T. See SweDate.setGlobalTidalAcc(double).</b>
  * @see SweDate#setGlobalTidalAcc(double)
  */
  public int swe_topo_arcus_visionis(double tjdut, double[] dgeo, double[] datm, double[] dobs, int helflag, double mag, double azi_obj, double alt_obj, double azi_sun, double azi_moon, double alt_moon, double[] dret, StringBuilder serr) {
    double sunra;
    SweDate.swi_set_tid_acc(tjdut, helflag, 0);
    sunra = SunRA(tjdut, helflag, serr);
    if (serr != null && serr.length() > 0)
      return SweConst.ERR;
    return TopoArcVisionis(mag, dobs, alt_obj, azi_obj, alt_moon, azi_moon, tjdut, azi_sun, sunra, dgeo[1], dgeo[2], datm, helflag, dret, serr);
  }

  /*###################################################################*/
  /*' Magn [-]
  ' age [Year]
  ' SN Snellen factor of the visual aquity of the observer
    see: http://www.i-see.org/eyecharts.html#make-your-own
  ' AziO [deg]
  ' AltM [deg]
  ' AziM [deg]
  ' MoonDistance [km]
  ' JDNDaysUT [-]
  ' AziS [deg]
  ' Lat [deg]
  ' HeightEye [m]
  ' Temperature [C]
  ' Pressure [mbar]
  ' RH [%]   relative humidity
  ' VR [km]  Meteorological Range, 
    see http://www.iol.ie/~geniet/eng/atmoastroextinction.htm
  ' TypeAngle 
  '   [0=Object's altitude, 
  '    1=Arcus Visonis (Object's altitude - Sun's altitude), 
  '    2=Sun's altitude]
  ' HeliacalAngle [deg]
  */
  private int HeliacalAngle(double Magn, double[] dobs, double AziO, double AltM, double AziM, double JDNDaysUT, double AziS, double[] dgeo, double[] datm, int helflag, double[] dangret, StringBuilder serr) {
    double x, minx, maxx, xmin, ymin, Xl, xR, Yr[] = new double[1], Yl[] = new double[1], Xm, Ym[] = new double[1], xmd, ymd[] = new double[1];
    double Arc[] = new double[1], DELTAx;
    double sunra = SunRA(JDNDaysUT, helflag, serr);
    double Lat = dgeo[1];
    double HeightEye = dgeo[2];
    if (PLSV == 1) {
      dangret[0] = criticalangle;
      dangret[1] = criticalangle + Magn * 2.492 + 13.447;
      dangret[2] = -(Magn * 2.492 + 13.447); /* Magn * 1.1 + 8.9;*/
      return SweConst.OK;
    }
    minx = 2;
    maxx = 20;
    xmin = 0;
    ymin = 10000;
    for (x = minx; x <= maxx; x++) {
      if (TopoArcVisionis(Magn, dobs, x, AziO, AltM, AziM, JDNDaysUT, AziS, sunra, Lat, HeightEye, datm, helflag, Arc, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (Arc[0] < ymin) {
        ymin = Arc[0];
        xmin = x;
      }
    }
    Xl = xmin - 1;
    xR = xmin + 1;
    if (TopoArcVisionis(Magn, dobs, xR, AziO, AltM, AziM, JDNDaysUT, AziS, sunra, Lat, HeightEye, datm, helflag, Yr, serr) == SweConst.ERR)
      return SweConst.ERR;
    if (TopoArcVisionis(Magn, dobs, Xl, AziO, AltM, AziM, JDNDaysUT, AziS, sunra, Lat, HeightEye, datm, helflag, Yl, serr) == SweConst.ERR)
      return SweConst.ERR;
    /* http://en.wikipedia.org/wiki/Bisection_method*/
    while(SMath.abs(xR - Xl) > 0.1) {
      /* Calculate midpoint of domain */
      Xm = (xR + Xl) / 2.0;
      DELTAx = 0.025;
      xmd = Xm + DELTAx;
      if (TopoArcVisionis(Magn, dobs, Xm, AziO, AltM, AziM, JDNDaysUT, AziS, sunra, Lat, HeightEye, datm, helflag, Ym, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (TopoArcVisionis(Magn, dobs, xmd, AziO, AltM, AziM, JDNDaysUT, AziS, sunra, Lat, HeightEye, datm, helflag, ymd, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (Ym[0] >= ymd[0]) {
        /* Throw away left half */
        Xl = Xm;
        Yl[0] = Ym[0];
      } else {
        /*Throw away right half */
        xR = Xm;
        Yr[0] = Ym[0];
      }
    }
    Xm = (xR + Xl) / 2.0;
    Ym[0] = (Yr[0] + Yl[0]) / 2.0;
    dangret[1] = Ym[0];
    dangret[2] = Xm - Ym[0];
    dangret[0] = Xm;
    return SweConst.OK;
  }

  /**
  * <b>ATTENTION: This method possibly (re-)sets a global parameter used
  * in calculation of delta T. See SweDate.setGlobalTidalAcc(double).</b>
  * @see SweDate#setGlobalTidalAcc(double)
  */
  public int swe_heliacal_angle(double tjdut, double[] dgeo, double[] datm, double[] dobs, int helflag, double mag, double azi_obj, double azi_sun, double azi_moon, double alt_moon, double[] dret, StringBuilder serr) {
    if (dgeo[2] < SwephData.SEI_ECL_GEOALT_MIN || dgeo[2] > SwephData.SEI_ECL_GEOALT_MAX) {
      if (serr != null) {
        serr.setLength(0);
        serr.append(String.format(Locale.US, "location for heliacal events must be between %.0f and %.0f m above sea", SwephData.SEI_ECL_GEOALT_MIN, SwephData.SEI_ECL_GEOALT_MAX));
      }
      return SweConst.ERR;
    }
    SweDate.swi_set_tid_acc(tjdut, helflag, 0);
    return HeliacalAngle(mag, dobs, azi_obj, alt_moon, azi_moon, tjdut, azi_sun, dgeo, datm, helflag, dret, serr);
  }

  /*###################################################################
  ' AltO [deg]
  ' AziO [deg]
  ' AltS [deg]
  ' AziS [deg]
  ' parallax [deg]
  ' WidthMoon [deg]
  */
  private double WidthMoon(double AltO, double AziO, double AltS, double AziS, double parallax) {
    /* Yallop 1998, page 3*/
    double GeoAltO = AltO + parallax;
    return 0.27245 * parallax * (1 + SMath.sin(GeoAltO * SwissData.DEGTORAD) * SMath.sin(parallax * SwissData.DEGTORAD)) * (1 - SMath.cos((AltS - GeoAltO) * SwissData.DEGTORAD) * SMath.cos((AziS - AziO) * SwissData.DEGTORAD));
  }

  /*###################################################################
  ' W [deg]
  ' LengthMoon [deg]
  */
  private double LengthMoon(double W, double Diamoon) {
    double Wi, D;
    if (Diamoon == 0) Diamoon = AvgRadiusMoon * 2;
    Wi = W * 60;
    D = Diamoon * 60;
    /* Crescent length according: http://calendar.ut.ac.ir/Fa/Crescent/Data/Sultan2005.pdf*/
    return (D - 0.3 * (D + Wi) / 2.0 / Wi) / 60.0;
  }

  /*###################################################################
  ' W [deg]
  ' GeoARCVact [deg]
  ' q [-]
  */
  private double qYallop(double W, double GeoARCVact) {
    double Wi = W * 60;
    return (GeoARCVact - (11.8371 - 6.3226 * Wi + 0.7319 * Wi * Wi - 0.1018 * Wi * Wi * Wi)) / 10;
  }

  /*###################################################################
  'A (0,p)
  'B (1,q)
  'C (0,r)
  'D (1,s)
  */
  private double crossing(double A, double B, double C, double D) {
    return (C - A) / ((B - A) - (D - C));
  }

  /*###################################################################*/
  private int DeterTAV(double[] dobs, double JDNDaysUT, double[] dgeo, double[] datm, StringBuilder ObjectName, int helflag, double[] dret, StringBuilder serr) {
    double[] Magn = new double[1], AltO = new double[1], AziS = new double[1], AziO = new double[1], AziM = new double[1], AltM = new double[1];
    double sunra = SunRA(JDNDaysUT, helflag, serr);
    if (Magnitude(JDNDaysUT, dgeo, ObjectName, helflag, Magn, 0, serr) == SweConst.ERR)
      return SweConst.ERR;
    if (ObjectLoc(JDNDaysUT, dgeo, datm, ObjectName, 0, helflag, AltO, serr) == SweConst.ERR)
      return SweConst.ERR;
    if (ObjectLoc(JDNDaysUT, dgeo, datm, ObjectName, 1, helflag, AziO, serr) == SweConst.ERR)
      return SweConst.ERR;
    if (ObjectName.toString().startsWith("moon")) {
      AltM[0] = -90; 
      AziM[0] = 0;
    } else {
      if (ObjectLoc(JDNDaysUT, dgeo, datm, "moon", 0, helflag, AltM, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (ObjectLoc(JDNDaysUT, dgeo, datm, "moon", 1, helflag, AziM, serr) == SweConst.ERR)
        return SweConst.ERR;
    }
    if (ObjectLoc(JDNDaysUT, dgeo, datm, "sun", 1, helflag, AziS, serr) == SweConst.ERR)
      return SweConst.ERR;
    if (TopoArcVisionis(Magn[0], dobs, AltO[0], AziO[0], AltM[0], AziM[0], JDNDaysUT, AziS[0], sunra, dgeo[1], dgeo[2], datm, helflag, dret, serr) == SweConst.ERR)
      return SweConst.ERR;
    return SweConst.OK;
  }

  /*###################################################################
  ' A y-value at x=1
  ' B y-value at x=0
  ' C y-value at x=-1
  ' x2min minimum for the quadratic function
  */
  private double x2min(double A, double B, double C) {
    double term = A + C - 2 * B;
    if (term == 0)
      return 0;
    return -(A - C) / 2.0 / term;
  }


  /*###################################################################
  ' A y-value at x=1
  ' B y-value at x=0
  ' C y-value at x=-1
  ' x
  ' y is y-value of quadratic function
  */
  private double funct2(double A, double B, double C, double x) {
    return (A + C - 2 * B) / 2.0 * x * x + (A - C) / 2.0 * x + B;
  }

  private void strcpy_VBsafe(StringBuilder sout, String sin) { 
//  char *sp, *sp2; 
//  int iw = 0;
//  sp = sin; 
//  sp2 = sout;
//  while((isalnum(*sp) || *sp == ' ' || *sp == '-') && iw < 30) {
//    *sp2 = *sp;
//    sp++; sp2++; iw++;
//  }
//  *sp2 = '\0';
   sout.setLength(0);
   for(int iw = 0; iw < Math.min(sin.length(), 30); iw++) {
       sout.append(sin.charAt(iw));
   }
}

  /*###################################################################
  ' JDNDaysUT [JDN]
  ' HPheno
  '0=AltO [deg]		topocentric altitude of object (unrefracted)
  '1=AppAltO [deg]        apparent altitude of object (refracted)
  '2=GeoAltO [deg]        geocentric altitude of object
  '3=AziO [deg]           azimuth of object
  '4=AltS [deg]           topocentric altitude of Sun
  '5=AziS [deg]           azimuth of Sun
  '6=TAVact [deg]         actual topocentric arcus visionis
  '7=ARCVact [deg]        actual (geocentric) arcus visionis
  '8=DAZact [deg]         actual difference between object's and sun's azimuth
  '9=ARCLact [deg]        actual longitude difference between object and sun
  '10=kact [-]            extinction coefficient
  '11=minTAV [deg]        smallest topocentric arcus visionis
  '12=TfistVR [JDN]       first time object is visible, according to VR
  '13=TbVR [JDN]          optimum time the object is visible, according to VR
  '14=TlastVR [JDN]       last time object is visible, according to VR
  '15=TbYallop[JDN]       best time the object is visible, according to Yallop
  '16=WMoon [deg]         cresent width of moon
  '17=qYal [-]            q-test value of Yallop 
  '18=qCrit [-]           q-test criterion of Yallop
  '19=ParO [deg]          parallax of object
  '20 Magn [-]            magnitude of object
  '21=RiseO [JDN]         rise/set time of object
  '22=RiseS [JDN]         rise/set time of sun
  '23=Lag [JDN]           rise/set time of object minus rise/set time of sun
  '24=TvisVR [JDN]        visibility duration
  '25=LMoon [deg]         cresent length of moon
  '26=CVAact [deg]
  '27=Illum [%] 'new
  '28=CVAact [deg] 'new
  '29=MSk [-]
  */
  /**
  * <b>ATTENTION: This method possibly (re-)sets a global parameter used
  * in calculation of delta T. See SweDate.setGlobalTidalAcc(double).</b>
  * @see SweDate#setGlobalTidalAcc(double)
  */
  public int swe_heliacal_pheno_ut(double JDNDaysUT, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectNameIn, int TypeEvent, int helflag, double[] darr, StringBuilder serr) {
    double[] AziS = new double[1], AltS = new double[1], AltS2 = new double[1], AziO = new double[1], AltO = new double[1], AltO2 = new double[1], GeoAltO = new double[1], MagnO = new double[1];
    double AppAltO, DAZact, TAVact, ParO;
    double ARCVact, ARCLact, kact, WMoon, LMoon = 0, qYal, qCrit;
    double[] RiseSetO = new double[1], RiseSetS = new double[1];
    double Lag, TbYallop, TfirstVR, TlastVR, TbVR;
    double[] MinTAVact = new double[1];
// Attention: MinTAV does not get initialized in C but assigned to darr[] sometimes later
    double MinTAV = 0, Ta, Tc, TimeStep, TimePointer, MinTAVoud = 0, DeltaAltoud = 0, DeltaAlt, TvisVR, crosspoint;
    double OldestMinTAV, extrax, illum;
    double elong, attr[]=new double[30];
    double[] LocalminCheck = new double[1];
    double TimeCheck;
    int retval = SweConst.OK, RS, Planet;
    boolean noriseO = false;
    StringBuilder ObjectName = new StringBuilder();
    double sunra;
    int iflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    if (dgeo[2] < SwephData.SEI_ECL_GEOALT_MIN || dgeo[2] > SwephData.SEI_ECL_GEOALT_MAX) {
      if (serr != null) {
        serr.setLength(0);
        serr.append(String.format(Locale.US, "location for heliacal events must be between %.0f and %.0f m above sea", SwephData.SEI_ECL_GEOALT_MIN, SwephData.SEI_ECL_GEOALT_MAX));
      }
      return SweConst.ERR;
    }
    SweDate.swi_set_tid_acc(JDNDaysUT, helflag, 0);
    sunra = SunRA(JDNDaysUT, helflag, serr);
    /* note, the fixed stars functions rewrite the star name. The input string 
       may be too short, so we have to make sure we have enough space */
    strcpy_VBsafe(ObjectName, ObjectNameIn.toString());
    default_heliacal_parameters(datm, dgeo, dobs, helflag);
    sw.swe_set_topo(dgeo[0], dgeo[1], dgeo[2]);
    retval = ObjectLoc(JDNDaysUT, dgeo, datm, "sun", 1, helflag, AziS, serr);
    if (retval == SweConst.OK)
      retval = ObjectLoc(JDNDaysUT, dgeo, datm, "sun", 0, helflag, AltS, serr);
    if (retval == SweConst.OK)
      retval = ObjectLoc(JDNDaysUT, dgeo, datm, ObjectName, 1, helflag, AziO, serr);
    if (retval == SweConst.OK)
      retval = ObjectLoc(JDNDaysUT, dgeo, datm, ObjectName, 0, helflag, AltO, serr);
    if (retval == SweConst.OK)
      retval = ObjectLoc(JDNDaysUT, dgeo, datm, ObjectName, 7, helflag, GeoAltO, serr);
    if (retval == SweConst.ERR)
      return SweConst.ERR;
    AppAltO = AppAltfromTopoAlt(AltO[0], datm[1], datm[0], helflag);
    DAZact = AziS[0] - AziO[0];
    TAVact = AltO[0] - AltS[0];
    /*this parallax seems to be somewhat smaller then in Yallop and SkyMap! Needs to be studied*/
    ParO = GeoAltO[0] - AltO[0];
    if (Magnitude(JDNDaysUT, dgeo, ObjectName, helflag, MagnO, 0, serr) == SweConst.ERR)
      return SweConst.ERR;
    ARCVact = TAVact + ParO;
    ARCLact = SMath.acos(SMath.cos(ARCVact * SwissData.DEGTORAD) * SMath.cos(DAZact * SwissData.DEGTORAD)) / SwissData.DEGTORAD;
    Planet = DeterObject(ObjectName);
    if (Planet == -1) {
      elong = ARCLact;
      illum = 100;
    } else {
      retval = sw.swe_pheno_ut(JDNDaysUT, Planet, iflag|(SweConst.SEFLG_TOPOCTR|SweConst.SEFLG_EQUATORIAL), attr, serr);
      if (retval == SweConst.ERR) return SweConst.ERR;
      elong = attr[2];
      illum = attr[1] * 100;
    }
    kact = kt(AltS[0], sunra, dgeo[1], dgeo[2], datm[1], datm[2], datm[3], 4, serr);
    if (false) {
darr[26] = kR(AltS[0], dgeo[2]);
darr[27] = kW(dgeo[2], datm[1], datm[2]);
darr[28] = kOZ(AltS[0], sunra, dgeo[1]);
darr[29] = ka(AltS[0], sunra, dgeo[1], dgeo[2], datm[1], datm[2], datm[3], serr);
darr[30] = darr[26] + darr[27] + darr[28] + darr[29];
    }
    WMoon = 0;
    qYal = 0;
    qCrit = 0;
    LMoon = 0;
    if (Planet == SweConst.SE_MOON) {
      WMoon = WidthMoon(AltO[0], AziO[0], AltS[0], AziS[0], ParO);
      LMoon = LengthMoon(WMoon, 0);
      qYal = qYallop(WMoon, ARCVact);
      if (qYal > 0.216) qCrit = 1; /* A */
      if (qYal < 0.216 && qYal > -0.014) qCrit = 2; /* B */
      if (qYal < -0.014 && qYal > -0.16) qCrit = 3; /* C */
      if (qYal < -0.16 && qYal > -0.232) qCrit = 4; /* D */
      if (qYal < -0.232 && qYal > -0.293) qCrit = 5; /* E */
      if (qYal < -0.293) qCrit = 6; /* F */
    }
    /*determine if rise or set event*/
    RS = 2;
    if (TypeEvent == 1 || TypeEvent == 4) RS = 1;
    retval = RiseSet(JDNDaysUT - 4.0 / 24.0, dgeo, datm, "sun", RS, helflag, 0, RiseSetS, serr);
    if (retval == SweConst.ERR)
      return SweConst.ERR;
    retval = RiseSet(JDNDaysUT - 4.0 / 24.0, dgeo, datm, ObjectName.toString(), RS, helflag, 0, RiseSetO, serr);
    if (retval == SweConst.ERR)
      return SweConst.ERR;
    TbYallop = SweConst.TJD_INVALID;
    if (retval == -2) { /* object does not rise or set */
      Lag = 0;
      noriseO = true;
    } else {
      Lag = RiseSetO[0] - RiseSetS[0];
      if (Planet == SweConst.SE_MOON)
        TbYallop = (RiseSetO[0] * 4 + RiseSetS[0] * 5) / 9.0;
    }
    if ((TypeEvent == 3 || TypeEvent == 4) && (Planet == -1 || Planet >= SweConst.SE_MARS)) {
      TfirstVR = SweConst.TJD_INVALID;
      TbVR = SweConst.TJD_INVALID;
      TlastVR = SweConst.TJD_INVALID;
      TvisVR = 0;
      MinTAV = 0;
      // goto output_heliacal_pheno;
      goto_output_heliacal_pheno(darr, AltO, AppAltO, GeoAltO, AziO, AltS, AziS, TAVact, ARCVact, DAZact, ARCLact, kact, MinTAV, TfirstVR, TbVR, TlastVR, TbYallop, WMoon, qYal, qCrit, ParO, MagnO, RiseSetO, RiseSetS, Lag, TvisVR, LMoon, elong, illum);
    }
    /* If HPheno >= 11 And HPheno <= 14 Or HPheno = 24 Then*/
    /*te bepalen m.b.v. walkthrough*/
    MinTAVact[0] = 199;
    DeltaAlt = 0; 
    OldestMinTAV = 0;
    Ta = 0;
    Tc = 0;
    TbVR = 0;
    TimeStep = -TimeStepDefault / 24.0 / 60.0;
    if (RS == 2) TimeStep = -TimeStep;
    TimePointer = RiseSetS[0] - TimeStep;
    do {
      TimePointer = TimePointer + TimeStep;
      OldestMinTAV = MinTAVoud;
      MinTAVoud = MinTAVact[0];
      DeltaAltoud = DeltaAlt;
      retval = ObjectLoc(TimePointer, dgeo, datm, "sun", 0, helflag, AltS2, serr);
      if (retval == SweConst.OK)
        retval = ObjectLoc(TimePointer, dgeo, datm, ObjectName, 0, helflag, AltO2, serr);
      if (retval != SweConst.OK)
        return SweConst.ERR;
      DeltaAlt = AltO2[0] - AltS2[0];
      if (DeterTAV(dobs, TimePointer, dgeo, datm, ObjectName, helflag, MinTAVact, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (MinTAVoud < MinTAVact[0] && TbVR == 0) {
        /* determine if this is a local minimum with object still above horizon*/
        TimeCheck = TimePointer + Sgn(TimeStep) * LocalMinStep / 24.0 / 60.0;
        if (RiseSetO[0] != 0) {
          if (TimeStep > 0)
            TimeCheck = SMath.min(TimeCheck, RiseSetO[0]);
          else
            TimeCheck = SMath.max(TimeCheck, RiseSetO[0]);
        }
        if (DeterTAV(dobs, TimeCheck, dgeo, datm, ObjectName, helflag, LocalminCheck, serr) == SweConst.ERR)
          return SweConst.ERR;
        if (LocalminCheck[0] > MinTAVact[0]) {
          extrax = x2min(MinTAVact[0], MinTAVoud, OldestMinTAV);
          TbVR = TimePointer - (1 - extrax) * TimeStep;
          MinTAV = funct2(MinTAVact[0], MinTAVoud, OldestMinTAV, extrax);
        }
      }
      if (DeltaAlt > MinTAVact[0] && Tc == 0 && TbVR == 0) {
        crosspoint = crossing(DeltaAltoud, DeltaAlt, MinTAVoud, MinTAVact[0]);
        Tc = TimePointer - TimeStep * (1 - crosspoint);
      }
      if (DeltaAlt < MinTAVact[0] && Ta == 0 && Tc != 0) {
        crosspoint = crossing(DeltaAltoud, DeltaAlt, MinTAVoud, MinTAVact[0]);
        Ta = TimePointer - TimeStep * (1 - crosspoint);
      }
    } while (SMath.abs(TimePointer - RiseSetS[0]) <= MaxTryHours / 24.0 && Ta == 0 && !((TbVR != 0 && (TypeEvent == 3 || TypeEvent == 4) && (!ObjectName.toString().startsWith("moon") && !ObjectName.toString().startsWith("venus") && !ObjectName.toString().startsWith("mercury")))));
    if (RS == 2) {
      TfirstVR = Tc;
      TlastVR = Ta;
    } else {
      TfirstVR = Ta;
      TlastVR = Tc;
    }
    if (TfirstVR == 0 && TlastVR == 0) {
      if (RS == 1)
        TfirstVR = TbVR - 0.000001;
      else
        TlastVR = TbVR + 0.000001;
    }
    if (!noriseO) {
      if (RS == 1)
        TfirstVR = SMath.max(TfirstVR, RiseSetO[0]);
      else
        TlastVR = SMath.min(TlastVR, RiseSetO[0]);
    }
    TvisVR = SweConst.TJD_INVALID; /*"#NA!" */
    if (TlastVR != 0 && TfirstVR != 0)
      TvisVR = TlastVR - TfirstVR;
    if (TlastVR == 0) TlastVR = SweConst.TJD_INVALID; /*"#NA!" */
    if (TbVR == 0) TbVR = SweConst.TJD_INVALID; /*"#NA!" */
    if (TfirstVR == 0) TfirstVR = SweConst.TJD_INVALID; /*"#NA!" */

    goto_output_heliacal_pheno(darr, AltO, AppAltO, GeoAltO, AziO, AltS, AziS, TAVact, ARCVact, DAZact, ARCLact, kact, MinTAV, TfirstVR, TbVR, TlastVR, TbYallop, WMoon, qYal, qCrit, ParO, MagnO, RiseSetO, RiseSetS, Lag, TvisVR, LMoon, elong, illum);
    return SweConst.OK;
  }
  private void goto_output_heliacal_pheno(
      double[] darr,
      double[] AltO,
      double AppAltO,
      double[] GeoAltO,
      double[] AziO,
      double[] AltS,
      double[] AziS,
      double TAVact,
      double ARCVact,
      double DAZact,
      double ARCLact,
      double kact,
      double MinTAV,
      double TfirstVR,
      double TbVR,
      double TlastVR,
      double TbYallop,
      double WMoon,
      double qYal,
      double qCrit,
      double ParO,
      double[] MagnO,
      double[] RiseSetO,
      double[] RiseSetS,
      double Lag,
      double TvisVR,
      double LMoon,
      double elong,
      double illum) {
    /*End If*/
    darr[0] = AltO[0];
    darr[1] = AppAltO;
    darr[2] = GeoAltO[0];
    darr[3] = AziO[0];
    darr[4] = AltS[0];
    darr[5] = AziS[0];
    darr[6] = TAVact;
    darr[7] = ARCVact;
    darr[8] = DAZact;
    darr[9] = ARCLact;
    darr[10] = kact;
    darr[11] = MinTAV;
    darr[12] = TfirstVR;
    darr[13] = TbVR;
    darr[14] = TlastVR;
    darr[15] = TbYallop;
    darr[16] = WMoon;
    darr[17] = qYal;
    darr[18] = qCrit;
    darr[19] = ParO;
    darr[20] = MagnO[0];
    darr[21] = RiseSetO[0];
    darr[22] = RiseSetS[0];
    darr[23] = Lag;
    darr[24] = TvisVR;
    darr[25] = LMoon;
    darr[26] = elong;
    darr[27] = illum;
  }


  private double get_synodic_period(int Planet) {
    /* synodic periods from:
     * Kelley/Milone/Aveni, "Exploring ancient Skies", p. 43. */
    switch(Planet) {
      case SweConst.SE_MOON: return 29.530588853;
      case SweConst.SE_MERCURY: return 115.8775;
      case SweConst.SE_VENUS: return 583.9214;
      case SweConst.SE_MARS: return 779.9361;
      case SweConst.SE_JUPITER: return 398.8840;
      case SweConst.SE_SATURN: return 378.0919;
      case SweConst.SE_URANUS: return 369.6560;
      case SweConst.SE_NEPTUNE: return 367.4867;
      case SweConst.SE_PLUTO: return 366.7207;
    }
    return 366; /* for stars and default for far away planets */
  }

  /*###################################################################*/
  private int moon_event_arc_vis(double JDNDaysUTStart, double[] dgeo, double[] datm, double[] dobs, int TypeEvent, int helflag, double[] dret, StringBuilder serr) {
    double x[]=new double[20], MinTAV[] = new double[1], MinTAVoud, OldestMinTAV;
    double phase1, phase2, JDNDaysUT, JDNDaysUTi;
    double[] tjd_moonevent = new double[1];
    double tjd_moonevent_start;
    double DeltaAltoud, TimeCheck, LocalminCheck[] = new double[1];
    double[] AltS = new double[1], AltO = new double[1];
    double DeltaAlt = 90;
    StringBuilder ObjectName = new StringBuilder();
    int iflag, Daystep, goingup, Planet, retval;
    int avkind = helflag & SweConst.SE_HELFLAG_AVKIND;
    int epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    dret[0] = JDNDaysUTStart; /* will be returned in error case */
    if (avkind == 0)
      avkind = SweConst.SE_HELFLAG_AVKIND_VR;
    if (avkind != SweConst.SE_HELFLAG_AVKIND_VR) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("error: in valid AV kind for the moon");
      }
      return SweConst.ERR;
    }
    if (TypeEvent == 1 || TypeEvent == 2) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("error: the moon has no morning first or evening last");
      }
      return SweConst.ERR;
    }
    ObjectName.append("moon");
    Planet = SweConst.SE_MOON;
    iflag = SweConst.SEFLG_TOPOCTR | SweConst.SEFLG_EQUATORIAL | epheflag;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0)
      iflag |= SweConst.SEFLG_NONUT|SweConst.SEFLG_TRUEPOS;
    Daystep = 1;
    if (TypeEvent == 3) {
      /*morning last */
      TypeEvent = 2;
    } else {
      /*evening first*/
      TypeEvent = 1;
      Daystep = -Daystep;
    }
    /* check Synodic/phase Period */
    JDNDaysUT = JDNDaysUTStart;
    /* start 30 days later if TypeEvent=4 (1) */
    if (TypeEvent == 1) JDNDaysUT = JDNDaysUT + 30;
    /* determination of new moon date */
    sw.swe_pheno_ut(JDNDaysUT, Planet, iflag, x, serr);
    phase2 = x[0];
    goingup = 0;
    do {
      JDNDaysUT = JDNDaysUT + Daystep;
      phase1 = phase2;
      sw.swe_pheno_ut(JDNDaysUT, Planet, iflag, x, serr);
      phase2 = x[0];
      if (phase2 > phase1) 
        goingup = 1;
    } while (goingup == 0 || (goingup == 1 && (phase2 > phase1)));
    /* fix the date to get the day with the smallest phase (nwest moon) */
    JDNDaysUT = JDNDaysUT - Daystep;
    /* initialize the date to look for set */
    JDNDaysUTi = JDNDaysUT;
    JDNDaysUT = JDNDaysUT - Daystep;
    MinTAVoud = 199;
    do {
      JDNDaysUT = JDNDaysUT + Daystep;
      if ((retval = RiseSet(JDNDaysUT, dgeo, datm, ObjectName.toString(), TypeEvent, helflag, 0, tjd_moonevent, serr)) != SweConst.OK)
        return retval;
      tjd_moonevent_start = tjd_moonevent[0];
      MinTAV[0] = 199;
      OldestMinTAV = MinTAV[0];
      do {
        OldestMinTAV = MinTAVoud;
        MinTAVoud = MinTAV[0];
        DeltaAltoud = DeltaAlt;
        tjd_moonevent[0] = tjd_moonevent[0] - 1.0 / 60.0 / 24.0 * Sgn(Daystep);
//  private int ObjectLoc(double JDNDaysUT, double[] dgeo, double[] datm, String ObjectName, int Angle, int helflag, double[] dret, StringBuilder serr) {
//  private int ObjectLoc(double JDNDaysUT, double[] dgeo, double[] datm, StringBuilder ObjectName, int Angle, int helflag, double[] dret, StringBuilder serr) {
        if (ObjectLoc(tjd_moonevent[0], dgeo, datm, "sun", 0, helflag, AltS, serr) == SweConst.ERR)
          return SweConst.ERR;
        if (ObjectLoc(tjd_moonevent[0], dgeo, datm, ObjectName, 0, helflag, AltO, serr) == SweConst.ERR)
          return SweConst.ERR;
        DeltaAlt = AltO[0] - AltS[0];
        if (DeterTAV(dobs, tjd_moonevent[0], dgeo, datm, ObjectName, helflag, MinTAV, serr) == SweConst.ERR)
          return SweConst.ERR;
        TimeCheck = tjd_moonevent[0] - LocalMinStep / 60.0 / 24.0 * Sgn(Daystep);
        if (DeterTAV(dobs, TimeCheck, dgeo, datm, ObjectName, helflag, LocalminCheck, serr) == SweConst.ERR)
          return SweConst.ERR;
/*printf("%f, %f <= %f\n", tjd_moonevent, MinTAV, MinTAVoud);*/
      /* while (MinTAV <= MinTAVoud && SMath.abs(tjd_moonevent - tjd_moonevent_start) < 120.0 / 60.0 / 24.0);*/
      } while ((MinTAV[0] <= MinTAVoud || LocalminCheck[0] < MinTAV[0]) && SMath.abs(tjd_moonevent[0] - tjd_moonevent_start) < 120.0 / 60.0 / 24.0);
    /* while (DeltaAlt < MinTAVoud && SMath.abs(JDNDaysUT - JDNDaysUTi) < 15);*/
    } while (DeltaAltoud < MinTAVoud && SMath.abs(JDNDaysUT - JDNDaysUTi) < 15);
    if (SMath.abs(JDNDaysUT - JDNDaysUTi) < 15) {
      tjd_moonevent[0] += (1 - x2min(MinTAV[0], MinTAVoud, OldestMinTAV)) * Sgn(Daystep) / 60.0 / 24.0;
    } else {
      serr.setLength(0);
      serr.append("no date found for lunar event");
      return SweConst.ERR;
    }
    dret[0] = tjd_moonevent[0];
    return SweConst.OK;
  }

  private int heliacal_ut_arc_vis(double JDNDaysUTStart, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int TypeEventIn, int helflag, double[] dret, StringBuilder serr_ret) {
    double x[]=new double[6];
    double xin[]=new double[2];
    double xaz[]=new double[2];
    double dang[]=new double[3];
    double objectmagn[] = new double[]{0}, maxlength, DayStep;
    double JDNDaysUT, JDNDaysUTfinal, JDNDaysUTstep, JDNDaysUTstepoud, JDNarcvisUT, tjd_tt, tret[] = new double[1], OudeDatum, JDNDaysUTinp = JDNDaysUTStart, JDNDaysUTtijd;
    double ArcusVis, ArcusVisDelta, ArcusVisPto, ArcusVisDeltaoud;
    double Trise, sunsangle, Theliacal, Tdelta, Angle;
    double TimeStep, TimePointer, OldestMinTAV[] = new double[1], MinTAVoud[] = new double[1], MinTAVact[] = new double[1], extrax, TbVR = 0;
    double AziS, AltS, AziO, AltO, DeltaAlt;
    double direct, Pressure, Temperature, d;
    int epheflag, retval = SweConst.OK;
    int iflag, Planet, eventtype;
    int TypeEvent = TypeEventIn;
    int doneoneday;
    StringBuilder serr = new StringBuilder();
    dret[0] = JDNDaysUTStart;
    Planet = DeterObject(ObjectName);
    Pressure = datm[0];
    Temperature = datm[1];
    /* determine Magnitude of star*/
    if ((retval = Magnitude(JDNDaysUTStart, dgeo, ObjectName, helflag, objectmagn, 0, serr)) == SweConst.ERR)
//    goto swe_heliacal_err;
      return goto_swe_heliacal_err1(serr, serr_ret, retval);
    epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    iflag = SweConst.SEFLG_TOPOCTR | SweConst.SEFLG_EQUATORIAL | epheflag;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0) 
      iflag |= SweConst.SEFLG_NONUT | SweConst.SEFLG_TRUEPOS;
    /* start values for search of heliacal rise
     * maxlength = phase period in days, smaller than minimal synodic period */
    /* days per step (for heliacal rise) in power of two */
    switch(Planet) {
      case SweConst.SE_MERCURY: 
        DayStep = 1; maxlength = 100; break;
      case SweConst.SE_VENUS: 
        DayStep = 64; maxlength = 384; break;
      case SweConst.SE_MARS: 
        DayStep = 128; maxlength = 640; break;
      case SweConst.SE_JUPITER: 
        DayStep = 64; maxlength = 384; break;
      case SweConst.SE_SATURN: 
        DayStep = 64; maxlength = 256; break;
      default:
        DayStep = 64; maxlength = 256; break;
    }
    /* heliacal setting */
    eventtype = TypeEvent;
    if (eventtype == 2) DayStep = -DayStep;
    /* acronychal setting */
    if (eventtype == 4) {
        eventtype = 1;
        DayStep = -DayStep;
    }
    /* acronychal rising */
    if (eventtype == 3) eventtype = 2;
    eventtype |= SweConst.SE_BIT_DISC_CENTER;
    /* normalize the maxlength to the step size */
    {
      /* check each Synodic/phase Period */
      JDNDaysUT = JDNDaysUTStart;
      /* make sure one can find an event on the just after the JDNDaysUTStart */
      JDNDaysUTfinal = JDNDaysUT + maxlength;
      JDNDaysUT = JDNDaysUT - 1;
      if (DayStep < 0) {
        JDNDaysUTtijd = JDNDaysUT;
        JDNDaysUT = JDNDaysUTfinal;
        JDNDaysUTfinal = JDNDaysUTtijd;
      }
      /* prepair the search */
      JDNDaysUTstep = JDNDaysUT - DayStep;
      doneoneday = 0;
      ArcusVisDelta = 199;
      ArcusVisPto = -5.55;
      do { /* this is a do {} while() loop */
        if (SMath.abs(DayStep) == 1) doneoneday = 1;
        do { /* this is a do {} while() loop */
          /* init search for heliacal rise */
          JDNDaysUTstepoud = JDNDaysUTstep;
          ArcusVisDeltaoud = ArcusVisDelta;
          JDNDaysUTstep = JDNDaysUTstep + DayStep;
          /* determine rise/set time */
          if ((retval = my_rise_trans(JDNDaysUTstep, SweConst.SE_SUN, "", eventtype, helflag, dgeo, datm, tret, serr)) == SweConst.ERR)
//          goto swe_heliacal_err;
            return goto_swe_heliacal_err1(serr, serr_ret, retval);
          /* determine time compensation to get Sun's altitude at heliacal rise */
          tjd_tt = tret[0] + DeltaT(tret[0], 0) / D2S;
          if ((retval = sw.swe_calc(tjd_tt, SweConst.SE_SUN, iflag, x, serr)) == SweConst.ERR)
//          goto swe_heliacal_err;
            return goto_swe_heliacal_err1(serr, serr_ret, retval);
          xin[0] = x[0];
          xin[1] = x[1];
          sc.swe_azalt(tret[0], SweConst.SE_EQU2HOR, dgeo, Pressure, Temperature, xin, xaz);
          Trise = HourAngle(xaz[1], x[1], dgeo[1]);
          sunsangle = ArcusVisPto;
          if ((helflag & SweConst.SE_HELFLAG_AVKIND_MIN7) != 0) sunsangle = -7;
          if ((helflag & SweConst.SE_HELFLAG_AVKIND_MIN9) != 0) sunsangle = -9;
          Theliacal = HourAngle(sunsangle, x[1], dgeo[1]);
          Tdelta = Theliacal - Trise;
          if (TypeEvent == 2 || TypeEvent== 3) Tdelta = -Tdelta;
          /* determine appr.time when sun is at the wanted Sun's altitude */
          JDNarcvisUT = tret[0] - Tdelta / 24;
          tjd_tt = JDNarcvisUT + DeltaT(JDNarcvisUT, 0) / D2S;
          /* determine Sun's position */
          if ((retval = sw.swe_calc(tjd_tt, SweConst.SE_SUN, iflag, x, serr)) == SweConst.ERR)
//          goto swe_heliacal_err;
            return goto_swe_heliacal_err1(serr, serr_ret, retval);
          xin[0] = x[0];
          xin[1] = x[1];
          sc.swe_azalt(JDNarcvisUT, SweConst.SE_EQU2HOR, dgeo, Pressure, Temperature, xin, xaz);
          AziS = xaz[0] + 180;
          if (AziS >= 360) AziS = AziS - 360;
          AltS = xaz[1];
          /* determine Moon's position */
          /* determine object's position */
          if (Planet != -1) {
            if ((retval = sw.swe_calc(tjd_tt, Planet, iflag, x, serr)) == SweConst.ERR)
//            goto swe_heliacal_err;
              return goto_swe_heliacal_err1(serr, serr_ret, retval);
            /* determine magnitude of Planet */
            if ((retval = Magnitude(JDNarcvisUT, dgeo, ObjectName, helflag, objectmagn, 0, serr)) == SweConst.ERR)
//            goto swe_heliacal_err;
              return goto_swe_heliacal_err1(serr, serr_ret, retval);
          } else {
            if ((retval = call_swe_fixstar(ObjectName, tjd_tt, iflag, x, serr)) == SweConst.ERR)
//            goto swe_heliacal_err;
              return goto_swe_heliacal_err1(serr, serr_ret, retval);
          }
          xin[0] = x[0];
          xin[1] = x[1];
          sc.swe_azalt(JDNarcvisUT, SweConst.SE_EQU2HOR, dgeo, Pressure, Temperature, xin, xaz);
          AziO = xaz[0] + 180;
          if (AziO >= 360) AziO = AziO - 360;
          AltO = xaz[1];
          /* determine arcusvisionis */
          DeltaAlt = AltO - AltS;
          /*if ((retval = HeliacalAngle(objectmagn, dobs, AziO, AltM, AziM, JDNarcvisUT, AziS, dgeo, datm, helflag, dang, serr)) == SweConst.ERR)*/
          if ((retval = HeliacalAngle(objectmagn[0], dobs, AziO, -1, 0, JDNarcvisUT, AziS, dgeo, datm, helflag, dang, serr)) == SweConst.ERR)
//          goto swe_heliacal_err;
            return goto_swe_heliacal_err1(serr, serr_ret, retval);
          ArcusVis = dang[1];
          ArcusVisPto = dang[2];
          ArcusVisDelta = DeltaAlt - ArcusVis;
        /*{} while (((ArcusVisDeltaoud > 0 && ArcusVisDelta < 0) || ArcusVisDelta < 0) && (JDNDaysUTfinal - JDNDaysUTstep) * Sgn(DayStep) > 0);*/
        } while ((ArcusVisDeltaoud > 0 || ArcusVisDelta < 0) && (JDNDaysUTfinal - JDNDaysUTstep) * Sgn(DayStep) > 0);
        if (doneoneday == 0 && (JDNDaysUTfinal - JDNDaysUTstep) * Sgn(DayStep) > 0) {
          /* go back to date before heliacal altitude */
          ArcusVisDelta = ArcusVisDeltaoud;
          DayStep = ((int) (SMath.abs(DayStep) / 2.0)) * Sgn(DayStep);
          JDNDaysUTstep = JDNDaysUTstepoud;
        }
      } while (doneoneday == 0 && (JDNDaysUTfinal - JDNDaysUTstep) * Sgn(DayStep) > 0);
    }
    d = (JDNDaysUTfinal - JDNDaysUTstep) * Sgn(DayStep);
    if (d <= 0 || d >= maxlength) {
      dret[0] = JDNDaysUTinp; /* no date found, just return input */
      retval = -2; /* marks "not found" within synodic period */
      serr.setLength(0);
      serr.append("heliacal event not found within maxlength " + maxlength + "\n");
//    goto swe_heliacal_err;
      return goto_swe_heliacal_err1(serr, serr_ret, retval);
    } 
    direct = TimeStepDefault / 24.0 / 60.0;
    if (DayStep < 0) direct = -direct;
    if ((helflag & SweConst.SE_HELFLAG_AVKIND_VR) != 0) {
      /*te bepalen m.b.v. walkthrough*/
      TimeStep = direct;
      TbVR = 0;
      TimePointer = JDNarcvisUT;
      if (DeterTAV(dobs, TimePointer, dgeo, datm, ObjectName, helflag, OldestMinTAV, serr) == SweConst.ERR)
        return SweConst.ERR;
      TimePointer = TimePointer + TimeStep;
      if (DeterTAV(dobs, TimePointer, dgeo, datm, ObjectName, helflag, MinTAVoud, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (MinTAVoud[0] > OldestMinTAV[0]) {
        TimePointer = JDNarcvisUT;
        TimeStep = -TimeStep;
        MinTAVact[0] = OldestMinTAV[0];
      } else {
        MinTAVact[0] = MinTAVoud[0];
        MinTAVoud[0] = OldestMinTAV[0];
      }
      /*TimePointer = TimePointer - Timestep*/
      do {
        TimePointer = TimePointer + TimeStep;
        OldestMinTAV[0] = MinTAVoud[0];
        MinTAVoud[0] = MinTAVact[0];
        if (DeterTAV(dobs, TimePointer, dgeo, datm, ObjectName, helflag, MinTAVact, serr) == SweConst.ERR)
          return SweConst.ERR;
        if (MinTAVoud[0] < MinTAVact[0]) {
          extrax = x2min(MinTAVact[0], MinTAVoud[0], OldestMinTAV[0]);
          TbVR = TimePointer - (1 - extrax) * TimeStep;
        }
      } while (TbVR == 0);
      JDNarcvisUT = TbVR;
    }
    /*if (strncmp(AVkind, "pto", 3) == 0) */
    if ((helflag & SweConst.SE_HELFLAG_AVKIND_PTO) != 0) {
      do {
        OudeDatum = JDNarcvisUT;
        JDNarcvisUT = JDNarcvisUT - direct;
        tjd_tt = JDNarcvisUT + DeltaT(JDNarcvisUT, 0) / D2S;
        if (Planet != -1) {
          if ((retval = sw.swe_calc(tjd_tt, Planet, iflag, x, serr)) == SweConst.ERR)
//          goto swe_heliacal_err;
            return goto_swe_heliacal_err1(serr, serr_ret, retval);
        } else {
          if ((retval = call_swe_fixstar(ObjectName, tjd_tt, iflag, x, serr)) == SweConst.ERR)
//          goto swe_heliacal_err;
            return goto_swe_heliacal_err1(serr, serr_ret, retval);
        }
        xin[0] = x[0];
        xin[1] = x[1];
        sc.swe_azalt(JDNarcvisUT, SweConst.SE_EQU2HOR, dgeo, Pressure, Temperature, xin, xaz);
        Angle = xaz[1];
      } while (Angle > 0);
      JDNarcvisUT = (JDNarcvisUT + OudeDatum) / 2.0;
    }
    if (JDNarcvisUT < -9999999 || JDNarcvisUT > 9999999) {
      dret[0] = JDNDaysUT; /* no date found, just return input */
      serr.setLength(0);
      serr.append("no heliacal date found");
      retval = SweConst.ERR;
//    goto swe_heliacal_err;
      return goto_swe_heliacal_err1(serr, serr_ret, retval);
    }
    dret[0] = JDNarcvisUT;
    return goto_swe_heliacal_err1(serr, serr_ret, retval);
  }
  private int goto_swe_heliacal_err1(StringBuilder serr, StringBuilder serr_ret, int retval) {
    if (serr_ret != null && serr.length() != 0) {
      serr_ret.setLength(0);
      serr_ret.append(serr);
    }
    return retval;
  }

  private int get_asc_obl(double tjd, int ipl, String star, int iflag, double[] dgeo, boolean desc_obl, double[] daop, StringBuilder serr) {
    return get_asc_obl(tjd, ipl, new StringBuilder(star), iflag, dgeo, desc_obl, daop, serr);
  }
  private int get_asc_obl(double tjd, int ipl, StringBuilder star, int iflag, double[] dgeo, boolean desc_obl, double[] daop, StringBuilder serr) {
    int retval;
    int epheflag = iflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    double x[] = new double[6], adp;
    String s = "";
    StringBuilder star2 = new StringBuilder(star);
    if (ipl == -1) {
      if ((retval = sw.swe_fixstar(star2, tjd, epheflag | SweConst.SEFLG_EQUATORIAL, x, serr)) == SweConst.ERR)
        return SweConst.ERR;
    } else {
      if ((retval = sw.swe_calc(tjd, ipl, epheflag | SweConst.SEFLG_EQUATORIAL, x, serr)) == SweConst.ERR)
        return SweConst.ERR;
    }
    adp = SMath.tan(dgeo[1] * SwissData.DEGTORAD) * SMath.tan(x[1] * SwissData.DEGTORAD);
    if (SMath.abs(adp) > 1) {
      if (star != null && star.length() > 0)
        s = star.toString();
      else
        s = sw.swe_get_planet_name(ipl);
      serr.setLength(0);
      serr.append(s + " is circumpolar, cannot calculate heliacal event");
      return -2;
    }
    adp = SMath.asin(adp) / SwissData.DEGTORAD;
    if (desc_obl)
      daop[0] = x[0] + adp;
    else
      daop[0] = x[0] - adp;
    daop[0] = sl.swe_degnorm(daop[0]);
    return SweConst.OK;
  }

  private int get_asc_obl_diff(double tjd, int ipl, StringBuilder star, int iflag, double[] dgeo, boolean desc_obl, boolean is_acronychal, double[] dsunpl, StringBuilder serr) {
    int retval = SweConst.OK;
    double[] aosun = new double[1], aopl = new double[1];
    /* ascensio obliqua of sun */
    retval = get_asc_obl(tjd, SweConst.SE_SUN, "", iflag, dgeo, desc_obl, aosun, serr);
    if (retval != SweConst.OK)
      return retval;
    if (is_acronychal) {
      if (desc_obl == true)	// desc_obl = !desc_obl;
        desc_obl = false;
      else
        desc_obl = true;
    }
    /* ascensio obliqua of body */
    retval = get_asc_obl(tjd, ipl, star, iflag, dgeo, desc_obl, aopl, serr);
    if (retval != SweConst.OK)
      return retval;
    dsunpl[0] = sl.swe_degnorm(aosun[0] - aopl[0]);
    if (is_acronychal)
      dsunpl[0] = sl.swe_degnorm(dsunpl[0] - 180);
    if (dsunpl[0] > 180) dsunpl[0] -= 360;
    return SweConst.OK;
  }


  /* times of 
   * - superior and inferior conjunction (Mercury and Venus)
   * - conjunction and opposition (ipl >= Mars)
   */
  private double tcon[] = new double[] {
    0, 0, 
    2451550, 2451550,  /* Moon */
    2451604, 2451670,  /* Mercury */
    2451980, 2452280,  /* Venus */
    2451727, 2452074,  /* Mars */
    2451673, 2451877,  /* Jupiter */ 
    2451675, 2451868,  /* Saturn */
    2451581, 2451768,  /* Uranus */ 
    2451568, 2451753,  /* Neptune */
  };

  private int find_conjunct_sun(double tjd_start, int ipl, int helflag, int TypeEvent, double[] tjd, StringBuilder serr) {
    int epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    int i;
    double tjdcon, tjd0, ds, dsynperiod, x[]=new double[6], xs[]=new double[6], daspect = 0;
    if (ipl >= SweConst.SE_MARS && TypeEvent >= 3)
      daspect = 180;
    i = (TypeEvent - 1) / 2 + ipl * 2;
    tjd0 = tcon[i];
    dsynperiod = get_synodic_period(ipl);
    // type cast to "floor"???
    // tjdcon = tjd0 + ((floor) ((tjd_start - tjd0) / dsynperiod) + 1) * dsynperiod;
    tjdcon = tjd0 + (SMath.floor(((tjd_start - tjd0) / dsynperiod)) + 1) * dsynperiod;
    ds = 100;
    while (ds > 0.5) {
      if (sw.swe_calc(tjdcon, ipl, epheflag|SweConst.SEFLG_SPEED, x, serr) == SweConst.ERR)
        return SweConst.ERR;
      if (sw.swe_calc(tjdcon, SweConst.SE_SUN, epheflag|SweConst.SEFLG_SPEED, xs, serr) == SweConst.ERR)
        return SweConst.ERR;
      ds = sl.swe_degnorm(x[0] - xs[0] - daspect);
      if (ds > 180) ds -= 360;
      tjdcon -= ds / (x[3] - xs[3]);
    }
    tjd[0] = tjdcon;
    return SweConst.OK;
  }

  private int get_asc_obl_with_sun(double tjd_start, int ipl, StringBuilder star, int helflag, int evtyp, double dperiod, double[] dgeo, double[] tjdret, StringBuilder serr) {
    int i, retval;
    boolean is_acronychal = false;
    int epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    double dsunpl[] = new double[]{1}, dsunpl_save, dsunpl_test[] = new double[1], tjd, daystep;
    boolean desc_obl = false, retro = false;
    if (evtyp == SweConst.SE_EVENING_LAST || evtyp == SweConst.SE_EVENING_FIRST)
      desc_obl = true;
    if (evtyp == SweConst.SE_MORNING_FIRST || evtyp == SweConst.SE_EVENING_LAST)
      retro = true;
    if (evtyp == SweConst.SE_ACRONYCHAL_RISING)
      desc_obl = true;
    if (evtyp == SweConst.SE_ACRONYCHAL_RISING || evtyp ==  SweConst.SE_ACRONYCHAL_SETTING) {
      is_acronychal = true;
      if (ipl != SweConst.SE_MOON)
        retro = true;
    }
    //  if (evtyp == 3 || evtyp == 4)
    //    dangsearch = 180;
    /* find date when sun and object have the same ascensio obliqua */
    tjd = tjd_start;
    dsunpl_save = -999999999;
    retval = get_asc_obl_diff(tjd, ipl, star, epheflag, dgeo, desc_obl, is_acronychal, dsunpl, serr);
    if (retval != SweConst.OK)  /* retval may be ERR or -2 */
      return retval;
    daystep = 20;
    i = 0;
    while (dsunpl_save == -999999999 ||
        /*fabs(dsunpl - dsunpl_save) > 180 ||*/
        SMath.abs(dsunpl[0]) + SMath.abs(dsunpl_save) > 180 ||
        (retro && !(dsunpl_save < 0 && dsunpl[0] >= 0)) ||
        (!retro && !(dsunpl_save >= 0 && dsunpl[0] < 0))) {
      i++;
      if (i > 5000) {
        serr.setLength(0);
        serr.append("loop in get_asc_obl_with_sun() (1)");
        return SweConst.ERR;
      }
      dsunpl_save = dsunpl[0];
      tjd += 10.0;
      if (dperiod > 0 && tjd - tjd_start > dperiod)
        return -2;
      retval = get_asc_obl_diff(tjd, ipl, star, epheflag, dgeo, desc_obl, is_acronychal, dsunpl, serr);
      if (retval != SweConst.OK)  /* retval may be ERR or -2 */
        return retval;
    }
    tjd_start = tjd - daystep;
    daystep /= 2.0;
    tjd = tjd_start + daystep;
    retval = get_asc_obl_diff(tjd, ipl, star, epheflag, dgeo, desc_obl, is_acronychal, dsunpl_test, serr);
    if (retval != SweConst.OK)  /* retval may be ERR or -2 */
      return retval;
    i = 0;
    while (SMath.abs(dsunpl[0]) > 0.00001) {
      i++;
      if (i > 5000) {
        serr.setLength(0);
        serr.append("loop in get_asc_obl_with_sun() (2)");
        return SweConst.ERR;
      }
      if (dsunpl_save * dsunpl_test[0] >= 0) {
        dsunpl_save = dsunpl_test[0];
        tjd_start = tjd;
      } else {
        dsunpl[0] = dsunpl_test[0];
      }
      daystep /= 2.0;
      tjd = tjd_start + daystep;
      retval = get_asc_obl_diff(tjd, ipl, star, epheflag, dgeo, desc_obl, is_acronychal, dsunpl_test, serr);
      if (retval != SweConst.OK)  /* retval may be ERR or -2 */
        return retval;
    }
    tjdret[0] = tjd;
    return SweConst.OK;
  }



  private int get_heliacal_day(double tjd, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int helflag, int TypeEvent, double[] thel, StringBuilder serr) {
    int is_rise_or_set = 0, ndays, retval, retval_old;
    double direct_day = 0, direct_time = 0, tfac, tend, daystep, tday, vdelta, tret[] =new double[1];
    double darr[] = new double[30], vd, dmag[] = new double[1];
    int ipl = DeterObject(ObjectName);
    /* 
     * find the day and minute on which the object becomes visible 
     */
    switch (TypeEvent) {
      /* morning first */
      case 1: is_rise_or_set = SweConst.SE_CALC_RISE; 
        direct_day = 1; direct_time = -1;
        break;
      /* evening last */
      case 2: is_rise_or_set = SweConst.SE_CALC_SET; 
        direct_day = -1; direct_time = 1;
        break;
      /* evening first */
        case 3: is_rise_or_set = SweConst.SE_CALC_SET; 
        direct_day = 1; direct_time = 1;
        break;
      /* morning last */
      case 4: is_rise_or_set = SweConst.SE_CALC_RISE; 
        direct_day = -1; direct_time = -1;
        break;
    }
    tfac = 1;
    switch (ipl) {
      case SweConst.SE_MOON: 
        ndays = 16; 
        daystep = 1;
        break;
      case SweConst.SE_MERCURY: 
        ndays = 60; tjd -= 0 * direct_day; 
        daystep = 5;
        tfac = 5;
        break;
      case SweConst.SE_VENUS: 
        ndays = 300; tjd -= 30 * direct_day; 
        daystep = 5;
        if (TypeEvent >= 3) {
          daystep = 15;
          tfac = 3;
        }
        break;
      case SweConst.SE_MARS: 
        ndays = 400; 
        daystep = 15;
        tfac = 5;
        break; 
      case SweConst.SE_SATURN: 
        ndays = 300; 
        daystep = 20;
        tfac = 5;
        break; 
      case -1:
        ndays = 300;
        if (call_swe_fixstar_mag(ObjectName, dmag, 0, serr) == SweConst.ERR) {
          return SweConst.ERR;
        }
        daystep = 15;
        tfac = 10;
        if (dmag[0] > 2) {
          daystep = 15;
        }
        if (dmag[0] < 0) {
	  tfac = 3;
        }
        break;
      default:
        ndays = 300; 
        daystep = 15;
        tfac = 3;
        break;
    }
    tend = tjd + ndays * direct_day;
    retval_old = -2;
    for (tday = tjd;
         (direct_day > 0 && tday < tend) || (direct_day < 0 && tday > tend);
         tday += daystep * direct_day) {
      vdelta = -100; 
      if ((retval = my_rise_trans(tday, SweConst.SE_SUN, "", is_rise_or_set, helflag, dgeo, datm, tret, serr)) == SweConst.ERR) {
        return SweConst.ERR;
      }
      /* sun does not rise: try next day */
      if (retval == -2) {
        retval_old = retval;
        continue;
      }
      retval = swe_vis_limit_mag(tret[0], dgeo, datm, dobs, ObjectName, helflag, darr, serr);
      if (retval == SweConst.ERR)
        return SweConst.ERR;
      /*  object has appeared above horizon: reduce daystep */
      if (retval_old == -2 && retval >= 0 && daystep > 1) {
        retval_old = retval;
        tday -= daystep * direct_day;
        daystep = 1;
        /* Note: beyond latitude 55N (?), Mars can have a morning last. 
         * If the period of visibility is less than 5 days, we may miss the
         * event. I don't know if this happens */
        if (ipl >= SweConst.SE_MARS || ipl == -1)
          daystep = 5;
        continue;
      }
      retval_old = retval;
      /*  object below horizon: try next day */
      if (retval == -2)
        continue;
      vdelta = darr[0] - darr[7];
      /* find minute of object's becoming visible */
      while (retval != -2 && (vd = darr[0] - darr[7]) < 0) {
        if (vd < -1.0)
          tret[0] += 5.0 / 1440.0 * direct_time * tfac;
        else if (vd < -0.5)
          tret[0] += 2.0 / 1440.0 * direct_time * tfac;
        else if (vd < -0.1)
          tret[0] += 1.0 / 1440.0 * direct_time * tfac;
        else 
          tret[0] += 1.0 / 1440.0 * direct_time;
        retval = swe_vis_limit_mag(tret[0], dgeo, datm, dobs, ObjectName, helflag, darr, serr);
        if (retval == SweConst.ERR)
          return SweConst.ERR;
      }
      vdelta = darr[0] - darr[7];
      /* object is visible, save time of appearance */
      if (vdelta > 0) {
        if ((ipl >= SweConst.SE_MARS || ipl == -1) && daystep > 1) {
          tday -= daystep * direct_day;
          daystep = 1;
        } else {
          thel[0] = tret[0];
          return SweConst.OK;
        }
      }
    }
    serr.setLength(0);
    serr.append("heliacal event does not happen");
    return -2;
  }



  private int time_optimum_visibility(double tjd, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int helflag, double[] tret, int tret_offset, StringBuilder serr) {
    int retval, retval_sv, i;
    double d, vl, darr[]=new double[10], phot_scot_opic, phot_scot_opic_sv;
    tret[tret_offset] = tjd;
    retval = swe_vis_limit_mag(tjd, dgeo, datm, dobs, ObjectName, helflag, darr, serr);
    if (retval == SweConst.ERR) return SweConst.ERR;
    retval_sv = retval;
    vl = darr[0] - darr[7];
    phot_scot_opic_sv = retval & SweConst.SE_SCOTOPIC_FLAG;
    for (i = 0, d = 100.0 / 86400.0; i < 3; i++, d /= 10.0) {
      while((retval = swe_vis_limit_mag(tjd - d, dgeo, datm, dobs, ObjectName, helflag, darr, serr)) >= 0 
          && darr[0] > darr[7] 
          && darr[0] - darr[7] > vl) { 
        tjd -= d; vl = darr[0] - darr[7]; 
        retval_sv = retval;
        phot_scot_opic_sv = retval & SweConst.SE_SCOTOPIC_FLAG;
      /*  printf("1: %f\n", darr[8]);*/
      }
      if (retval == SweConst.ERR) return SweConst.ERR;
      while((retval = swe_vis_limit_mag(tjd + d, dgeo, datm, dobs, ObjectName, helflag, darr, serr)) >= 0 
          && darr[0] > darr[7] 
          && darr[0] - darr[7] > vl) { 
        tjd += d; vl = darr[0] - darr[7]; 
        retval_sv = retval;
        phot_scot_opic_sv = retval & SweConst.SE_SCOTOPIC_FLAG;
      /*  printf("2: %f\n", darr[8]);*/
      }
      if (retval == SweConst.ERR) return SweConst.ERR;
    }
      /*  printf("3: %f <-> %f\n", darr[8], phot_scot_opic_sv);*/
    tret[tret_offset] = tjd;
    if (retval >= 0) {
      /* search for optimum came to an end because change scotopic/photopic: */
      phot_scot_opic = (retval & SweConst.SE_SCOTOPIC_FLAG);
      if (phot_scot_opic_sv != phot_scot_opic) {
        /* calling function writes warning into serr */
        return -2;
      }
      /* valid result found but it is close to the scotopic/photopic limit */
      if ((retval_sv & SweConst.SE_MIXEDOPIC_FLAG) != 0) {
        return -2;
      }
    }
    return SweConst.OK;
  }

  private int time_limit_invisible(double tjd, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int helflag, int direct, double[] tret, int tret_offset, StringBuilder serr) {
    int retval, retval_sv, i, ncnt = 3;
    double d = 0, darr[]=new double[10], phot_scot_opic, phot_scot_opic_sv;
    double d0 = 100.0 / 86400.0;
    tret[tret_offset] = tjd;
    if (ObjectName.toString().equals("moon")) {
      d0 *= 10;
      ncnt = 4;
    }
    retval = swe_vis_limit_mag(tjd + d * direct, dgeo, datm, dobs, ObjectName, helflag, darr, serr);
    if (retval == SweConst.ERR) return SweConst.ERR;
    retval_sv = retval;
    phot_scot_opic_sv = retval & SweConst.SE_SCOTOPIC_FLAG;
    for (i = 0, d = d0; i < ncnt; i++, d /= 10.0) {
      while((retval = swe_vis_limit_mag(tjd + d * direct, dgeo, datm, dobs, ObjectName, helflag, darr, serr)) >= 0 
          && darr[0] > darr[7]) { 
        tjd += d * direct; 
        retval_sv = retval;
        phot_scot_opic_sv = retval & SweConst.SE_SCOTOPIC_FLAG;
    /*    printf("%d: %f\n", direct, darr[8]); */
      }
    }
     /*   printf("4: %f, %f/%f %f <-> %f\n", darr[8], darr[0], darr[7], tjd, phot_scot_opic_sv); */
    tret[tret_offset] = tjd;
    /* if object disappears at setting, retval is -2, but we want it SweConst.OK, and
     * also suppress the warning "object is below local horizon" */
    serr.setLength(0); 
    if (retval >= 0) {
      /* search for limit came to an end because change scotopic/photopic: */
      phot_scot_opic = (retval & SweConst.SE_SCOTOPIC_FLAG);
      if (phot_scot_opic_sv != phot_scot_opic) {
        /* calling function writes warning into serr */
        return -2;
      }
      /* valid result found but it is close to the scotopic/photopic limit */
      if ((retval_sv & SweConst.SE_MIXEDOPIC_FLAG) != 0) {
        return -2;
      }
    }
    return SweConst.OK;
  }

  private int get_acronychal_day(double tjd, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int helflag, int TypeEvent, double[] thel, StringBuilder serr) {
    double tjd_intern[] = new double[] {tjd};
    double tret[] = new double[1], tret_dark[] = new double[1], darr[] = new double[30], dtret;
    /* x[6], xaz[6], alto, azio, alto_dark, azio_dark;*/
    int retval, is_rise_or_set, direct;
    int ipl = DeterObject(ObjectName);
    helflag |= SweConst.SE_HELFLAG_VISLIM_PHOTOPIC;
    /*int32 epheflag = helflag & (SEFLG_JPLEPH|SEFLG_SWIEPH|SEFLG_MOSEPH);*/
    /* int32 iflag = epheflag | SEFLG_EQUATORIAL | SEFLG_TOPOCTR;*/
    if (TypeEvent == 3 || TypeEvent == 5) {
      is_rise_or_set = SweConst.SE_CALC_RISE; 
      /* tret = tjdc - 3;
      if (ipl >= SE_MARS)
        tret = tjdc - 3;*/
      direct = -1;
    } else {
      is_rise_or_set = SweConst.SE_CALC_SET; 
      /*tret = tjdc + 3;
      if (ipl >= SE_MARS)
        tret = tjdc + 3;*/
      direct = 1;
    }
    dtret = 999;
    while (SMath.abs(dtret) > 0.5 / 1440.0) {
      tjd_intern[0] += 0.7 * direct;
      if (direct < 0) tjd_intern[0] -= 1;
      retval = my_rise_trans(tjd_intern[0], ipl, ObjectName.toString(), is_rise_or_set, helflag, dgeo, datm, tjd_intern, serr);
      if (retval == SweConst.ERR) return SweConst.ERR;
      retval = swe_vis_limit_mag(tjd_intern[0], dgeo, datm, dobs, ObjectName, helflag, darr, serr);
      while(darr[0] < darr[7]) {
        tjd_intern[0] += 10.0 / 1440.0 * -direct;
        retval = swe_vis_limit_mag(tjd_intern[0], dgeo, datm, dobs, ObjectName, helflag, darr, serr);
      }
      retval = time_limit_invisible(tjd_intern[0], dgeo, datm, dobs, ObjectName, helflag | SweConst.SE_HELFLAG_VISLIM_DARK, direct, tret_dark, 0, serr);
      if (retval == SweConst.ERR) return SweConst.ERR;
      retval = time_limit_invisible(tjd_intern[0], dgeo, datm, dobs, ObjectName, helflag | SweConst.SE_HELFLAG_VISLIM_NOMOON, direct, tret, 0, serr);
      if (retval == SweConst.ERR) return SweConst.ERR;
      dtret = SMath.abs(tret[0] - tret_dark[0]);
    }
    if (azalt_cart(tret[0], dgeo, datm, new StringBuilder("sun"), helflag, darr, serr) == SweConst.ERR)
      return SweConst.ERR;
    thel[0] = tret[0];
    if (darr[1] < -12) {
      serr.setLength(0);
      serr.append("acronychal rising/setting not available, " + darr[1]);
      return SweConst.OK;
    } else {
      serr.setLength(0);
      serr.append("solar altitude, " + darr[1]);
    }
    return SweConst.OK;
  }

  private int get_heliacal_details(double tday, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int TypeEvent, int helflag, double[] dret, StringBuilder serr) {
    int i, retval, direct;
    boolean optimum_undefined, limit_1_undefined, limit_2_undefined;
    /* find next optimum visibility */
    optimum_undefined = false;
    retval = time_optimum_visibility(tday, dgeo, datm, dobs, ObjectName, helflag, dret, 1, serr);
    if (retval == SweConst.ERR) return SweConst.ERR;
    if (retval == -2) {
      retval = SweConst.OK;
      optimum_undefined = true; /* change photopic <-> scotopic vision */
    }
    /* find moment of becoming visible */
    direct = 1;
    if (TypeEvent == 1 || TypeEvent == 4)
      direct = -1;
    limit_1_undefined = false;
    retval = time_limit_invisible(tday, dgeo, datm, dobs, ObjectName, helflag, direct, dret, 0, serr);
    if (retval == SweConst.ERR) return SweConst.ERR;
    if (retval == -2) {
      retval = SweConst.OK;
      limit_1_undefined = true; /* change photopic <-> scotopic vision */
    }
    /* find moment of end of visibility */
    direct *= -1;
    limit_2_undefined = false;
    retval = time_limit_invisible(dret[1], dgeo, datm, dobs, ObjectName, helflag, direct, dret, 2, serr);
    if (retval == SweConst.ERR) return SweConst.ERR;
    if (retval == -2) {
      retval = SweConst.OK;
      limit_2_undefined = true; /* change photopic <-> scotopic vision */
    }
    /* correct sequence of times: 
     * with event types 2 and 3 swap dret[0] and dret[2] */
    if (TypeEvent == 2 || TypeEvent == 3) {
      tday = dret[2];
      dret[2] = dret[0];
      dret[0] = tday;
      i = (limit_1_undefined?1:0);
      limit_1_undefined = limit_2_undefined;
      limit_2_undefined = (i != 0);
    }
    /*if (retval == SweConst.OK && dret[0] == dret[1]) */
    if (optimum_undefined || limit_1_undefined || limit_2_undefined) {
      serr.setLength(0);
      serr.append("return values [");
      if (limit_1_undefined)
        serr.append("0,");
      if (optimum_undefined)
        serr.append("1,");
      if (limit_2_undefined)
        serr.append("2,");
      serr.append("] are uncertain due to change between photopic and scotopic vision");
    }
    return SweConst.OK;
  }

  private int heliacal_ut_vis_lim(double tjd_start, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int TypeEventIn, int helflag, double[] dret, StringBuilder serr_ret) {
    int i;
    double d, darr[]=new double[10], direct = 1, tjd[] = new double[1], tday[] = new double[1];
    int epheflag, retval = SweConst.OK, helflag2;
    int iflag, ipl;
    int TypeEvent = TypeEventIn;
    StringBuilder serr = new StringBuilder();
    for (i = 0; i < 10; i++)
      dret[i] = 0;
    dret[0] = tjd_start;
    ipl = DeterObject(ObjectName);
    epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    iflag = SweConst.SEFLG_TOPOCTR | SweConst.SEFLG_EQUATORIAL | epheflag;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0) 
      iflag |= SweConst.SEFLG_NONUT | SweConst.SEFLG_TRUEPOS;
    if (ipl == SweConst.SE_MERCURY)
      tjd[0] = tjd_start - 30;
    else
      tjd[0] = tjd_start - 50; /* -50 makes sure, that no event is missed, 
                              * but may return an event before start date */
    helflag2 = helflag;
    /*helflag2 &= ~SweConst.SE_HELFLAG_HIGH_PRECISION;*/
    /* 
     * heliacal event
     */
    if (ipl == SweConst.SE_MERCURY || ipl == SweConst.SE_VENUS || TypeEvent <= 2) {
      if (ipl == -1) {
        /* find date when star rises with sun (cosmic rising) */
        retval = get_asc_obl_with_sun(tjd[0], ipl, ObjectName, helflag, TypeEvent, 0, dgeo, tjd, serr);
        if (retval != SweConst.OK)
//        goto swe_heliacal_err; /* retval may be -2 or SweConst.ERR */
          return goto_swe_heliacal_err2(serr, serr_ret, retval);
        } else {
          /* find date of conjunction of object with sun */
          if ((retval = find_conjunct_sun(tjd[0], ipl, helflag, TypeEvent, tjd, serr)) == SweConst.ERR) {
//        goto swe_heliacal_err;
            return goto_swe_heliacal_err2(serr, serr_ret, retval);
          }
      }
      /* find the day and minute on which the object becomes visible */
      retval = get_heliacal_day(tjd[0], dgeo, datm, dobs, ObjectName, helflag2, TypeEvent, tday, serr); 
      if (retval != SweConst.OK)
//      goto swe_heliacal_err;
        return goto_swe_heliacal_err2(serr, serr_ret, retval);
    /* 
     * acronychal event
     */
    } else {
      if (true || ipl == -1) {
        /*retval = get_asc_obl_acronychal(tjd, ipl, ObjectName, helflag2, TypeEvent, dgeo, &tjd, serr);*/
        retval = get_asc_obl_with_sun(tjd[0], ipl, ObjectName, helflag, TypeEvent, 0, dgeo, tjd, serr);
        if (retval != SweConst.OK)
//        goto swe_heliacal_err;
          return goto_swe_heliacal_err2(serr, serr_ret, retval);
      } else {
        /* find date of conjunction of object with sun */
        if ((retval = find_conjunct_sun(tjd[0], ipl, helflag, TypeEvent, tjd, serr)) == SweConst.ERR)
//        goto swe_heliacal_err;
          return goto_swe_heliacal_err2(serr, serr_ret, retval);
      }
      tday = tjd;
      retval = get_acronychal_day(tjd[0], dgeo, datm, dobs, ObjectName, helflag2, TypeEvent, tday, serr); 
      if (retval != SweConst.OK)
        // goto swe_heliacal_err;
        return goto_swe_heliacal_err2(serr, serr_ret, retval);
    }
    dret[0] = tday[0];
    if ((helflag & SweConst.SE_HELFLAG_NO_DETAILS) == 0) {
      /* more precise event times for 
       * - morning first, evening last
       * - venus and mercury's evening first and morning last
       */
      if (ipl == SweConst.SE_MERCURY || ipl == SweConst.SE_VENUS || TypeEvent <= 2) {
        retval = get_heliacal_details(tday[0], dgeo, datm, dobs, ObjectName, TypeEvent, helflag2, dret, serr);
        if (retval == SweConst.ERR) // goto swe_heliacal_err;
          return goto_swe_heliacal_err2(serr, serr_ret, retval);
      } else if (false) {
        if (TypeEvent == 4 || TypeEvent == 6) direct = -1;
        for (i = 0, d = 100.0 / 86400.0; i < 3; i++, d /= 10.0) {
          while((retval = swe_vis_limit_mag(dret[0] + d * direct, dgeo, datm, dobs, ObjectName, helflag, darr, serr)) == -2 || (retval >= 0 && darr[0] < darr[7])) { 
            dret[0] += d * direct; 
          }
        }
        /* the last time step must be added */
        if (retval == SweConst.OK)
          dret[0] += 1.0 / 86400.0 * direct;
      }
    } /* if (1) */
    return goto_swe_heliacal_err2(serr, serr_ret, retval);
  }
  int goto_swe_heliacal_err2(StringBuilder serr, StringBuilder serr_ret, int retval) {
    if (serr_ret != null && serr.length() != 0)
      serr_ret.append(serr);
    return retval;
  }

  /*###################################################################*/
  private int moon_event_vis_lim(double tjdstart, double[] dgeo, double[] datm, double[] dobs, int TypeEvent, int helflag, double[] dret, StringBuilder serr_ret) {
    double tjd[] = new double[1], trise[] = new double[1];
    StringBuilder serr = new StringBuilder();
    //JAVA char ObjectName[30];
    StringBuilder ObjectName = new StringBuilder();
    int iflag, ipl, retval, helflag2, direct;
    int epheflag = helflag & (SweConst.SEFLG_JPLEPH|SweConst.SEFLG_SWIEPH|SweConst.SEFLG_MOSEPH);
    dret[0] = tjdstart; /* will be returned in error case */
    if (TypeEvent == 1 || TypeEvent == 2) {
      if (serr != null) {
        serr.setLength(0);
        serr.append("error: the moon has no morning first or evening last");
      }
      return SweConst.ERR;
    }
    ObjectName.append("moon");
    ipl = SweConst.SE_MOON;
    iflag = SweConst.SEFLG_TOPOCTR | SweConst.SEFLG_EQUATORIAL | epheflag;
    if ((helflag & SweConst.SE_HELFLAG_HIGH_PRECISION) == 0)
      iflag |= SweConst.SEFLG_NONUT|SweConst.SEFLG_TRUEPOS;
    helflag2 = helflag;
    helflag2 &= ~SweConst.SE_HELFLAG_HIGH_PRECISION;
    /* check Synodic/phase Period */
    tjd[0] = tjdstart - 30; /* -50 makes sure, that no event is missed, 
                           * but may return an event before start date */
    if ((retval = find_conjunct_sun(tjd[0], ipl, helflag, TypeEvent, tjd, serr)) == SweConst.ERR)
      return SweConst.ERR;
    /* find the day and minute on which the object becomes visible */
    retval = get_heliacal_day(tjd[0], dgeo, datm, dobs, ObjectName, helflag2, TypeEvent, tjd, serr); 
    if (retval != SweConst.OK)
//    goto moon_event_err;
      return goto_moon_event_err(serr, serr_ret, retval);
    dret[0] = tjd[0];
    /* find next optimum visibility */
    retval = time_optimum_visibility(tjd[0], dgeo, datm, dobs, ObjectName, helflag, tjd, 0, serr);
    if (retval == SweConst.ERR) // goto moon_event_err;
      return goto_moon_event_err(serr, serr_ret, retval);
    dret[1] = tjd[0];
    /* find moment of becoming visible */
    /* Note: The on the day of fist light the moon may become visible 
     * already during day. It also may appear during day, disappear again
     * and then reappear after sunset */
    direct = 1;
    if (TypeEvent == 4)
      direct = -1;
    retval = time_limit_invisible(tjd[0], dgeo, datm, dobs, ObjectName, helflag, direct, tjd, 0, serr);
    if (retval == SweConst.ERR) // goto moon_event_err;
      return goto_moon_event_err(serr, serr_ret, retval);
    dret[2] = tjd[0];
    /* find moment of end of visibility */
    direct *= -1;
    retval = time_limit_invisible(dret[1], dgeo, datm, dobs, ObjectName, helflag, direct, tjd, 0, serr);
    dret[0] = tjd[0];
    if (retval == SweConst.ERR) // goto moon_event_err;
      return goto_moon_event_err(serr, serr_ret, retval);
    /* if the moon is visible before sunset, we return sunset as start time */
    if (TypeEvent == 3) {
      if ((retval = my_rise_trans(tjd[0], SweConst.SE_SUN, "", SweConst.SE_CALC_SET, helflag, dgeo, datm, trise, serr)) == SweConst.ERR)
        return SweConst.ERR;
      if (trise[0] < dret[1]) {
        dret[0] = trise[0];
        /* do not warn, it happens too often */
        /*strcpy(serr, "start time given is sunset, but moon is observable before that");*/
      }
    /* if the moon is visible after sunrise, we return sunrise as end time */
    } else {
      if ((retval = my_rise_trans(dret[1], SweConst.SE_SUN, "", SweConst.SE_CALC_RISE, helflag, dgeo, datm, trise, serr)) == SweConst.ERR)
        return SweConst.ERR;
      if (dret[0] > trise[0]) {
        dret[0] = trise[0];
        /* do not warn, it happens too often */
        /*strcpy(serr, "end time given is sunrise, but moon is observable after that");*/
      }
    }
    /* correct order of the three times: */
    if (TypeEvent == 4) {
      tjd[0] = dret[0];
      dret[0] = dret[2];
      dret[2] = tjd[0];
    }
    return goto_moon_event_err(serr, serr_ret, retval);
  }
  int goto_moon_event_err(StringBuilder serr, StringBuilder serr_ret, int retval) {
    if (serr_ret != null && serr.length() != 0)
      serr_ret.append(serr);
    return retval;
  }

  private int MoonEventJDut(double JDNDaysUTStart, double[] dgeo, double[] datm, double[] dobs, int TypeEvent, int helflag, double[] dret, StringBuilder serr) {
    int avkind = helflag & SweConst.SE_HELFLAG_AVKIND;
    if (avkind != 0) 
      return moon_event_arc_vis(JDNDaysUTStart, dgeo, datm, dobs, TypeEvent, helflag, dret, serr);
    else
      return moon_event_vis_lim(JDNDaysUTStart, dgeo, datm, dobs, TypeEvent, helflag, dret, serr);
  }

  private int heliacal_ut(double JDNDaysUTStart, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectName, int TypeEventIn, int helflag, double[] dret, StringBuilder serr_ret) {
    int avkind = helflag & SweConst.SE_HELFLAG_AVKIND;
    if (avkind != 0) 
      return heliacal_ut_arc_vis(JDNDaysUTStart, dgeo, datm, dobs, ObjectName, TypeEventIn, helflag, dret, serr_ret);
    else
      return heliacal_ut_vis_lim(JDNDaysUTStart, dgeo, datm, dobs, ObjectName, TypeEventIn, helflag, dret, serr_ret);
  }

  /*' Magn [-]
  ' tjd_ut            start date (JD) for event search
  ' dgeo[3]           geogr. longitude, latitude, eye height (m above sea level)
  ' datm[4]           atm. pressure, temperature, RH, and VR
  ' - pressure        atmospheric pressure (mbar, =hPa) default 1013.25hPa
  ' - temperature      deg C, default 15 deg C (if at
  '                   If both attemp and atpress are 0, a temperature and
  '                   atmospheric pressure are estimated from the above-mentioned
  '                   default values and the height above sea level.
  ' - RH              relative humidity in %
  ' - VR              VR>=1: the Meteorological range: default 40 km
  '                   1>VR>0: the ktot (so the total atmospheric coefficient): 
  '                   a good default would be 0.25
  '                   VR=-1: the ktot is calculated from the other atmospheric 
  '                   constants.
  ' age [Year]        default 36, experienced sky observer in ancient times
  '                   optimum age is 23
  ' SN                Snellen factor of the visual aquity of the observer
  '                   default 1
  '                   see: http://www.i-see.org/eyecharts.html#make-your-own
  ' TypeEvent         1 morning first
  '                   2 evening last
  '                   3 evening first
  '                   4 morning last
  ' dret              output: time (tjd_ut) of heliacal event
  '                   dret[0]: beginning of visibility (Julian day number)
  '                   dret[1]: optimum visibility (Julian day number; 0 if SE_HELFLAG_AV)
  '                   dret[2]: end of visibility (Julian day number; 0 if SE_HELFLAG_AV)
  ' see http://www.iol.ie/~geniet/eng/atmoastroextinction.htm
  */
  /**
  * <b>ATTENTION: This method possibly (re-)sets a global parameter used
  * in calculation of delta T. See SweDate.setGlobalTidalAcc(double).</b>
  * @see SweDate#setGlobalTidalAcc(double)
  */
  public int swe_heliacal_ut(double JDNDaysUTStart, double[] dgeo, double[] datm, double[] dobs, StringBuilder ObjectNameIn, int TypeEvent, int helflag, double[] dret, StringBuilder serr_ret) {
    int retval, Planet, itry;
    StringBuilder ObjectName = new StringBuilder(), serr = new StringBuilder();
    String s;
    double tjd0 = JDNDaysUTStart, tjd, dsynperiod, tjdmax, tadd;
    int MaxCountSynodicPeriod = MAX_COUNT_SYNPER;
    String sevent[] = new String[] {"", "morning first", "evening last", "evening first", "morning last", "acronychal rising", "acronychal setting"};
    if (dgeo[2] < SwephData.SEI_ECL_GEOALT_MIN || dgeo[2] > SwephData.SEI_ECL_GEOALT_MAX) {
      if (serr_ret != null) {
        serr_ret.setLength(0);
        serr_ret.append(String.format(Locale.US, "location for heliacal events must be between %.0f and %.0f m above sea", SwephData.SEI_ECL_GEOALT_MIN, SwephData.SEI_ECL_GEOALT_MAX));
      }
      return SweConst.ERR;
    }
    SweDate.swi_set_tid_acc(JDNDaysUTStart, helflag, 0);
    if ((helflag & SweConst.SE_HELFLAG_LONG_SEARCH) != 0)
      MaxCountSynodicPeriod = MAX_COUNT_SYNPER_MAX;
  /*  if (helflag & SE_HELFLAG_SEARCH_1_PERIOD)
        MaxCountSynodicPeriod = 1; */
    serr.setLength(0);
    if (serr_ret != null)
      serr_ret.setLength(0);
    /* note, the fixed stars functions rewrite the star name. The input string 
       may be too short, so we have to make sure we have enough space */
    strcpy_VBsafe(ObjectName, ObjectNameIn.toString());
    default_heliacal_parameters(datm, dgeo, dobs, helflag);
    sw.swe_set_topo(dgeo[0], dgeo[1], dgeo[2]);
    Planet = DeterObject(ObjectName);
    /* 
     * Moon events
     */
    if (Planet == SweConst.SE_MOON) {
      if (TypeEvent == 1 || TypeEvent == 2) {
        if (serr_ret != null) {
          serr_ret.setLength(0);
          serr_ret.append(sevent[TypeEvent] + " (event type " + TypeEvent + ") does not exist for the moon\n");
        }
        return SweConst.ERR;
      }
      tjd = tjd0;
      retval = MoonEventJDut(tjd, dgeo, datm, dobs, TypeEvent, helflag, dret, serr);
      while (retval != -2 && dret[0] < tjd0) {
        tjd += 15;
        serr.setLength(0);
        retval = MoonEventJDut(tjd, dgeo, datm, dobs, TypeEvent, helflag, dret, serr);
      }
      if (serr_ret != null && serr.length() != 0) {
        serr_ret.setLength(0);
        serr_ret.append(serr);
      }
      return retval;
    }
    /* 
     * planets and fixed stars 
     */
    if ((helflag & SweConst.SE_HELFLAG_AVKIND) == 0) {
      if (Planet == -1 || Planet >= SweConst.SE_MARS) {
        if (TypeEvent == 3 || TypeEvent == 4) {
          if (serr_ret != null) {
            if (Planet == -1)
              s = ObjectName.toString();
            else
              s = sw.swe_get_planet_name(Planet);
            serr_ret.setLength(0);
            serr_ret.append(sevent[TypeEvent] + " (event type " + TypeEvent + ") does not exist for " + s + "\n");
          }
          return SweConst.ERR;
        }
      }
    }
    /* arcus visionis method: set the TypeEvent for acronychal events */
    if ((helflag & SweConst.SE_HELFLAG_AVKIND) != 0) {
      if (Planet == -1 || Planet >= SweConst.SE_MARS) {
        if (TypeEvent == SweConst.SE_ACRONYCHAL_RISING)
	  TypeEvent = 3;
        if (TypeEvent == SweConst.SE_ACRONYCHAL_SETTING)
	  TypeEvent = 4;
      }
    /* acronychal rising and setting (cosmic setting) are ill-defined.
     * We do not calculate them with the "visibility limit method" */
    } else if (true) {
      if (TypeEvent == SweConst.SE_ACRONYCHAL_RISING || TypeEvent == SweConst.SE_ACRONYCHAL_SETTING) {
        if (serr_ret != null) {
	  if (Planet == -1)
            s = ObjectName.toString();
	  else
            s = sw.swe_get_planet_name(Planet);
          serr_ret.setLength(0);
          serr_ret.append(sevent[TypeEvent] + " (event type " + TypeEvent + ") is not provided for " + s + "\n");
        }
        return SweConst.ERR;
      }
    }
    dsynperiod = get_synodic_period(Planet);
    tjdmax = tjd0 + dsynperiod * MaxCountSynodicPeriod;
    tadd = dsynperiod * 0.6;
    if (Planet == SweConst.SE_MERCURY)
      tadd = 30;
    /* 
     * this is the outer loop over n synodic periods 
     */
    tjd = tjd0;
    retval = -2;  /* indicates that another synodic period has to be done */
    for (itry = 0; 
         tjd < tjdmax && retval == -2; 
         itry++, tjd += tadd) {
      serr.setLength(0);
      retval = heliacal_ut(tjd, dgeo, datm, dobs, ObjectName, TypeEvent, helflag, dret, serr);
      /* if resulting event date < start date for search (tjd0): retry starting
       * from half a period later. The event must be found now, unless there
       * is none, as is often the case with Mercury */
      while (retval != -2 && dret[0] < tjd0) {
        tjd += tadd;
        serr.setLength(0);
        retval = heliacal_ut(tjd, dgeo, datm, dobs, ObjectName, TypeEvent, helflag, dret, serr);
      }
    }
    /* 
     * no event was found within MaxCountSynodicPeriod, return error
     */ 
    if ((helflag & SweConst.SE_HELFLAG_SEARCH_1_PERIOD) != 0 && (retval == -2 || dret[0] > tjd0 + dsynperiod * 1.5)) {
      serr.setLength(0);
      serr.append("no heliacal date found within this synodic period");
      retval = -2;
    } else if (retval == -2) {
      serr.setLength(0);
      serr.append("no heliacal date found within " + MaxCountSynodicPeriod + " synodic periods");
      retval = SweConst.ERR;
    }
    if (serr_ret != null && serr.length() != 0) {
      serr_ret.setLength(0);
      serr_ret.append(serr);
    }
    return retval;
  }
}
