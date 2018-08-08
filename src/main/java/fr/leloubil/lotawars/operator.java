package fr.leloubil.lotawars;

public enum operator {

    //Classe avec les operateurs pour les requetes SQL ( pour que ca soir plus simple, au lieu d'écrire la requete a chaque fois a la main
    BIGGERTHAN(">"),
    LOWERTHAN("<"),
    NOTEQUAL("!="),
    EQUAL("="),
    BIGGEROREQUAL(">="),
    LOWEROREQUAL("<=");
    operator(String s) {
    }
}

