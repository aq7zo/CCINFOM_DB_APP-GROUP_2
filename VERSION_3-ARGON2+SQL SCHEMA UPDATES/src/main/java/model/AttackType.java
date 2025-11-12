package model;

/**
 * Model for Attack Types Record Management
 * Fields: AttackTypeID, AttackName, Description, SeverityLevel
 */
public class AttackType {
    private int attackTypeID;
    private String attackName;
    private String description;
    private String severityLevel; // Low, Medium, High

    // Default constructor
    public AttackType() {}

    // Constructor
    public AttackType(String attackName, String description, String severityLevel) {
        this.attackName = attackName;
        this.description = description;
        this.severityLevel = severityLevel;
    }

    // Getters and Setters
    public int getAttackTypeID() { return attackTypeID; }
    public void setAttackTypeID(int attackTypeID) { this.attackTypeID = attackTypeID; }

    public String getAttackName() { return attackName; }
    public void setAttackName(String attackName) { this.attackName = attackName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    @Override
    public String toString() {
        return String.format("%s [%s]", attackName, severityLevel);
    }
}