package com.client.enums;

/**
 * Enum que representa las diferentes fases del juego en Pong
 */
public enum GamePhase {
    WAITING("waiting"),
    CHOOSING("choosing"),
    ANNOUNCING("announcing"),
    COUNTDOWN("countdown"),
    PLAYING("playing"),
    FINISHED("finished");

    private final String value;

    GamePhase(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Obtiene el enum a partir de su valor string
     */
    public static GamePhase fromValue(String value) {
        for (GamePhase phase : GamePhase.values()) {
            if (phase.value.equals(value)) {
                return phase;
            }
        }
        return WAITING; // valor por defecto
    }
}
