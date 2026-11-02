package cl.duoc.gamerevents.model;

/** Modalidad del evento. Se guarda en la BD como texto (PRESENCIAL u ONLINE). */
public enum Modality {
    PRESENCIAL("Presencial"),
    ONLINE("Online");

    private final String label;

    Modality(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
