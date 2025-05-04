package com.kerware.reusinage_simulateur;

/**
 *  Cette classe permet de simuler le calcul de l'impôt sur le revenu
 *  en France pour l'année 2024 sur les revenus de l'année 2023 pour
 *  des cas simples de contribuables célibataires, mariés, divorcés, veufs
 *  ou pacsés avec ou sans enfants à charge ou enfants en situation de handicap
 *  et parent isolé.
 *
 *  Pas de lisibilité, pas de commentaires, pas de tests
 *  Pas de documentation, pas de gestion des erreurs
 *  Pas de logique métier, pas de modularité
 *  Pas de gestion des exceptions, pas de gestion des logs
 *  Principe "Single Responsability" non respecté
 *  Pas de traçabilité vers les exigences métier
 **/

public class Simulateur {

    // Les limites des tranches de revenus imposables
    private final int[] limites = new int[]{0,11294,28797,82341,177106,Integer.MAX_VALUE};

    // Les taux d'imposition par tranche
    private final double[] taux = new double[]{0.0,0.11,0.3,0.41,0.45};

    // Les limites des tranches pour la contribution exceptionnelle sur les hauts revenus
    private final int[] limitesCEHR = new int[]{0,250000,500000,1000000,Integer.MAX_VALUE};

    // Les taux de la contribution exceptionnelle sur les hauts revenus pour les celibataires
    private final double[] tauxCEHRCelibataire = new double[]{0.0,0.03,0.04,0.04};

    // Les taux de la contribution exceptionnelle sur les hauts revenus pour les couples
    private final double[] tauxCEHRCouple = new double[]{0.0,0.0,0.03,0.04};

    private final int NB_ENFANT_MIN = 0;
    private final int NB_ENFANT_MAX = 7;
    
    // Abattement
    private final int LIMITE_ABATTEMENT_MAX = 14171;
    private final int LIMITE_ABATTEMENT_MIN = 495;
    private final double TAUX_ABATTEMENT = 0.1;

    // Plafond de baisse maximal par demi part
    private final double PLAFOND_DEMI_PART = 1759;

    private final double SEUIL_DECOTE_DECLARANT_SEUL = 1929;
    private final double SEUIL_DECOTE_DECLARANT_COUPLE    = 3191;

    private final double DECOTE_MAX_DECLARANT_SEUL = 873;
    private final double DECOTE_MAX_DECLARANT_COUPLE = 1444;
    private final double TAUX_DECOTE = 0.4525;

    
    
    // revenu net
    private int rNetDecl1 = 0;
    private int rNetDecl2 = 0;
    // nb enfants
    private int nbEnf = 0;
    // nb enfants handicapés
    private int nbEnfH = 0;
    // situation familiale
    private SituationFamiliale sitFam = SituationFamiliale.CELIBATAIRE;
    // revenu fiscal de référence
    private double rFRef = 0;
    // revenu imposable
    private double rImposable = 0;
    // abattement
    private double abt = 0;
    // nombre de parts des déclarants
    private double nbPtsDecl = 0;
    // nombre de parts du foyer fiscal
    private double nbPts = 0;
    // decote
    private double decote = 0;
    // impôt des déclarants
    private double mImpDecl = 0;
    // impôt du foyer fiscal
    private double mImp = 0;
    private double mImpAvantDecote = 0;
    // parent isolé
    private boolean parIso = false;
    // Contribution exceptionnelle sur les hauts revenus
    private double contribExceptionnelle = 0;

    // Fonction de calcul de l'impôt sur le revenu net en France en 2024 sur les revenus 2023

    /**
     * Calcule l'impôt sur le revenu net en France pour l'année 2024, basé sur les revenus de l'année 2023.
     * @param revNetDecl1 Le revenu net du déclarant 1
     * @param revNetDecl2 Le revenu net du déclarant 2
     * @param sitFamilliale La situation familiale (CELIBATAIRE, MARIE, DIVORCE, VEUF ou PACSE)
     * @param nbEnfants Le nombre d'enfants à charge
     * @param nbEnfantsHandicapes Le nombre d'enfants à charge en situation de handicap
     * @param parentIsol Si le déclarant est un parent isolé ou non
     * @return Le montant final de l'impôt sur le revenu après calcul
     * @throws IllegalArgumentException Si l'un des revenus est négatif, ou si le nombre d'enfants/d'enfants handicapés est invalide, ou en cas d'incohérence dans la situation familiale
     */
    public int calculImpot (
            int revNetDecl1,
            int revNetDecl2,
            SituationFamiliale sitFamilliale,
            int nbEnfants,
            int nbEnfantsHandicapes,
            boolean parentIsol
    ) {

        // Préconditions
        if ( revNetDecl1  < 0 || revNetDecl2 < 0 ) {
            throw new IllegalArgumentException("Le revenu net ne peut pas être négatif");
        }

        if ( nbEnfants < NB_ENFANT_MIN ) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif");
        }

        if ( nbEnfantsHandicapes < NB_ENFANT_MIN ) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être négatif");
        }

        if ( sitFamilliale == null ) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être null");
        }

        if ( nbEnfantsHandicapes > nbEnfants ) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être supérieur au nombre d'enfants");
        }

        if ( nbEnfants > NB_ENFANT_MAX ) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être supérieur à "+NB_ENFANT_MAX);
        }

        if ( parentIsol && ( sitFamilliale == SituationFamiliale.MARIE || sitFamilliale == SituationFamiliale.PACSE ) ) {
            throw new IllegalArgumentException("Un parent isolé ne peut pas être marié ou pacsé");
        }

        boolean seul = sitFamilliale == SituationFamiliale.CELIBATAIRE || sitFamilliale == SituationFamiliale.DIVORCE || sitFamilliale == SituationFamiliale.VEUF;
        if (  seul && revNetDecl2 > 0 ) {
            throw new IllegalArgumentException("Un célibataire, un divorcé ou un veuf ne peut pas avoir de revenu pour le déclarant 2");
        }

        // Initialisation des variables

        rNetDecl1 = revNetDecl1;
        rNetDecl2 = revNetDecl2;
        sitFam = sitFamilliale;

        nbEnf = nbEnfants;
        nbEnfH = nbEnfantsHandicapes;
        parIso = parentIsol;

        System.out.println("--------------------------------------------------");
        System.out.println( "Revenu net declarant1 : " + rNetDecl1 );
        System.out.println( "Revenu net declarant2 : " + rNetDecl2 );
        System.out.println( "Situation familiale : " + sitFam.name() );

        // Abattement
        calculAbatement();

        // parts déclarants
        calculPartDeclarants();

        // Contribution exceptionnelle sur les hauts revenus
        calculContributionHautsRevenus();

        // Calcul impôt des declarants
        calculImpotdeclarants();

        // Calcul impôt foyer fiscal complet
        calculImpotFoyerFiscalComplet();

        // Vérification de la baisse d'impôt autorisée
        calculBaisseImpot();

        // Calcul de la decote
        calculDecote();

        mImp = mImp - decote;

        mImp += contribExceptionnelle;

        mImp = Math.round( mImp );

        System.out.println( "Impôt sur le revenu net final : " + mImp );
        return  (int)mImp;
    }


    /**
     * EXIGENCE : EXG_IMPOT_02
     * Calcule l'abattement applicable sur les revenus nets en se basant sur un taux prédéfini, tout en respectant des limites minimales
     * et maximales. Met à jour ensuite le revenu fiscal de référence en soustrayant l'abattement du total des revenus nets.
     */
    private void calculAbatement(){
        long abt1 = Math.round(rNetDecl1 * TAUX_ABATTEMENT);
        long abt2 = Math.round(rNetDecl2 * TAUX_ABATTEMENT);

        if (abt1 > LIMITE_ABATTEMENT_MAX) {
            abt1 = LIMITE_ABATTEMENT_MAX;
        }
        if ( sitFam == SituationFamiliale.MARIE || sitFam == SituationFamiliale.PACSE ) {
            if (abt2 > LIMITE_ABATTEMENT_MAX) {
                abt2 = LIMITE_ABATTEMENT_MAX;
            }
        }

        if (abt1 < LIMITE_ABATTEMENT_MIN) {
            abt1 = LIMITE_ABATTEMENT_MIN;
        }

        if ( sitFam == SituationFamiliale.MARIE || sitFam == SituationFamiliale.PACSE ) {
            if (abt2 < LIMITE_ABATTEMENT_MIN) {
                abt2 = LIMITE_ABATTEMENT_MIN;
            }
        }

        abt = abt1 + abt2;
        System.out.println( "Abattement : " + abt );

        rFRef = rNetDecl1 + rNetDecl2 - abt;
        if ( rFRef < 0 ) {
            rFRef = 0;
        }

        System.out.println( "Revenu fiscal de référence : " + rFRef );
    }


    /**
     * EXIGENCE : EXG_IMPOT_03
     * Calcule le nombre de parts fiscales du foyer en fonction de la situation familiale, du nombre d'enfants à charge,
     * du nombre d'enfants handicapés et du statut de parent isolé.
     */
    private void calculPartDeclarants(){
        switch ( sitFam ) {
            case CELIBATAIRE, DIVORCE, VEUF:
                nbPtsDecl = 1;
                break;
            case MARIE, PACSE:
                nbPtsDecl = 2;
                break;
        }

        System.out.println( "Nombre d'enfants  : " + nbEnf );
        System.out.println( "Nombre d'enfants handicapés : " + nbEnfH );

        // parts enfants à charge
        if ( nbEnf <= 2 ) {
            nbPts = nbPtsDecl + nbEnf * 0.5;
        } else if ( nbEnf > 2 ) {
            nbPts = nbPtsDecl+  1.0 + ( nbEnf - 2 );
        }

        // parent isolé
        System.out.println( "Parent isolé : " + parIso );

        if ( parIso ) {
            if ( nbEnf > 0 ){
                nbPts = nbPts + 0.5;
            }
        }

        // Veuf avec enfant
        if ( sitFam == SituationFamiliale.VEUF && nbEnf > 0 ) {
            nbPts = nbPts + 1;
        }

        // enfant handicapé
        nbPts = nbPts + nbEnfH * 0.5;

        System.out.println( "Nombre de parts : " + nbPts );
    }

    /**
     * EXIGENCE : EXG_IMPOT_07:
     * Calcule la contribution exceptionnelle sur les hauts revenus en se basant sur le revenu fiscal de référence et
     * en appliquant des taux spécifiques selon des tranches définies pour les célibataires et les couples.
     */
    private void calculContributionHautsRevenus(){
        contribExceptionnelle = 0;
        int i = 0;
        do {
            if ( rFRef >= limitesCEHR[i] && rFRef < limitesCEHR[i+1] ) {
                if ( nbPtsDecl == 1 ) {
                    contribExceptionnelle += ( rFRef - limitesCEHR[i] ) * tauxCEHRCelibataire[i];
                } else {
                    contribExceptionnelle += ( rFRef - limitesCEHR[i] ) * tauxCEHRCouple[i];
                }
                break;
            } else {
                if ( nbPtsDecl == 1 ) {
                    contribExceptionnelle += ( limitesCEHR[i+1] - limitesCEHR[i] ) * tauxCEHRCelibataire[i];
                } else {
                    contribExceptionnelle += ( limitesCEHR[i+1] - limitesCEHR[i] ) * tauxCEHRCouple[i];
                }
            }
            i++;
        } while( i < 5);

        contribExceptionnelle = Math.round( contribExceptionnelle );
        System.out.println( "Contribution exceptionnelle sur les hauts revenus : " + contribExceptionnelle );
    }


    /**
     * EXIGENCE : EXG_IMPOT_04
     * Calcule l'impôt brut des déclarants en répartissant le revenu fiscal de référence sur le nombre de parts
     * du déclarant et en appliquant les tranches d'imposition avec leurs taux respectifs.
     */
    private void calculImpotdeclarants(){
        rImposable = rFRef / nbPtsDecl ;

        mImpDecl = 0;

        int i = 0;
        do {
            if ( rImposable >= limites[i] && rImposable < limites[i+1] ) {
                mImpDecl += ( rImposable - limites[i] ) * taux[i];
                break;
            } else {
                mImpDecl += ( limites[i+1] - limites[i] ) * taux[i];
            }
            i++;
        } while( i < 5);

        mImpDecl = mImpDecl * nbPtsDecl;
        mImpDecl = Math.round( mImpDecl );

        System.out.println( "Impôt brut des déclarants : " + mImpDecl );
    }


    /**
     * EXIGENCE : EXG_IMPOT_04
     * Calcule l'impôt brut du foyer fiscal complet en répartissant le revenu fiscal de référence sur le nombre total de parts
     * et en appliquant les différentes tranches d'imposition avec leurs taux respectifs.
     */
    private void calculImpotFoyerFiscalComplet(){

        rImposable =  rFRef / nbPts;
        mImp = 0;
        int i = 0;

        do {
            if ( rImposable >= limites[i] && rImposable < limites[i+1] ) {
                mImp += ( rImposable - limites[i] ) * taux[i];
                break;
            } else {
                mImp += ( limites[i+1] - limites[i] ) * taux[i];
            }
            i++;
        } while( i < 5);

        mImp = mImp * nbPts;
        mImp = Math.round( mImp );

        System.out.println( "Impôt brut du foyer fiscal complet : " + mImp );
    }

    /**
     * EXIGENCE : EXG_IMPOT_05
     * Calcule la baisse d'impôt autorisée en comparant l'impôt des déclarants et l'impôt du foyer fiscal complet,
     * puis en appliquant un plafonnement basé sur le nombre de parts supplémentaires obtenues.
     */
    private void calculBaisseImpot(){
        // baisse impot

        double baisseImpot = mImpDecl - mImp;

        System.out.println( "Baisse d'impôt : " + baisseImpot );

        // dépassement plafond
        double ecartPts = nbPts - nbPtsDecl;

        double plafond = (ecartPts / 0.5) * PLAFOND_DEMI_PART;

        System.out.println( "Plafond de baisse autorisée " + plafond );

        if ( baisseImpot >= plafond ) {
            mImp = mImpDecl - plafond;
        }

        System.out.println( "Impôt brut après plafonnement avant decote : " + mImp );
        mImpAvantDecote = mImp;
    }


    /**
     * EXIGENCE : EXG_IMPOT_06
     * Calcule le montant de la décote applicable en fonction de l'impôt brut du foyer fiscal, des seuils et
     * taux définis pour un déclarant seul ou un couple. La décote ne peut pas excéder le montant total de l'impôt.
     */
    private void calculDecote(){
        decote = 0;
        // decote
        if ( nbPtsDecl == 1 ) {
            if ( mImp < SEUIL_DECOTE_DECLARANT_SEUL ) {
                decote = DECOTE_MAX_DECLARANT_SEUL - ( mImp  * TAUX_DECOTE );
            }
        }
        if (  nbPtsDecl == 2 ) {
            if ( mImp < SEUIL_DECOTE_DECLARANT_COUPLE ) {
                decote =  DECOTE_MAX_DECLARANT_COUPLE - ( mImp  * TAUX_DECOTE  );
            }
        }
        decote = Math.round( decote );

        if ( mImp <= decote ) {
            decote = mImp;
        }

        System.out.println( "Decote : " + decote );
    }

    // Getters pour adapter le code legacy pour les tests unitaires

    public double getRevenuReference() {
        return rFRef;
    }

    public double getDecote() {
        return decote;
    }

    public double getAbattement() {
        return abt;
    }

    public double getNbParts() {
        return nbPts;
    }

    public double getImpotAvantDecote() {
        return mImpAvantDecote;
    }

    public double getImpotNet() {
        return mImp;
    }

    public int getRevenuNetDeclatant1() {
        return rNetDecl1;
    }

    public int getRevenuNetDeclatant2() {
        return rNetDecl2;
    }

    public double getContribExceptionnelle() {
        return contribExceptionnelle;
    }

}
