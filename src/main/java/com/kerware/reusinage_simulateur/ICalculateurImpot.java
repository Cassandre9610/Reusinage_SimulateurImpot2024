package com.kerware.reusinage_simulateur;

public interface ICalculateurImpot {

    void setRevenusNetDeclarant1(int rn);
    void setRevenusNetDeclarant2( int rn );
    void setSituationFamiliale( SituationFamiliale sf );
    void setNbEnfantsACharge( int nbe );
    void setNbEnfantsSituationHandicap( int nbesh );
    void setParentIsole( boolean pi );

    void calculImpotSurRevenuNet();

    int getRevenuNetDeclatant1();
    int getRevenuNetDeclatant2();
    double getContribExceptionnelle();
    int getRevenuFiscalReference();
    int getAbattement();
    double getNbPartsFoyerFiscal();
    int getImpotAvantDecote();
    int getDecote();
    int getImpotSurRevenuNet();

}
